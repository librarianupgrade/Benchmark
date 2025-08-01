package org.apache.maven.scm.provider.perforce.command.add;

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

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class PerforceAddConsumerTest extends ScmTestCase {
	public void testParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/addlog.txt");

		PerforceAddConsumer consumer = new PerforceAddConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		List<ScmFile> adds = consumer.getAdditions();
		assertEquals("Wrong number of entries returned", 3, adds.size());
		String entry = adds.get(0).getPath();
		assertTrue(entry.startsWith("//"));
		assertTrue(entry.endsWith("foo.xml"));
	}
}
