package uos.software.sirip.account.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uos.software.sirip.account.user.domain.UserService;
import uos.software.sirip.account.user.dto.UserDto;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public String createUser(UserDto userDto) {
        userService.createUser(userDto);
        return "OK";
    }
}
