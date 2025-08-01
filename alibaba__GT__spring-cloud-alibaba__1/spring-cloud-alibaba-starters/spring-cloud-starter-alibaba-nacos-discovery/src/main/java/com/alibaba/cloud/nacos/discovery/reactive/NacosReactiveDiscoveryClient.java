/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.discovery.reactive;

import java.util.function.Function;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.discovery.ServiceCache;
import com.alibaba.nacos.api.exception.NacosException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

/**
 * @author <a href="mailto:echooy.mxq@gmail.com">echooymxq</a>
 * @author freeman
 **/
public class NacosReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	private static final Logger log = LoggerFactory.getLogger(NacosReactiveDiscoveryClient.class);

	private NacosServiceDiscovery serviceDiscovery;

	@Value("${spring.cloud.nacos.discovery.failure-tolerance-enabled:false}")
	private boolean failureToleranceEnabled;

	public NacosReactiveDiscoveryClient(NacosServiceDiscovery nacosServiceDiscovery) {
		this.serviceDiscovery = nacosServiceDiscovery;
	}

	@Override
	public String description() {
		return "Spring Cloud Nacos Reactive Discovery Client";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {

		return Mono.justOrEmpty(serviceId).flatMapMany(loadInstancesFromNacos())
				.subscribeOn(Schedulers.boundedElastic());
	}

	private Function<String, Publisher<ServiceInstance>> loadInstancesFromNacos() {
		return serviceId -> {
			try {
				return Mono.justOrEmpty(serviceDiscovery.getInstances(serviceId)).flatMapMany(instances -> {
					ServiceCache.setInstances(serviceId, instances);
					return Flux.fromIterable(instances);
				});
			} catch (NacosException e) {
				log.error("get service instance[{}] from nacos error!", serviceId, e);
				return failureToleranceEnabled ? Flux.fromIterable(ServiceCache.getInstances(serviceId)) : Flux.empty();
			}
		};
	}

	@Override
	public Flux<String> getServices() {
		return Flux.defer(() -> {
			try {
				return Mono.justOrEmpty(serviceDiscovery.getServices()).flatMapMany(services -> {
					ServiceCache.setServiceIds(services);
					return Flux.fromIterable(services);
				});
			} catch (Exception e) {
				log.error("get services from nacos server fail,", e);
				return failureToleranceEnabled ? Flux.fromIterable(ServiceCache.getServiceIds()) : Flux.empty();
			}
		}).subscribeOn(Schedulers.boundedElastic());
	}

}
