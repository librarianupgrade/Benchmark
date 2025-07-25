/*
 * Copyright 2009-2010, 2013 Jan Ouwens
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

import nl.jqno.equalsverifier.internal.exceptions.AssertionException;

/**
 * Alternative for org.junit.Assert, so we can assert things without having a
 * dependency on JUnit.
 *
 * @author Jan Ouwens
 */
public final class Assert {
	private Assert() {
		// Do not instantiate
	}

	/**
	 * Asserts that two Objects are equal to one another. Does nothing if they
	 * are; throws an AssertionException if they're not.
	 *
	 * @param message Message to be included in the {@link AssertionException}.
	 * @param expected Expected value.
	 * @param actual Actual value.
	 * @throws AssertionException If {@code expected} and {@code actual} are not
	 *          equal.
	 */
	public static void assertEquals(Formatter message, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionException(message);
		}
	}

	/**
	 * Asserts that an assertion is true. Does nothing if it is; throws an
	 * AssertionException if it isn't.
	 *
	 * @param message Message to be included in the {@link AssertionException}.
	 * @param assertion Assertion that must be true.
	 * @throws AssertionException If {@code assertion} is false.
	 */
	public static void assertFalse(Formatter message, boolean assertion) {
		if (assertion) {
			throw new AssertionException(message);
		}
	}

	/**
	 * Asserts that an assertion is false. Does nothing if it is; throws an
	 * AssertionException if it isn't.
	 *
	 * @param message Message to be included in the {@link AssertionException}.
	 * @param assertion Assertion that must be true.
	 * @throws AssertionException If {@code assertion} is false.
	 */
	public static void assertTrue(Formatter message, boolean assertion) {
		if (!assertion) {
			throw new AssertionException(message);
		}
	}

	/**
	 * Throws an AssertionException.
	 *
	 * @param message Message to be included in the {@link AssertionException}.
	 * @throws AssertionException Always.
	 */
	public static void fail(Formatter message) {
		throw new AssertionException(message);
	}

	/**
	 * Throws an AssertionException.
	 *
	 * @param message Message to be included in the {@link AssertionException}.
	 * @throws AssertionException Always.
	 */
	public static void fail(Formatter message, Throwable cause) {
		throw new AssertionException(message, cause);
	}
}
