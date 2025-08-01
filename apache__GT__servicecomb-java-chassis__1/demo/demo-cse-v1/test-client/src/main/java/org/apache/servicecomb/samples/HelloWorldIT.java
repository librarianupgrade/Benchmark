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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class HelloWorldIT implements CategorizedTestCase {
	RestOperations template = new RestTemplate();

	@Override
	public void testRestTransport() throws Exception {
		testHelloWorldFallback();
		testHelloWorldNoHeader();
		testHelloWorld();
		testHelloWorldCanary();
		testHelloWorldEmptyProtectionCloseWeight100();
		testHelloWorldeEmptyProtectionCloseWeightLess100();
		testHelloWorldEmptyProtectionCloseFallback();
		testHelloWorldEmptyProtectionCloseWeight100Two();
	}

	private void testHelloWorld() {
		for (int i = 0; i < 10; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "old");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			String result = template
					.exchange(Config.GATEWAY_URL + "/sayHello?name=World", HttpMethod.GET, entity, String.class)
					.getBody();
			TestMgr.check("Hello World", result);
		}
	}

	private void testHelloWorldCanary() {
		int oldCount = 0;
		int newCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "new");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			String result = template
					.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class)
					.getBody();
			if (result.equals("Hello Canary World")) {
				oldCount++;
			} else if (result.equals("Hello Canary in canary World")) {
				newCount++;
			} else {
				TestMgr.fail("not expected result testHelloWorldCanary");
				return;
			}
		}

		double ratio = oldCount / (float) (oldCount + newCount);
		TestMgr.check(ratio > 0.1 && ratio < 0.3, true);
	}

	private void testHelloWorldFallback() {
		int oldCount = 0;
		int newCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "fallback");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			String result = template
					.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class)
					.getBody();
			if (result.equals("\"Hello Canary World\"")) {
				oldCount++;
			} else if (result.equals("\"Hello Canary in canary World\"")) {
				newCount++;
			} else {
				TestMgr.fail("not expected result testHelloWorldCanary");
				return;
			}
		}

		double ratio = oldCount / (float) (oldCount + newCount);
		TestMgr.check(Math.abs(ratio - 0.2) <= 0.1, true);
	}

	private void testHelloWorldNoHeader() {
		int oldCount = 0;
		int newCount = 0;

		for (int i = 0; i < 20; i++) {
			String result = template.getForObject(Config.GATEWAY_URL + "/sayHelloCanary?name=World", String.class);
			if (result.equals("\"Hello Canary World\"")) {
				oldCount++;
			} else if (result.equals("\"Hello Canary in canary World\"")) {
				newCount++;
			} else {
				TestMgr.fail("not expected result testHelloWorldCanary");
				return;
			}
		}

		double ratio = oldCount / (float) (oldCount + newCount);
		TestMgr.check(Double.compare(ratio, 0.5) == 0, true);
	}

	private void testHelloWorldEmptyProtectionCloseWeight100() {
		int failCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "emptyProtectionClose100");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			try {
				template.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity,
						String.class);
			} catch (Exception e) {
				failCount++;
			}
		}

		TestMgr.check(failCount == 20, true);
	}

	private void testHelloWorldeEmptyProtectionCloseWeightLess100() {
		int failCount = 0;
		int succCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "emptyProtectionCloseLess100");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			try {
				template.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity,
						String.class);
				succCount++;
			} catch (Exception e) {
				failCount++;
			}
		}

		TestMgr.check(succCount == 20, true);
	}

	private void testHelloWorldEmptyProtectionCloseFallback() {
		int failCount = 0;
		int succCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "emptyProtectionCloseFallback");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			try {
				template.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity,
						String.class);
				succCount++;
			} catch (Exception e) {
				failCount++;
			}
		}

		TestMgr.check(succCount == 20, true);
	}

	private void testHelloWorldEmptyProtectionCloseWeight100Two() {
		int failCount = 0;
		int succCount = 0;

		for (int i = 0; i < 20; i++) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("canary", "emptyProtectionClose100-2");
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			try {
				template.exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity,
						String.class);
				succCount++;
			} catch (Exception e) {
				failCount++;
			}
		}

		TestMgr.check(failCount == succCount, true);
	}
}
