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
package org.apache.struts2.components;

import com.mockobjects.dynamic.Mock;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.StrutsInternalTestCase;
import org.apache.struts2.dispatcher.HttpParameters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;

public class ActionComponentTest extends StrutsInternalTestCase {

	public void testCreateParametersForContext() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		Mock mockValueStack = new Mock(ValueStack.class);
		HashMap<String, Object> ctx = new HashMap<>();
		mockValueStack.expectAndReturn("getContext", ctx);
		mockValueStack.expectAndReturn("getContext", ctx);
		mockValueStack.expectAndReturn("getActionContext", ActionContext.getContext());

		ActionComponent comp = new ActionComponent((ValueStack) mockValueStack.proxy(), req, res);
		comp.addParameter("foo", "bar");
		comp.addParameter("baz", new String[] { "jim", "sarah" });
		HttpParameters params = comp.createParametersForContext();
		assertNotNull(params);
		assertEquals(2, params.keySet().size());
		assertEquals("bar", params.get("foo").getValue());
		assertEquals(2, params.get("baz").getMultipleValues().length);
		mockValueStack.verify();
	}
}
