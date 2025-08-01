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

package org.apache.servicecomb.demo.crossapp;

import java.util.Collections;
import java.util.TreeSet;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CrossappClient {
	@RpcReference(microserviceName = "appServer:appService", schemaId = "helloworld")
	private static HelloWorld helloWorld;

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(CrossappClient.class).web(WebApplicationType.NONE).run(args);

		run();
	}

	public static void run() {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		String result = InvokerUtils.syncInvoke("appServer:appService", "helloworld", "sayHello", null, String.class);
		TestMgr.check("hello world", result);

		RestOperations restTemplate = RestTemplateBuilder.create();
		result = restTemplate.getForObject("cse://appServer:appService/helloworld/hello", String.class);
		TestMgr.check("hello world", result);
		result = restTemplate.getForObject("servicecomb://appServer:appService/helloworld/hello", String.class);
		TestMgr.check("hello world", result);

		result = helloWorld.sayHello();
		TestMgr.check("hello world", result);

		testCorsHandlerOptions();
		testCorsHandlerGet();

		TestMgr.summary();
		System.setProperty("sun.net.http.allowRestrictedHeaders", "false");
	}

	private static void testCorsHandlerOptions() {
		// first domain
		RestOperations springRestTemplate = new RestTemplate();
		MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
		requestHeaders.put("Origin", Collections.singletonList("http://test.domain:8080"));
		requestHeaders.put("Access-Control-Request-Method", Collections.singletonList("PUT"));
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestHeaders);
		ResponseEntity<String> responseEntity = springRestTemplate.exchange("http://127.0.0.1:8080/helloworld/hello",
				HttpMethod.OPTIONS, requestEntity, String.class);
		TestMgr.check("204", responseEntity.getStatusCode().value());
		TreeSet<String> sortedSet = new TreeSet<>(responseEntity.getHeaders().get("Access-Control-Allow-Methods"));
		TestMgr.check("[DELETE,POST,GET,PUT]", sortedSet);
		sortedSet = new TreeSet<>(responseEntity.getHeaders().get("Access-Control-Allow-Headers"));
		TestMgr.check("[abc,def]", sortedSet);
		TestMgr.check("http://test.domain:8080", responseEntity.getHeaders().getFirst("Access-Control-Allow-Origin"));

		// second domain
		requestHeaders = new LinkedMultiValueMap<>();
		requestHeaders.put("Origin", Collections.singletonList("http://test.domain:9090"));
		requestHeaders.put("Access-Control-Request-Method", Collections.singletonList("PUT"));
		requestEntity = new HttpEntity<>(requestHeaders);
		responseEntity = springRestTemplate.exchange("http://127.0.0.1:8080/helloworld/hello", HttpMethod.OPTIONS,
				requestEntity, String.class);
		TestMgr.check("204", responseEntity.getStatusCode().value());
		sortedSet = new TreeSet<>(responseEntity.getHeaders().get("Access-Control-Allow-Methods"));
		TestMgr.check("[DELETE,POST,GET,PUT]", sortedSet);
		sortedSet = new TreeSet<>(responseEntity.getHeaders().get("Access-Control-Allow-Headers"));
		TestMgr.check("[abc,def]", sortedSet);
		TestMgr.check("http://test.domain:9090", responseEntity.getHeaders().getFirst("Access-Control-Allow-Origin"));
	}

	private static void testCorsHandlerGet() {
		// allowed origin
		RestOperations springRestTemplate = new RestTemplate();
		MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
		requestHeaders.put("Origin", Collections.singletonList("http://test.domain:8080"));
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestHeaders);
		ResponseEntity<String> responseEntity = springRestTemplate.exchange("http://127.0.0.1:8080/helloworld/hello",
				HttpMethod.GET, requestEntity, String.class);

		TestMgr.check("200", responseEntity.getStatusCode().value());
		TestMgr.check("hello world", responseEntity.getBody());

		// allowed origin
		requestHeaders = new LinkedMultiValueMap<>();
		requestHeaders.put("Origin", Collections.singletonList("http://test.domain:9090"));
		requestEntity = new HttpEntity<>(requestHeaders);
		responseEntity = springRestTemplate.exchange("http://127.0.0.1:8080/helloworld/hello", HttpMethod.GET,
				requestEntity, String.class);

		TestMgr.check("200", responseEntity.getStatusCode().value());
		TestMgr.check("hello world", responseEntity.getBody());

		// not allowed origin
		try {
			requestHeaders = new LinkedMultiValueMap<>();
			requestHeaders.put("Origin", Collections.singletonList("http://test.domain:7070"));
			requestEntity = new HttpEntity<>(requestHeaders);
			springRestTemplate.exchange("http://127.0.0.1:8080/helloworld/hello", HttpMethod.GET, requestEntity,
					String.class);
			TestMgr.fail("must throw");
		} catch (HttpServerErrorException e) {
			TestMgr.check(500, e.getStatusCode().value());
			TestMgr.check(true, e.getMessage().contains("500 CORS Rejected"));
		}
	}
}
