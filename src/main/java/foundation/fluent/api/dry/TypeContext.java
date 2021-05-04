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

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

/**
 * Context of java reflection type, holding also resolved actual type parameters.
 */
public final class TypeContext {

    private final Class<?> type;
    private final Map<String, TypeContext> typeParameters;

    private TypeContext(Class<?> type, Map<String, TypeContext> typeParameters) {
        this.type = type;
        this.typeParameters = typeParameters;
    }

    public static TypeContext typeContext(Class<?> aClass, List<TypeContext> arguments) {
        if(aClass.getTypeParameters().length != arguments.size()) {
            throw new IllegalArgumentException(Arrays.toString(aClass.getTypeParameters()) + " required for type " + aClass + ", but actual provided arguments do not match: " + arguments);
        }
        return new TypeContext(aClass, unmodifiableMap(range(0, arguments.size()).boxed().collect(toMap(i -> aClass.getTypeParameters()[i].getName(), arguments::get))));
    }

    public static TypeContext typeContext(Class<?> aClass) {
        return typeContext(aClass, emptyList());
    }

    public Class<?> getType() {
        return type;
    }

    public Map<String, TypeContext> getTypeParameters() {
        return typeParameters;
    }

    public TypeContext resolve(Class<?> declaringClass, Type returnType) {
        return resolve(returnType, contextOf(declaringClass, this).typeParameters);
    }

    @Override
    public String toString() {
        return type.getName() + (typeParameters.isEmpty() ? "" : typeParameters);
    }

    private static TypeContext resolve(Type type, final Map<String, TypeContext> typeParameters) {
        if(type instanceof Class<?>) {
            return typeContext((Class<?>) type);
        }
        if(type instanceof TypeVariable<?>) {
            String name = ((TypeVariable<?>) type).getName();
            if(typeParameters.containsKey(name)){
                return typeParameters.get(name);
            } else {
                throw new IllegalStateException("No type argument identified for parameter " + name);
            }
        }
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return typeContext(
                    (Class<?>) parameterizedType.getRawType(),
                    Stream.of(parameterizedType.getActualTypeArguments()).map(t -> resolve(t, typeParameters)).collect(Collectors.toList())
            );
        }
        if(type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            for(Type bound: wildcardType.getUpperBounds()) {
                return resolve(bound, typeParameters);
            }
            return typeContext(Object.class);
        }
        if(type instanceof GenericArrayType) {
            TypeContext context = resolve(((GenericArrayType) type).getGenericComponentType(), typeParameters);
            return new TypeContext(newInstance(context.getType(), 0).getClass(), context.getTypeParameters());
        }
        throw new IllegalArgumentException("Unsupported type representation: " + type.getClass());
    }

    private static TypeContext contextOf(Class<?> type, TypeContext start) {
        Queue<TypeContext> queue = new LinkedList<>(singleton(start));
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
