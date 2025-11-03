package uos.software.sirip.coupon.api.response;

import java.time.LocalDateTime;
import uos.software.sirip.coupon.application.CouponSummary;
import uos.software.sirip.coupon.domain.CouponStatus;

public class CouponResponse {

    private final Long couponId;
    private final Long eventId;
    private final Long accountId;
    private final CouponStatus status;
    private final Integer queuePosition;
    private final LocalDateTime appliedAt;
    private final LocalDateTime issuedAt;
    private final LocalDateTime redeemedAt;
    private final LocalDateTime noShowAt;

    public CouponResponse(
        Long couponId,
        Long eventId,
        Long accountId,
        CouponStatus status,
        Integer queuePosition,
        LocalDateTime appliedAt,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt,
        LocalDateTime noShowAt
    ) {
        this.couponId = couponId;
        this.eventId = eventId;
        this.accountId = accountId;
        this.status = status;
        this.queuePosition = queuePosition;
        this.appliedAt = appliedAt;
        this.issuedAt = issuedAt;
        this.redeemedAt = redeemedAt;
        this.noShowAt = noShowAt;
    }

    public static CouponResponse from(CouponSummary summary) {
        return new CouponResponse(
            summary.getCouponId(),
            summary.getEventId(),
            summary.getAccountId(),
            summary.getStatus(),
            summary.getQueuePosition(),
            summary.getAppliedAt(),
            summary.getIssuedAt(),
            summary.getRedeemedAt(),
            summary.getNoShowAt()
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

    public Integer getQueuePosition() {
        return queuePosition;
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
}
