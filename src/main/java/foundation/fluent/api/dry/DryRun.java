/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2018, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.fluent.api.dry;

import foundation.fluent.api.dry.handlers.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static foundation.fluent.api.dry.TypeContext.typeContext;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.nonNull;

/**
 * Dry run fluent api handler allowing to execute a method.
 */
public class DryRun {

    private static final Map<Class<?>, Class<?>> primitives = new LinkedHashMap<Class<?>, Class<?>>() {{
        put(Boolean.class, boolean.class);
        put(Byte.class, byte.class);
        put(Double.class, double.class);
        put(Float.class, float.class);
        put(Character.class, char.class);
        put(Integer.class, int.class);
        put(Long.class, long.class);
        put(Short.class, short.class);
    }};

    private final DryRunInvocationHandler invocationHandler;

    private DryRun(DryRunInvocationHandler dryRunInvocationHandler) {
        this.invocationHandler = dryRunInvocationHandler;
    }

    public static DryRun withHandler(DryRunInvocationHandler handler) {
        return new DryRun(handler);
    }

    public <T> T forClass(Class<T> aClass) {
        return aClass.cast(proxy(typeContext(aClass)));
    }

    public static DryRunInvocationHandler listener(DryRunInvocationListener listener, DryRunInvocationHandler handler) {
        return new ListeningInvocationHandler(listener, handler);
    }
    private Object proxy(final TypeContext context) {
        return proxy(invocationHandler, context);
    }

    public static Object proxy(DryRunInvocationHandler invocationHandler, TypeContext context) {
        return newProxyInstance(context.getType().getClassLoader(), new Class<?>[]{context.getType()}, new DryRunInvocationHandlerAdapter(context, invocationHandler));
    }

    public static final DryRunInvocationHandler UNDEFINED = (context, proxy, method, args, resolvedReturnType) -> {
        throw new IllegalArgumentException("No default value provided for non-interface return type " + resolvedReturnType + " of method " + method.getName());
    };

    public static DryRunInvocationHandler objectHandler(Object obj, DryRunInvocationHandler next) {
        return new InvocationHandlerDelegatingObjectMethods(obj, next);
    }

    public static DryRunInvocationHandler proxy(DryRunInvocationHandler next) {
        return new InvocationHandlerPropagatingProxy(next);
    }

    public static DryRunInvocationHandler returning(Map<Class<?>, Object> defaultValues, DryRunInvocationHandler next) {
        return new InvocationHandlerReturningDefaultValues(defaultValues, next);
    }

    public static DryRunInvocationHandler returning(DryRunInvocationHandler next, Object... defaultValues) {
        Map<Class<?>, Object> instances = new HashMap<>();
        Stream.of(defaultValues).forEach(instance -> add(instances, instance.getClass(), instance, instances.get(instance.getClass())));
        return returning(instances, next);
    }

    private static void add(Map<Class<?>, Object> instances, Class<?> type, Object instance, Object allowed) {
        if(nonNull(type) && Objects.equals(instances.get(type), allowed)) {
            instances.put(type, instance);
            add(instances, type.getSuperclass(), instance, allowed);
            Stream.of(type.getInterfaces()).forEach(i -> add(instances, i, instance, allowed));
            add(instances, primitives.get(type), instance, allowed);
        }
    }

    public static DryRunInvocationHandler resolveReturnType(DryRunInvocationHandler next) {
        return new InvocationHandlerResolvingGenericReturnTypeParameters(next);
    }

}
