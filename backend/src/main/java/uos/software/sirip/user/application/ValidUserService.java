package uos.software.sirip.user.application;

public interface ValidUserService {

    UserData isValid(String email, String password);
}
