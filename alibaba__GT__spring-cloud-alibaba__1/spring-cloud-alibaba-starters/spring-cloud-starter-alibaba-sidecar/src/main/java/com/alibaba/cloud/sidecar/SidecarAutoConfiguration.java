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

package com.alibaba.cloud.sidecar;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.client.RestTemplate;

/**
 * @author www.itmuch.com
 */
@Configuration(proxyBeanMethods = false)
public class SidecarAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@ConditionalOnEnabledHealthIndicator("sidecar")
	public SidecarHealthIndicator sidecarHealthIndicator(SidecarProperties sidecarProperties,
			RestTemplate restTemplate) {
		return new SidecarHealthIndicator(sidecarProperties, restTemplate);
	}

	@Bean
	public SidecarHealthChecker sidecarHealthChecker(SidecarDiscoveryClient sidecarDiscoveryClient,
			SidecarHealthIndicator sidecarHealthIndicator, SidecarProperties sidecarProperties,
			ConfigurableEnvironment environment) {
		SidecarHealthChecker cleaner = new SidecarHealthChecker(sidecarDiscoveryClient, sidecarHealthIndicator,
				sidecarProperties, environment);
		cleaner.check();
		return cleaner;
	}

}
