package uos.software.sirip.coupon.infra.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyJpaRepository extends JpaRepository<PenaltyJpaEntity, Long> {

    Optional<PenaltyJpaEntity> findFirstByAccountIdAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtDesc(
        Long accountId,
        LocalDateTime startsAt,
        LocalDateTime endsAt
    );

    List<PenaltyJpaEntity> findByAccountIdOrderByEndsAtDesc(Long accountId);
}
