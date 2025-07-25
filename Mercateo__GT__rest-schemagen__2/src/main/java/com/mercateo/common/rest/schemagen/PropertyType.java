package com.mercateo.common.rest.schemagen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum PropertyType {
	OBJECT, STRING, BOOLEAN, INTEGER, FLOAT, ARRAY;

	public static final Set<PropertyType> PRIMITIVE_TYPES = new HashSet<>(
			Arrays.asList(STRING, BOOLEAN, INTEGER, FLOAT));
}
