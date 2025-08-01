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
@SpringBootTest(classes = NacosConfigurationXmlJsonTest.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=xmlApp", "spring.profiles.active=dev",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848", "spring.cloud.nacos.config.namespace=test-namespace",
		"spring.cloud.nacos.config.encode=utf-8", "spring.cloud.nacos.config.timeout=1000",
		"spring.cloud.nacos.config.group=test-group", "spring.cloud.nacos.config.name=test-name",
		"spring.cloud.nacos.config.cluster-name=test-cluster", "spring.cloud.nacos.config.file-extension=xml",
		"spring.cloud.nacos.config.contextPath=test-contextpath",
		"spring.cloud.nacos.config.ext-config[0].data-id=ext-json-test.json",
		"spring.cloud.nacos.config.ext-config[1].data-id=ext-common02.properties",
		"spring.cloud.nacos.config.ext-config[1].group=GLOBAL_GROUP",
		"spring.cloud.nacos.config.shared-dataids=shared-data1.properties,shared-data.json",
		"spring.cloud.nacos.config.accessKey=test-accessKey", "spring.cloud.nacos.config.secretKey=test-secretKey",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosConfigurationXmlJsonTest {

	static {

		try {
			NacosConfigService mockedNacosConfigService = Mockito.mock(NacosConfigService.class);
			when(mockedNacosConfigService.getConfig(any(), any(), anyLong())).thenAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocationOnMock) throws Throwable {
					String dataId = invocationOnMock.getArgument(0, String.class);
					String group = invocationOnMock.getArgument(1, String.class);
					if ("xmlApp.xml".equals(dataId) && "test-group".equals(group)) {
						return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<top>\n" + "    <first>one</first>\n"
								+ "    <sencond value=\"two\">\n" + "        <third>three</third>\n"
								+ "    </sencond>\n" + "</top>";
					}
					if ("test-name.xml".equals(dataId) && "test-group".equals(group)) {
						return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
								+ "<Server port=\"8005\" shutdown=\"SHUTDOWN\"> \n"
								+ "    <Service name=\"Catalina\"> \n" + "        <Connector value=\"第二个连接器\"> \n"
								+ "            <open>开启服务</open> \n" + "            <init>初始化一下</init> \n"
								+ "            <process>\n" + "                <top>\n"
								+ "                    <first>one</first>\n"
								+ "                    <sencond value=\"two\">\n"
								+ "                        <third>three</third>\n" + "                    </sencond>\n"
								+ "                </top>\n" + "            </process> \n"
								+ "            <destory>销毁一下</destory> \n" + "            <close>关闭服务</close> \n"
								+ "        </Connector> \n" + "    </Service> \n" + "</Server> ";
					}

					if ("test-name-dev.xml".equals(dataId) && "test-group".equals(group)) {
						return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
								+ "<application android:label=\"@string/app_name\" android:icon=\"@drawable/osg\">\n"
								+ "    <activity android:name=\".osgViewer\"\n"
								+ "              android:label=\"@string/app_name\" android:screenOrientation=\"landscape\">\n"
								+ "        <intent-filter>\n"
								+ "            <action android:name=\"android.intent.action.MAIN\" />\n"
								+ "            <category android:name=\"android.intent.category.LAUNCHER\" />\n"
								+ "        </intent-filter>\n" + "    </activity>\n" + "</application>";
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

					if ("shared-data.json".equals(dataId) && "DEFAULT_GROUP".equals(group)) {
						return "{\n" + "    \"test\" : {\n" + "        \"name\" : \"test\",\n"
								+ "        \"list\" : [\n" + "            {\n"
								+ "                \"name\" :\"listname1\",\n" + "                \"age\":1\n"
								+ "            },\n" + "            {\n" + "                \"name\" :\"listname2\",\n"
								+ "                \"age\":2\n" + "            }\n" + "        ],\n"
								+ "        \"metadata\" : {\n" + "            \"intKey\" : 123,\n"
								+ "            \"booleanKey\" : true\n" + "        }\n" + "    }\n" + "}";
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

		checkJsonParser();
	}

	private void checkJsonParser() {
		assertThat(environment.getProperty("test.name", String.class)).isEqualTo("test");

		assertThat(environment.getProperty("test.list[0].name", String.class)).isEqualTo("listname1");
		assertThat(environment.getProperty("test.list[0].age", Integer.class)).isEqualTo(1);

		assertThat(environment.getProperty("test.list[1].name", String.class)).isEqualTo("listname2");
		assertThat(environment.getProperty("test.list[1].age", Integer.class)).isEqualTo(2);

		assertThat((Integer) environment.getProperty("test.metadata.intKey", Object.class)).isEqualTo(123);
		assertThat((Boolean) environment.getProperty("test.metadata.booleanKey", Object.class)).isEqualTo(true);
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
		assertThat(properties.getName()).isEqualTo("test-name");
	}

	private void checkoutNacosConfigGroup() {
		assertThat(properties.getGroup()).isEqualTo("test-group");
	}

	private void checkoutNacosConfigFileExtension() {
		assertThat(properties.getFileExtension()).isEqualTo("xml");
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
