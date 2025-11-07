package uos.software.sirip.user.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uos.software.sirip.config.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(String email, String password) {
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenProvider.createToken(account.getAccountId(), account.getRole());
    }

    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid account id"));
    }
}
