package uos.software.sirip.coupon.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uos.software.sirip.user.domain.Account;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {

    Optional<CouponJpaEntity> findByEventIdAndAccount(Long eventId, Account account);

    List<CouponJpaEntity> findByAccountOrderByAppliedAtDesc(Account account);

    int countByEventIdAndStatus(Long eventId, CouponStatus status);

    List<CouponJpaEntity> findByEventIdAndStatusOrderByQueuePositionAsc(Long eventId,
        CouponStatus status);
}
