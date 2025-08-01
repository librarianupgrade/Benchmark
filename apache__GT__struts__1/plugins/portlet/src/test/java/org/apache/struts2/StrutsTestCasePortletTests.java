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
import com.opensymphony.xwork2.XWorkTestCase;
import com.opensymphony.xwork2.config.Configuration;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

/*
 * Changes:  This is a copy of org.apache.struts2.StrutsTestCase from the Struts 2 junit-plugin, kept in
 *           in the same package org.apache.struts2 and renamed.  Removed some unused imports, made
 *           imports alphabetical and made some whitespace changes and modified a comment.
 *           The StrutsTestCasePortletTests is needed in order to break a dependency-cycle between the 
 *           portlet-plugin and junit-plugin with respect to StrutsTestCase.
 *
 * Note:     If the junit-plugin StrutsTestCase is updated/modified, it may be appropriate to update
 *           this version as well, for consistency (but they may diverge in the future).
 *           The StrutsTestCasePortletTests is intentionally kept at the test scope of the portlet-plugin
 *           as it only exists due to the dependency-cycle issue.  The junit-plugin StrutsTestCase should
 *           be used elswehere.
 */

/**
 * Base test case for JUnit testing Struts within the portlet-plugin unit tests.
 */
public abstract class StrutsTestCasePortletTests extends XWorkTestCase {
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

		if (response.getStatus() != HttpServletResponse.SC_OK) {
			throw new ServletException(
					"Error code [" + response.getStatus() + "], Error: [" + response.getErrorMessage() + "]");
		}
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

		return proxy;
	}

	/**
	 * A helper method which allows instantiate an action if this action extends
	 * {@link com.opensymphony.xwork2.ActionSupport} or any other action class
	 * that requires framework's dependencies injection.
	 */
	protected <T> T createAction(Class<T> clazz) {
		return container.inject(clazz);
	}

	protected void initActionContext(ActionContext actionContext) {
		actionContext.setParameters(HttpParameters.create(request.getParameterMap()).build());
		initSession(actionContext);
		applyAdditionalParams(actionContext);
		// set the action context to the one used by the proxy
		ActionContext.bind(actionContext);
	}

	protected void initSession(ActionContext actionContext) {
		if (actionContext.getSession() == null) {
			actionContext.setSession(new HashMap<>());
			request.setSession(new MockHttpSession(servletContext));
		}
	}

	/**
	 * Can be overwritten in subclass to provide additional context's params and settings used during action invocation
	 *
	 * @param context current {@link ActionContext}
	 */
	protected void applyAdditionalParams(ActionContext context) {
		// empty be default
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

	/**
	 * Sets up the configuration settings, XWork configuration, and
	 * message resources
	 */
	protected void setUp() throws Exception {
		super.setUp();
		initServletMockObjects();
		setupBeforeInitDispatcher();
		dispatcher = initDispatcher(dispatcherInitParams);
		setupAfterInitDispatcher(dispatcher);
	}

	protected void setupBeforeInitDispatcher() throws Exception {
		// empty by default
	}

	protected void setupAfterInitDispatcher(Dispatcher dispatcher) {
		// empty by default
	}

	protected void initServletMockObjects() {
		servletContext = new MockServletContext(resourceLoader);
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
		pageContext = new MockPageContext(servletContext, request, response);
	}

	protected Dispatcher initDispatcher(Map<String, String> params) {
		Dispatcher du = StrutsTestCaseHelper.initDispatcher(servletContext, params);
		configurationManager = du.getConfigurationManager();
		configuration = configurationManager.getConfiguration();
		container = configuration.getContainer();
		return du;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// maybe someone else already destroyed Dispatcher
		if (dispatcher != null && dispatcher.getConfigurationManager() != null) {
			dispatcher.cleanup();
			dispatcher = null;
		}
		StrutsTestCaseHelper.tearDown();
	}

}
