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

package com.alibaba.cloud.nacos;

import java.util.Map;

import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpoint;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.nacos.client.config.NacosConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 *
 * @author zkz
 * @author freeman
 */
@SpringBootTest(classes = NacosConfigurationNoSuffixTest.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=app-no-suffix", "spring.profiles.active=dev",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848", "spring.cloud.nacos.config.namespace=test-namespace",
		"spring.cloud.nacos.config.encode=utf-8", "spring.cloud.nacos.config.timeout=1000",
		"spring.cloud.nacos.config.group=test-group", "spring.cloud.nacos.config.name=test-no-suffix-name",
		"spring.cloud.nacos.config.cluster-name=test-cluster", "spring.cloud.nacos.config.contextPath=test-contextpath",
		"spring.cloud.nacos.config.ext-config[0].data-id=ext-json-test.json",
		"spring.cloud.nacos.config.ext-config[1].data-id=ext-common02.properties",
		"spring.cloud.nacos.config.ext-config[1].group=GLOBAL_GROUP",
		"spring.cloud.nacos.config.shared-dataids=shared-data1.properties,shared-data2.xml",
		"spring.cloud.nacos.config.accessKey=test-accessKey", "spring.cloud.nacos.config.secretKey=test-secretKey",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosConfigurationNoSuffixTest {

	static {

		try {
			NacosConfigService mockedNacosConfigService = Mockito.mock(NacosConfigService.class);
			when(mockedNacosConfigService.getConfig(any(), any(), anyLong())).thenAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocationOnMock) throws Throwable {
					String dataId = invocationOnMock.getArgument(0, String.class);
					String group = invocationOnMock.getArgument(1, String.class);
					if ("app-no-suffix".equals(dataId) && "test-group".equals(group)) {
						return "test-no-suffix=value-no-suffix-1";
					}
					if ("app-no-suffix.properties".equals(dataId) && "test-group".equals(group)) {
						return "test-no-suffix=value-no-suffix-2";
					}

					if ("test-no-suffix-name".equals(dataId) && "test-group".equals(group)) {
						return "test-no-suffix-assign=assign-value-no-suffix-111";
					}
					if ("test-no-suffix-name.properties".equals(dataId) && "test-group".equals(group)) {
						return "test-no-suffix-assign=assign-value-no-suffix-222";
					}
					if ("test-no-suffix-name-dev.properties".equals(dataId) && "test-group".equals(group)) {
						return "test-no-suffix-assign=assign-dev-value-no-suffix-333";
					}

					if ("ext-json-test.json".equals(dataId) && "DEFAULT_GROUP".equals(group)) {
						return "{\n" + "    \"people\":{\n" + "        \"firstName\":\"Brett\",\n"
								+ "        \"lastName\":\"McLaughlin\"\n" + "    }\n" + "}";
					}

					if ("ext-config-common02.properties".equals(dataId) && "GLOBAL_GROUP".equals(group)) {
						return "global-ext-config=global-config-value-2";
					}

					if ("shared-data1.properties".equals(dataId) && "DEFAULT_GROUP".equals(group)) {
						return "shared-name=shared-value-1";
					}

					if ("shared-data2.xml".equals(dataId) && "DEFAULT_GROUP".equals(group)) {
						return "<Server port=\"8005\" shutdown=\"SHUTDOWN\"> \n" + "    <Service name=\"Catalina\"> \n"
								+ "        <Connector value=\"第二个连接器\"> \n" + "            <open>开启服务</open> \n"
								+ "            <init>初始化一下</init> \n" + "            <process>\n"
								+ "                <top>\n" + "                    <first>one</first>\n"
								+ "                    <sencond value=\"two\">\n"
								+ "                        <third>three</third>\n" + "                    </sencond>\n"
								+ "                </top>\n" + "            </process> \n"
								+ "            <destory>销毁一下</destory> \n" + "            <close>关闭服务</close> \n"
								+ "        </Connector> \n" + "    </Service> \n" + "</Server> ";
					}

					return "";
				}

			});

			ReflectionTestUtils.setField(NacosConfigManager.class, "service", mockedNacosConfigService);

		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	@Autowired
	private NacosPropertySourceLocator locator;

	@Autowired
	private NacosConfigProperties properties;

	@Autowired
	private NacosRefreshHistory refreshHistory;

	@Autowired
	private Environment environment;

	@Test
	public void contextLoads() throws Exception {

		assertThat(locator).isNotNull();
		assertThat(properties).isNotNull();

		checkoutNacosConfigServerAddr();
		checkoutNacosConfigNamespace();
		checkoutNacosConfigClusterName();
		checkoutNacosConfigAccessKey();
		checkoutNacosConfigSecrectKey();
		checkoutNacosConfigName();
		checkoutNacosConfigGroup();
		checkoutNacosConfigContextPath();
		checkoutNacosConfigFileExtension();
		checkoutNacosConfigTimeout();
		checkoutNacosConfigEncode();

		checkoutEndpoint();
		checkEnvironmentProperties();
	}

	private void checkEnvironmentProperties() {
		assertThat(environment.getProperty("test-no-suffix")).isNull();
		assertThat(environment.getProperty("test-no-suffix-assign")).isEqualTo("assign-dev-value-no-suffix-333");
	}

	private void checkoutNacosConfigServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	private void checkoutNacosConfigNamespace() {
		assertThat(properties.getNamespace()).isEqualTo("test-namespace");
	}

	private void checkoutNacosConfigClusterName() {
		assertThat(properties.getClusterName()).isEqualTo("test-cluster");
	}

	private void checkoutNacosConfigAccessKey() {
		assertThat(properties.getAccessKey()).isEqualTo("test-accessKey");
	}

	private void checkoutNacosConfigSecrectKey() {
		assertThat(properties.getSecretKey()).isEqualTo("test-secretKey");
	}

	private void checkoutNacosConfigContextPath() {
		assertThat(properties.getContextPath()).isEqualTo("test-contextpath");
	}

	private void checkoutNacosConfigName() {
		assertThat(properties.getName()).isEqualTo("test-no-suffix-name");
	}

	private void checkoutNacosConfigGroup() {
		assertThat(properties.getGroup()).isEqualTo("test-group");
	}

	private void checkoutNacosConfigFileExtension() {
		assertThat(properties.getFileExtension()).isEqualTo("properties");
	}

	private void checkoutNacosConfigTimeout() {
		assertThat(properties.getTimeout()).isEqualTo(1000);
	}

	private void checkoutNacosConfigEncode() {
		assertThat(properties.getEncode()).isEqualTo("utf-8");
	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint nacosConfigEndpoint = new NacosConfigEndpoint(properties, refreshHistory);
		Map<String, Object> map = nacosConfigEndpoint.invoke();
		assertThat(properties).isEqualTo(map.get("NacosConfigProperties"));
		assertThat(refreshHistory.getRecords()).isEqualTo(map.get("RefreshHistory"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class, NacosConfigAutoConfiguration.class,
			NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
