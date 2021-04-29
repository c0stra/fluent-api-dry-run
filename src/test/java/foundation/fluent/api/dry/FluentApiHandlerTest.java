package foundation.fluent.api.dry;

import org.testng.annotations.Test;

import java.util.Arrays;

import static foundation.fluent.api.dry.FluentApiHandler.dryRun;

public class FluentApiHandlerTest {

    User user = dryRun("Dry run", User.class, ((id, proxy, method, arguments) -> System.out.println(method + ": " + Arrays.toString(arguments))));

    @Test
    public void testDirectParameter() {
        user.opens("login page url").andEnters().login("login").password("password").andSubmit();
    }

    @Test
    public void testIndirectParameter() {
        user.opens("login page url").andEnters().login("login").password("password").andSubmit();
    }

}
