package uos.software.sirip.event.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uos.software.sirip.event.domain.Event;
import uos.software.sirip.event.domain.EventRepository;

@Service
public class EventQueryService {

    private final EventRepository eventRepository;
    private final Clock clock;

    public EventQueryService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    public List<EventSummary> listEvents() {
        LocalDateTime now = LocalDateTime.now(clock);
        return eventRepository.findAll()
            .stream()
            .sorted((left, right) -> left.getStartAt().compareTo(right.getStartAt()))
            .map(event -> toSummary(event, now))
            .collect(Collectors.toList());
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
