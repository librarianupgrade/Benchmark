/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.performance;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

public class TestRandomDataUtil {

	@Test
	public void testParselRandVarTemplateString() throws Exception {
		LinkedList<StringItem> result = RandomDataValueUtil.parselRandVarTemplateString(
				"${date(yyyy-MM-dd HH [2014-2015]y-[1-6]M-[1-31]d-[7-21]H-[0-59]m-[5-33]s-[111-990]S)}");
		Assert.assertEquals(true, result.size() == 1);
		DateVarItem item = (DateVarItem) result.get(0);
		Assert.assertEquals(Arrays.toString(item.dayRang), Arrays.toString(new int[] { 1, 31 }));
		Assert.assertEquals(Arrays.toString(item.yearRang), Arrays.toString(new int[] { 2014, 2015 }));
		Assert.assertEquals(Arrays.toString(item.monRang), Arrays.toString(new int[] { 1, 6 }));
		Assert.assertEquals(Arrays.toString(item.hourRang), Arrays.toString(new int[] { 7, 21 }));
		Assert.assertEquals(Arrays.toString(item.secondRang), Arrays.toString(new int[] { 5, 33 }));
		Assert.assertEquals(Arrays.toString(item.sssRang), Arrays.toString(new int[] { 111, 990 }));
	}

}
