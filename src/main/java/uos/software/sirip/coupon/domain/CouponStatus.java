package uos.software.sirip.coupon.domain;

public enum CouponStatus {
    ISSUED,
    REDEEMED,
    WAITING,
    CANCELLED,
    NO_SHOW;

    public boolean isTerminal() {
        return this == REDEEMED || this == CANCELLED || this == NO_SHOW;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public boolean isIssued() {
        return this == ISSUED;
    }
}
