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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;

/**
 * map swagger arguments to producer arguments
 */
public class ProducerArgumentsMapper implements ArgumentsMapper {
	private final List<ArgumentMapper> producerArgMapperList;

	public ProducerArgumentsMapper(List<ArgumentMapper> producerArgMapperList) {
		this.producerArgMapperList = producerArgMapperList;
	}

	@Override
	public Map<String, Object> swaggerArgumentToInvocationArguments(SwaggerInvocation invocation,
			Map<String, Object> swaggerArguments) {
		Map<String, Object> invocationArguments = new HashMap<>(swaggerArguments.size());
		for (ArgumentMapper argMapper : producerArgMapperList) {
			argMapper.swaggerArgumentToInvocationArguments(invocation, swaggerArguments, invocationArguments);
		}

		return invocationArguments;
	}
}
