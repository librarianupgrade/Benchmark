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

package org.apache.servicecomb.provider.rest.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.producer.AbstractProducerContextArgMapper;

public class ProducerHttpRequestArgMapper extends AbstractProducerContextArgMapper {
	public ProducerHttpRequestArgMapper(String invocationArgumentName, String swaggerArgumentName) {
		super(invocationArgumentName, swaggerArgumentName);
	}

	@Override
	public Object createContextArg(SwaggerInvocation swaggerInvocation) {
		Invocation invocation = (Invocation) swaggerInvocation;
		// 从rest transport来
		HttpServletRequest request = (HttpServletRequest) invocation.getHandlerContext().get(RestConst.REST_REQUEST);
		if (request != null) {
			return request;
		}

		// 通过args模拟request
		return new InvocationToHttpServletRequest(invocation);
	}
}
