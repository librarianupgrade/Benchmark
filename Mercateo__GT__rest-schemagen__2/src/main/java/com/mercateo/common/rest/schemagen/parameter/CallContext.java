package com.mercateo.common.rest.schemagen.parameter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

public class CallContext {

	private final Map<Object, Parameter<?>> parameters = new IdentityHashMap<>();

	private final Map<Class<?>, Set<?>> additionalObjects = new HashMap<>();

	public <T> Parameter.Builder<T> builderFor(Class<T> parameterClass) {
		return new Parameter.Builder<>(parameterClass, this);
	}

	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	public boolean hasParameter(Object value) {
		return parameters.containsKey(value);
	}

	public Parameter<?> getParameter(Object value) {
		return parameters.get(value);
	}

	<T> void addParameter(T value, Parameter<T> parameter) {
		parameters.put(value, parameter);
	}

	public static CallContext create() {
		return new CallContext();
	}

	@SuppressWarnings("unchecked")
	public <T> CallContext addAddionalObjects(Class<T> clazz, T object, T... objects) {
		checkNotNull(clazz);
		if (object == null) {
			return this;
		}
		Set<T> set = Sets.newHashSet(object);
		if (objects != null) {
			Arrays.stream(objects).forEach(o -> set.add(o));
		}
		if (additionalObjects.containsKey(clazz)) {
			Set<T> setInMap = (Set<T>) additionalObjects.get(clazz);
			setInMap.addAll(set);

		} else {
			additionalObjects.put(clazz, set);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<Set<T>> getAddionalObjectsFor(Class<T> clazz) {
		checkNotNull(clazz);
		Set<T> set = (Set<T>) additionalObjects.get(clazz);
		if (set != null) {
			return Optional.of(new HashSet<>(set));
		}
		return Optional.empty();
	}
}
