package uos.software.sirip.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String studentId;

    public User(Account account, String name, String studentId) {
        this.account = account;
        this.name = name;
        this.studentId = studentId;
    }
}
