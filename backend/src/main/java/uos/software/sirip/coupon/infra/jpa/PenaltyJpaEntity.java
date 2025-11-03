package uos.software.sirip.coupon.infra.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.coupon.domain.Penalty;

@Entity
@Table(name = "penalties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PenaltyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    public PenaltyJpaEntity(Long id, Long accountId, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.id = id;
        this.accountId = accountId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public static PenaltyJpaEntity fromDomain(Penalty penalty) {
        return new PenaltyJpaEntity(
            penalty.getId(),
            penalty.getAccountId(),
            penalty.getStartsAt(),
            penalty.getEndsAt()
        );
    }

    public Penalty toDomain() {
        return new Penalty(id, accountId, startsAt, endsAt);
    }
}
