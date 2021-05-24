package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import static foundation.fluent.api.dry.DryRun.proxy;
import static foundation.fluent.api.dry.handlers.DryRunInvocationHandlerAdapter.handler;

public class InvocationHandlerPropagatingProxy implements DryRunInvocationHandler {
    private final DryRunInvocationHandler next;

    public InvocationHandlerPropagatingProxy(DryRunInvocationHandler next) {
        this.next = next;
    }

    @Override
    public Object invoke(TypeContext context, Object proxy, Method method, Object[] args, Class<?> resolvedReturnType) throws Throwable {
        if (void.class.equals(resolvedReturnType)) {
            return null;
        }
        if (resolvedReturnType.isInterface()) {
            return proxy(handler(proxy), context);
        }
        if (resolvedReturnType.isArray()) {
            return Array.newInstance(resolvedReturnType.getComponentType(), 0);
        }
        return next.invoke(context, proxy, method, args, resolvedReturnType);
    }
}
