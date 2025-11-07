package uos.software.sirip.coupon.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.user.domain.Account;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime issuedAt;
    private LocalDateTime redeemedAt;
    private LocalDateTime noShowAt;

    @Column(name = "queue_position")
    private Integer queuePosition;

    // 정적 팩토리 메서드
    public static CouponJpaEntity issued(Event event, Account account, LocalDateTime appliedAt,
        LocalDateTime issuedAt) {
        CouponJpaEntity entity = new CouponJpaEntity();
        entity.event = event;
        entity.account = account;
        entity.status = CouponStatus.ISSUED;
        entity.appliedAt = appliedAt;
        entity.issuedAt = issuedAt;
        return entity;
    }

    public static CouponJpaEntity waiting(Event event, Account account, LocalDateTime appliedAt,
        int queuePosition) {
        CouponJpaEntity entity = new CouponJpaEntity();
        entity.event = event;
        entity.account = account;
        entity.status = CouponStatus.WAITING;
        entity.appliedAt = appliedAt;
        entity.queuePosition = queuePosition;
        return entity;
    }

    // 도메인 행위 메서드
    public void redeem(LocalDateTime redeemedAt) {
        if (!status.isIssued()) {
            throw new IllegalStateException("Only issued coupons can be redeemed");
        }
        this.status = CouponStatus.REDEEMED;
        this.redeemedAt = redeemedAt;
    }

    public void markNoShow(LocalDateTime noShowAt) {
        if (!status.isIssued()) {
            throw new IllegalStateException("Only issued coupons can be marked as no-show");
        }
        this.status = CouponStatus.NO_SHOW;
        this.noShowAt = noShowAt;
    }

    public void promoteFromWaitlist(LocalDateTime issuedAt) {
        if (!status.isWaiting()) {
            throw new IllegalStateException("Only waiting coupons can be promoted");
        }
        this.status = CouponStatus.ISSUED;
        this.issuedAt = issuedAt;
        this.queuePosition = null;
    }

    public void updateQueuePosition(int newPosition) {
        if (!status.isWaiting()) {
            throw new IllegalStateException(
                "Queue position can only be changed for waiting coupons");
        }
        this.queuePosition = newPosition;
    }
}
