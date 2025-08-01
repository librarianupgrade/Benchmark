package com.oneandone.snmpman.configuration.modifier;

import com.oneandone.snmpman.configuration.type.ModifierProperties;
import org.snmp4j.smi.UnsignedInteger32;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class UnsignedInteger32ModifierTest {

	@Test
	public void testModify() {
		final ModifierProperties modifierProperties = new ModifierProperties();
		modifierProperties.put("minimum", 0);
		modifierProperties.put("maximum", 3000);
		modifierProperties.put("minimumStep", 1);
		modifierProperties.put("maximumStep", 10);

		final UnsignedInteger32Modifier modifier = new UnsignedInteger32Modifier();
		modifier.init(modifierProperties);

		final UnsignedInteger32 unsignedInteger32 = new UnsignedInteger32(0);
		assertEquals(unsignedInteger32.getValue(), 0);

		final UnsignedInteger32 modifiedVariable = modifier.modify(unsignedInteger32);
		assertNotEquals(modifiedVariable.getValue(), 0);
	}

}