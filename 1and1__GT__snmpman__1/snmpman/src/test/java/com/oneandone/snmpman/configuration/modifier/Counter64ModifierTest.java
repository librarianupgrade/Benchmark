package com.oneandone.snmpman.configuration.modifier;

import com.google.common.primitives.UnsignedLong;
import com.oneandone.snmpman.configuration.type.ModifierProperties;
import org.snmp4j.smi.Counter64;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class Counter64ModifierTest {

	@Test
	public void testProperties() {
		final ModifierProperties modifierProperties = new ModifierProperties();
		modifierProperties.put("minimum", UnsignedLong.ZERO);
		modifierProperties.put("maximum", UnsignedLong.valueOf(10L));
		modifierProperties.put("minimumStep", UnsignedLong.valueOf(1L));
		modifierProperties.put("maximumStep", UnsignedLong.valueOf(10L));

		final Counter64Modifier modifier = new Counter64Modifier();
		modifier.init(modifierProperties);

		assertEquals(modifier.getMinimum(), UnsignedLong.ZERO);
		assertEquals(modifier.getMaximum(), UnsignedLong.valueOf(10L));
		assertEquals(modifier.getMinimumStep(), UnsignedLong.valueOf(1L));
		assertEquals(modifier.getMaximumStep(), UnsignedLong.valueOf(10L));
	}

	@Test
	public void testModify() {
		final ModifierProperties modifierProperties = new ModifierProperties();
		modifierProperties.put("minimum", UnsignedLong.ZERO);
		modifierProperties.put("maximum", UnsignedLong.valueOf(10L));
		modifierProperties.put("minimumStep", UnsignedLong.valueOf(1L));
		modifierProperties.put("maximumStep", UnsignedLong.valueOf(10L));

		final Counter64Modifier modifier = new Counter64Modifier();
		modifier.init(modifierProperties);

		final Counter64 counter64 = new Counter64(0);
		assertEquals(counter64.getValue(), 0);

		final Counter64 modifiedVariable = modifier.modify(counter64);
		assertNotEquals(modifiedVariable.getValue(), 0);
	}
}