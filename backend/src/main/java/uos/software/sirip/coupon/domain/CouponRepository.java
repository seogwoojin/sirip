package uos.software.sirip.coupon.domain;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findByEventIdAndAccountId(Long eventId, Long accountId);

    List<Coupon> findByAccountId(Long accountId);

    int countWaitingByEventId(Long eventId);

    List<Coupon> findWaitingByEventIdOrderByQueuePosition(Long eventId);
}
