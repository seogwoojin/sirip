package uos.software.sirip.event.infra.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.event.domain.Event;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String rewardDescription;

    private int totalCoupons;

    private int remainingCoupons;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public EventJpaEntity(
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
        this.title = title;
        this.description = description;
        this.rewardDescription = rewardDescription;
        this.totalCoupons = totalCoupons;
        this.remainingCoupons = remainingCoupons;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static EventJpaEntity fromDomain(Event event) {
        return new EventJpaEntity(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getRewardDescription(),
            event.getTotalCoupons(),
            event.getRemainingCoupons(),
            event.getStartAt(),
            event.getEndAt()
        );
    }

    public Event toDomain() {
        return new Event(
            id,
            title,
            description,
            rewardDescription,
            totalCoupons,
            remainingCoupons,
            startAt,
            endAt
        );
    }
}
