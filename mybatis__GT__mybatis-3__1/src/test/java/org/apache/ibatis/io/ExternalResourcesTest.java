/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.*;

public class ExternalResourcesTest {

	private File sourceFile;
	private File destFile;
	private File badFile;
	private File tempFile;

	/*
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tempFile = File.createTempFile("migration", "properties");
		tempFile.canWrite();
		sourceFile = File.createTempFile("test1", "sql");
		destFile = File.createTempFile("test2", "sql");
	}

	@Test
	public void testcopyExternalResource() {

		try {
			ExternalResources.copyExternalResource(sourceFile, destFile);
		} catch (IOException e) {
		}

	}

	@Test
	public void testcopyExternalResource_fileNotFound() {

		try {
			badFile = new File("/tmp/nofile.sql");
			ExternalResources.copyExternalResource(badFile, destFile);
		} catch (IOException e) {
			assertTrue(e instanceof FileNotFoundException);
		}

	}

	@Test
	public void testcopyExternalResource_emptyStringAsFile() {

		try {
			badFile = new File(" ");
			ExternalResources.copyExternalResource(badFile, destFile);
		} catch (Exception e) {
			assertTrue(e instanceof FileNotFoundException);
		}

	}

	@Test
	public void testGetConfiguredTemplate() {
		String templateName = "";

		try {
			FileWriter fileWriter = new FileWriter(tempFile);
			fileWriter.append("new_command.template=templates/col_new_template_migration.sql");
			fileWriter.flush();
			templateName = ExternalResources.getConfiguredTemplate(tempFile.getAbsolutePath(), "new_command.template");
			assertEquals("templates/col_new_template_migration.sql", templateName);
		} catch (Exception e) {
			fail("Test failed with execption: " + e.getMessage());
		}
	}

	@After
	public void cleanUp() {
		sourceFile.delete();
		destFile.delete();
		tempFile.delete();
	}
}
