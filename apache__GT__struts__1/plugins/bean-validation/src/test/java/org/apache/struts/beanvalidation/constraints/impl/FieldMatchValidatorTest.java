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
package org.apache.struts.beanvalidation.constraints.impl;

import org.apache.struts.beanvalidation.constraints.FieldMatch;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldMatchValidatorTest {

	@Test
	public void matchingFields() {
		// given
		FieldMatchValidator validator = new FieldMatchValidator();
		validator.initialize(FieldMatchTestBean.class.getAnnotation(FieldMatch.class));

		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

		// when
		FieldMatchTestBean bean = new FieldMatchTestBean("12345678", "12345678");

		boolean valid = validator.isValid(bean, context);

		// then
		assertTrue(valid);
	}

	@Test
	public void notMatchingFields() {
		// given
		FieldMatchValidator validator = new FieldMatchValidator();
		validator.initialize(FieldMatchTestBean.class.getAnnotation(FieldMatch.class));

		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

		// when
		FieldMatchTestBean bean = new FieldMatchTestBean("12345678", "87654321");

		boolean valid = validator.isValid(bean, context);

		// then
		assertFalse(valid);
	}

	@FieldMatch(first = "password", second = "repeatPassword")
	public static class FieldMatchTestBean {
		String password;
		String repeatPassword;

		public FieldMatchTestBean(String password, String repeatPassword) {
			this.password = password;
			this.repeatPassword = repeatPassword;
		}

		public String getPassword() {
			return password;
		}

		public String getRepeatPassword() {
			return repeatPassword;
		}
	}
}
