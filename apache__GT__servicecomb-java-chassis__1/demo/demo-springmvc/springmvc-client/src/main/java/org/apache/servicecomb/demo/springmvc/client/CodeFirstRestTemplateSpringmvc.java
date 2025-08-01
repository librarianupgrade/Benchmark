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

package org.apache.servicecomb.demo.springmvc.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import jakarta.servlet.http.Part;

@Component
public class CodeFirstRestTemplateSpringmvc extends CodeFirstRestTemplate {
	interface UploadPartAndFile {
		String fileUpload(Part file1, File someFile);
	}

	interface UploadStreamAndResource {
		String fileUpload(InputStream file1, Resource someFile);
	}

	private UploadPartAndFile uploadPartAndFile = Invoker.createProxy("springmvc", "codeFirst",
			UploadPartAndFile.class);

	private UploadStreamAndResource uploadStreamAndResource = Invoker.createProxy("springmvc", "codeFirst",
			UploadStreamAndResource.class);

	private TestResponse testResponse = new TestResponse();

	private TestObject testObject = new TestObject();

	private TestGeneric testGeneric = new TestGeneric();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();

	private TestContentType testContentType = new TestContentType();

	@Autowired
	public void setEnvironment(Environment environment) {
		this.environment = environment;
		testResponse.setEnvironment(environment);
	}

