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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toMap;

/**
 * Context of java reflection type, holding also resolved actual type parameters.
 */
public class TypeContext {

    private final Class<?> type;
    private final Map<String, TypeContext> typeParameters;

    public TypeContext(final Class<?> type, final List<TypeContext> typeParameters) {
        this.type = type;
        this.typeParameters = IntStream.range(0, typeParameters.size()).boxed().collect(toMap(i -> type.getTypeParameters()[i].getName(), i -> typeParameters.get(i)));
    }

    public TypeContext(Class<?> type) {
        this(type, emptyList());
    }

    public Class<?> getType() {
        return type;
    }

    public TypeContext resolve(Method method) {
        return resolve(method.getGenericReturnType(), );
    }

    private static TypeContext resolve(Type type, final Map<String, TypeContext> typeParameters) {
        if(type instanceof Class<?>) {
            return new TypeContext((Class<?>) type);
        }
        if(type instanceof TypeVariable<?>) {
            return typeParameters.get(((TypeVariable<?>) type).getName());
        }
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new TypeContext(
                    (Class<?>) parameterizedType.getRawType(),
                    Stream.of(parameterizedType.getActualTypeArguments()).map(t -> resolve(t, typeParameters)).collect(Collectors.toList())
            );
        }
        throw new IllegalArgumentException("Unsupported type representation: " + type.getClass());
    }

    private static TypeContext contextOf(Class<?> type, TypeContext start) {
        Queue<TypeContext> queue = new LinkedList<TypeContext>(singleton(start));
        for(TypeContext current = queue.poll(); current != null; current = queue.poll()) {
            if(current.getType().equals(type)) {
                return current;
            }
            for(Type i : current.getType().getGenericInterfaces()) {
                queue.add(resolve(i, current.typeParameters));
            }
        }
        throw new IllegalArgumentException("Class " + type.getName() + " is not inherited by " + start.type.getName());
    }

}
