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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Arrays;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class PerforceCheckInCommandTest extends ScmTestCase {
	private static final File workingDirectory = getTestFile("target/perforce-checkin-command-test");

	private static final String cmdPrefix = "p4 -d " + workingDirectory.getAbsolutePath();

	public void testGetCommandLine() throws Exception {
		ScmFileSet files = new ScmFileSet(new File("."),
				Arrays.asList(new File[] { new File("foo.xml"), new File("bar.xml") }));
		testCommandLine("scm:perforce://depot/projects/pathname", cmdPrefix + " submit -i", files);
	}

	public void testGetCommandLineAbsolutePaths() throws Exception {
		ScmFileSet files = new ScmFileSet(new File(".").getAbsoluteFile(), Arrays
				.asList(new File[] { new File("foo.xml").getAbsoluteFile(), new File("bar.xml").getAbsoluteFile() }));
		testCommandLine("scm:perforce://depot/projects/pathname", cmdPrefix + " submit -i", files);
	}

	public void testGetCommandLineWithHost() throws Exception {
		ScmFileSet files = new ScmFileSet(new File("."),
				Arrays.asList(new File[] { new File("foo.xml"), new File("bar.xml") }));
		testCommandLine("scm:perforce:a:username@//depot/projects/pathname", cmdPrefix + " -p a -u username submit -i",
				files);
	}

	public void testGetCommandLineWithHostAndPort() throws Exception {
		ScmFileSet files = new ScmFileSet(new File("."),
				Arrays.asList(new File[] { new File("foo.xml"), new File("bar.xml") }));
		testCommandLine("scm:perforce:myhost:1234:username@//depot/projects/pathname",
				cmdPrefix + " -p myhost:1234 -u username submit -i", files);
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	private void testCommandLine(String scmUrl, String commandLine, ScmFileSet files) throws Exception {
		ScmRepository repository = getScmManager().makeScmRepository(scmUrl);
		PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
				.getProviderRepository();
		Commandline cl = PerforceCheckInCommand.createCommandLine(svnRepository, workingDirectory);

		assertCommandLine(commandLine, null, cl);

		String change = PerforceCheckInCommand.createChangeListSpecification(svnRepository, files, "Test checkin",
				"//depot/projects/pathname", "testjob");
		assertContains(change, "//depot/projects/pathname/foo.xml");
		assertContains(change, "//depot/projects/pathname/bar.xml");
		assertContains(change, "Test checkin");
		assertContains(change, "testjob");
	}

	private void assertContains(String block, String element) {
		if (block.indexOf(element) == -1) {
			fail("Block '" + block + "' does not contain element '" + element + "'");
		}
	}
}
