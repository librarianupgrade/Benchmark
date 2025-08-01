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

package org.apache.servicecomb.serviceregistry.registry;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;

import com.google.common.eventbus.EventBus;

public class EmptyMockServiceRegistry implements ServiceRegistry {
	@Override
	public String getName() {
		return null;
	}

	@Override
	public void init() {

	}

	@Override
	public void run() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public EventBus getEventBus() {
		return null;
	}

	@Override
	public String getAppId() {
		return null;
	}

	@Override
	public Microservice getMicroservice() {
		return null;
	}

	@Override
	public List<Microservice> getAllMicroservices() {
		return null;
	}

	@Override
	public MicroserviceInstance getMicroserviceInstance() {
		return null;
	}

	@Override
	public ServiceRegistryClient getServiceRegistryClient() {
		return null;
	}

	@Override
	public List<MicroserviceInstance> findServiceInstance(String appId, String microserviceName,
			String microserviceVersionRule) {
		return null;
	}

	@Override
	public MicroserviceInstances findServiceInstances(String appId, String microserviceName,
			String microserviceVersionRule) {
		return null;
	}

	@Override
	public MicroserviceCache findMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
		return null;
	}

	@Override
	public boolean updateMicroserviceProperties(Map<String, String> properties) {
		return false;
	}

	@Override
	public boolean updateInstanceProperties(Map<String, String> instanceProperties) {
		return false;
	}

	@Override
	public Microservice getRemoteMicroservice(String microserviceId) {
		return null;
	}

	@Override
	public Microservice getAggregatedRemoteMicroservice(String microserviceId) {
		return null;
	}
}
