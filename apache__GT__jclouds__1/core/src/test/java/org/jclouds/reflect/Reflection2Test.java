/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.reflect;

import static com.google.common.base.Functions.toStringFunction;
import static org.jclouds.reflect.Reflection2.constructors;
import static org.jclouds.reflect.Reflection2.method;
import static org.jclouds.reflect.Reflection2.methods;
import static org.jclouds.reflect.Reflection2.typeToken;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

@Test
public class Reflection2Test {

	/**
	* useful when converting to and from type literals from other libraries such as guice and gson.
	*/
	public void testTypeTokenForType() {
		TypeLiteral<Set<String>> guice = new TypeLiteral<Set<String>>() {
		};

		assertEquals(typeToken(guice.getType()), new TypeToken<Set<String>>() {
			private static final long serialVersionUID = 1L;
		});
	}

	public void testConstructors() {
		Set<String> ctorParams = FluentIterable.from(constructors(TypeToken.of(HashSet.class)))
				.transform(new Function<Invokable<?, ?>, Iterable<Parameter>>() {
					public Iterable<Parameter> apply(Invokable<?, ?> input) {
						return input.getParameters();
					}
				}).transform(toStringFunction()).toSet();

		assertEquals(ctorParams, ImmutableSet.of("[]", "[java.util.Collection<? extends E> arg0]",
				"[int arg0, float arg1]", "[int arg0]", "[int arg0, float arg1, boolean arg2]"));
	}

	private abstract static class MyValue {
		abstract String foo();

		static MyValue create(String foo) {
			return null;
		}
	}

	public void testConstructorsReturnsFactoryMethods() {
		Set<String> ctorParams = FluentIterable.from(constructors(TypeToken.of(MyValue.class)))
				.transform(new Function<Invokable<?, ?>, Iterable<Parameter>>() {
					public Iterable<Parameter> apply(Invokable<?, ?> input) {
						return input.getParameters();
					}
				}).transform(toStringFunction()).toSet();

		assertEquals(ctorParams, ImmutableSet.of("[]", "[java.lang.String arg0]"));
	}

	public void testTypeTokenForClass() {
		assertEquals(typeToken(String.class), TypeToken.of(String.class));
	}

	public void testMethodFromJavaMethod() throws SecurityException, NoSuchMethodException {
		assertEquals(method(typeToken(String.class), String.class.getMethod("toString")),
				TypeToken.of(String.class).method(String.class.getMethod("toString")).returning(String.class));
	}

	public void testMethodFromClassAndNoParams() {
		@SuppressWarnings("rawtypes")
		Invokable<Set, Object> methodInSuper = method(Set.class, "iterator");

		assertEquals(methodInSuper.getOwnerType(), typeToken(Set.class));
	}

	public void testMethodFromClassAndParams() {
		@SuppressWarnings("rawtypes")
		Invokable<Set, Object> methodInSuper = method(Set.class, "equals", Object.class);

		assertEquals(methodInSuper.getOwnerType(), typeToken(Set.class));
		assertEquals(methodInSuper.getParameters().get(0).getType().getRawType(), Object.class);
	}

	ImmutableSet<String> SET_METHODS = ImmutableSet.of(
			// Java 6 and 7 methods
			"add", "addAll", "clear", "contains", "containsAll", "equals", "hashCode", "isEmpty", "iterator", "remove",
			"removeAll", "retainAll", "size", "toArray",
			// Java 8 methods
			"forEach", "parallelStream", "removeIf", "spliterator", "stream");

	ImmutableSet<String> SORTED_SET_METHODS = ImmutableSet.<String>builder().addAll(SET_METHODS).add("comparator")
			.add("first").add("headSet").add("last").add("subSet").add("tailSet").build();

	public void testMethods() {
		Set<String> methodNames = FluentIterable.from(methods(Set.class)).transform(invokableToName)
				.transform(toStringFunction()).filter(Predicates.not(Predicates.<String>in(SET_METHODS))).toSet();

		assertEquals(methodNames, ImmutableSet.<String>of());
	}

	public void testMethodsSubClass() {
		Set<String> methodNames = FluentIterable.from(methods(SortedSet.class)).transform(invokableToName)
				.transform(toStringFunction()).filter(Predicates.not(Predicates.<String>in(SORTED_SET_METHODS)))
				.toSet();

		assertEquals(methodNames, ImmutableSet.<String>of());
	}

	static final Function<Invokable<?, ?>, String> invokableToName = new Function<Invokable<?, ?>, String>() {
		public String apply(Invokable<?, ?> input) {
			return input.getName();
		}
	};
}
