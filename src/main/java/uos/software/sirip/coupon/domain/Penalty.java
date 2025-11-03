package uos.software.sirip.coupon.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Penalty {

    private final Long id;
    private final Long accountId;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;

    public Penalty(Long id, Long accountId, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt must not be null");
        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("Penalty end must be after start");
        }
    }

    public static Penalty create(Long accountId, LocalDateTime startsAt, LocalDateTime endsAt) {
        return new Penalty(null, accountId, startsAt, endsAt);
    }

    public boolean isActive(LocalDateTime now) {
        return (now.isEqual(startsAt) || now.isAfter(startsAt)) && now.isBefore(endsAt);
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }
}
