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
package com.opensymphony.xwork2.validator;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.SimpleAction;
import com.opensymphony.xwork2.StubTextProvider;
import com.opensymphony.xwork2.StubValueStack;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.XWorkTestCase;
import com.opensymphony.xwork2.config.providers.MockConfigurationProvider;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork2.interceptor.ValidationAware;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.validator.validators.ValidatorSupport;
import org.apache.struts2.config.StrutsXmlConfigurationProvider;
import org.apache.struts2.dispatcher.HttpParameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Jason Carreira
 */
public class SimpleActionValidationTest extends XWorkTestCase {

	public void testAliasValidation() {
		Map<String, Object> params = new HashMap<>();
		params.put("baz", "10");

		//valid values
		params.put("bar", "7");
		params.put("date", "12/23/2002");
		params.put("percentage", "1.23456789");

		Map<String, Object> extraContext = ActionContext.of(new HashMap<>())
				.withParameters(HttpParameters.create(params).build()).bind().getContextMap();

		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("",
					MockConfigurationProvider.VALIDATION_ACTION_NAME, null, extraContext);
			proxy.execute();

			ValidationAware validationAware = (ValidationAware) proxy.getAction();
			assertFalse(validationAware.hasFieldErrors());

			params.put("bar", "42");
			extraContext = ActionContext.of(new HashMap<>()).withParameters(HttpParameters.create(params).build())
					.bind().getContextMap();

			proxy = actionProxyFactory.createActionProxy("", MockConfigurationProvider.VALIDATION_ALIAS_NAME, null,
					extraContext);
			proxy.execute();
			validationAware = (ValidationAware) proxy.getAction();
			assertTrue(validationAware.hasFieldErrors());

			Map<String, List<String>> errors = validationAware.getFieldErrors();
			assertTrue(errors.containsKey("baz"));

			List<String> bazErrors = errors.get("baz");
			assertEquals(1, bazErrors.size());

			String message = bazErrors.get(0);
			assertEquals("baz out of range.", message);
			assertTrue(errors.containsKey("bar"));

			List<String> barErrors = errors.get("bar");
			assertEquals(1, barErrors.size());
			message = barErrors.get(0);
			assertEquals("bar must be between 6 and 10, current value is 42.", message);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testLookingUpFieldNameAsTextKey() {
		HashMap<String, Object> params = new HashMap<>();

		// should cause a message
		params.put("baz", "-1");

		//valid values
		params.put("bar", "7");

		HashMap<String, Object> extraContext = new HashMap<>();
		extraContext.put(ActionContext.PARAMETERS, HttpParameters.create(params).build());

		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("",
					MockConfigurationProvider.VALIDATION_ACTION_NAME, null, extraContext);
			proxy.execute();
			assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

			Map<String, List<String>> errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
			List<String> bazErrors = errors.get("baz");
			assertEquals(1, bazErrors.size());

			String errorMessage = bazErrors.get(0);
			assertNotNull(errorMessage);
			assertEquals("Baz Field must be greater than 0", errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testMessageKey() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("foo", "200");

		HashMap<String, Object> extraContext = new HashMap<>();
		extraContext.put(ActionContext.PARAMETERS, HttpParameters.create(params).build());

		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("",
					MockConfigurationProvider.VALIDATION_ACTION_NAME, null, extraContext);
			ValueStack stack = ActionContext.getContext().getValueStack();
			stack.getActionContext().withLocale(Locale.US);

			proxy.execute();
			assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

			Map<String, List<String>> errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
			List<String> fooErrors = errors.get("foo");
			assertEquals(1, fooErrors.size());

			String errorMessage = fooErrors.get(0);
			assertNotNull(errorMessage);
			assertEquals("Foo Range Message", errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testMessageKeyIsReturnedIfNoOtherDefault() throws ValidationException {
		Validator validator = new ValidatorSupport() {
			public void validate(Object object) throws ValidationException {
				addActionError(object);
			}
		};
		validator.setValueStack(ActionContext.getContext().getValueStack());

		String messageKey = "does.not.exist";
		validator.setMessageKey(messageKey);

		SimpleAction action = new SimpleAction();
		container.inject(action);

		ValidatorContext validatorContext = new DelegatingValidatorContext(action,
				container.getInstance(TextProviderFactory.class));
		validator.setValidatorContext(validatorContext);
		validator.validate(this);
		assertTrue(validatorContext.hasActionErrors());

		Collection<String> errors = validatorContext.getActionErrors();
		assertEquals(1, errors.size());
		assertEquals(messageKey, errors.toArray()[0]);
	}

	public void testParamterizedMessage() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("bar", "42");

		HashMap<String, Object> extraContext = new HashMap<>();
		extraContext.put(ActionContext.PARAMETERS, HttpParameters.create(params).build());

		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("",
					MockConfigurationProvider.VALIDATION_ACTION_NAME, null, extraContext);
			proxy.execute();
			assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

			Map<String, List<String>> errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
			List<String> barErrors = errors.get("bar");
			assertEquals(1, barErrors.size());

			String errorMessage = barErrors.get(0);
			assertNotNull(errorMessage);
			assertEquals("bar must be between 6 and 10, current value is 42.", errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testSubPropertiesAreValidated() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("baz", "10");

		//valid values
		params.put("foo", "8");
		params.put("bar", "7");
		params.put("date", "12/23/2002");

		params.put("bean.name", "Name should be valid");

		// this should cause a message
		params.put("bean.count", "100");

		HashMap<String, Object> extraContext = new HashMap<>();
		extraContext.put(ActionContext.PARAMETERS, HttpParameters.create(params).build());

		try {
			ActionProxy proxy = actionProxyFactory.createActionProxy("",
					MockConfigurationProvider.VALIDATION_SUBPROPERTY_NAME, null, extraContext);
			proxy.execute();
			assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

			Map<String, List<String>> errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
			List<String> beanCountErrors = errors.get("bean.count");
			assertEquals(1, beanCountErrors.size());

			String errorMessage = beanCountErrors.get(0);
			assertNotNull(errorMessage);
			assertEquals("bean.count out of range.", errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testInitializable() throws Exception {
		ValidatorFactory validatorFactory = container.getInstance(ValidatorFactory.class);
		assertEquals("com.opensymphony.xwork2.validator.validators.RequiredFieldValidator",
				validatorFactory.lookupRegisteredValidatorType("requiredAnother"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		XmlConfigurationProvider provider = new StrutsXmlConfigurationProvider("xwork-test-beans.xml");
		container.inject(provider);
		loadConfigurationProviders(provider, new MockConfigurationProvider());
	}

}
