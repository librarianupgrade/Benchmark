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

package org.apache.servicecomb.demo.edge.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.invocation.response.ResponseMetaMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

public class CustomResponseMetaMapper implements ResponseMetaMapper {
	private static final Map<Integer, JavaType> CODES = new HashMap<>(1);

	static {
		CODES.put(500, SimpleType.constructUnsafe(IllegalStateErrorData.class));
	}

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public Map<Integer, JavaType> getMapper() {
		return CODES;
	}
}
