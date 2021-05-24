package foundation.fluent.api.dry;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Function;

import static foundation.fluent.api.dry.DryRun.*;

public class DryRunInvocationHandlerTest {

    DryRunInvocationHandler next = resolveReturnType(proxy(UNDEFINED));
    User user = DryRun.withHandler((context, proxy, method, arguments, resolvedReturnType) -> {
        System.out.println(method + ": " + Arrays.toString(arguments));
        if(method.getName().equals("fill") && arguments[0] instanceof Function) {
            return ((Function)arguments[0]).apply(proxy);
        }
        return next.invoke(context, proxy, method, arguments, resolvedReturnType);
    }).forClass(User.class);

    @Test
    public void testDirectParameter() {
        user.opens("login page url").fill(page -> page.andEnters().login("login").password("password")).andSubmit();
    }

}
