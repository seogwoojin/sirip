package uos.software.sirip.user.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import uos.software.sirip.user.application.UserData;
import uos.software.sirip.user.infra.PortalValidUserService;

class PortalValidUserServiceTest {

    PortalValidUserService service = new PortalValidUserService();

    @Test
    public void portalValidUserServiceTest() {
        UserData data = service.isValid("zaza0804", "zaza4490^^");

        assertThat(data.username()).isEqualTo("석우진");
        assertThat(data.studentId()).isEqualTo("2020920032");
    }

}