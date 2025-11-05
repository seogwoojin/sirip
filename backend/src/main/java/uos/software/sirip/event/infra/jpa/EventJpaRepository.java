package uos.software.sirip.event.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
}
