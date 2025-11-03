package uos.software.sirip.coupon.infra.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.domain.Penalty;
import uos.software.sirip.coupon.domain.PenaltyRepository;

@Component
@Transactional(readOnly = true)
public class PenaltyRepositoryAdapter implements PenaltyRepository {

    private final PenaltyJpaRepository penaltyJpaRepository;

    public PenaltyRepositoryAdapter(PenaltyJpaRepository penaltyJpaRepository) {
        this.penaltyJpaRepository = penaltyJpaRepository;
    }

    @Override
    @Transactional
    public Penalty save(Penalty penalty) {
        PenaltyJpaEntity saved = penaltyJpaRepository.save(PenaltyJpaEntity.fromDomain(penalty));
        return saved.toDomain();
    }

    @Override
    public Optional<Penalty> findActiveByAccountId(Long accountId, LocalDateTime now) {
        return penaltyJpaRepository
            .findFirstByAccountIdAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtDesc(accountId, now, now)
            .map(PenaltyJpaEntity::toDomain);
    }

    @Override
    public List<Penalty> findByAccountId(Long accountId) {
        return penaltyJpaRepository.findByAccountIdOrderByEndsAtDesc(accountId)
            .stream()
            .map(PenaltyJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}
