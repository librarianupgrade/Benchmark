/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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
package org.b3log.latke.util;

import junit.framework.Assert;
import org.b3log.latke.mock.MockHttpServletRequest;
import org.testng.annotations.Test;

/**
 * {@link Requests} test case.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Feb 6, 2012
 */
public class RequestsTestCase {

	/**
	 * Tests method {@link Requests#searchEngineBotRequest(javax.servlet.http.HttpServletRequest)}.
	 */
	@Test
	public void searchEngineBotRequest() {
		final MockHttpServletRequest request = new MockHttpServletRequest();

		request.setHeader("User-Agent", "compatible; Googlebot/2.1; +http://www.google.com/bot.html");
		Assert.assertTrue(Requests.searchEngineBotRequest(request));

		request.setHeader("User-Agent", "bingbot");
		Assert.assertTrue(Requests.searchEngineBotRequest(request));

		request.setHeader("User-Agent", "not search engine");
		Assert.assertFalse(Requests.searchEngineBotRequest(request));
	}
}
