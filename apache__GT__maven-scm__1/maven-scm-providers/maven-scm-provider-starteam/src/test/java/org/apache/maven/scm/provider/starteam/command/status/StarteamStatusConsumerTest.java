package org.apache.maven.scm.provider.starteam.command.status;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.util.ConsumerUtils;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamStatusConsumerTest extends ScmTestCase {
	// must match with the test file    
	private static final String WORKING_DIR = "/usr/scm-starteam/driver";

	private File testFile;

	public void setUp() throws Exception {
		super.setUp();

		testFile = getTestFile("/src/test/resources/starteam/status/status.txt");
	}

	public void testParse() throws Exception {
		StarteamStatusConsumer consumer = new StarteamStatusConsumer(new DefaultLog(), new File(WORKING_DIR));

		ConsumerUtils.consumeFile(testFile, consumer);

		assertEquals("Wrong number of entries returned", 4, consumer.getChangedFiles().size());

		// TODO add more validation to the entries
	}
}
