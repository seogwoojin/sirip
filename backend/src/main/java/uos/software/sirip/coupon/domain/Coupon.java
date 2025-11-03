package uos.software.sirip.coupon.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Coupon {

    private final Long id;
    private final Long eventId;
    private final Long accountId;
    private final CouponStatus status;
    private final LocalDateTime appliedAt;
    private final LocalDateTime issuedAt;
    private final LocalDateTime redeemedAt;
    private final LocalDateTime noShowAt;
    private final Integer queuePosition;

    public Coupon(
        Long id,
        Long eventId,
        Long accountId,
        CouponStatus status,
        LocalDateTime appliedAt,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt,
        LocalDateTime noShowAt,
        Integer queuePosition
    ) {
        this.id = id;
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.appliedAt = Objects.requireNonNull(appliedAt, "appliedAt must not be null");
        this.issuedAt = issuedAt;
        this.redeemedAt = redeemedAt;
        this.noShowAt = noShowAt;
        this.queuePosition = queuePosition;
    }

    public static Coupon issued(Long id, Long eventId, Long accountId, LocalDateTime appliedAt, LocalDateTime issuedAt) {
        return new Coupon(id, eventId, accountId, CouponStatus.ISSUED, appliedAt, issuedAt, null, null, null);
    }

    public static Coupon waiting(Long id, Long eventId, Long accountId, LocalDateTime appliedAt, int queuePosition) {
        return new Coupon(id, eventId, accountId, CouponStatus.WAITING, appliedAt, null, null, null, queuePosition);
    }

    public Coupon redeem(LocalDateTime redeemedAt) {
        if (!status.isIssued()) {
            throw new IllegalStateException("Only issued coupons can be redeemed");
        }
        Objects.requireNonNull(redeemedAt, "redeemedAt must not be null");
        return new Coupon(id, eventId, accountId, CouponStatus.REDEEMED, appliedAt, issuedAt, redeemedAt, null, null);
    }

    public Coupon markNoShow(LocalDateTime noShowAt) {
        if (!status.isIssued()) {
            throw new IllegalStateException("Only issued coupons can be marked as no-show");
        }
        Objects.requireNonNull(noShowAt, "noShowAt must not be null");
        return new Coupon(id, eventId, accountId, CouponStatus.NO_SHOW, appliedAt, issuedAt, redeemedAt, noShowAt, null);
    }

    public Coupon promoteFromWaitlist(LocalDateTime issuedAt) {
        if (!status.isWaiting()) {
            throw new IllegalStateException("Only waiting coupons can be promoted");
        }
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        return new Coupon(id, eventId, accountId, CouponStatus.ISSUED, appliedAt, issuedAt, null, null, null);
    }

    public Coupon withQueuePosition(int queuePosition) {
        if (!status.isWaiting()) {
            throw new IllegalStateException("Queue position can only be changed for waiting coupons");
        }
        return new Coupon(id, eventId, accountId, status, appliedAt, issuedAt, redeemedAt, noShowAt, queuePosition);
    }

    public Long getId() {
        return id;
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
