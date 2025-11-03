package uos.software.sirip.coupon.infra.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.coupon.domain.Coupon;
import uos.software.sirip.coupon.domain.CouponStatus;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long accountId;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private LocalDateTime appliedAt;

    private LocalDateTime issuedAt;

    private LocalDateTime redeemedAt;

    private LocalDateTime noShowAt;

    @Column(name = "queue_position")
    private Integer queuePosition;

    public CouponJpaEntity(
        Long id,
        Long eventId,
        Long accountId,
        CouponStatus status,
        LocalDateTime appliedAt,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt,
        LocalDateTime noShowAt,
        Integer queuePosition
    ) {
        this.id = id;
        this.eventId = eventId;
        this.accountId = accountId;
        this.status = status;
        this.appliedAt = appliedAt;
        this.issuedAt = issuedAt;
        this.redeemedAt = redeemedAt;
        this.noShowAt = noShowAt;
        this.queuePosition = queuePosition;
    }

    public static CouponJpaEntity fromDomain(Coupon coupon) {
        return new CouponJpaEntity(
            coupon.getId(),
            coupon.getEventId(),
            coupon.getAccountId(),
            coupon.getStatus(),
            coupon.getAppliedAt(),
            coupon.getIssuedAt(),
            coupon.getRedeemedAt(),
            coupon.getNoShowAt(),
            coupon.getQueuePosition()
        );
    }

    public Coupon toDomain() {
        return new Coupon(
            id,
            eventId,
            accountId,
            status,
            appliedAt,
            issuedAt,
            redeemedAt,
            noShowAt,
            queuePosition
        );
    }
}
