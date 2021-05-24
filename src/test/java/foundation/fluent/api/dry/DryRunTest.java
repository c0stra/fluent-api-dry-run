package foundation.fluent.api.dry;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static foundation.fluent.api.dry.DryRun.*;

public class DryRunTest {

    User user = DryRun.withHandler(listener(
            (context, proxy, method, arguments, returnType) -> System.out.println(method + ": " + Arrays.toString(arguments)),
            resolveReturnType(proxy(UNDEFINED))
    )).forClass(User.class);

    @Test
    public void testDirectParameter() {
        user.opens("login page url").andEnters().login("login").password("password").andSubmit();
    }

    @Test
    public void testIndirectParameter() {
        user.opensLoginPage("login page url").andEnters().login("login").password("password").andSubmit();
    }

    @Test
    public void testConsumer() {
        user.apply().accept(null);
    }

    @Test
    public void testArray() {
        Assert.assertEquals(user.get().length, 0);
    }

    @Test
    public void testWC() {
        Assert.assertEquals(user.wc().length, 0);
    }

    @Test
    public void testWildcard() {
        user.opens("").provider();
    }

}
