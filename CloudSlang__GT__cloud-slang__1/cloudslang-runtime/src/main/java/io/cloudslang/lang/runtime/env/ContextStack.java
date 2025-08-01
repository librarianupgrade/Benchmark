/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import io.cloudslang.lang.entities.bindings.values.Value;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 12:53
 */
public class ContextStack implements Serializable {

	private Deque<Context> stack;

	public ContextStack() {
		stack = new ArrayDeque<>();
	}

	public void pushContext(Context newContext) {
		stack.push(newContext);
	}

	public Context popContext() {
		if (stack.isEmpty()) {
			return null;
		} else {
			return stack.pop();
		}
	}

	public Context peekContext() {
		return stack.peek();
	}

	public boolean updateVariables(Map<String, Value> newVariables) {
		Context flowContext = peekContext();
		AtomicReference<Boolean> changedContext = new AtomicReference<>(false);
		newVariables.keySet().stream().filter(variable -> flowContext.getVariable(variable) != null
				&& !flowContext.getVariable(variable).equals(newVariables.get(variable))).forEach(variable -> {
					flowContext.putVariable(variable, newVariables.get(variable));
					changedContext.set(true);
				});
		return changedContext.get();
	}
}
