package uos.software.sirip.event.api.request;

import java.time.LocalDateTime;

public class UpdateEventScheduleRequest {

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
