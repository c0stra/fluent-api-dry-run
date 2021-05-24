package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.Method;
import java.util.Map;

public class InvocationHandlerReturningDefaultValues implements DryRunInvocationHandler {
    private final Map<Class<?>, Object> defaultValues;
    private final DryRunInvocationHandler next;

    public InvocationHandlerReturningDefaultValues(Map<Class<?>, Object> defaultValues, DryRunInvocationHandler next) {
        this.defaultValues = defaultValues;
        this.next = next;
    }

    @Override
    public Object invoke(TypeContext context, Object proxy, Method method, Object[] args, Class<?> resolvedReturnType) throws Throwable {
        if (defaultValues.containsKey(resolvedReturnType)) {
            return defaultValues.get(resolvedReturnType);
        }
        return next.invoke(context, proxy, method, args, resolvedReturnType);
    }
}
