/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-present, b3log.org
 *
 * Solo is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *         http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.b3log.solo.repository;

import org.b3log.solo.AbstractTestCase;
import org.b3log.solo.model.Option;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link OptionRepository} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jan 29, 2019
 * @since 0.6.0
 */
@Test(suiteName = "repository")
public final class OptionRepositoryImplTestCase extends AbstractTestCase {

	/**
	 * Tests.
	 *
	 * @throws Exception exception
	 */
	@Test
	public void test() throws Exception {
		final OptionRepository optionRepository = getOptionRepository();

		Assert.assertTrue(0 < optionRepository.count());
		Assert.assertNotNull(optionRepository.get(Option.ID_C_BLOG_TITLE));
	}
}
