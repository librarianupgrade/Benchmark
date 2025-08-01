/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import java.io.Serializable;

/**
 * InOutParam value
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public interface Value extends Serializable {

	Serializable get();

	boolean isSensitive();

	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@Override
	String toString();

	static String toStringSafe(Value value) {
		if (value != null && value.isSensitive()) {
			return value.get() != null ? value.get().toString() : null;
		}

		return value != null && value.get() != null ? value.toString() : null;
	}

	static String toStringSafeEmpty(Value value) {
		return value != null && value.get() != null ? value.toString() : "";
	}

}
