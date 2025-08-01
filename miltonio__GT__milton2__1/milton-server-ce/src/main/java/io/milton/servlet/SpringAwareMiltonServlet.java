/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.servlet;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * MiltonServlet is a thin wrapper around HttpManager. It takes care of initialisation
 * and delegates requests to the HttpManager
 * 
 * The servlet API is hidden by the Milton API, however you can get access to
 * the underlying request and response objects from the static request and response
 * methods which use ThreadLocal variables
 *
 * This spring aware servlet will load the spring context from a classpath
 * resource named /applicationContext.xml
 *
 * It will then load a bean named milton.http.manager which must be of type
 * HttpManager.
 *
 * An example applicationContext.xml might look like this
 *
 * <PRE>
 * {@code
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
 *
 *   <bean id="milton.resource.factory" class="com.ettrema.http.fs.FileSystemResourceFactory">
 *       <property name="securityManager" ref="milton.fs.security.manager" />
 *       <property name="maxAgeSeconds" value="3600" />
 *   </bean>
 *
 *   <bean id="milton.fs.security.manager" class="com.ettrema.http.fs.NullSecurityManager" >
 *       <property name="realm" value="aRealm" />
 *   </bean>
 *
 *   <bean id="milton.response.handler" class="io.milton.http.DefaultResponseHandler" />
 *
 *   <bean id="milton.http.manager" class="io.milton.http.HttpManager">
 *       <constructor-arg ref="milton.resource.factory" />
 *       <constructor-arg ref="milton.response.handler" />
 *   </bean>
 * </beans>
 * }
 * </PRE>
 * @author brad
 */
public class SpringAwareMiltonServlet implements Servlet {

	private final Logger log = LoggerFactory.getLogger(SpringAwareMiltonServlet.class);

	ServletConfig config;
	ApplicationContext context;
	HttpManager httpManager;

	private ServletContext servletContext;

	private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<>();
	private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<>();

	public static HttpServletRequest request() {
		return originalRequest.get();
	}

	public static HttpServletResponse response() {
		return originalResponse.get();
	}

	public static void forward(String url) {
		try {
			request().getRequestDispatcher(url).forward(originalRequest.get(), originalResponse.get());
		} catch (IOException | ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			this.config = config;
			servletContext = config.getServletContext();
			context = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });
			httpManager = (HttpManager) context.getBean("milton.http.manager");
		} catch (Throwable ex) {
			log.error("Exception starting milton servlet", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void service(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse)
			throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		HttpServletResponse resp = (HttpServletResponse) servletResponse;
		try {
			originalRequest.set(req);
			originalResponse.set(resp);
			Request request = new ServletRequest(req, servletContext);
			Response response = new ServletResponse(resp);
			httpManager.process(request, response);
		} finally {
			originalRequest.remove();
			originalResponse.remove();
			ServletRequest.clearThreadLocals();
			servletResponse.getOutputStream().flush();
			servletResponse.flushBuffer();
		}
	}

	@Override
	public String getServletInfo() {
		return "SpringAwareMiltonServlet";
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public void destroy() {

	}
}
