package foundation.fluent.api.dry;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static foundation.fluent.api.dry.DryRun.*;

public class MockTest {

    private final Map<Call, Object> responses = new HashMap<>();
    private Call call = null;
    private DryRunInvocationHandler next = resolveReturnType(proxy(UNDEFINED));

    private final User user = DryRun.withHandler((context, proxy, method, args, resolvedReturnType) -> {
        call = new Call(method, args);
        if(responses.containsKey(call)) {
            return responses.get(call);
        }
        return next.invoke(context, proxy, method, args, resolvedReturnType);
    }).forClass(User.class);

    @Test
    public void test() {
        when(user.opensLoginPage("http://localhost/").andEnters().login("user")).thenReturn(null);
        Assert.assertNull(user.opensLoginPage("http://localhost/").andEnters().login("user"));
    }

    private <T> Response<T> when(T t) {
        return new Response<>();
    }

    public final class Response<T> {
        void thenReturn(T type) {
            responses.put(call, type);
        }
    }

    private static final class Call {
        private final Method method;
        private final Object[] args;

        private Call(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Call call = (Call) o;
            return method.equals(call.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method);
        }
    }

}
