/*
 * Copyright 2014 Jan Ouwens
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
package nl.jqno.equalsverifier.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.jqno.equalsverifier.internal.exceptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static nl.jqno.equalsverifier.internal.Util.classForName;

/**
 * Allows instantiation of classes that may or may not be present on the classpath.
 *
 * @author Jan Ouwens
 */
public class ConditionalInstantiator {
	private final String fullyQualifiedClassName;

	/**
	 * Constructor.
	 *
	 * @param fullyQualifiedClassName
	 *          The fully-qualified name of the class that we intend to
	 *          instantiate.
	 */
	public ConditionalInstantiator(String fullyQualifiedClassName) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
	}

	/**
	 * Attempts to resolve the type.
	 *
	 * @return The corresponding class object if the type exists; null otherwise.
	 */
	public Class<?> resolve() {
		return classForName(fullyQualifiedClassName);
	}

	/**
	 * Attempts to instantiate the type.
	 *
	 * @param paramTypes
	 *          The types of the constructor parameters of the constructor
	 *          that we want to call.
	 * @param paramValues
	 *          The values that we want to pass into the constructor.
	 * @return An instance of the type given in the constructor with the given
	 *         parameter values, or null if the type does not exist.
	 * @throws ReflectionException If instantiation fails.
	 */
	@SuppressFBWarnings(value = "DP_DO_INSIDE_DO_PRIVILEGED", justification = "EV is run only from within unit tests")
	public Object instantiate(Class<?>[] paramTypes, Object[] paramValues) {
		try {
			Class<?> type = resolve();
			if (type == null) {
				return null;
			}
			Constructor<?> c = type.getConstructor(paramTypes);
			c.setAccessible(true);
			return c.newInstance(paramValues);
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	/**
	 * Attempts to call a static factory method on the type.
	 *
	 * @param factoryMethod
	 *          The name of the factory method.
	 * @param paramTypes
	 *          The types of the parameters of the specific overload of the
	 *          factory method we want to call.
	 * @param paramValues
	 *          The values that we want to pass into the factory method.
	 * @return An instance of the type given by the factory method with the
	 *         given parameter values, or null of the type does not exist.
	 * @throws ReflectionException
	 *          If the call to the factory method fails.
	 */
	public Object callFactory(String factoryMethod, Class<?>[] paramTypes, Object[] paramValues) {
		return callFactory(fullyQualifiedClassName, factoryMethod, paramTypes, paramValues);
	}

	/**
	 * Attempts to call a static factory method on a type.
	 *
	 * @param factoryTypeName
	 *          The type that contains the factory method.
	 * @param factoryMethod
	 *          The name of the factory method.
	 * @param paramTypes
	 *          The types of the parameters of the specific overload of the
	 *          factory method we want to call.
	 * @param paramValues
	 *          The values that we want to pass into the factory method.
	 * @return An instance of the type given by the factory method with the
	 *          given parameter values, or null of the type does not exist.
	 * @throws ReflectionException
	 *          If the call to the factory method fails.
	 */
	@SuppressFBWarnings(value = "DP_DO_INSIDE_DO_PRIVILEGED", justification = "EV is run only from within unit tests")
	public Object callFactory(String factoryTypeName, String factoryMethod, Class<?>[] paramTypes,
			Object[] paramValues) {
		try {
			Class<?> type = resolve();
			if (type == null) {
				return null;
			}
			Class<?> factoryType = Class.forName(factoryTypeName);
			Method factory = factoryType.getMethod(factoryMethod, paramTypes);
			factory.setAccessible(true);
			return factory.invoke(null, paramValues);
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	/**
	 * Attempts to resolve a static constant on the type.
	 *
	 * @param constantName
	 *          The name of the constant.
	 * @return The value of the constant, or null if the type does not exist.
	 * @throws ReflectionException
	 *          If resolving the constant fails.
	 */
	@SuppressFBWarnings(value = "DP_DO_INSIDE_DO_PRIVILEGED", justification = "EV is run only from within unit tests")
	public Object returnConstant(String constantName) {
		try {
			Class<?> type = resolve();
			if (type == null) {
				return null;
			}
			Field field = type.getField(constantName);
			field.setAccessible(true);
			return field.get(null);
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}
}
