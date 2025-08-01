/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.api.ldap.schema.extractor;

import java.io.File;
import java.io.IOException;

import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Tests the DefaultSchemaLdifExtractor class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class SchemaLdifExtractorTest {
	private static String workingDirectory;

	@BeforeAll
	public static void setup() throws IOException {
		workingDirectory = System.getProperty("workingDirectory");

		if (workingDirectory == null) {
			String path = SchemaLdifExtractorTest.class.getResource("").getPath();
			int targetPos = path.indexOf("target");
			workingDirectory = path.substring(0, targetPos + 6);
		}

		// Cleanup the target directory
		FileUtils.deleteDirectory(new File(workingDirectory + "/schema"));
	}

	@AfterAll
	public static void cleanup() throws IOException {
		// Cleanup the target directory
		FileUtils.deleteDirectory(new File(workingDirectory + "/schema"));
	}

	@Test
	public void testExtract() throws Exception {
		SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(new File(workingDirectory));
		extractor.extractOrCopy();
	}
}
