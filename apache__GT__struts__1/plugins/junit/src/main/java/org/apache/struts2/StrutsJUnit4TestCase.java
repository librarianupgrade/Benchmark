/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.XWorkJUnit4TestCase;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.interceptor.ValidationAware;
import com.opensymphony.xwork2.interceptor.annotations.After;
import com.opensymphony.xwork2.interceptor.annotations.Before;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.util.StrutsTestCaseHelper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public abstract class StrutsJUnit4TestCase<T> extends XWorkJUnit4TestCase {

	protected MockHttpServletResponse response;
	protected MockHttpServletRequest request;
	protected MockPageContext pageContext;
	protected MockServletContext servletContext;
	protected Map<String, String> dispatcherInitParams;
	protected Dispatcher dispatcher;

	protected DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * gets an object from the stack after an action is executed
	 */
	protected Object findValueAfterExecute(String key) {
		return ServletActionContext.getValueStack(request).findValue(key);
	}

	/**
	 * gets an object from the stack after an action is executed
	 *
	 * @return The executed action
	 */
	@SuppressWarnings("unchecked")
	protected T getAction() {
		return (T) findValueAfterExecute("action");
	}

	protected boolean containsErrors() {
		T action = this.getAction();
		if (action instanceof ValidationAware) {
			return ((ValidationAware) action).hasActionErrors();
		}
		throw new UnsupportedOperationException("Current action does not implement ValidationAware interface");
	}

	/**
	 * Executes an action and returns it's output (not the result returned from
	 * execute()), but the actual output that would be written to the response.
	 * For this to work the configured result for the action needs to be
	 * FreeMarker, or Velocity (JSPs can be used with the Embedded JSP plugin)
	 */
	protected String executeAction(String uri) throws ServletException, UnsupportedEncodingException {
		request.setRequestURI(uri);
		ActionMapping mapping = getActionMapping(request);

		assertNotNull(mapping);
		Dispatcher.getInstance().serviceAction(request, response, mapping);

		if (response.getStatus() != HttpServletResponse.SC_OK)
			throw new ServletException(
					"Error code [" + response.getStatus() + "], Error: [" + response.getErrorMessage() + "]");

		return response.getContentAsString();
	}

	/**
	 * Creates an action proxy for a request, and sets parameters of the ActionInvocation to the passed
	 * parameters. Make sure to set the request parameters in the protected "request" object before calling this method.
	 */
	protected ActionProxy getActionProxy(String uri) {
		request.setRequestURI(uri);
		ActionMapping mapping = getActionMapping(request);
		String namespace = mapping.getNamespace();
		String name = mapping.getName();
		String method = mapping.getMethod();

		Configuration config = configurationManager.getConfiguration();
		ActionProxy proxy = config.getContainer().getInstance(ActionProxyFactory.class).createActionProxy(namespace,
				name, method, new HashMap<String, Object>(), true, false);

		initActionContext(proxy.getInvocation().getInvocationContext());

		// this is normally done in onSetUp(), but we are using Struts internal
		// objects (proxy and action invocation)
		// so we have to hack around so it works
		ServletActionContext.setServletContext(servletContext);
		ServletActionContext.setRequest(request);
		ServletActionContext.setResponse(response);

		ServletActionContext.getContext().put(ServletActionContext.ACTION_MAPPING, mapping);

		return proxy;
	}

	protected void initActionContext(ActionContext actionContext) {
		actionContext.setParameters(HttpParameters.create(request.getParameterMap()).build());
		initSession(actionContext);
		// set the action context to the one used by the proxy
		ActionContext.bind(actionContext);
	}

	protected void initSession(ActionContext actionContext) {
		if (actionContext.getSession() == null) {
			actionContext.withSession(new HashMap<>());
			request.setSession(new MockHttpSession(servletContext));
		}
	}

	/**
	 * Finds an ActionMapping for a given request
	 */
	protected ActionMapping getActionMapping(HttpServletRequest request) {
		return container.getInstance(ActionMapper.class).getMapping(request, configurationManager);
	}

	/**
	 * Finds an ActionMapping for a given url
	 */
	protected ActionMapping getActionMapping(String url) {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setRequestURI(url);
		return getActionMapping(req);
	}

	/**
	 * Injects dependencies on an Object using Struts internal IoC container
	 */
	protected void injectStrutsDependencies(Object object) {
		container.inject(object);
	}

	protected void setupBeforeInitDispatcher() throws Exception {
	}

	protected void initServletMockObjects() {
		servletContext = new MockServletContext(resourceLoader);
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
		pageContext = new MockPageContext(servletContext, request, response);
	}

	public void finishExecution() {
		HttpSession session = this.request.getSession();
		Enumeration attributeNames = session.getAttributeNames();

		MockHttpServletRequest nextRequest = new MockHttpServletRequest();

		while (attributeNames.hasMoreElements()) {
			String key = (String) attributeNames.nextElement();
			Object attribute = session.getAttribute(key);
			nextRequest.getSession().setAttribute(key, attribute);
		}

		this.response = new MockHttpServletResponse();
		this.request = nextRequest;
		this.pageContext = new MockPageContext(servletContext, request, response);
	}

	/**
	 * Sets up the configuration settings, XWork configuration, and
	 * message resources
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		initServletMockObjects();
		setupBeforeInitDispatcher();
		initDispatcherParams();
		initDispatcher(dispatcherInitParams);
	}

	protected void initDispatcherParams() {
		if (StringUtils.isNotBlank(getConfigPath())) {
			dispatcherInitParams = new HashMap<>();
			dispatcherInitParams.put("config", "struts-default.xml," + getConfigPath());
		}
	}

	protected Dispatcher initDispatcher(Map<String, String> params) {
		dispatcher = StrutsTestCaseHelper.initDispatcher(servletContext, params);
		configurationManager = dispatcher.getConfigurationManager();
		configuration = configurationManager.getConfiguration();
		container = configuration.getContainer();
		container.inject(dispatcher);
		return dispatcher;
	}

	/**
	 * Override this method to return a comma separated list of paths to a configuration
	 * file.
	 * <p>The default implementation simply returns <code>null</code>.
	 * @return a comma separated list of config locations
	 */
	protected String getConfigPath() {
		return null;
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		if (dispatcher != null && dispatcher.getConfigurationManager() != null) {
			dispatcher.cleanup();
			dispatcher = null;
		}
		StrutsTestCaseHelper.tearDown();
	}

}
