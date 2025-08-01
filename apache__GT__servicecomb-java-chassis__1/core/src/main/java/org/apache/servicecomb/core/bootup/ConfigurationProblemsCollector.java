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
package org.apache.servicecomb.core.bootup;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.event.AlarmEvent.Type;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.springframework.core.env.Environment;

/**
 * Detect deprecated and wrong usages of configurations
 * and print warning messages
 * and sending ConfigurationProblemsAlarmEvent.
 */
public class ConfigurationProblemsCollector implements BootUpInformationCollector {
	private static final String SERVICE_NAME_PATTERN_STRING = "([A-Za-z])|([A-Za-z][A-Za-z0-9_\\-.]*[A-Za-z0-9])";

	private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile(SERVICE_NAME_PATTERN_STRING);

	@Override
	public String collect(SCBEngine engine) {
		StringBuilder result = new StringBuilder();
		collectCsePrefix(engine.getEnvironment(), result);
		collectServiceDefinition(engine.getEnvironment(), result);
		collectServiceDefinitionValidation(engine.getEnvironment(), result);
		collectTimeoutConfiguration(engine.getEnvironment(), result);
		collectIsolationConfiguration(engine.getEnvironment(), result);
		if (result.isEmpty()) {
			return null;
		}
		String warnings = "[WARN]Configurations warnings:\n" + result;
		EventManager.post(new ConfigurationProblemsAlarmEvent(Type.OPEN, warnings));
		return warnings;
	}

	private void collectServiceDefinitionValidation(Environment environment, StringBuilder result) {
		String application = BootStrapProperties.readApplication(environment);
		if (!SERVICE_NAME_PATTERN.matcher(application).matches()) {
			result.append("application does not match pattern ").append(SERVICE_NAME_PATTERN_STRING).append(".");
		}
		String serviceName = BootStrapProperties.readServiceName(environment);
		if (!SERVICE_NAME_PATTERN.matcher(serviceName).matches()) {
			result.append("service name does not match pattern ").append(SERVICE_NAME_PATTERN_STRING).append(".");
		}
	}

	private void collectIsolationConfiguration(Environment environment, StringBuilder result) {
		int percentage = environment.getProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage",
				int.class, -1);
		int continuous = environment.getProperty("servicecomb.loadbalance.isolation.continuousFailureThreshold",
				int.class, -1);
		if (percentage == -1 && continuous == -1) {
			return;
		}
		result.append("Configuration `servicecomb.loadbalance.isolation.*` is removed, use governance instead. "
				+ "See https://servicecomb.apache.org/references/java-chassis/"
				+ "zh_CN/references-handlers/governance-best-practise.html");
	}

	private void collectTimeoutConfiguration(Environment environment, StringBuilder result) {
		int keepAliveTimeoutInSeconds = environment
				.getProperty("servicecomb.rest.client.connection.keepAliveTimeoutInSeconds", int.class, 60);
		int idleTimeoutInSeconds = environment.getProperty("servicecomb.rest.client.connection.idleTimeoutInSeconds",
				int.class, 150);
		if (keepAliveTimeoutInSeconds >= idleTimeoutInSeconds) {
			result.append("Configuration `servicecomb.rest.client.connection.keepAliveTimeoutInSeconds` is longer than "
					+ "servicecomb.rest.client.connection.idleTimeoutInSeconds.");
			result.append("[").append(keepAliveTimeoutInSeconds).append(",").append(idleTimeoutInSeconds).append("]\n");
		}
		keepAliveTimeoutInSeconds = environment
				.getProperty("servicecomb.rest.client.http2.connection.keepAliveTimeoutInSeconds", int.class, 60);
		idleTimeoutInSeconds = environment.getProperty("servicecomb.rest.client.http2.connection.idleTimeoutInSeconds",
				int.class, 150);
		if (keepAliveTimeoutInSeconds >= idleTimeoutInSeconds) {
			result.append(
					"Configuration `servicecomb.rest.client.http2.connection.keepAliveTimeoutInSeconds` is longer than "
							+ "servicecomb.rest.client.http2.connection.idleTimeoutInSeconds.");
			result.append("[").append(keepAliveTimeoutInSeconds).append(",").append(idleTimeoutInSeconds).append("]\n");
		}
	}

	private void collectServiceDefinition(Environment environment, StringBuilder result) {
		if (environment.getProperty("APPLICATION_ID") != null) {
			result.append("Configurations `APPLICATION_ID` is deprecated, "
					+ "use `servicecomb.service.application` instead.\n");
		}
		Set<String> names = ConfigUtil.propertiesWithPrefix(environment, "service_description.");
		if (!names.isEmpty()) {
			result.append("Configurations with prefix `service_description` is deprecated, "
					+ "use `servicecomb.service` instead. Find keys ");
			result.append(Arrays.toString(names.toArray()));
			result.append("\n");
		}
	}

	private void collectCsePrefix(Environment environment, StringBuilder result) {
		Set<String> names = ConfigUtil.propertiesWithPrefix(environment, "cse.");
		if (!names.isEmpty()) {
			result.append("Configurations with prefix `cse` is deprecated, use `servicecomb` instead. Find keys ");
			result.append(Arrays.toString(names.toArray()));
			result.append("\n");
		}
	}

	@Override
	public String collect() {
		return null;
	}

	@Override
	public int getOrder() {
		return 1000;
	}
}
