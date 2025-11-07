package uos.software.sirip.coupon.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {

    Optional<CouponJpaEntity> findByEventIdAndAccountId(Long eventId, Long accountId);

    List<CouponJpaEntity> findByAccountIdOrderByAppliedAtDesc(Long accountId);

    int countByEventIdAndStatus(Long eventId, CouponStatus status);

    List<CouponJpaEntity> findByEventIdAndStatusOrderByQueuePositionAsc(Long eventId,
        CouponStatus status);
}
