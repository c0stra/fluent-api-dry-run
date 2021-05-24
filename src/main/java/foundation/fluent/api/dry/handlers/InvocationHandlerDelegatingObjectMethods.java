package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.Method;

public class InvocationHandlerDelegatingObjectMethods implements DryRunInvocationHandler {
    private final Object object;
    private final DryRunInvocationHandler next;

    public InvocationHandlerDelegatingObjectMethods(Object object, DryRunInvocationHandler next) {
        this.object = object;
        this.next = next;
    }

    @Override
    public Object invoke(TypeContext context, Object proxy, Method method, Object[] args, Class<?> resolvedReturnType) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(object, args);
        }
        return next.invoke(context, proxy, method, args, resolvedReturnType);
    }
}
