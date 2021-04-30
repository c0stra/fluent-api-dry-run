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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Dry run fluent api handler allowing to execute a method.
 */
public class DryRun {

    private static final List<Object> globalDefaultInstances = asList("DRY RUN VALUE", 0, false, 0L, 0.0);
    private final Object id;
    private final List<Object> defaultInstances;
    private final DryRunInvocationHandler invocationHandler;

    private DryRun(Object id, List<Object> defaultInstances, DryRunInvocationHandler dryRunInvocationHandler) {
        this.id = id;
        this.defaultInstances = defaultInstances;
        this.invocationHandler = dryRunInvocationHandler;
    }

    public static Builder create(Object id) {
        return new Builder(id);
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
        for(Object defaultInstance : defaultInstances) {
            if(resolvedType.getType().isInstance(defaultInstance)) {
                return defaultInstance;
            }
        }
        if(resolvedType.getType().isInterface()) {
            return proxy(resolvedType);
        }
        throw new IllegalArgumentException("No default value provided for non-interface return type " + resolvedType.getType() + " of method " + method.getName());
    }

    public static class Builder {
        private final Object id;
        private final List<Object> defaultInstances = new LinkedList<>(globalDefaultInstances);
        private DryRunInvocationHandler handler;

        public Builder(Object id) {
            this.id = id;
        }

        public Builder handler(DryRunInvocationHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder addDefaultInstances(Object... defaultInstances) {
            this.defaultInstances.addAll(asList(defaultInstances));
            return this;
        }

        public Builder setDefaultInstances(Object... defaultInstances) {
            this.defaultInstances.clear();
            return addDefaultInstances(defaultInstances);
        }

        public <T> T forClass(Class<T> aClass) {
            return aClass.cast(new DryRun(id, defaultInstances, handler).proxy(new TypeContext(aClass)));
        }

    }

}
