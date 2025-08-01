package org.apache.maven.scm.provider.perforce.command.status;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 */
public class PerforceStatusConsumerTest extends ScmTestCase {
	public void testGoodParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/status_good.txt");

		PerforceStatusConsumer consumer = new PerforceStatusConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		assertEquals("", consumer.getOutput());
		assertTrue(consumer.isSuccess());
		List<String> results = consumer.getDepotfiles();
		assertEquals("Wrong number of entries returned", 4, results.size());
		String entry = (String) results.get(0);
		assertEquals(33, entry.indexOf("Foo.java"));

		List<ScmFile> scmFiles = PerforceStatusCommand.createResults("//depot/sandbox/mperham/scm-test", consumer);
		assertEquals(4, results.size());
		ScmFile file = scmFiles.get(0);
		assertEquals("Foo.java", file.getPath());
		assertEquals(ScmFileStatus.ADDED, file.getStatus());
	}
}
