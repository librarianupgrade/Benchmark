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
package org.apache.servicecomb.swagger.invocation.exception;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

import mockit.Mocked;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestInvocationExceptionToProducerResponseConverter {
	InvocationExceptionToProducerResponseConverter converter = new InvocationExceptionToProducerResponseConverter();

	@Test
	public void getExceptionClass() {
		Assertions.assertEquals(InvocationException.class, converter.getExceptionClass());
	}

	@Test
	public void convert(@Mocked SwaggerInvocation swaggerInvocation) {
		InvocationException e = new InvocationException(javax.ws.rs.core.Response.Status.BAD_REQUEST, "test");
		Response response = converter.convert(swaggerInvocation, e);
		Assertions.assertSame(e, response.getResult());
	}
}
