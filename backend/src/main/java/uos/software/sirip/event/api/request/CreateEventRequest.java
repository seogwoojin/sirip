package uos.software.sirip.event.api.request;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CreateEventRequest {

    private String title;
    private String description;
    private String rewardDescription;
    private int totalCoupons;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // 🔥 새 필드 5개 추가
    private String eventType;
    private String organizerType;
    private String targetMajor;
    private String targetGrade;
    private Double brandScore;

}
