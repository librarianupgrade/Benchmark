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
package org.apache.servicecomb.swagger.generator;

import org.apache.servicecomb.swagger.generator.core.OperationGeneratorContext;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

public interface OperationGenerator {
	OpenAPI getSwagger();

	Operation getOperation();

	void setHttpMethod(String httpMethod);

	String getHttpMethod();

	void addOperationToSwagger();

	void setPath(String value);

	OperationGeneratorContext getOperationGeneratorContext();

	/**
	 * Used to check if one of operation has form parameter
	 */
	boolean isForm();

	/**
	 * Used to check if one of operation form parameter is binary
	 */
	boolean isBinary();

	/**
	 *
	 * Used to check if this operation is websocket
	 */
	boolean isWebsocket();
}
