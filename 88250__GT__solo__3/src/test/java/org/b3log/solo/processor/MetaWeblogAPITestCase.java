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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.model.User;
import org.b3log.solo.AbstractTestCase;
import org.b3log.solo.api.MetaWeblogAPI;
import org.b3log.solo.service.InitService;
import org.b3log.solo.service.UserQueryService;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MetaWeblogAPI} test case.
 *
 * @author yugt
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Oct 19, 2018
 * @since 1.7.0
 */
@Test(suiteName = "api")
public class MetaWeblogAPITestCase extends AbstractTestCase {

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
	 * 手动构造rpc请求
	 *
	 * @throws Exception exception
	 */
	@Test(dependsOnMethods = "init")
	public void metaWeblog() throws Exception {
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getServletContext()).thenReturn(mock(ServletContext.class));
		when(request.getRequestURI()).thenReturn("/apis/metaweblog");
		when(request.getMethod()).thenReturn("POST");

		//        Date date = (Date) DateFormatUtils.ISO_DATETIME_FORMAT.parseObject("2004-05-03T17:30:08");
		Date date = DateUtils.parseDate("20040503T17:30:08",
				new String[] { "yyyyMMdd'T'HH:mm:ss", "yyyyMMdd'T'HH:mm:ss'Z'" });

		final class MockServletInputStream extends ServletInputStream {

			private ByteArrayInputStream stream;

			public MockServletInputStream(byte[] data) {
				stream = new ByteArrayInputStream(data);
			}

			public int read() throws IOException {
				return stream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>").append("<methodCall>")
				.append("<methodName>metaWeblog.newPost</methodName>").append("<params>").append("<param>")
				.append("<value><int>11</int></value>").append("</param>").append("<param>")
				.append("<value><string>test@gmail.com</string></value>").append("</param>").append("<param>")
				.append("<value><string>pass</string></value>").append("</param>").append("<param>").append("<value>")
				.append("<struct>").append("<member>").append("<name>dateCreated</name>")
				.append("<value><dateTime.iso8601>20040503T17:30:08</dateTime.iso8601></value>").append("</member>")
				.append("<member>").append("<name>title</name>").append("<value><string>title</string></value>")
				.append("</member>").append("<member>").append("<name>description</name>")
				.append("<value><string>description</string></value>").append("</member>").append("<member>")
				.append("<name>categories</name>").append("<value>").append("<array>").append("<data>")
				.append("<value>").append("<string>Solo</string>").append("</value>").append("</data>")
				.append("</array>").append("</value>").append("</member>").append("</struct>").append("</value>")
				.append("</param>").append("<param>").append("<value><boolean>1</boolean></value>").append("</param>")
				.append("</params>").append("</methodCall>");
		when(request.getInputStream()).thenReturn(new MockServletInputStream(sb.toString().getBytes()));

		final MockDispatcherServlet dispatcherServlet = new MockDispatcherServlet();
		dispatcherServlet.init();

		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);

		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(printWriter);

		dispatcherServlet.service(request, response);

		final String content = stringWriter.toString();
		// System.out.println("xxxxxcontent:" + content);
		Assert.assertTrue(StringUtils.startsWith(content, "<?xml version=\"1.0\""));
	}

	/**
	 * 使用XmlRpcClient发送rpc请求
	 *
	 * @throws Exception exception
	 */
	//    @Test(dependsOnMethods = "init")
	//    public void metaWeblog2() throws Exception {
	//    	final MetaWeblogAPI metaWeblogAPI = getMetaWeblogAPI();
	//        metaWeblogAPI.metaWeblog(null,null,null);
	//        
	//    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();  
	//        config.setServerURL(new URL("http://localhost:8080/solo/apis/metaweblog"));
	//        XmlRpcClient client = new XmlRpcClient();  
	//        client.setConfig(config);  
	//        Vector<Object> params= new Vector<Object>();
	//        params.add(1, 12);
	//        params.add(2, "gangtaoyu@gmail.com");
	//        params.add(3, "sky");
	//        params.add(4, new Struct());
	//        params.add(5, "publish");
	//        Integer result=(Integer)client.execute("metaWeblog.newPost",params);  
	//
	//        System.out.println(result);  
	//        
	//        
	//    }

	class Struct {
		String title = "title";
		String link = "link";
		String description = "description";
		String author = "author";
		String[] category = { "category1", "category2" };
		String comments = "comments";
		String enclosure = "enclosure";
		String guid = "guid";
		String pubDate = "pubDate";
		String source = "source";
	}

}
