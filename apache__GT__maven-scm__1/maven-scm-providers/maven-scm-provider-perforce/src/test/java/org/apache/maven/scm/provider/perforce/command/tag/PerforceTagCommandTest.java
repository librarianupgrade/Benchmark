package org.apache.maven.scm.provider.perforce.command.tag;

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
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 */
public class PerforceTagCommandTest extends ScmTestCase {
	private static final File workingDirectory = getTestFile("target/perforce-tag-command-test");

	private static final String cmdPrefix = "p4 -d " + workingDirectory.getAbsolutePath();

	public void testCreateCommandLine() throws Exception {
		testCommandLine("foo-tag", cmdPrefix + " label -i", cmdPrefix + " labelsync -l foo-tag foo.xml bar.xml");
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	private void testCommandLine(String tag, String create, String sync) throws Exception {
		ScmRepository repository = getScmManager().makeScmRepository("scm:perforce://depot/projects/pathname");
		PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
				.getProviderRepository();
		ScmFileSet files = new ScmFileSet(new File("."),
				Arrays.asList(new File[] { new File("foo.xml"), new File("bar.xml") }));

		Commandline cl1 = PerforceTagCommand.createLabelCommandLine(svnRepository, workingDirectory);
		assertCommandLine(create, null, cl1);

		Commandline cl2 = PerforceTagCommand.createLabelsyncCommandLine(svnRepository, workingDirectory, files, tag);
		assertCommandLine(sync, null, cl2);
	}
}
