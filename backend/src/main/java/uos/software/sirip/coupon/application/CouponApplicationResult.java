package uos.software.sirip.coupon.application;

public class CouponApplicationResult {

    private final CouponSummary coupon;
    private final boolean issuedImmediately;

    public CouponApplicationResult(CouponSummary coupon, boolean issuedImmediately) {
        this.coupon = coupon;
        this.issuedImmediately = issuedImmediately;
    }

    public static CouponApplicationResult issued(CouponSummary coupon) {
        return new CouponApplicationResult(coupon, true);
    }

    public static CouponApplicationResult queued(CouponSummary coupon) {
        return new CouponApplicationResult(coupon, false);
    }

    public CouponSummary getCoupon() {
        return coupon;
    }

    public boolean isIssuedImmediately() {
        return issuedImmediately;
    }
}
