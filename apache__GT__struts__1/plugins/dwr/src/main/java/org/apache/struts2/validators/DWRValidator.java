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
package org.apache.struts2.validators;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ApplicationMap;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.RequestMap;
import org.apache.struts2.dispatcher.SessionMap;

import org.directwebremoting.WebContextFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.DefaultActionInvocation;
import com.opensymphony.xwork2.interceptor.ValidationAware;
import com.opensymphony.xwork2.ValidationAwareSupport;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * <p>
 * Use the dwr configuration as follows:
 * </p>
 *
 * <pre>
 * <!-- START SNIPPET: dwrConfiguration -->
 *
 * &lt;dwr&gt;
 *    &lt;allow&gt;
 *      &lt;create creator="new" javascript="validator" class="org.apache.struts2.validators.DWRValidator"/&gt;
 *      &lt;convert converter="bean" match="com.opensymphony.xwork2.ValidationAwareSupport"/&gt;
 *    &lt;/allow&gt;
 * &lt;/dwr&gt;
 *
 * <!-- END SNIPPET: dwrConfiguration -->
 * </pre>
 */
public class DWRValidator {

	private static final Logger LOG = LogManager.getLogger(DWRValidator.class);

	public ValidationAwareSupport doPost(String namespace, String actionName, Map params) throws Exception {
		HttpServletRequest req = WebContextFactory.get().getHttpServletRequest();
		ServletContext servletContext = WebContextFactory.get().getServletContext();
		HttpServletResponse res = WebContextFactory.get().getHttpServletResponse();

		HttpParameters.Builder requestParams = HttpParameters.create(req.getParameterMap());
		if (params != null) {
			requestParams = requestParams.withExtraParams(params);
		}
		Map<String, Object> requestMap = new RequestMap(req);
		Map<String, Object> session = new SessionMap<>(req);
		Map<String, Object> application = new ApplicationMap(servletContext);
		Dispatcher du = Dispatcher.getInstance();
		Map<String, Object> ctx = du.createContextMap(requestMap, requestParams.build(), session, application, req,
				res);

		try {
			ActionProxyFactory actionProxyFactory = du.getContainer().getInstance(ActionProxyFactory.class);
			ActionProxy proxy = actionProxyFactory.createActionProxy(namespace, actionName, null, ctx, true, true);
			proxy.execute();
			Object action = proxy.getAction();

			if (action instanceof ValidationAware) {
				ValidationAware aware = (ValidationAware) action;
				ValidationAwareSupport vas = new ValidationAwareSupport();
				vas.setActionErrors(aware.getActionErrors());
				vas.setActionMessages(aware.getActionMessages());
				vas.setFieldErrors(aware.getFieldErrors());

				return vas;
			} else {
				return null;
			}
		} catch (Exception e) {
			LOG.error("Error while trying to validate", e);
			return null;
		}
	}

	public static class ValidatorActionInvocation extends DefaultActionInvocation {

		protected ValidatorActionInvocation(Map<String, Object> extraContext, boolean pushAction) throws Exception {
			super(extraContext, pushAction);
		}

		protected String invokeAction(Object action, ActionConfig actionConfig) throws Exception {
			return Action.NONE; // don't actually execute the action
		}
	}

}
