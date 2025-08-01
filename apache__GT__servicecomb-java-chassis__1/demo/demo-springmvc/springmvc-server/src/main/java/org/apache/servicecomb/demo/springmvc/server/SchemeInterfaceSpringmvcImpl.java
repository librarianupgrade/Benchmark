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

package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.provider.rest.common.RestSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;

@RestSchema(schemaId = "SchemeInterfaceSpringmvc", schemaInterface = SchemeInterfaceSpringmvc.class)
public class SchemeInterfaceSpringmvcImpl implements SchemeInterfaceSpringmvc {
	@Override
	public int add(@Min(1) int a, @Min(1) int b) {
		return a + b;
	}

	@Override
	public String tailingSlash(HttpServletRequest request, int a, int b) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURI()).append(";").append(request.getRequestURL()).append(";")
				.append(request.getPathInfo()).append(";").append(a - b);
		return sb.toString();
	}

	@Override
	public String nonTailingSlash(HttpServletRequest request, int a, int b) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURI()).append(";").append(request.getRequestURL()).append(";")
				.append(request.getPathInfo()).append(";").append(a - b);
		return sb.toString();
	}

	public int reduce(int a, int b) {
		return a - b;
	}
}
