package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.Method;

public class InvocationHandlerResolvingGenericReturnTypeParameters implements DryRunInvocationHandler {
    private final DryRunInvocationHandler next;

    public InvocationHandlerResolvingGenericReturnTypeParameters(DryRunInvocationHandler next) {
        this.next = next;
    }

    @Override
    public Object invoke(TypeContext context, Object proxy, Method method, Object[] args, Class<?> resolvedReturnType) throws Throwable {
        TypeContext resolvedContext = context.resolve(method.getDeclaringClass(), method.getGenericReturnType());
        return next.invoke(resolvedContext, proxy, method, args, resolvedContext.getType());
    }
}
