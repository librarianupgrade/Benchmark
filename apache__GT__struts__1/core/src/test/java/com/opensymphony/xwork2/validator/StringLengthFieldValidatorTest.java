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
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.XWorkTestCase;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.validator.validators.StringLengthFieldValidator;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author tm_jee
 * @version $Date$ $Id$
 */
public class StringLengthFieldValidatorTest extends XWorkTestCase {

	protected InternalActionSupport action;
	protected StringLengthFieldValidator validator;

	public void testStringLengthEmptyNoTrim1() throws Exception {
		action.setMyField("");

		validator.setTrim(false);
		validator.validate(action);

		assertEquals(action.getMyField(), "");
		assertFalse(action.hasFieldErrors());
	}

	public void testStringLengthNullNoTrim() throws Exception {
		action.setMyField(null);

		validator.setTrim(false);
		validator.validate(action);

		assertEquals(action.getMyField(), null);
		assertFalse(action.hasFieldErrors());
	}

	public void testStringLengthEmptyTrim1() throws Exception {
		action.setMyField("   ");

		validator.setTrim(true);
		validator.validate(action);

		assertEquals(action.getMyField(), "   ");
		assertFalse(action.hasFieldErrors());
	}

	public void testStringLengthEmptyNoTrim2() throws Exception {
		action.setMyField("          ");

		validator.setTrim(false);
		validator.validate(action);

		assertEquals(action.getMyField(), "          ");
		assertTrue(action.hasFieldErrors());
	}

	public void testStringLengthNullTrim() throws Exception {
		action.setMyField(null);

		validator.setTrim(true);
		validator.validate(action);

		assertEquals(action.getMyField(), null);
		assertFalse(action.hasFieldErrors());
	}

	public void testInvalidStringLengthNoTrim() throws Exception {
		action.setMyField("abcdefghijklmn");

		validator.setTrim(false);
		validator.validate(action);

		assertEquals(action.getMyField(), "abcdefghijklmn");
		assertTrue(action.hasFieldErrors());
	}

	public void testInvalidStringLengthTrim() throws Exception {
		action.setMyField("abcdefghijklmn   ");

		validator.setTrim(true);
		validator.validate(action);

		assertEquals(action.getMyField(), "abcdefghijklmn   ");
		assertTrue(action.hasFieldErrors());
	}

	public void testValidStringLengthNoTrim() throws Exception {
		action.setMyField("   ");

		validator.setTrim(false);
		validator.validate(action);

		assertEquals(action.getMyField(), "   ");
		assertFalse(action.hasFieldErrors());
	}

	public void testValidStringLengthTrim() throws Exception {
		action.setMyField("asd          ");

		validator.setTrim(true);
		validator.validate(action);

		assertEquals(action.getMyField(), "asd          ");
		assertFalse(action.hasFieldErrors());
	}

	public void testArrayOfStringsLengthTrim() throws Exception {
		action.setStrings(new String[] { "123456", "    ", null });

		validator.setFieldName("strings");
		validator.setTrim(true);
		validator.validate(action);

		assertTrue(action.hasFieldErrors());
		assertEquals(1, action.getFieldErrors().get("strings").size());
	}

	public void testCollectionOfStringsLengthTrim() throws Exception {
		action.setStringCollection(Arrays.asList("123456", "    ", null));

		validator.setFieldName("stringCollection");
		validator.setTrim(true);
		validator.validate(action);

		assertTrue(action.hasFieldErrors());
		assertEquals(1, action.getFieldErrors().get("stringCollection").size());
	}

	public void testMinLengthViaExpression() throws Exception {
		assertEquals(2, validator.getMinLength());
		action.setMinLengthValue(10);

		validator.setMinLengthExpression("${minLengthValue}");

		assertEquals(10, validator.getMinLength());
	}

	public void testMaxLengthViaExpression() throws Exception {
		assertEquals(5, validator.getMaxLength());
		action.setMaxLengthValue(100);

		validator.setMaxLengthExpression("${maxLengthValue}");

		assertEquals(100, validator.getMaxLength());
	}

	public void testTrimViaExpression() throws Exception {
		assertTrue(validator.isTrim());
		action.setTrimValue(false);

		validator.setTrimExpression("${trimValue}");

		assertFalse(validator.isTrim());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		action = new InternalActionSupport();
		container.inject(action);

		ValueStack valueStack = ActionContext.getContext().getValueStack();
		valueStack.push(action);

		validator = new StringLengthFieldValidator();
		validator.setFieldName("myField");
		validator.setMessageKey("error");
		validator.setValidatorContext(
				new DelegatingValidatorContext(action, container.getInstance(TextProviderFactory.class)));
		validator.setMaxLength(5);
		validator.setMinLength(2);
		validator.setValueStack(valueStack);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		action = null;
		validator = null;
	}

	public static class InternalActionSupport extends ActionSupport {

		private static final long serialVersionUID = 1L;

		private String myField;
		private boolean trimValue;
		private int minLengthValue;
		private int maxLengthValue;

		private String[] strings;
		private Collection<String> stringCollection;

		public String getMyField() {
			return this.myField;
		}

		public void setMyField(String myField) {
			this.myField = myField;
		}

		public boolean isTrimValue() {
			return trimValue;
		}

		public void setTrimValue(boolean trimValue) {
			this.trimValue = trimValue;
		}

		public int getMinLengthValue() {
			return minLengthValue;
		}

		public void setMinLengthValue(int minLengthValue) {
			this.minLengthValue = minLengthValue;
		}

		public int getMaxLengthValue() {
			return maxLengthValue;
		}

		public void setMaxLengthValue(int maxLengthValue) {
			this.maxLengthValue = maxLengthValue;
		}

		public String[] getStrings() {
			return strings;
		}

		public void setStrings(String[] strings) {
			this.strings = strings;
		}

		public Collection<String> getStringCollection() {
			return stringCollection;
		}

		public void setStringCollection(Collection<String> stringCollection) {
			this.stringCollection = stringCollection;
		}
	}

}
