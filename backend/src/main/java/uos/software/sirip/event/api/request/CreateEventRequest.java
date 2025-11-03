package uos.software.sirip.event.api.request;

import java.time.LocalDateTime;

public class CreateEventRequest {

    private String title;
    private String description;
    private String rewardDescription;
    private int totalCoupons;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public int getTotalCoupons() {
        return totalCoupons;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
