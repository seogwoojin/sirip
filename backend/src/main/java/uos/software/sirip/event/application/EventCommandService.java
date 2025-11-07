package uos.software.sirip.event.application;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.application.CouponApplicationService;
import uos.software.sirip.event.exception.EventNotFoundException;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.event.infra.jpa.EventJpaRepository;
import uos.software.sirip.user.domain.Account;
import uos.software.sirip.user.domain.AuthService;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCommandService {

    private final EventJpaRepository eventJpaRepository;
    private final CouponApplicationService couponApplicationService;
    private final Clock clock;
    private final AuthService authService;

    /**
     * ✅ 이벤트 생성
     */
    public EventSummary create(
        Long accountId,
        String title,
        String description,
        String rewardDescription,
        int totalCoupons,
        LocalDateTime startAt,
        LocalDateTime endAt) {
        Account account = authService.getAccount(accountId);

        Event event = new Event(
            null, title, description, rewardDescription,
            totalCoupons, totalCoupons, startAt, endAt, account
        );

        Event saved = eventJpaRepository.save(event);
        return toSummary(saved);
    }

    /**
     * ✅ 보상 수정
     */
    public EventSummary updateReward(Long accountId, Long eventId, String rewardDescription) {
        Event event = findOwnedEvent(accountId, eventId);
        event.changeRewardDescription(rewardDescription);
        return toSummary(eventJpaRepository.save(event));
    }

    /**
     * ✅ 일정 수정
     */
    public EventSummary updateSchedule(Long accountId, Long eventId,
        LocalDateTime startAt, LocalDateTime endAt) {
        Event event = findOwnedEvent(accountId, eventId);
        event.changeEventDate(startAt, endAt);
        return toSummary(eventJpaRepository.save(event));
    }

//    /** ✅ 발급량 수정 */
//    public EventSummary updateCapacity(Long accountId, Long eventId, int totalCoupons) {
//        Event event = findOwnedEvent(accountId, eventId);
//        event.setTotalCoupons(totalCoupons);
//        event.setRemainingCoupons(totalCoupons);
//
//        Event saved = eventJpaRepository.save(event);
//        if (saved.getRemainingCoupons() > 0) {
//            couponApplicationService.fillWaitlist(eventId);
//        }
//        return toSummary(saved);
//    }

    /**
     * ✅ 단건 조회
     */
    public EventSummary get(Long accountId, Long eventId) {
        Event event = findOwnedEvent(accountId, eventId);
        return toSummary(event);
    }

    /**
     * ✅ 공통 메서드: 본인 이벤트 검증
     */
    private Event findOwnedEvent(Long accountId, Long eventId) {
        Event event = eventJpaRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getAccount().getAccountId().equals(accountId)) {
            throw new SecurityException("본인 이벤트만 접근할 수 있습니다.");
        }
        return event;
    }

    /**
     * ✅ 요약 변환
     */
    private EventSummary toSummary(Event e) {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean active = now.isAfter(e.getStartAt()) && now.isBefore(e.getEndAt());
        return new EventSummary(
            e.getId(),
            e.getTitle(),
            e.getDescription(),
            e.getRewardDescription(),
            e.getTotalCoupons(),
            e.getRemainingCoupons(),
            e.getStartAt(),
            e.getEndAt(),
            active
        );
    }
}
