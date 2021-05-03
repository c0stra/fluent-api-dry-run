package foundation.fluent.api.dry;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MockTest {

    private final User user = DryRun.create().forClass(User.class);

    private final Map<Call, Object> responses = new HashMap<>();

    @Test
    public void test() {
        when(user.opensLoginPage("http://localhost/").andEnters().login("user")).thenReturn(null);
        //Assert.assertNull(user.opensLoginPage("http://localhost/").andEnters().login("user"));
    }

    private <T> Response<T> when(T t) {
        return new Response<>();
    }

    public static final class Response<T> {
        void thenReturn(T type) {

        }
    }

    private static final class Call {
        private final Method method;
        private final Object[] args;

        private Call(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }
    }

}
