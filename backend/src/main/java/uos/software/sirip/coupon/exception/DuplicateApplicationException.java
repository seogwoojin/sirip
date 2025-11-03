package uos.software.sirip.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(Long accountId, Long eventId) {
        super("Account " + accountId + " already has a coupon for event " + eventId);
    }
}
