/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.test.naming;

import com.alibaba.nacos.Nacos;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.test.base.Params;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.test.naming.NamingBase.TEST_GROUP_1;
import static com.alibaba.nacos.test.naming.NamingBase.TEST_GROUP_2;
import static com.alibaba.nacos.test.naming.NamingBase.randomDomainName;

/**
 * @author nkorange
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Nacos.class, properties = {
		"server.servlet.context-path=/nacos" }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MultiTenant_InstanceAPI_ITCase {

	private NamingService naming;

	private NamingService naming1;

	private NamingService naming2;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private URL base;

	private final List<Instance> instances = Collections.emptyList();

	@Before
	public void init() throws Exception {

		NamingBase.prepareServer(port);

		String url = String.format("http://localhost:%d/", port);
		this.base = new URL(url);

		naming = NamingFactory.createNamingService("127.0.0.1" + ":" + port);

		while (true) {
			if (!"UP".equals(naming.getServerStatus())) {
				Thread.sleep(1000L);
				continue;
			}
			break;
		}

		Properties properties = new Properties();
		properties.put(PropertyKeyConst.NAMESPACE, "namespace-1");
		properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1" + ":" + port);
		naming1 = NamingFactory.createNamingService(properties);

		properties = new Properties();
		properties.put(PropertyKeyConst.NAMESPACE, "namespace-2");
		properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1" + ":" + port);
		naming2 = NamingFactory.createNamingService(properties);
	}

	/**
	 * @TCDescription : 多租户注册IP，listInstance接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_listInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888);

		TimeUnit.SECONDS.sleep(5L);

		String url = "/nacos/v1/ns/instance/list";
		ResponseEntity<String> response = request(url, Params.newParams().appendParam("serviceName", serviceName)
				.appendParam("namespaceId", "namespace-1").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());

		Assert.assertEquals("11.11.11.11", json.get("hosts").get(0).get("ip").asText());

		response = request(url, Params.newParams().appendParam("serviceName", serviceName).done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		json = JacksonUtils.toObj(response.getBody());

		Assert.assertEquals(1, json.get("hosts").size());
	}

	/**
	 * @TCDescription : 多租户, 多group下, 注册IP，listInstance接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_group_listInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);

		naming1.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, TEST_GROUP_1, "33.33.33.33", 8888);
		naming.registerInstance(serviceName, TEST_GROUP_2, "44.44.44.44", 8888);

		TimeUnit.SECONDS.sleep(5L);

		String url = "/nacos/v1/ns/instance/list";
		ResponseEntity<String> response = request(
				url, Params.newParams().appendParam("serviceName", serviceName)
						.appendParam("namespaceId", "namespace-1").appendParam("groupName", TEST_GROUP_1).done(),
				String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());

		Assert.assertEquals("11.11.11.11", json.get("hosts").get(0).get("ip").asText());

		response = request(url, Params.newParams().appendParam("serviceName", serviceName)
				.appendParam("groupName", TEST_GROUP_1).done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		json = JacksonUtils.toObj(response.getBody());

		Assert.assertEquals(1, json.get("hosts").size());
		Assert.assertEquals("33.33.33.33", json.get("hosts").get(0).get("ip").asText());
	}

	/**
	 * @TCDescription : 多租户注册IP，getInstance接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_getInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888, "c1");

		TimeUnit.SECONDS.sleep(5L);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("ip", "33.33.33.33") //错误的IP，隔离验证
						.appendParam("port", "8888").appendParam("namespaceId", "namespace-2").done(),
				String.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName)
				.appendParam("clusters", "c1").appendParam("healthyOnly", "true").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
		Assert.assertEquals("33.33.33.33", json.get("hosts").get(0).get("ip").asText());
	}

	/**
	 * @TCDescription : 多租户注册IP，getInstance接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_group_getInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888, "c1");
		naming.registerInstance(serviceName, "44.44.44.44", 8888, "c2");

		TimeUnit.SECONDS.sleep(5L);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("groupName", TEST_GROUP_1)
						.appendParam("ip", "33.33.33.33") //不存在的IP，隔离验证
						.appendParam("port", "8888").appendParam("namespaceId", "namespace-2").done(),
				String.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName)
				.appendParam("clusters", "c2").appendParam("healthyOnly", "true").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
		Assert.assertEquals("44.44.44.44", json.get("hosts").get(0).get("ip").asText());
	}

	/**
	 * @TCDescription : 多租户注册IP，putInstance接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_putInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888);
		naming.registerInstance(serviceName, "44.44.44.44", 8888);

		TimeUnit.SECONDS.sleep(5L);

		ResponseEntity<String> response = request(
				"/nacos/v1/ns/instance", Params.newParams().appendParam("serviceName", serviceName)
						.appendParam("ip", "33.33.33.33").appendParam("port", "8888").done(),
				String.class, HttpMethod.PUT);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-1").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());

		//namespace-2个数
		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-2").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		json = JacksonUtils.toObj(response.getBody());
		System.out.println(json);
		Assert.assertEquals(1, json.get("hosts").size());
	}

	/**
	 * @TCDescription : 多租户, 多group下，注册IP，putInstance接口, 更新实例
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_group_putInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);
		naming2.registerInstance(serviceName, TEST_GROUP_2, "22.22.22.22", 80);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("groupName", TEST_GROUP_2)
						.appendParam("ip", "22.22.22.22").appendParam("port", "80")
						.appendParam("namespaceId", "namespace-2").appendParam("weight", "8.0").done(),
				String.class, HttpMethod.PUT);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		TimeUnit.SECONDS.sleep(5L);

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-2").appendParam("groupName", TEST_GROUP_2).done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
		Assert.assertEquals("8.0", json.get("hosts").get(0).get("weight").asText());
	}

	/**
	 * @TCDescription : 多租户, 多group下，注册IP，patchInstance接口, 部分更新实例
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_group_patchInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);
		naming2.registerInstance(serviceName, TEST_GROUP_2, "22.22.22.22", 80);

		TimeUnit.SECONDS.sleep(3L);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("groupName", TEST_GROUP_2)
						.appendParam("ip", "22.22.22.22").appendParam("port", "80")
						.appendParam("namespaceId", "namespace-2").appendParam("weight", "8.0").done(),
				String.class, HttpMethod.PUT);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("groupName", TEST_GROUP_2)
						.appendParam("ip", "22.22.22.22").appendParam("port", "80")
						.appendParam("namespaceId", "namespace-2").done(),
				String.class, HttpMethod.PATCH);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		TimeUnit.SECONDS.sleep(3L);

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-2").appendParam("groupName", TEST_GROUP_2).done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
		Assert.assertEquals("8.0", json.get("hosts").get(0).get("weight").asText());
	}

	/**
	 * @TCDescription : 多租户注册IP，update一个没有的实例接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_updateInstance_notExsitInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888);
		naming.registerInstance(serviceName, "44.44.44.44", 8888);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("ip", "33.33.33.33")
						.appendParam("port", "8888").appendParam("namespaceId", "namespace-1") //新增
						.done(),
				String.class, HttpMethod.POST);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		TimeUnit.SECONDS.sleep(5L);
		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-1").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(2, json.get("hosts").size());

		//namespace-2个数
		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-2").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
	}

	/**
	 * @TCDescription : 多租户,多group下，注册IP，注册一个没有的实例接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_group_updateInstance_notExsitInstance() throws Exception {
		String serviceName = randomDomainName();

		naming1.registerInstance(serviceName, "11.11.11.11", 80);
		naming2.registerInstance(serviceName, "22.22.22.22", 80);
		TimeUnit.SECONDS.sleep(5L);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("ip", "33.33.33.33")
						.appendParam("port", "8888").appendParam("namespaceId", "namespace-1") //新增
						.appendParam("groupName", TEST_GROUP_1).done(),
				String.class, HttpMethod.POST);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-1").appendParam("groupName", TEST_GROUP_1).done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		String body = response.getBody();
		System.out.println("multipleTenant_group_updateInstance_notExsitInstance received body:  " + body);
		JsonNode json = JacksonUtils.toObj(body);
		Assert.assertEquals("33.33.33.33", json.get("hosts").get(0).get("ip").asText());
	}

	/**
	 * @TCDescription : 多租户注册IP，update一个已有的实例接口
	 * @TestStep :
	 * @ExpectResult :
	 */
	@Test
	public void multipleTenant_updateInstance() throws Exception {
		String serviceName = randomDomainName();

		naming2.registerInstance(serviceName, "22.22.22.22", 80);

		naming.registerInstance(serviceName, "33.33.33.33", 8888);
		naming.registerInstance(serviceName, "44.44.44.44", 8888);

		TimeUnit.SECONDS.sleep(5L);

		ResponseEntity<String> response = request("/nacos/v1/ns/instance",
				Params.newParams().appendParam("serviceName", serviceName).appendParam("ip", "11.11.11.11")
						.appendParam("port", "80").appendParam("namespaceId", "namespace-1") //新增
						.done(),
				String.class, HttpMethod.POST);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-1").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		JsonNode json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());

		//namespace-2个数
		response = request("/nacos/v1/ns/instance/list", Params.newParams().appendParam("serviceName", serviceName) //获取naming中的实例
				.appendParam("namespaceId", "namespace-2").done(), String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		json = JacksonUtils.toObj(response.getBody());
		Assert.assertEquals(1, json.get("hosts").size());
	}

	private void verifyInstanceListForNaming(NamingService naming, int size, String serviceName) throws Exception {
		int i = 0;
		while (i < 20) {
			List<Instance> instances = naming.getAllInstances(serviceName);
			if (instances.size() == size) {
				break;
			} else {
				TimeUnit.SECONDS.sleep(3);
				i++;
			}
		}
	}

	private <T> ResponseEntity<T> request(String path, MultiValueMap<String, String> params, Class<T> clazz) {
		return request(path, params, clazz, HttpMethod.GET);
	}

	private <T> ResponseEntity<T> request(String path, MultiValueMap<String, String> params, Class<T> clazz,
			HttpMethod httpMethod) {

		HttpHeaders headers = new HttpHeaders();

		HttpEntity<?> entity = new HttpEntity<T>(headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.base.toString() + path)
				.queryParams(params);

		return this.restTemplate.exchange(builder.toUriString(), httpMethod, entity, clazz);
	}
}
