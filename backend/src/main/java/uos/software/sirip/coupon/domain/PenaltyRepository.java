package uos.software.sirip.coupon.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PenaltyRepository {

    Penalty save(Penalty penalty);

    Optional<Penalty> findActiveByAccountId(Long accountId, LocalDateTime now);

    List<Penalty> findByAccountId(Long accountId);
}
