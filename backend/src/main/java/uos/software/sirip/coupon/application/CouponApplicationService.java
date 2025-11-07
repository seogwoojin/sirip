package uos.software.sirip.coupon.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        CouponJpaEntity existing = couponJpaRepository.findByEventIdAndAccountId(eventId, accountId)
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
        return couponJpaRepository.findByAccountIdOrderByAppliedAtDesc(accountId).stream()
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
