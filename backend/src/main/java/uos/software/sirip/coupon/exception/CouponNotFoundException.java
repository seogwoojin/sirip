package uos.software.sirip.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(Long couponId) {
        super("Coupon " + couponId + " not found");
    }
}
