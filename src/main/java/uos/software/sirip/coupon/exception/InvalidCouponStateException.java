package uos.software.sirip.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCouponStateException extends RuntimeException {

    public InvalidCouponStateException(String message) {
        super(message);
    }
}
