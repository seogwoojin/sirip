package uos.software.sirip.event.application;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.application.CouponApplicationService;
import uos.software.sirip.event.domain.Event;
import uos.software.sirip.event.domain.EventRepository;
import uos.software.sirip.event.exception.EventNotFoundException;

@Service
@Transactional
public class EventCommandService {

    private final EventRepository eventRepository;
    private final CouponApplicationService couponApplicationService;
    private final Clock clock;

    public EventCommandService(
        EventRepository eventRepository,
        CouponApplicationService couponApplicationService,
        Clock clock
    ) {
        this.eventRepository = eventRepository;
        this.couponApplicationService = couponApplicationService;
        this.clock = clock;
    }

    public EventSummary create(
        String title,
        String description,
        String rewardDescription,
        int totalCoupons,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        Event event = new Event(null, title, description, rewardDescription, totalCoupons, totalCoupons, startAt, endAt);
        Event saved = eventRepository.save(event);
        return toSummary(saved, LocalDateTime.now(clock));
    }

    public EventSummary updateReward(Long eventId, String rewardDescription) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
        Event updated = event.updateReward(rewardDescription);
        Event saved = eventRepository.save(updated);
        return toSummary(saved, LocalDateTime.now(clock));
    }

    public EventSummary updateSchedule(Long eventId, LocalDateTime startAt, LocalDateTime endAt) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
        Event updated = event.updateSchedule(startAt, endAt);
        Event saved = eventRepository.save(updated);
        return toSummary(saved, LocalDateTime.now(clock));
    }

    public EventSummary updateCapacity(Long eventId, int totalCoupons) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
        Event adjusted = event.adjustCapacity(totalCoupons);
        Event saved = eventRepository.save(adjusted);
        if (saved.getRemainingCoupons() > 0) {
            couponApplicationService.fillWaitlist(eventId);
            saved = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        }
        return toSummary(saved, LocalDateTime.now(clock));
    }

    public EventSummary get(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
        return toSummary(event, LocalDateTime.now(clock));
    }

    private EventSummary toSummary(Event event, LocalDateTime now) {
        return new EventSummary(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getRewardDescription(),
            event.getTotalCoupons(),
            event.getRemainingCoupons(),
            event.getStartAt(),
            event.getEndAt(),
            event.isActive(now)
        );
    }
}
