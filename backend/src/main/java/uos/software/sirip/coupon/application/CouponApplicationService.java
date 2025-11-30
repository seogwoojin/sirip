package uos.software.sirip.coupon.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.domain.CouponStatus;
import uos.software.sirip.coupon.exception.*;
import uos.software.sirip.coupon.domain.CouponJpaEntity;
import uos.software.sirip.coupon.domain.CouponJpaRepository;
import uos.software.sirip.event.exception.EventNotFoundException;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.event.infra.jpa.EventJpaRepository;
import uos.software.sirip.user.domain.Account;
import uos.software.sirip.user.domain.AuthService;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponApplicationService {

    private static final int PENALTY_MONTHS = 6;

    private final CouponJpaRepository couponJpaRepository;
    private final EventJpaRepository eventJpaRepository;
    private final Clock clock;
    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * ✅ 쿠폰 신청
     */
    public CouponApplicationResult apply(Long accountId, Long eventId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Account account = authService.getAccount(accountId);

        if (account.getPenalty()) {
            throw new ActivePenaltyException(accountId);
        }

        Event event = eventJpaRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.isActive(now)) {
            throw new EventClosedException(eventId);
        }

        // 중복 신청 확인
        CouponJpaEntity existing = couponJpaRepository.findByEventIdAndAccount(eventId, account)
            .orElse(null);
        if (existing != null) {
            if (existing.getStatus() == CouponStatus.WAITING) {
                return CouponApplicationResult.queued(CouponSummary.from(existing));
            }
            if (!existing.getStatus().isTerminal()) {
                throw new DuplicateApplicationException(accountId, eventId);
            }
        }

        // 즉시 발급 or 대기열 등록
        CouponJpaEntity saved;
        if (event.getRemainingCoupons() > 0) {
            CouponJpaEntity issued = CouponJpaEntity.issued(event, account, now, now);
            saved = couponJpaRepository.save(issued);
            event.decrementRemaining();
            eventJpaRepository.save(event);
            return CouponApplicationResult.issued(CouponSummary.from(saved));
        }

        int queuePosition =
            couponJpaRepository.countByEventIdAndStatus(eventId, CouponStatus.WAITING) + 1;
        CouponJpaEntity waiting = CouponJpaEntity.waiting(event, account, now, queuePosition);
        saved = couponJpaRepository.save(waiting);
        return CouponApplicationResult.queued(CouponSummary.from(saved));
    }

    /**
     * ✅ 쿠폰 신청 (대기열 제거, Redis 기반 초고속 발급)
     */
    public CouponApplicationResult applyV2(Long accountId, Long eventId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Account account = authService.getAccount(accountId);

        if (Boolean.TRUE.equals(account.getPenalty())) {
            throw new ActivePenaltyException(accountId);
        }

        Event event = eventJpaRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.isActive(now)) {
            throw new EventClosedException(eventId);
        }

        // 1️⃣ Redis SET으로 중복 신청 방지
        String appliedKey = buildAppliedKey(eventId);
        Long added = stringRedisTemplate.opsForSet()
            .add(appliedKey, accountId.toString());

        // added == 0 이면 이미 존재(중복 신청)
        if (added != null && added == 0L) {
            throw new DuplicateApplicationException(accountId, eventId);
        }

        // 2️⃣ Redis DECR로 남은 수량 원자 감소
        String remainKey = buildRemainKey(eventId);
        Long remain = stringRedisTemplate.opsForValue()
            .decrement(remainKey);
        System.out.println(remain);
        if (remain == null) {
            // 설정 안 되어 있거나 Redis 문제인 경우 → 운영 정책에 맞게 처리
            // 여기서는 시스템 오류로 처리
            // 중복 SET 롤백
            stringRedisTemplate.opsForSet().remove(appliedKey, accountId.toString());
            throw new IllegalStateException("쿠폰 잔여 수량이 설정되어 있지 않습니다. eventId=" + eventId);
        }

        if (remain < 0) {
            // 수량 소진 상태 → 롤백
            stringRedisTemplate.opsForValue().increment(remainKey);              // DECR 롤백
            stringRedisTemplate.opsForSet().remove(appliedKey, accountId.toString()); // 중복 신청 세트 롤백
            throw new CouponSoldOutException(eventId);
        }

        // 3️⃣ 실제 쿠폰 발급 (DB 기록)
        CouponJpaEntity issued = CouponJpaEntity.issued(event, account, now, now);
        CouponJpaEntity saved = couponJpaRepository.save(issued);

        // (선택) Event 엔티티의 remainingCoupons 필드는
        // 이제 진실의 근원이 아니면, 업데이트 하지 않거나, 배치/동기화용으로만 사용
        // event.decrementRemaining();
        // eventJpaRepository.save(event);

        return CouponApplicationResult.issued(CouponSummary.from(saved));
    }

    private String buildRemainKey(Long eventId) {
        return "coupon:" + eventId + ":remain";
    }

    private String buildAppliedKey(Long eventId) {
        return "coupon:" + eventId + ":applied";
    }

    /**
     * ✅ 쿠폰 사용
     */
    public CouponSummary redeem(Long couponId) {
        LocalDateTime now = LocalDateTime.now(clock);
        CouponJpaEntity coupon = couponJpaRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));

        if (!coupon.getStatus().isIssued()) {
            throw new InvalidCouponStateException("Coupon must be issued to redeem");
        }

        coupon.redeem(now);
        return CouponSummary.from(couponJpaRepository.save(coupon));
    }

    /**
     * ✅ 노쇼 처리
     */
    public CouponSummary markNoShow(Long accountId, Long couponId) {
        LocalDateTime now = LocalDateTime.now(clock);
        CouponJpaEntity coupon = couponJpaRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));

        if (!coupon.getStatus().isIssued()) {
            throw new InvalidCouponStateException("Only issued coupons can be marked as no-show");
        }

        coupon.markNoShow(now);
        couponJpaRepository.save(coupon);

        Event event = eventJpaRepository.findById(coupon.getEvent().getId())
            .orElseThrow(() -> new EventNotFoundException(coupon.getEvent().getId()));

        event.incrementRemaining();
        eventJpaRepository.save(event);

        Account account = authService.getAccount(accountId);
        account.registerPenalty();
        promoteNextWaiting(event);
        return CouponSummary.from(coupon);
    }

    /**
     * ✅ 사용자 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponSummary> listUserCoupons(Long accountId) {
        Account account = authService.getAccount(accountId);
        return couponJpaRepository.findByAccountOrderByAppliedAtDesc(account).stream()
            .map(CouponSummary::from)
            .collect(Collectors.toList());
    }

    /**
     * ✅ 대기열 쿠폰 승급
     */
    private void promoteNextWaiting(Event event) {
        List<CouponJpaEntity> waitingCoupons =
            couponJpaRepository.findByEventIdAndStatusOrderByQueuePositionAsc(
                event.getId(), CouponStatus.WAITING);

        if (waitingCoupons.isEmpty() || event.getRemainingCoupons() <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        int promotedCount = 0;

        for (CouponJpaEntity waiting : waitingCoupons) {
            if (event.getRemainingCoupons() <= 0) {
                break;
            }
            waiting.promoteFromWaitlist(now);
            couponJpaRepository.save(waiting);
            event.decrementRemaining();
            eventJpaRepository.save(event);
            promotedCount++;
        }

        // 남은 대기열 순서 갱신
        for (int i = promotedCount; i < waitingCoupons.size(); i++) {
            CouponJpaEntity remaining = waitingCoupons.get(i);
            remaining.updateQueuePosition(i - promotedCount + 1);
            couponJpaRepository.save(remaining);
        }
    }
}
