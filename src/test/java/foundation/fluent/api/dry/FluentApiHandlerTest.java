package foundation.fluent.api.dry;

import org.testng.annotations.Test;

import static foundation.fluent.api.dry.FluentApiHandler.dryRun;

public class FluentApiHandlerTest {

    @Test
    public void test() {
        User user = dryRun("Dry run", User.class);

        user.opens("login page url").andEnters().login("login").password("password").andSubmit();

    }

}