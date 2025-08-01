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
package org.apache.struts2.interceptor;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.StrutsInternalTestCase;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.mock.MockActionInvocation;

/**
 * Test case for ClearSessionInterceptor.
 */
public class ClearSessionInterceptorTest extends StrutsInternalTestCase {

	public void testCreateSession() throws Exception {
		ClearSessionInterceptor interceptor = new ClearSessionInterceptor();
		MockActionInvocation invocation = new MockActionInvocation();
		ActionContext context = ActionContext.of(new HashMap<>()).bind();
		Map<String, Object> session = new HashMap<>();
		session.put("Test1", "Test1");
		session.put("Test2", "Test2");
		session.put("Test3", "Test3");
		context.setSession(session);
		invocation.setInvocationContext(context);
		interceptor.intercept(invocation);

		assertEquals(0, session.size());
	}
}
