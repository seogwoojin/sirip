package uos.software.sirip.user.infra;

import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Service;
import uos.software.sirip.user.application.UserData;
import uos.software.sirip.user.application.ValidUserService;

import com.microsoft.playwright.*;

@Service
public class PortalValidUserService implements ValidUserService {

    private static final String PORTAL_URL = "https://portal.uos.ac.kr/p/STUD/";

    @Override
    public UserData isValid(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setExecutablePath(
                        Paths.get("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"))
                    .setArgs(
                        List.of("--ignore-certificate-errors", "--disable-gpu", "--no-sandbox"))
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(PORTAL_URL);

            // 실제 DOM에 맞게 셀렉터 수정 필요
            page.fill("input[id='user_id']", email);
            page.fill("input[id='user_password']", password);
            page.click("button[title='로그인']");

            try {
                page.waitForSelector("li.name",
                    new Page.WaitForSelectorOptions().setTimeout(10000));

                // 사용자 정보 추출
                Locator nameLocator = page.locator("li.name");
                String text = nameLocator.innerText().replaceAll("\\s+", ""); // 줄바꿈 제거
                // 예: "석우진(2020920032)"

                String username = text.replaceAll("\\(.*\\)", ""); // 괄호 제거 → 석우진
                String studentId = text.replaceAll(".*\\((\\d+)\\).*",
                    "$1"); // 괄호 안 숫자만 추출 → 2020920032

                return new UserData(username, studentId);
            } catch (PlaywrightException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
