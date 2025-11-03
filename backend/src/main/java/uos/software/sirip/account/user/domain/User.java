package uos.software.sirip.account.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uos.software.sirip.account.Account;

@Entity(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private Long accountId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    private String name;

    public User(Account account, String name) {
        this.account = account;
        this.name = name;
    }
}
