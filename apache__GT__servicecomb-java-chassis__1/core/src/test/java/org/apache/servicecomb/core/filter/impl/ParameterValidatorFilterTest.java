/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.core.filter.impl;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.servicecomb.core.exception.ExceptionCodes.DEFAULT_VALIDATE;
import static org.apache.servicecomb.core.exception.converter.ConstraintViolationExceptionConverter.KEY_CODE;
import static org.apache.servicecomb.core.filter.impl.ParameterValidatorFilter.ENABLE_EL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.exception.converter.ConstraintViolationExceptionConverter;
import org.apache.servicecomb.core.exception.converter.ValidateDetail;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mockit.Expectations;
import mockit.Mocked;

public class ParameterValidatorFilterTest {
	public static class BaseModel {
		@NotNull(message = "can not be null")
		private String name;

		public String getName() {
			return name;
		}

		public BaseModel setName(String name) {
			this.name = name;
			return this;
		}
	}

	public static class Model extends BaseModel {
		@NotBlank(message = "can not be blank")
		@JsonProperty(value = "nick-name")
		private String nickName;

		public String getNickName() {
			return nickName;
		}

		public Model setNickName(String nickName) {
			this.nickName = nickName;
			return this;
		}
	}

	public static class Controller {
		public void op(@NotNull(message = "not null") String query, @Valid Model model) {

		}
	}

	ParameterValidatorFilter filter = new ParameterValidatorFilter();

	@Mocked
	Invocation invocation;

	@Mocked
	SwaggerProducerOperation operation;

	ConfigurableEnvironment environment;

	@Before
	public void setUp() throws Exception {
		environment = Mockito.mock(ConfigurableEnvironment.class);
		SCBEngine engine = SCBBootstrap.createSCBEngineForTest(environment);
		MutablePropertySources sources = new MutablePropertySources();

		Mockito.when(environment.getProperty(ENABLE_EL, boolean.class, false)).thenReturn(false);
		Mockito.when(environment.getPropertySources()).thenReturn(sources);
		filter.setEnvironment(environment);
		filter.afterPropertiesSet();
		engine.setEnvironment(environment);
		new Expectations() {
			{
				operation.getProducerInstance();
				result = new Controller();

				operation.getProducerMethod();
				result = MethodUtils.getAccessibleMethod(Controller.class, "op", String.class, Model.class);

				invocation.toProducerArguments();
				result = new Object[] { null, new Model() };
			}
		};
	}

	private InvocationException getException() {
		Throwable throwable = catchThrowable(() -> filter.onFilter(invocation, FilterNode.EMPTY).get());
		return Exceptions.convert(invocation, throwable, BAD_REQUEST);
	}

	private CommonExceptionData getExceptionData() {
		InvocationException invocationException = getException();
		return (CommonExceptionData) invocationException.getErrorData();
	}

	@Test
	public void status_code_should_be_bad_request() {
		InvocationException invocationException = getException();

		assertThat(invocationException.getStatusCode()).isEqualTo(BAD_REQUEST.getStatusCode());
	}

	@Test
	public void error_code_and_message_should_be_build_in_value() {
		Mockito.when(environment.getProperty(KEY_CODE, String.class, DEFAULT_VALIDATE)).thenReturn(DEFAULT_VALIDATE);
		CommonExceptionData errorData = getExceptionData();

		assertThat(errorData.getCode()).isEqualTo(DEFAULT_VALIDATE);
		assertThat(errorData.getMessage()).isEqualTo("invalid parameters.");
	}

	@Test
	public void should_allow_customize_error_code_by_configuration() {
		Mockito.when(environment.getProperty(KEY_CODE, String.class, DEFAULT_VALIDATE)).thenReturn("test.0001");
		SPIServiceUtils.getTargetService(ExceptionConverter.class, ConstraintViolationExceptionConverter.class);
		CommonExceptionData errorData = getExceptionData();

		assertThat(errorData.getCode()).isEqualTo("test.0001");
	}

	@Test
	public void should_use_json_property_value_as_property_name() {
		CommonExceptionData errorData = getExceptionData();
		List<ValidateDetail> details = errorData.getDynamic("validateDetail");

		assertThat(details.stream().map(ValidateDetail::getPropertyPath)).contains("op.model.nick-name");
	}

	@Test
	public void should_include_all_validate_detail() {
		CommonExceptionData errorData = getExceptionData();
		List<ValidateDetail> details = errorData.getDynamic("validateDetail");

		assertThat(details.stream().map(ValidateDetail::getPropertyPath)).contains("op.query", "op.model.name",
				"op.model.nick-name");
		assertThat(details.stream().map(ValidateDetail::getMessage)).contains("not null", "can not be null",
				"can not be blank");
	}
}
