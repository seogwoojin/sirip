package uos.software.sirip.coupon.application;

import java.time.LocalDateTime;
import lombok.Getter;
import uos.software.sirip.coupon.domain.CouponStatus;
import uos.software.sirip.coupon.domain.CouponJpaEntity;

@Getter
public class CouponSummary {

    private final Long couponId;
    private final Long eventId;
    private final Long accountId;
    private final CouponStatus status;
    private final LocalDateTime appliedAt;
    private final LocalDateTime issuedAt;
    private final LocalDateTime redeemedAt;
    private final LocalDateTime noShowAt;
    private final Integer queuePosition;

    public CouponSummary(
        Long couponId,
        Long eventId,
        Long accountId,
        CouponStatus status,
        LocalDateTime appliedAt,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt,
        LocalDateTime noShowAt,
        Integer queuePosition
    ) {
        this.couponId = couponId;
        this.eventId = eventId;
        this.accountId = accountId;
        this.status = status;
        this.appliedAt = appliedAt;
        this.issuedAt = issuedAt;
        this.redeemedAt = redeemedAt;
        this.noShowAt = noShowAt;
        this.queuePosition = queuePosition;
    }

    public static CouponSummary from(CouponJpaEntity coupon) {
        return new CouponSummary(
            coupon.getId(),
            coupon.getEvent().getId(),
            coupon.getAccount().getAccountId(),
            coupon.getStatus(),
            coupon.getAppliedAt(),
            coupon.getIssuedAt(),
            coupon.getRedeemedAt(),
            coupon.getNoShowAt(),
            coupon.getQueuePosition()
        );
    }

}
