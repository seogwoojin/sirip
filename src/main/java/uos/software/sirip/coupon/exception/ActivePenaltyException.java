package uos.software.sirip.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ActivePenaltyException extends RuntimeException {

    public ActivePenaltyException(Long accountId) {
        super("Account " + accountId + " has an active penalty");
    }
}
