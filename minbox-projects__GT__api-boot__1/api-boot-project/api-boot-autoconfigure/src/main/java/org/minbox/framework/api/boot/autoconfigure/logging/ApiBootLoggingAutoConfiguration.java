/*
 * Copyright [2019] [恒宇少年 - 于起宇]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package org.minbox.framework.api.boot.autoconfigure.logging;

import org.minbox.framework.logging.client.LoggingFactoryBean;
import org.minbox.framework.logging.client.admin.discovery.LoggingAdminDiscovery;
import org.minbox.framework.logging.client.admin.report.LoggingReportScheduled;
import org.minbox.framework.logging.spring.context.annotation.client.EnableLoggingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.minbox.framework.api.boot.autoconfigure.logging.ApiBootLoggingProperties.API_BOOT_LOGGING_PREFIX;

/**
 * ApiBoot Logging Auto Configuration
 *
 * @author 恒宇少年
 */
@Configuration
@ConditionalOnClass({ LoggingFactoryBean.class, EnableLoggingClient.class })
@EnableConfigurationProperties(ApiBootLoggingProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication
@EnableAsync
@Import({ ApiBootLoggingAdminDiscoveryAutoConfiguration.class, ApiBootLoggingAdminAppointAutoConfiguration.class,
		ApiBootLoggingOpenfeignAutoConfiguration.class, ApiBootLoggingRestTemplateAutoConfiguration.class,
		ApiBootLoggingWebAutoConfiguration.class, ApiBootLoggingGlobalLogStorageAutoConfiguration.class })
@EnableLoggingClient
public class ApiBootLoggingAutoConfiguration {
	/**
	 * logger instance
	 */
	static Logger logger = LoggerFactory.getLogger(ApiBootLoggingAutoConfiguration.class);
	/**
	 * ApiBoot Logging Properties
	 */
	private ApiBootLoggingProperties apiBootLoggingProperties;

	public ApiBootLoggingAutoConfiguration(ApiBootLoggingProperties apiBootLoggingProperties) {
		this.apiBootLoggingProperties = apiBootLoggingProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public LoggingFactoryBeanCustomizers loggingFactoryBeanCustomizers(
			ObjectProvider<LoggingFactoryBeanCustomizer> customizers) {
		return new LoggingFactoryBeanCustomizers(customizers.orderedStream().collect(Collectors.toList()));
	}

	/**
	 * logging factory bean
	 * {@link LoggingFactoryBean}
	 *
	 * @param loggingAdminDiscoveryObjectProvider Logging Admin Discovery Instance Provider
	 * @param customizers                         LoggingFactory Bean Customizers
	 * @return LoggingFactoryBean
	 */
	@Bean
	@ConditionalOnMissingBean
	public LoggingFactoryBean loggingFactoryBean(
			ObjectProvider<LoggingAdminDiscovery> loggingAdminDiscoveryObjectProvider,
			LoggingFactoryBeanCustomizers customizers) {
		LoggingFactoryBean factoryBean = new LoggingFactoryBean();
		factoryBean.setIgnorePaths(apiBootLoggingProperties.getIgnorePaths());
		factoryBean.setIgnoreHttpStatus(apiBootLoggingProperties.getIgnoreHttpStatus());
		factoryBean.setReportAway(apiBootLoggingProperties.getReportAway());
		factoryBean.setNumberOfRequestLog(apiBootLoggingProperties.getReportNumberOfRequestLog());
		factoryBean.setReportInitialDelaySecond(apiBootLoggingProperties.getReportInitialDelaySecond());
		factoryBean.setReportIntervalSecond(apiBootLoggingProperties.getReportIntervalSecond());
		factoryBean.setLoggingAdminDiscovery(loggingAdminDiscoveryObjectProvider.getIfAvailable());
		factoryBean.setShowConsoleLog(apiBootLoggingProperties.isShowConsoleLog());
		factoryBean.setFormatConsoleLog(apiBootLoggingProperties.isFormatConsoleLogJson());

		logger.info("【LoggingFactoryBean】init successfully.");
		return customizers.customize(factoryBean);
	}

	/**
	 * Logging Report Scheduled Task Job
	 * When the configuration parameter "api.boot.logging.report-away=timing" is configured,
	 * the creation timing task is performed to report log information to admin node
	 * {@link ApiBootLoggingProperties}
	 * {@link LoggingReportScheduled}
	 *
	 * @param factoryBean logging factory bean
	 * @return LoggingReportScheduled
	 */
	@Bean
	@ConditionalOnProperty(prefix = API_BOOT_LOGGING_PREFIX, name = "report-away", havingValue = "timing")
	@ConditionalOnMissingBean
	public LoggingReportScheduled loggingReportScheduled(LoggingFactoryBean factoryBean) {
		return new LoggingReportScheduled(factoryBean);
	}
}
