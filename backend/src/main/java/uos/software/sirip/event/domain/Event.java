package uos.software.sirip.event.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Event {

    private final Long id;
    private final String title;
    private final String description;
    private final String rewardDescription;
    private final int totalCoupons;
    private final int remainingCoupons;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    public Event(
        Long id,
        String title,
        String description,
        String rewardDescription,
        int totalCoupons,
        int remainingCoupons,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.rewardDescription = Objects.requireNonNull(rewardDescription, "rewardDescription must not be null");
        this.totalCoupons = totalCoupons;
        this.remainingCoupons = remainingCoupons;
        this.startAt = Objects.requireNonNull(startAt, "startAt must not be null");
        this.endAt = Objects.requireNonNull(endAt, "endAt must not be null");
        if (!this.endAt.isAfter(this.startAt)) {
            throw new IllegalArgumentException("Event endAt must be after startAt");
        }
    }

    public Event decrementRemaining() {
        if (remainingCoupons <= 0) {
            throw new IllegalStateException("No remaining coupons to decrement");
        }
        return new Event(
            id,
            title,
            description,
            rewardDescription,
            totalCoupons,
            remainingCoupons - 1,
            startAt,
            endAt
        );
    }

    public Event incrementRemaining() {
        if (remainingCoupons >= totalCoupons) {
            return this;
        }
        return new Event(
            id,
            title,
            description,
            rewardDescription,
            totalCoupons,
            Math.min(remainingCoupons + 1, totalCoupons),
            startAt,
            endAt
        );
    }

    public Event updateReward(String newRewardDescription) {
        return new Event(
            id,
            title,
            description,
            Objects.requireNonNull(newRewardDescription, "reward description must not be null"),
            totalCoupons,
            remainingCoupons,
            startAt,
            endAt
        );
    }

    public Event updateSchedule(LocalDateTime newStartAt, LocalDateTime newEndAt) {
        Objects.requireNonNull(newStartAt, "startAt must not be null");
        Objects.requireNonNull(newEndAt, "endAt must not be null");
        if (!newEndAt.isAfter(newStartAt)) {
            throw new IllegalArgumentException("Event endAt must be after startAt");
        }
        return new Event(
            id,
            title,
            description,
            rewardDescription,
            totalCoupons,
            remainingCoupons,
            newStartAt,
            newEndAt
        );
    }

    public Event adjustCapacity(int newTotalCoupons) {
        if (newTotalCoupons < 0) {
            throw new IllegalArgumentException("Total coupons must be positive");
        }
        int adjustedRemaining = Math.min(remainingCoupons + (newTotalCoupons - totalCoupons), newTotalCoupons);
        if (adjustedRemaining < 0) {
            adjustedRemaining = 0;
        }
        return new Event(
            id,
            title,
            description,
            rewardDescription,
            newTotalCoupons,
            adjustedRemaining,
            startAt,
            endAt
        );
    }

    public Long getId() {
        return id;
    }

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

    public int getRemainingCoupons() {
        return remainingCoupons;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public boolean isActive(LocalDateTime now) {
        return (now.isEqual(startAt) || now.isAfter(startAt))
            && now.isBefore(endAt);
    }
}
