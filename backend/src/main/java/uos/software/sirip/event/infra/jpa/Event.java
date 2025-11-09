package uos.software.sirip.event.infra.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.user.domain.Account;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public Event(
        String title,
        String description,
        String rewardDescription,
        int totalCoupons,
        int remainingCoupons,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Account account
    ) {
        this.title = title;
        this.description = description;
        this.rewardDescription = rewardDescription;
        this.totalCoupons = totalCoupons;
        this.remainingCoupons = remainingCoupons;
        this.startAt = startAt;
        this.endAt = endAt;
        this.account = account;
    }

    public void changeRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public void changeEventDate(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public boolean isActive(LocalDateTime now) {
        return startAt.isBefore(now) || endAt.isAfter(now);
    }

    public void decrementRemaining() {
        remainingCoupons -= 1;
    }

    public void incrementRemaining() {
        remainingCoupons += 1;
    }
}
