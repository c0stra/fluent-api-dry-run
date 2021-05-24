package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.DryRunInvocationListener;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.Method;

public class ListeningInvocationHandler implements DryRunInvocationHandler {
    private final DryRunInvocationListener listener;
    private final DryRunInvocationHandler handler;

    public ListeningInvocationHandler(DryRunInvocationListener listener, DryRunInvocationHandler handler) {
        this.listener = listener;
        this.handler = handler;
    }

    @Override
    public Object invoke(TypeContext context, Object proxy, Method method, Object[] args, Class<?> resolvedReturnType) throws Throwable {
        listener.invoked(context, proxy, method, args, resolvedReturnType);
        return handler.invoke(context, proxy, method, args, resolvedReturnType);
    }
}