	@Override
	protected void testOnlyRest(String microservcieName, RestOperations template, String cseUrlPrefix) {
		try {
			testUpload(template, cseUrlPrefix);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		testResponseEntity("springmvc", template, cseUrlPrefix);
		testCodeFirstTestFormRest(template, cseUrlPrefix);

		testResponse.runRest();
		testObject.runRest();
		testGeneric.runRest();
		testRestTemplate.runRest();
		testContentType.runAllTest();

		super.testOnlyRest(microservcieName, template, cseUrlPrefix);
	}

	@Override
	protected void testOnlyHighway(RestOperations template, String cseUrlPrefix) {
		testResponse.runHighway();
		testObject.runHighway();
		testGeneric.runHighway();
		testCodeFirstTestFormHighway(template, cseUrlPrefix);
		super.testOnlyHighway(template, cseUrlPrefix);
	}

	@Override
	protected void testAllTransport(String microserviceName, RestOperations template, String cseUrlPrefix) {
		testResponse.runAllTransport();
		testObject.runAllTransport();
		testGeneric.runAllTransport();
		testRestTemplate.runAllTest();
		testResponseEntity("springmvc", template, cseUrlPrefix);
		testCodeFirstTestForm(template, cseUrlPrefix);

		super.testAllTransport(microserviceName, template, cseUrlPrefix);
	}

	private void testUpload(RestOperations template, String cseUrlPrefix) throws IOException {
		String file1Content = "hello world";
		File file1 = File.createTempFile("测 试", ".txt");
		FileUtils.writeStringToFile(file1, file1Content, StandardCharsets.UTF_8, false);

		String file2Content = " bonjour";
		File someFile = File.createTempFile("upload2", ".txt");
		FileUtils.writeStringToFile(someFile, file2Content, StandardCharsets.UTF_8, false);

		String expect = String.format("%s:%s:%s\n" + "%s:%s:%s", file1.getName(), MediaType.TEXT_PLAIN_VALUE,
				file1Content, someFile.getName(), MediaType.TEXT_PLAIN_VALUE, file2Content);

		String result = testRestTemplateUpload(template, cseUrlPrefix, file1, someFile);
		TestMgr.check(expect, result);

		result = uploadPartAndFile.fileUpload(new FilePart(null, file1), someFile);
		TestMgr.check(expect, result);

		expect = "hello world";
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file1", new FileSystemResource(file1));
		result = template.postForObject(cseUrlPrefix + "/upload1", new HttpEntity<>(map), String.class);
		TestMgr.check(expect, result);

		expect = String.format("null:%s:%s\n" + "%s:%s:%s", MediaType.APPLICATION_OCTET_STREAM_VALUE, file1Content,
				someFile.getName(), MediaType.TEXT_PLAIN_VALUE, file2Content);
		result = uploadStreamAndResource.fileUpload(
				new ByteArrayInputStream(file1Content.getBytes(StandardCharsets.UTF_8)),
				new org.springframework.core.io.PathResource(someFile.getAbsolutePath()));
		TestMgr.check(expect, result);
	}

	private String testRestTemplateUpload(RestOperations template, String cseUrlPrefix, File file1, File someFile) {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file1", new FileSystemResource(file1));
		map.add("someFile", new FileSystemResource(someFile));

		return template.postForObject(cseUrlPrefix + "/upload", new HttpEntity<>(map), String.class);
	}

	private void testResponseEntity(String microserviceName, RestOperations template, String cseUrlPrefix) {
		Map<String, Object> body = new HashMap<>();
		Date date = new Date();
		body.put("date", date);

		CseHttpEntity<Map<String, Object>> httpEntity = new CseHttpEntity<>(body);
		httpEntity.addContext("contextKey", "contextValue");

		String srcName = BootStrapProperties.readServiceName(environment);

		ResponseEntity<Date> responseEntity = template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.POST,
				httpEntity, Date.class);
		TestMgr.check(date, responseEntity.getBody());
		TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
		TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));
		checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());

		responseEntity = template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.PATCH, httpEntity, Date.class);
		TestMgr.check(date, responseEntity.getBody());
		TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
		TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));
		checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());

		int retryResult = template.getForObject(cseUrlPrefix + "retrySuccess?a=2&b=3", Integer.class);
		TestMgr.check(retryResult, 5);
		retryResult = template.getForObject(cseUrlPrefix + "retrySuccess?a=2&b=3", Integer.class);
		TestMgr.check(retryResult, 5);
	}

	private void testCodeFirstTestForm(RestOperations template, String cseUrlPrefix) {
		HttpHeaders formHeaders = new HttpHeaders();
		formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> map = new HashMap<>();
		String code = "servicecomb%2bwelcome%40%23%24%25%5e%26*()%3d%3d";
		map.put("form1", code);
		HttpEntity<Map<String, String>> formEntry = new HttpEntity<>(map, formHeaders);
		TestMgr.check(code + "null",
				template.postForEntity(cseUrlPrefix + "/testform", formEntry, String.class).getBody());
		map.put("form2", "hello");
		TestMgr.check(code + "hello",
				template.postForEntity(cseUrlPrefix + "/testform", formEntry, String.class).getBody());
	}

	private void testCodeFirstTestFormHighway(RestOperations template, String cseUrlPrefix) {
		HttpHeaders formHeaders = new HttpHeaders();
		formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> map = new HashMap<>();
		String code = "servicecomb%2bwelcome%40%23%24%25%5e%26*()%3d%3d";
		map.put("form1", code);
		map.put("form2", "");
		HttpEntity<Map<String, String>> formEntry = new HttpEntity<>(map, formHeaders);
		TestMgr.check(code + "", template.postForEntity(cseUrlPrefix + "/testform", formEntry, String.class).getBody());

		map = new HashMap<>();
		code = "servicecomb%2bwelcome%40%23%24%25%5e%26*()%3d%3d";
		map.put("form1", code);
		map.put("form2", null);
		formEntry = new HttpEntity<>(map, formHeaders);
		TestMgr.check(code + "null",
				template.postForEntity(cseUrlPrefix + "/testform", formEntry, String.class).getBody());
	}

	private void testCodeFirstTestFormRest(RestOperations template, String cseUrlPrefix) {
		HttpHeaders formHeaders = new HttpHeaders();
		formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> map = new HashMap<>();
		String code = "servicecomb%2bwelcome%40%23%24%25%5e%26*()%3d%3d";
		map.put("form1", code);
		map.put("form2", "");
		HttpEntity<Map<String, String>> formEntry = new HttpEntity<>(map, formHeaders);
		// Rest will have empty string, but users will try to avoid depend on this, This is different from highway
		TestMgr.check(code + "", template.postForEntity(cseUrlPrefix + "/testform", formEntry, String.class).getBody());
	}
}
