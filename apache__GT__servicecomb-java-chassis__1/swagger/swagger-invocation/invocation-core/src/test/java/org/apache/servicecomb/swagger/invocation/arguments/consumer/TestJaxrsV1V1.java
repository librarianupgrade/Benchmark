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
package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBeanParamV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddWrapperV1;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.swagger.v3.oas.models.OpenAPI;

@SuppressWarnings("unchecked")
public class TestJaxrsV1V1 {
	@Test
	public void should_mapper_consumer_multi_args_to_swagger_multi_args() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x", 1);
		arguments.put("y", 2);
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	interface ConsumerAddV1_diff_order {
		int add(int y, int x);
	}

	@Test
	public void should_mapper_consumer_multi_args_to_swagger_multi_args_with_diff_order() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1_diff_order.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x", 1);
		arguments.put("y", 2);
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	@Test
	public void should_mapper_consumer_multi_args_to_swagger_multi_args_gen_by_BeanParam() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddBeanParamV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x", 1);
		arguments.put("y", 2);
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	@Test
	public void should_mapper_consumer_multi_args_to_swagger_body() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddBodyV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x", 1);
		arguments.put("y", 2);
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(1, result.size());
		result = (Map<String, Object>) result.get("addBody");
		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	@Test
	public void should_mapper_consumer_wrapped_body_to_swagger_multi_args() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("addBody", new AddWrapperV1(1, 2));
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	@Test
	public void should_mapper_consumer_wrapped_body_to_swagger_multi_args_gen_by_BeanParam() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddBeanParamV1.class);

		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("addBody", new AddWrapperV1(1, 2));
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(1, (int) result.get("x"));
		Assertions.assertEquals(2, (int) result.get("y"));
	}

	@Test
	public void should_mapper_consumer_body_to_swagger_body() {
		SwaggerEnvironment environment = new SwaggerEnvironment();
		OpenAPI swagger = SwaggerGenerator.generate(JaxrsAddBodyV1.class);
		SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
		ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("addBody", new AddWrapperV1(1, 2));
		SwaggerInvocation invocation = new SwaggerInvocation();

		Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

		Assertions.assertSame(result.get("addBody"), arguments.get("addBody"));
	}
}
