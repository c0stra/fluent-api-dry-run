package foundation.fluent.api.dry;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Function;

public class DryRunInvocationHandlerTest {

    User user = DryRun.create("Dry run").handler((id, context, proxy, method, arguments, dryRunHandler) -> {
        System.out.println(method + ": " + Arrays.toString(arguments));
        if(method.getName().equals("fill") && arguments[0] instanceof Function) {
            return ((Function)arguments[0]).apply(proxy);
        }
        return dryRunHandler.invoke(context, method, arguments);
    }).forClass(User.class);

    @Test
    public void testDirectParameter() {
        user.opens("login page url").fill(page -> page.andEnters().login("login").password("password")).andSubmit();
    }

}
