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
package com.opensymphony.xwork2;

import com.opensymphony.xwork2.config.providers.MockConfigurationProvider;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.apache.struts2.config.StrutsXmlConfigurationProvider;

import java.util.Locale;

/**
 * LocaleAwareTest
 *
 * @author Jason Carreira
 * Created Feb 10, 2003 6:13:13 PM
 */
public class LocaleAwareTest extends XWorkTestCase {

	public void testGetText() {
		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("", MockConfigurationProvider.FOO_ACTION_NAME,
					null, null);
			ActionContext.getContext().withLocale(Locale.US);

			TextProvider localeAware = (TextProvider) proxy.getAction();
			assertEquals("Foo Range Message", localeAware.getText("foo.range"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testLocaleGetText() {
		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("", MockConfigurationProvider.FOO_ACTION_NAME,
					null, null);
			ActionContext.getContext().withLocale(Locale.GERMANY);

			TextProvider localeAware = (TextProvider) proxy.getAction();
			assertEquals("I don't know German", localeAware.getText("foo.range"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		XmlConfigurationProvider configurationProvider = new StrutsXmlConfigurationProvider("xwork-test-beans.xml");
		container.inject(configurationProvider);
		loadConfigurationProviders(configurationProvider, new MockConfigurationProvider());

		ValueStack stack = container.getInstance(ValueStackFactory.class).createValueStack();
		stack.getActionContext().withContainer(container);
		ActionContext.of(stack.getContext()).bind();
	}
}
