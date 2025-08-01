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
package org.apache.servicecomb.config;

import org.apache.servicecomb.config.inject.InjectBeanPostProcessor;
import org.apache.servicecomb.config.priority.ConfigObjectFactory;
import org.apache.servicecomb.config.priority.PriorityPropertyFactory;
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@Configuration
@SuppressWarnings("unused")
public class FoundationConfigConfiguration {
	@Bean
	public static InjectBeanPostProcessor scbInjectBeanPostProcessor(
			@Autowired @Lazy PriorityPropertyManager priorityPropertyManager) {
		return new InjectBeanPostProcessor(priorityPropertyManager);
	}

	@Bean
	public PriorityPropertyManager scbPriorityPropertyManager(ConfigObjectFactory configObjectFactory) {
		return new PriorityPropertyManager(configObjectFactory);
	}

	@Bean
	public PriorityPropertyFactory scbPriorityPropertyFactory(Environment environment) {
		return new PriorityPropertyFactory(environment);
	}

	@Bean
	public DynamicPropertiesImpl scbDynamicProperties(Environment environment) {
		return new DynamicPropertiesImpl(environment);
	}

	@Bean
	public ConfigObjectFactory scbConfigObjectFactory(PriorityPropertyFactory propertyFactory) {
		return new ConfigObjectFactory(propertyFactory);
	}

	@Bean
	@ConfigurationProperties(prefix = DataCenterProperties.PREFIX)
	public DataCenterProperties scbDataCenterProperties() {
		return new DataCenterProperties();
	}

	@Bean
	public InMemoryDynamicPropertiesSource scbInMemoryDynamicPropertiesSource() {
		return new InMemoryDynamicPropertiesSource();
	}
}
