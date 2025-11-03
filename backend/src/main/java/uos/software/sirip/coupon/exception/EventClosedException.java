package uos.software.sirip.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EventClosedException extends RuntimeException {

    public EventClosedException(Long eventId) {
        super("Event " + eventId + " is not accepting coupons at this time");
    }
}
