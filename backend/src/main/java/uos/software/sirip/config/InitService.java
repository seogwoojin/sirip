package uos.software.sirip.config;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.event.infra.jpa.EventJpaRepository;
import uos.software.sirip.user.domain.Account;
import uos.software.sirip.user.domain.AccountRepository;
import uos.software.sirip.user.domain.Role;

@Component
public class InitService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventJpaRepository eventJpaRepository;

    public InitService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
        EventJpaRepository eventJpaRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventJpaRepository = eventJpaRepository;
    }

    @PostConstruct
    public void initDatabase() {
        Account account = null;
        if (accountRepository.count() == 0) {
            for (int i = 0; i < 500; i++) {
                Account admin = new Account(
                    Role.USER,
                    "admin" + i + "@uos.ac.kr",
                    passwordEncoder.encode("encoded_password")
                );
                account = accountRepository.save(admin);
            }
        }

//        eventJpaRepository.save(
//            new Event(
//                "Test_event",
//                "test",
//                "test",
//                100,
//                100,
//                LocalDateTime.now(),
//                LocalDateTime.now(),
//                account
//            )
//        );
    }
}
