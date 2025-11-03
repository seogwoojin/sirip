package uos.software.sirip.coupon.application;

import java.time.LocalDateTime;
import uos.software.sirip.coupon.domain.Coupon;
import uos.software.sirip.coupon.domain.CouponStatus;

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

    public static CouponSummary from(Coupon coupon) {
        return new CouponSummary(
            coupon.getId(),
            coupon.getEventId(),
            coupon.getAccountId(),
            coupon.getStatus(),
            coupon.getAppliedAt(),
            coupon.getIssuedAt(),
            coupon.getRedeemedAt(),
            coupon.getNoShowAt(),
            coupon.getQueuePosition()
        );
    }

    public Long getCouponId() {
        return couponId;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public LocalDateTime getNoShowAt() {
        return noShowAt;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }
}
