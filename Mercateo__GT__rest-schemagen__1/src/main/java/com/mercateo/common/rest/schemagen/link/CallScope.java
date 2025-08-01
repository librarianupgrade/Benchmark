/**
 * Copyright © 2015 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.common.rest.schemagen.link;

import com.mercateo.common.rest.schemagen.parameter.CallContext;

import java.lang.reflect.Method;
import java.util.Optional;

public class CallScope extends Scope {
	private final Optional<CallContext> callContext;

	public CallScope(Class<?> clazz, Method method, Object[] params, CallContext callContext) {
		super(clazz, method, params);
		this.callContext = Optional.ofNullable(callContext);
	}

	public Optional<CallContext> getCallContext() {
		return callContext;
	}

	@Override
	public String toString() {
		return "CallScope{" + getInvokedClass().getName() + "." + getInvokedMethod().getName() + ", " + "callContext="
				+ callContext.orElse(null) + '}';
	}
}
