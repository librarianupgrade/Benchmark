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

package com.alibaba.cloud.nacos.endpoint;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author xiaojing
 */
@ConditionalOnWebApplication
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", matchIfMissing = true)
public class NacosConfigEndpointAutoConfiguration {

	@Autowired
	private NacosConfigManager nacosConfigManager;

	@Autowired
	private NacosRefreshHistory nacosRefreshHistory;

	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	@Bean
	public NacosConfigEndpoint nacosConfigEndpoint() {
		return new NacosConfigEndpoint(nacosConfigManager.getNacosConfigProperties(), nacosRefreshHistory);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledHealthIndicator("nacos-config")
	public NacosConfigHealthIndicator nacosConfigHealthIndicator() {
		return new NacosConfigHealthIndicator(nacosConfigManager.getConfigService());
	}

}
