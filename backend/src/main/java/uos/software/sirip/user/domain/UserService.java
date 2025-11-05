package uos.software.sirip.user.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uos.software.sirip.user.application.UserData;
import uos.software.sirip.user.application.ValidUserService;
import uos.software.sirip.user.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ValidUserService validUserService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public String createUser(UserDto userDto) {
        UserData userData = validUserService.isValid(userDto.email(), userDto.password());

        Account account = new Account(Role.USER, userDto.email(), passwordEncoder.encode(userDto.password()));
        Account saved = accountRepository.save(account);

        userRepository.save(new User(
            saved, userData.username(), userData.studentId()
        ));

        return userData.username();
    }

}
