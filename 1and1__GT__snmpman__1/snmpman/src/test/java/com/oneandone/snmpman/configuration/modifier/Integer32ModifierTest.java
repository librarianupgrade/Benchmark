package com.oneandone.snmpman.configuration.modifier;

import com.oneandone.snmpman.configuration.type.ModifierProperties;
import org.snmp4j.smi.Integer32;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class Integer32ModifierTest {

	@Test
	public void testModify() {
		final ModifierProperties modifierProperties = new ModifierProperties();
		modifierProperties.put("minimum", Integer.MIN_VALUE);
		modifierProperties.put("maximum", Integer.MAX_VALUE);
		modifierProperties.put("minimumStep", 1);
		modifierProperties.put("maximumStep", 10);

		final Integer32Modifier modifier = new Integer32Modifier();
		modifier.init(modifierProperties);

		final Integer32 integer32 = new Integer32(0);
		assertEquals(integer32.getValue(), 0);

		final Integer32 modifiedVariable = modifier.modify(integer32);
		assertNotEquals(modifiedVariable.getValue(), 0);
	}

	@Test
	public void testProperties() {
		final ModifierProperties modifierProperties = new ModifierProperties();
		modifierProperties.put("minimum", Integer.MIN_VALUE);
		modifierProperties.put("maximum", Integer.MAX_VALUE);
		modifierProperties.put("minimumStep", 1);
		modifierProperties.put("maximumStep", 10);

		final Integer32Modifier modifier = new Integer32Modifier();
		modifier.init(modifierProperties);

		assertEquals(modifier.getMinimum(), Integer.valueOf(Integer.MIN_VALUE));
		assertEquals(modifier.getMaximum(), Integer.valueOf(Integer.MAX_VALUE));
		assertEquals(modifier.getMinimumStep(), Integer.valueOf(1));
		assertEquals(modifier.getMaximumStep(), Integer.valueOf(10));
	}

}