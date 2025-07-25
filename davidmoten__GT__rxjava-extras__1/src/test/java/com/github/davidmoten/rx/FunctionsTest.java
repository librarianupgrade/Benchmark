package com.github.davidmoten.rx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class FunctionsTest {

	@Test
	public void testIdentity() {
		assertEquals(123, (int) Functions.<Integer>identity().call(123));
	}

	@Test
	public void testAlwaysTrue() {
		assertTrue(Functions.<Integer>alwaysTrue().call(123));
	}

	@Test
	public void testAlwaysFalse() {
		assertFalse(Functions.<Integer>alwaysFalse().call(123));
	}

	@Test
	public void testConstant() {
		assertEquals(123, (int) Functions.constant(123).call(1));
	}

	@Test
	public void testNot() {
		assertEquals(false, (boolean) Functions.not(Functions.alwaysTrue()).call(123));
	}

	@Test
	public void testConstructorIsPrivate() {
		Asserts.assertIsUtilityClass(Functions.class);
	}

}
