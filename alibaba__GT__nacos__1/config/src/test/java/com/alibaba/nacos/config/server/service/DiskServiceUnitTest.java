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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.config.server.utils.DiskUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class DiskServiceUnitTest {

	private DiskUtil diskService;

	private File tempFile;

	private String path;

	@Before
	public void setUp() throws IOException {
		this.tempFile = File.createTempFile("diskServiceTest", "tmp");
		this.path = tempFile.getParent();
		this.diskService = new DiskUtil();
	}

	@Test
	public void testCreateConfig() throws IOException {
		DiskUtil.saveToDisk("testDataId", "testGroup", "testTenant", "testContent");
		String content = DiskUtil.getConfig("testDataId", "testGroup", "testTenant");
		assertEquals(content, "testContent");

	}

	@After
	public void tearDown() throws IOException {
		tempFile.delete();
	}
}
