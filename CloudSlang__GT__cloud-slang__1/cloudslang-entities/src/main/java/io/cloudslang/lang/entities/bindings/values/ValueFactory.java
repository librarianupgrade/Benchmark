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

import io.cloudslang.lang.entities.SensitivityLevel;

import java.io.Serializable;

/**
 * InOutParam value factory
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public abstract class ValueFactory implements Serializable {

	private static final long serialVersionUID = 2022710429263189391L;

	public static Value create(Serializable content) {
		return create(content, false);
	}

	public static Value create(Serializable serializable, boolean sensitive) {
		return serializable instanceof Value
				? ValueFactory.createValue(((Value) serializable).get(),
						((Value) serializable).isSensitive() || sensitive)
				: ValueFactory.createValue(serializable, sensitive);
	}

	public static Value create(Serializable serializable, boolean sensitive, SensitivityLevel sensitivityLevel) {
		return serializable instanceof Value
				? ValueFactory.createValue(((Value) serializable).get(),
						((Value) serializable).isSensitive() || sensitive, sensitivityLevel)
				: ValueFactory.createValue(serializable, sensitive, sensitivityLevel);
	}

	public static SensitiveStringValue createEncryptedString(String value) {
		return new SensitiveStringValue(value, false);
	}

	public static SensitiveStringValue createEncryptedString(String value, boolean preEncrypted) {
		return new SensitiveStringValue(value, preEncrypted);
	}

	public static PyObjectValue createPyObjectValue(Serializable content, boolean sensitive, boolean externalPython) {
		if (externalPython) {
			return new PlainPyObjectValue(content, sensitive);
		} else {
			return PyObjectValueProxyFactory.create(content, sensitive);
		}
	}

	public static PyObjectValue createPyObjectValue(Value value, boolean externalPython) {
		return createPyObjectValue(value == null ? null : value.get(), value != null && value.isSensitive(),
				externalPython);
	}

	public static PyObjectValue createPyObjectValueForJython(Value value) {
		return PyObjectValueProxyFactory.create(value == null ? null : value.get(),
				value != null && value.isSensitive());
	}

	public static PyObjectValue createPyObjectValueForExternalPython(Value value) {
		return new PlainPyObjectValue(value == null ? null : value.get(), value != null && value.isSensitive());
	}

	private static Value createValue(Serializable content, boolean sensitive) {
		return sensitive ? new SensitiveValue(content) : new SimpleValue(content);
	}

	private static Value createValue(Serializable content, boolean sensitive, SensitivityLevel sensitivityLevel) {
		return sensitive ? new SensitiveValue(content, sensitivityLevel) : new SimpleValue(content);
	}

}
