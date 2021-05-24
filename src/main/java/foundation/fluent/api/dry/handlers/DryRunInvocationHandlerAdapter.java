package foundation.fluent.api.dry.handlers;

import foundation.fluent.api.dry.DryRunInvocationHandler;
import foundation.fluent.api.dry.TypeContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DryRunInvocationHandlerAdapter implements InvocationHandler {
    private final TypeContext context;
    private final DryRunInvocationHandler invocationHandler;

    public DryRunInvocationHandlerAdapter(TypeContext context, DryRunInvocationHandler invocationHandler) {
        this.context = context;
        this.invocationHandler = invocationHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return invocationHandler.invoke(context, proxy, method, args, method.getReturnType());
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getTargetException();
        }
    }


    static DryRunInvocationHandler handler(Object proxy) {
        return ((DryRunInvocationHandlerAdapter) Proxy.getInvocationHandler(proxy)).invocationHandler;
    }

}
