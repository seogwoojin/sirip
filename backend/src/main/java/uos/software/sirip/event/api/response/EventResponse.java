package uos.software.sirip.event.api.response;

import java.time.LocalDateTime;
import uos.software.sirip.event.application.EventSummary;

public record EventResponse(
    Long id,
    String title,
    String description,
    String rewardDescription,
    int totalCoupons,
    int remainingCoupons,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean active
) {
    public static EventResponse from(EventSummary summary) {
        return new EventResponse(
            summary.id(),
            summary.title(),
            summary.description(),
            summary.rewardDescription(),
            summary.totalCoupons(),
            summary.remainingCoupons(),
            summary.startAt(),
            summary.endAt(),
            summary.active()
        );
    }
}
