package org.apache.maven.scm.provider.cvslib.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.util.ConsumerUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class CvsChangeLogConsumerTest extends AbstractCvsScmTest {
	/**
	 * file with test results to check against
	 */
	private File testFile;

	/**
	 * Initialize per test data
	 *
	 * @throws Exception when there is an unexpected problem
	 */
	public void setUp() throws Exception {
		super.setUp();

		testFile = getTestFile("/src/test/resources/cvslib/changelog/cvslog.txt");
	}

	/**
	 * Test of parse method
	 *
	 * @throws Exception when there is an unexpected problem
	 */
	public void testParse() throws Exception {
		CvsChangeLogConsumer command = new CvsChangeLogConsumer(new DefaultLog(), null);
		ConsumerUtils.consumeFile(testFile, command);

		Collection<ChangeSet> entries = command.getModifications();
		assertEquals("Wrong number of entries returned", 3, entries.size());
		ChangeSet entry = null;
		for (Iterator<ChangeSet> i = entries.iterator(); i.hasNext();) {
			entry = i.next();
			assertTrue("ChangeLogEntry erroneously picked up", entry.toString().indexOf("ChangeLogEntry.java") == -1);
		}
	}
}
