package org.apache.maven.scm.provider.perforce.command.checkin;

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
public class PerforceCheckInConsumerTest extends ScmTestCase {
	public void testGoodParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/checkin_good.txt");

		PerforceCheckInConsumer consumer = new PerforceCheckInConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		assertTrue(consumer.getOutput(), consumer.isSuccess());
		assertEquals("", consumer.getOutput());
	}

	public void testGooderParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/checkin_gooder.txt");

		PerforceCheckInConsumer consumer = new PerforceCheckInConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		assertTrue(consumer.getOutput(), consumer.isSuccess());
		assertEquals("", consumer.getOutput());
	}

	public void testBadParse() throws Exception {
		File testFile = getTestFile("src/test/resources/perforce/checkin_bad.txt");

		PerforceCheckInConsumer consumer = new PerforceCheckInConsumer();

		ConsumerUtils.consumeFile(testFile, consumer);

		assertFalse(consumer.getOutput(), consumer.isSuccess());
		assertTrue(consumer.getOutput(), consumer.getOutput().length() > 0);
	}
}
