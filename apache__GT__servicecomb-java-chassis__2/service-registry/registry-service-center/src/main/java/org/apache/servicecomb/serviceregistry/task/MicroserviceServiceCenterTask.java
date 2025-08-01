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
package org.apache.servicecomb.serviceregistry.task;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

import com.google.common.eventbus.EventBus;

public class MicroserviceServiceCenterTask extends CompositeTask {
	public MicroserviceServiceCenterTask(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
			ServiceRegistryClient srClient, Microservice microservice) {
		addTask(new MicroserviceRegisterTask(eventBus, srClient, microservice));
		addTask(new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, srClient, microservice));
		addTask(new MicroserviceWatchTask(eventBus, serviceRegistryConfig, srClient, microservice));
		addTask(new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice));
		addTask(new MicroserviceInstanceStatusSyncTask(eventBus, srClient, microservice));
	}
}
