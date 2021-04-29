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

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * Dry run fluent api handler allowing to execute a method.
 */
public class FluentApiHandler {

    private static final List<Object> globalDefaultInstances = asList("DRY RUN VALUE", 0, false, 0L, 0.0);
    private final Object id;
    private final List<Object> defaultInstances;
    private final FluentApiCallEventHandler callEventHandler;

    public FluentApiHandler(Object id, List<Object> defaultInstances, FluentApiCallEventHandler callEventHandler) {
        this.id = id;
        this.defaultInstances = Stream.concat(globalDefaultInstances.stream(), defaultInstances.stream()).collect(Collectors.toList());
        this.callEventHandler = callEventHandler;
    }

    public static <T> T dryRun(Object id, Class<T> type, FluentApiCallEventHandler callEventHandler, Object... defaultInstances) {
        return type.cast(new FluentApiHandler(id, asList(defaultInstances), callEventHandler).proxy(new TypeContext(type)));
    }

    public static <T> T dryRun(Object id, Class<T> type, Object... defaultInstances) {
        return dryRun(id, type, (i, proxy, method, args) -> {}, defaultInstances);
    }

    private Object proxy(final TypeContext context) {
        return Proxy.newProxyInstance(context.getType().getClassLoader(), new Class<?>[]{context.getType()}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "toString": return id.toString();
                case "hashCode": return this.hashCode();
                case "equals": return this.equals(args[0]);
            }
            callEventHandler.onMethodCall(id, proxy, method, args);
            if(void.class.equals(method.getReturnType())) {
                return null;
            }
            TypeContext resolvedType = context.resolve(method);
            for(Object defaultInstance : defaultInstances) {
                if(resolvedType.getType().isInstance(defaultInstance)) {
                    return defaultInstance;
                }
            }
            if(resolvedType.getType().isInterface()) {
                return proxy(resolvedType);
            }
            throw new IllegalArgumentException("No default value provided for non-interface return type " + resolvedType.getType() + " of method " + method.getName());
        });
    }

}
