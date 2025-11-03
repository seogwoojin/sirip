package uos.software.sirip.coupon.infra.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.domain.Coupon;
import uos.software.sirip.coupon.domain.CouponRepository;
import uos.software.sirip.coupon.domain.CouponStatus;

@Component
@Transactional(readOnly = true)
public class CouponRepositoryAdapter implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    public CouponRepositoryAdapter(CouponJpaRepository couponJpaRepository) {
        this.couponJpaRepository = couponJpaRepository;
    }

    @Override
    @Transactional
    public Coupon save(Coupon coupon) {
        CouponJpaEntity saved = couponJpaRepository.save(CouponJpaEntity.fromDomain(coupon));
        return saved.toDomain();
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return couponJpaRepository.findById(couponId).map(CouponJpaEntity::toDomain);
    }

    @Override
    public Optional<Coupon> findByEventIdAndAccountId(Long eventId, Long accountId) {
        return couponJpaRepository.findByEventIdAndAccountId(eventId, accountId)
            .map(CouponJpaEntity::toDomain);
    }

    @Override
    public List<Coupon> findByAccountId(Long accountId) {
        return couponJpaRepository.findByAccountIdOrderByAppliedAtDesc(accountId)
            .stream()
            .map(CouponJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public int countWaitingByEventId(Long eventId) {
        return couponJpaRepository.countByEventIdAndStatus(eventId, CouponStatus.WAITING);
    }

    @Override
    public List<Coupon> findWaitingByEventIdOrderByQueuePosition(Long eventId) {
        return couponJpaRepository.findByEventIdAndStatusOrderByQueuePositionAsc(eventId, CouponStatus.WAITING)
            .stream()
            .map(CouponJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}
