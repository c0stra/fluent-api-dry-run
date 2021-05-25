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
