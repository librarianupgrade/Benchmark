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

package com.alibaba.cloud.circuitbreaker.sentinel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 * @author freeman
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
@ConditionalOnProperty(name = "spring.cloud.circuitbreaker.sentinel.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveSentinelCircuitBreakerAutoConfiguration {

	@Autowired(required = false)
	private List<Customizer<ReactiveSentinelCircuitBreakerFactory>> customizers = new ArrayList<>();

	@Bean
	@ConditionalOnMissingBean(ReactiveCircuitBreakerFactory.class)
	public ReactiveCircuitBreakerFactory reactiveSentinelCircuitBreakerFactory() {
		ReactiveSentinelCircuitBreakerFactory factory = new ReactiveSentinelCircuitBreakerFactory();
		customizers.forEach(customizer -> customizer.customize(factory));
		return factory;
	}

}
