package uos.software.sirip.event.application;

import java.time.LocalDateTime;

public record EventSummary(
    Long id,
    String title,
    String description,
    String rewardDescription,
    int totalCoupons,
    int remainingCoupons,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean active
) { }
