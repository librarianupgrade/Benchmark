package com.mercateo.common.rest.schemagen;

import java.util.HashMap;
import java.util.Map;

import com.mercateo.common.rest.schemagen.generictype.GenericType;

public final class PropertyTypeMapper {

	private PropertyTypeMapper() {
	}

	private static final Map<Class<?>, PropertyType> TYPE_MAP = new HashMap<>();

	static {
		TYPE_MAP.put(String.class, PropertyType.STRING);
		TYPE_MAP.put(Boolean.class, PropertyType.BOOLEAN);
		TYPE_MAP.put(boolean.class, PropertyType.BOOLEAN);
		TYPE_MAP.put(Integer.class, PropertyType.INTEGER);
		TYPE_MAP.put(int.class, PropertyType.INTEGER);
		TYPE_MAP.put(Long.class, PropertyType.INTEGER);
		TYPE_MAP.put(long.class, PropertyType.INTEGER);
		TYPE_MAP.put(Float.class, PropertyType.FLOAT);
		TYPE_MAP.put(float.class, PropertyType.FLOAT);
		TYPE_MAP.put(Double.class, PropertyType.FLOAT);
		TYPE_MAP.put(double.class, PropertyType.FLOAT);
	}

	public static PropertyType of(GenericType<?> type) {
		if (type.isIterable()) {
			return PropertyType.ARRAY;
		}

		final Class<?> clazz = type.getRawType();

		if (Enum.class.isAssignableFrom(clazz)) {
			return PropertyType.STRING;
		}

		if (TYPE_MAP.containsKey(clazz)) {
			return TYPE_MAP.get(clazz);
		}

		return PropertyType.OBJECT;
	}

	public static PropertyType of(Class<?> clazz) {
		return of(GenericType.of(clazz));
	}
}
