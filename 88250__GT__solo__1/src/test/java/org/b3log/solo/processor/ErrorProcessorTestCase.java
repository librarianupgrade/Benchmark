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
package org.b3log.solo.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.solo.AbstractTestCase;
import org.b3log.solo.MockRequest;
import org.b3log.solo.MockResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link ErrorProcessor} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.4, Apr 30, 2020
 * @since 1.7.0
 */
@Test(suiteName = "processor")
public class ErrorProcessorTestCase extends AbstractTestCase {

	/**
	 * showErrorPage.
	 */
	public void showErrorPage() {
		final MockRequest request = mockRequest();
		request.setRequestURI("/notfound");
		final MockResponse response = mockResponse();
		mockDispatcher(request, response);

		final String content = response.getString();
		Assert.assertTrue(StringUtils.contains(content, "<title>404 Not Found! - Solo 的个人博客</title>"));
	}
}
