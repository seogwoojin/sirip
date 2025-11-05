package uos.software.sirip.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manager {

    @Id
    private Long accountId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    private String department;

    public Manager(Account account, String department) {
        this.account = account;
        this.department = department;
    }
}
