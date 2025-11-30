package uos.software.sirip.coupon.application;

public class CouponSoldOutException extends RuntimeException {

    public CouponSoldOutException(Long eventId) {
        super("쿠폰이 모두 소진되었습니다. eventId=" + eventId);
    }
}