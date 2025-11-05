package uos.software.sirip.event.infra.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.event.domain.Event;
import uos.software.sirip.event.domain.EventRepository;

@Component
@Transactional(readOnly = true)
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    @Transactional
    public Event save(Event event) {
        uos.software.sirip.event.infra.jpa.Event saved = eventJpaRepository.save(
                uos.software.sirip.event.infra.jpa.Event.fromDomain(event));
        return saved.toDomain();
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventJpaRepository.findById(id).map(uos.software.sirip.event.infra.jpa.Event::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll()
            .stream()
            .map(uos.software.sirip.event.infra.jpa.Event::toDomain)
            .collect(Collectors.toList());
    }
}
