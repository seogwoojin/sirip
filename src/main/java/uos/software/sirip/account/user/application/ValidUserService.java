package uos.software.sirip.account.user.application;

public interface ValidUserService {

    UserData isValid(String email, String password);
}
