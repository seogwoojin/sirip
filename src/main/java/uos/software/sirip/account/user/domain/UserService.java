package uos.software.sirip.account.user.domain;

import org.springframework.stereotype.Service;
import uos.software.sirip.account.user.application.ValidUserService;
import uos.software.sirip.account.user.dto.UserDto;

@Service
public class UserService {

    private ValidUserService validUserService;

    public boolean createUser(UserDto userDto) {
        validUserService.isValid(userDto.email(), userDto.password());
        return true;
    }

}
