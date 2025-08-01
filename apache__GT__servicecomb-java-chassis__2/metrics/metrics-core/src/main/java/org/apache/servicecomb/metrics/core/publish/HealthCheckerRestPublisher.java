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

package org.apache.servicecomb.metrics.core.publish;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.servicecomb.foundation.metrics.health.HealthCheckResult;
import org.apache.servicecomb.foundation.metrics.health.HealthCheckerManager;

@Path("/health")
public class HealthCheckerRestPublisher {
	@Path("/")
	@GET
	public boolean checkHealth() {
		Map<String, HealthCheckResult> results = HealthCheckerManager.getInstance().check();
		for (HealthCheckResult result : results.values()) {
			if (!result.isHealthy()) {
				return false;
			}
		}
		return true;
	}

	@Path("/details")
	@GET
	public Map<String, HealthCheckResult> checkHealthDetails() {
		return HealthCheckerManager.getInstance().check();
	}
}
