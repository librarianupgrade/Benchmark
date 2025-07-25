/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.expression;

import java.lang.reflect.Type;

public enum IterableOperators implements Operator {

	/**
	 * Empty collection
	 */
	IS_EMPTY(Arity.UNARY),

	/**
	 * Collection of size > 0
	 */
	NOT_EMPTY(Arity.UNARY),

	/**
	 * Size of a collection
	 */
	HAS_SIZE(Arity.BINARY),

	/**
	 * Collection contains an element
	 */
	CONTAINS(Arity.BINARY),

	/**
	 * All elements match a condition
	 */
	ALL(Arity.BINARY),

	/**
	 * None of the elements match a condition
	 */
	NONE(Arity.BINARY),

	/**
	 * Some elements match a condition
	 */
	ANY(Arity.BINARY);

	private final Arity arity;

	IterableOperators(Arity arity) {
		this.arity = arity;
	}

	@Override
	public Arity arity() {
		return arity;
	}

	@Override
	public Type returnType() {
		return Boolean.class;
	}

}
