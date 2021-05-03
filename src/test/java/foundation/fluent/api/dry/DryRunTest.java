package foundation.fluent.api.dry;

import org.testng.annotations.Test;

import java.util.Arrays;

public class DryRunTest {

    User user = DryRun.createDefault("Dry run").handler((id, context, proxy, method, arguments, dryRun) -> {
        System.out.println(method + ": " + Arrays.toString(arguments));
        return dryRun.invoke(context, method, arguments);
    }).addInstance(int.class, 100).forClass(User.class);

    @Test
    public void testDirectParameter() {
        user.opens("login page url").andEnters().login("login").password("password").andSubmit();
    }

    @Test
    public void testIndirectParameter() {
        user.opensLoginPage("login page url").andEnters().login("login").password("password").andSubmit();
    }

}
