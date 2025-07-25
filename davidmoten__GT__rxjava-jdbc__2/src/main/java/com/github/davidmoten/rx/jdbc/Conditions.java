package com.github.davidmoten.rx.jdbc;

/**
 * Utility methods to checking conditions.
 */
final class Conditions {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Conditions() {
		// prevent instantiation
	}

	/**
	 * If and only if parameter is false throw a {@link RuntimeException}.
	 * 
	 * @param b
	 */
	static void checkTrue(boolean b) {
		if (!b)
			throw new RuntimeException("check failed");
	}

	static void checkNotNull(Object obj) {
		if (obj == null)
			throw new NullPointerException("argument cannot be null");
	}

}
