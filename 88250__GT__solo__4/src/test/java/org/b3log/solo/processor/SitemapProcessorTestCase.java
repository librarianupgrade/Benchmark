/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.solo.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.model.User;
import org.b3log.solo.AbstractTestCase;
import org.b3log.solo.service.InitService;
import org.b3log.solo.service.UserQueryService;
import org.json.JSONObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link SitemapProcessor} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 5, 2016
 * @since 1.7.0
 */
@Test(suiteName = "processor")
public class SitemapProcessorTestCase extends AbstractTestCase {

	/**
	 * Init.
	 *
	 * @throws Exception exception
	 */
	@Test
	public void init() throws Exception {
		final InitService initService = getInitService();

		final JSONObject requestJSONObject = new JSONObject();
		requestJSONObject.put(User.USER_EMAIL, "test@gmail.com");
		requestJSONObject.put(User.USER_NAME, "Admin");
		requestJSONObject.put(User.USER_PASSWORD, "pass");

		initService.init(requestJSONObject);

		final UserQueryService userQueryService = getUserQueryService();
		Assert.assertNotNull(userQueryService.getUserByEmailOrUserName("test@gmail.com"));
	}

	/**
	 * blogArticlesAtom.
	 *
	 * @throws Exception exception
	 */
	@Test(dependsOnMethods = "init")
	public void blogArticlesAtom() throws Exception {
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getServletContext()).thenReturn(mock(ServletContext.class));
		when(request.getRequestURI()).thenReturn("/sitemap.xml");
		when(request.getMethod()).thenReturn("GET");

		final MockDispatcherServlet dispatcherServlet = new MockDispatcherServlet();
		dispatcherServlet.init();

		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);

		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(printWriter);

		dispatcherServlet.service(request, response);

		final String content = stringWriter.toString();
		Assert.assertTrue(StringUtils.startsWith(content, "<?xml version=\"1.0\""));
	}
}
