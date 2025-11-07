package uos.software.sirip.event.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.event.infra.jpa.EventJpaRepository;

@Service
@RequiredArgsConstructor
public class EventQueryService {

    private final EventJpaRepository eventRepository;
    private final Clock clock;

    public List<EventSummary> listEvents() {
        LocalDateTime now = LocalDateTime.now(clock);
        return eventRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Event::getStartAt))
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
