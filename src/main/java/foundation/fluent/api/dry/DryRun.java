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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.isNull;
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

    private final Object id;
    private final Map<Class<?>, Object> instances;
    private final DryRunInvocationHandler invocationHandler;

    private DryRun(Object id, Map<Class<?>, Object> instances, DryRunInvocationHandler dryRunInvocationHandler) {
        this.id = id;
        this.invocationHandler = dryRunInvocationHandler;
        this.instances = instances;
    }

    public static Builder create(Object id) {
        return new Builder(id);
    }

    public static Builder createDefault(Object id) {
        return new Builder(id).addDefaultInstances("DRY RUN VALUE", 0, 0L, 0.0, false, 0.0F, '?', (short) 0, (byte) 0);
    }

    public static Builder create() {
        return new Builder(null);
    }
    private Object proxy(final TypeContext context) {
        return newProxyInstance(
                context.getType().getClassLoader(),
                new Class<?>[]{context.getType()},
                (proxy, method, args) -> invocationHandler.invoke(id, context, proxy, method, args, this)
        );
    }

    public Object invoke(TypeContext context, Method method, Object[] args) {
        switch (method.getName()) {
            case "toString": return id.toString();
            case "hashCode": return this.hashCode();
            case "equals": return this.equals(args[0]);
        }
        if(void.class.equals(method.getReturnType())) {
            return null;
        }
        TypeContext resolvedType = context.resolve(method.getDeclaringClass(), method.getGenericReturnType());
        Class<?> type = resolvedType.getType();
        if(instances.containsKey(type)) {
            return instances.get(type);
        }
        if(type.isInterface()) {
            return proxy(resolvedType);
        }
        throw new IllegalArgumentException("No default value provided for non-interface return type " + type + " of method " + method.getName());
    }

    public static class Builder {
        private final Object id;
        private final Map<Class<?>, Object> instances = new LinkedHashMap<>();
        private DryRunInvocationHandler handler = (id, context, proxy, method, args, dryRunHandler) -> dryRunHandler.invoke(context, method, args);

        public Builder(Object id) {
            this.id = id;
        }

        public Builder handler(DryRunInvocationHandler handler) {
            this.handler = handler;
            return this;
        }

        private Builder add(Class<?> type, Object instance, Object allowed) {
            if(nonNull(type) && Objects.equals(instances.get(type), allowed)) {
                instances.put(type, instance);
                add(type.getSuperclass(), instance, allowed);
                Stream.of(type.getInterfaces()).forEach(i -> add(i, instance, allowed));
                add(primitives.get(type), instance, allowed);
            }
            return this;
        }

        public <T> Builder addInstance(Class<T> type, T instance) {
            return add(type, instance, instances.get(type));
        }

        public <T> Builder addInstance(T instance) {
            return add(instance.getClass(), instance, instances.get(instance.getClass()));
        }

        public Builder addDefaultInstances(Object... defaultInstances) {
            Stream.of(defaultInstances).forEach(this::addInstance);
            return this;
        }

        public Builder setDefaultInstances(Object... defaultInstances) {
            this.instances.clear();
            return addDefaultInstances(defaultInstances);
        }

        public <T> T forClass(Class<T> aClass) {
            return aClass.cast(new DryRun(isNull(id) ? aClass.getSimpleName() : id, instances, handler).proxy(new TypeContext(aClass)));
        }

    }

}
