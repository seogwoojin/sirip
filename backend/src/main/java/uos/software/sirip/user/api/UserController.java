package uos.software.sirip.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uos.software.sirip.user.api.response.*;
import uos.software.sirip.user.domain.AuthService;
import uos.software.sirip.user.domain.UserService;
import uos.software.sirip.user.dto.UserDto;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public SignupResponse createUser(@RequestBody UserDto userDto) {
        return new SignupResponse(userService.createUser(userDto));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new TokenResponse(token);
    }
}
