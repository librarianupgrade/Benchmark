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
package org.apache.servicecomb.solution.basic.integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.provider.LocalOpenAPIRegistry;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.models.OpenAPI;

@RestSchema(schemaId = ManagementEndpoint.NAME, schemaInterface = ManagementEndpoint.class)
public class ManagementEndpointImpl implements ManagementEndpoint {
	private RegistrationManager registrationManager;

	private LocalOpenAPIRegistry localOpenAPIRegistry;

	@Autowired
	public void setRegistrationManager(RegistrationManager registrationManager) {
		this.registrationManager = registrationManager;
	}

	@Autowired
	public void setLocalOpenAPIRegistry(LocalOpenAPIRegistry localOpenAPIRegistry) {
		this.localOpenAPIRegistry = localOpenAPIRegistry;
	}

	@Override
	public boolean health(String instanceId, String registryName) {
		if (StringUtils.isEmpty(instanceId) || StringUtils.isEmpty(registryName)) {
			return false;
		}
		String mySelf = registrationManager.getInstanceId(registryName);
		if (StringUtils.isEmpty(mySelf)) {
			return false;
		}
		return mySelf.equals(instanceId);
	}

	@Override
	public Map<String, String> schemaContents() {
		Map<String, OpenAPI> apis = localOpenAPIRegistry.loadOpenAPI();
		Map<String, String> result = new HashMap<>(apis.size());
		apis.forEach((k, v) -> result.put(k, SwaggerUtils.swaggerToString(v)));
		return result;
	}
}
