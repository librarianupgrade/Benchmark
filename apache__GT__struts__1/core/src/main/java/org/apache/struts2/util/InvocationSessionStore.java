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
package org.apache.struts2.util;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * InvocationSessionStore
 *
 */
public class InvocationSessionStore {

	private static final String INVOCATION_MAP_KEY = "org.apache.struts2.util.InvocationSessionStore.invocationMap";

	private InvocationSessionStore() {
	}

	/**
	 * Checks the Map in the Session for the key and the token. If the
	 * ActionInvocation is saved in the Session, the ValueStack from the
	 * ActionProxy associated with the ActionInvocation is set into the
	 * ActionContext and the ActionInvocation is returned.
	 *
	 * @param key the name the DefaultActionInvocation and ActionContext were saved as
	 * @param token token for check
	 * @return the DefaultActionInvocation saved using the key, or null if none was found
	 */
	public static ActionInvocation loadInvocation(String key, String token) {
		InvocationContext invocationContext = (InvocationContext) getInvocationMap().get(key);

		if ((invocationContext == null) || !invocationContext.token.equals(token)) {
			return null;
		}

		final ActionInvocation savedInvocation = invocationContext.invocation;
		if (savedInvocation != null) {
			// WW-5026 - Preserve the previous PageContext (even if null) and restore it to the
			// ActionContext after loading the savedInvocation context.  The saved context's PageContext
			// would already be closed at this point (causing failures if used for output).
			final ActionContext previousActionContext = ActionContext.getContext();

			savedInvocation.getInvocationContext().withPageContext(previousActionContext.getPageContext())
					.withValueStack(savedInvocation.getStack()).bind();
		}

		return savedInvocation;
	}

	/**
	 * Stores the DefaultActionInvocation and ActionContext into the Session using the provided key for loading later using
	 * {@link #loadInvocation}
	 *
	 * @param key the name the DefaultActionInvocation and ActionContext were saved as
	 * @param token token for check
	 * @param invocation the action invocation
	 */
	public static void storeInvocation(String key, String token, ActionInvocation invocation) {
		InvocationContext invocationContext = new InvocationContext(invocation, token);
		Map<String, Object> invocationMap = getInvocationMap();
		invocationMap.put(key, invocationContext);
		setInvocationMap(invocationMap);
	}

	static void setInvocationMap(Map<String, Object> invocationMap) {
		Map<String, Object> session = ActionContext.getContext().getSession();

		if (session == null) {
			throw new IllegalStateException("Unable to access the session.");
		}

		session.put(INVOCATION_MAP_KEY, invocationMap);
	}

	static Map<String, Object> getInvocationMap() {
		Map<String, Object> session = ActionContext.getContext().getSession();

		if (session == null) {
			throw new IllegalStateException("Unable to access the session.");
		}

		Map<String, Object> invocationMap = (Map<String, Object>) session.get(INVOCATION_MAP_KEY);

		if (invocationMap == null) {
			invocationMap = new HashMap<>();
			setInvocationMap(invocationMap);
		}

		return invocationMap;
	}

	private static class InvocationContext implements Serializable {

		private static final long serialVersionUID = -286697666275777888L;

		//WW-4873 transient since 2.5.15
		transient ActionInvocation invocation;

		String token;

		public InvocationContext(ActionInvocation invocation, String token) {
			this.invocation = invocation;
			this.token = token;
		}
	}
}
