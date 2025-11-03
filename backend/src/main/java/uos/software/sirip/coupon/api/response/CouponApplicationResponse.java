package uos.software.sirip.coupon.api.response;

import uos.software.sirip.coupon.application.CouponApplicationResult;

public class CouponApplicationResponse {

    private final CouponResponse coupon;
    private final boolean issuedImmediately;

    public CouponApplicationResponse(CouponResponse coupon, boolean issuedImmediately) {
        this.coupon = coupon;
        this.issuedImmediately = issuedImmediately;
    }

    public static CouponApplicationResponse from(CouponApplicationResult result) {
        return new CouponApplicationResponse(CouponResponse.from(result.getCoupon()), result.isIssuedImmediately());
    }

    public CouponResponse getCoupon() {
        return coupon;
    }

    public boolean isIssuedImmediately() {
        return issuedImmediately;
    }
}
