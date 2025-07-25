/*
 * Copyright 2015-2016 Jan Ouwens
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
package nl.jqno.equalsverifier.internal.prefabvalues;

import nl.jqno.equalsverifier.internal.exceptions.RecursionException;
import nl.jqno.equalsverifier.internal.exceptions.ReflectionException;
import nl.jqno.equalsverifier.internal.prefabvalues.factories.FallbackFactory;
import nl.jqno.equalsverifier.internal.prefabvalues.factories.PrefabValueFactory;
import nl.jqno.equalsverifier.internal.prefabvalues.factories.SimpleFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Container and creator of prefabricated instances of objects and classes.
 *
 * Only creates values ones, and caches them once they've been created. Takes
 * generics into account; i.e., {@code List<Integer>} is different from
 * {@code List<String>}.
 *
 * @author Jan Ouwens
 */
public class PrefabValues {
	private static final Map<Class<?>, Class<?>> PRIMITIVE_OBJECT_MAPPER = createPrimitiveObjectMapper();

	private final Cache cache = new Cache();
	private final FactoryCache factoryCache = new FactoryCache();
	private final PrefabValueFactory<?> fallbackFactory = new FallbackFactory<>();

	/**
	 * Associates the factory that can create instances of the given type,
	 * with the specified class.
	 *
	 * @param <T> The type of value to which the factory is associated.
	 * @param type The class of the values.
	 * @param factory The factory.
	 */
	public <T> void addFactory(Class<T> type, PrefabValueFactory<T> factory) {
		factoryCache.put(type, factory);
	}

	/**
	 * Associates the specified values with the specified class.
	 *
	 * @param <T> The type of value to put into this {@link PrefabValues}.
	 * @param type The class of the values.
	 * @param red A value of type T.
	 * @param black Another value of type T.
	 */
	public <T> void addFactory(Class<T> type, T red, T black) {
		factoryCache.put(type, new SimpleFactory<>(red, black));
	}

	/**
	 * Returns the "red" prefabricated value of the sepcified type.
	 *
	 * It's always a different value from the "black" one.
	 *
	 * @param tag A description of the desired type, including generic
	 *            parameters.
	 */
	public <T> T giveRed(TypeTag tag) {
		return this.<T>giveTuple(tag, emptyStack()).getRed();
	}

	/**
	 * Returns the "black" prefabricated value of the sepcified type.
	 *
	 * It's always a different value from the "red" one.
	 *
	 * @param tag A description of the desired type, including generic
	 *            parameters.
	 */
	public <T> T giveBlack(TypeTag tag) {
		return this.<T>giveTuple(tag, emptyStack()).getBlack();
	}

	/**
	 * Returns a prefabricated value of the specified type, that is different
	 * from the specified value.
	 *
	 * @param tag A description of the desired type, including generic
	 *            parameters.
	 * @param value A value that is different from the value that will be
	 *              returned.
	 */
	public <T> T giveOther(TypeTag tag, T value) {
		Class<T> type = tag.getType();
		if (value != null && !type.isAssignableFrom(value.getClass()) && !wraps(type, value.getClass())) {
			throw new ReflectionException("TypeTag does not match value.");
		}

		Tuple<T> tuple = giveTuple(tag, emptyStack());
		if (type.isArray() && arraysAreDeeplyEqual(tuple.getRed(), value)) {
			return tuple.getBlack();
		}
		if (!type.isArray() && tuple.getRed().equals(value)) {
			return tuple.getBlack();
		}
		return tuple.getRed();
	}

	private boolean wraps(Class<?> expectedClass, Class<?> actualClass) {
		return PRIMITIVE_OBJECT_MAPPER.get(expectedClass) == actualClass;
	}

	private boolean arraysAreDeeplyEqual(Object x, Object y) {
		// Arrays.deepEquals doesn't accept Object values so we need to wrap them in another array.
		return Arrays.deepEquals(new Object[] { x }, new Object[] { y });
	}

	private <T> Tuple<T> giveTuple(TypeTag tag, LinkedHashSet<TypeTag> typeStack) {
		realizeCacheFor(tag, typeStack);
		return cache.getTuple(tag);
	}

	private LinkedHashSet<TypeTag> emptyStack() {
		return new LinkedHashSet<>();
	}

	/**
	 * Makes sure that values for the specified type are present in the cache,
	 * but doesn't return them.
	 *
	 * @param tag A description of the desired type, including generic
	 *            parameters.
	 * @param typeStack Keeps track of recursion in the type.
	 */
	public <T> void realizeCacheFor(TypeTag tag, LinkedHashSet<TypeTag> typeStack) {
		if (!cache.contains(tag)) {
			Tuple<T> tuple = createTuple(tag, typeStack);
			addToCache(tag, tuple);
		}
	}

	private <T> Tuple<T> createTuple(TypeTag tag, LinkedHashSet<TypeTag> typeStack) {
		if (typeStack.contains(tag)) {
			throw new RecursionException(typeStack);
		}

		Class<T> type = tag.getType();
		if (factoryCache.contains(type)) {
			PrefabValueFactory<T> factory = factoryCache.get(type);
			return factory.createValues(tag, this, typeStack);
		}

		@SuppressWarnings("unchecked")
		Tuple<T> result = (Tuple<T>) fallbackFactory.createValues(tag, this, typeStack);
		return result;
	}

	private void addToCache(TypeTag tag, Tuple<?> tuple) {
		cache.put(tag, tuple.getRed(), tuple.getBlack());
	}

	private static Map<Class<?>, Class<?>> createPrimitiveObjectMapper() {
		Map<Class<?>, Class<?>> result = new HashMap<>();
		result.put(boolean.class, Boolean.class);
		result.put(byte.class, Byte.class);
		result.put(char.class, Character.class);
		result.put(double.class, Double.class);
		result.put(float.class, Float.class);
		result.put(int.class, Integer.class);
		result.put(long.class, Long.class);
		result.put(short.class, Short.class);
		return result;
	}
}
