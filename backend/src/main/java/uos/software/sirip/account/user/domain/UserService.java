package uos.software.sirip.account.user.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uos.software.sirip.account.user.application.ValidUserService;
import uos.software.sirip.account.user.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ValidUserService validUserService;

    public boolean createUser(UserDto userDto) {
        validUserService.isValid(userDto.email(), userDto.password());
        return true;
    }

}
