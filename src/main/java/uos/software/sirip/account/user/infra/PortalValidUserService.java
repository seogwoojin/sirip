package uos.software.sirip.account.user.infra;

import uos.software.sirip.account.user.application.UserData;
import uos.software.sirip.account.user.application.ValidUserService;

import com.microsoft.playwright.*;

public class PortalValidUserService implements ValidUserService {

    private final String portalUrl = "https://portal.uos.ac.kr/p/STUD/";

    @Override
    public UserData isValid(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(portalUrl);

            // 실제 DOM에 맞게 셀렉터 수정 필요
            page.fill("input[name='userId']", email);
            page.fill("input[name='password']", password);
            page.click("button[type='submit']");

            try {
                // 로그인 성공 시 나타나는 학생 전용 요소를 기다림
                page.waitForSelector("#student-info",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
            } catch (PlaywrightException e) {
                throw new RuntimeException(e);
            }

            String text = page.locator("#student-info").innerText();
            return new UserData(text);
        }
    }
}
