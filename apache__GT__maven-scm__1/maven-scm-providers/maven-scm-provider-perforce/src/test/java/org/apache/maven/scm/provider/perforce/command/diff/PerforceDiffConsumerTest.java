package org.apache.maven.scm.provider.perforce.command.diff;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class PerforceDiffConsumerTest extends ScmTestCase {
	public void testParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/difflog.txt");

		PerforceDiffConsumer consumer = new PerforceDiffConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		// Linebreak differences will fail if we try to assert
		// the exact file length so we just use a rough approximation.
		assertTrue(consumer.getOutput().length() > 12500);
		assertTrue(consumer.getOutput().length() < 13500);
	}
}
