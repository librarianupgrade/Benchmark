package org.apache.maven.scm.provider.svn.svnexe.command.list;

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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 *
 */
public class SvnListCommandTest extends ScmTestCase {
	public void testCommandLineWithEmptyTag() throws Exception {
		testCommandLine("scm:svn:http://foo.com/svn/trunk", true, "svn --non-interactive list --recursive");
	}

	public void testCommandLineWithWhitespaceTag() throws Exception {
		testCommandLine("scm:svn:http://foo.com/svn/trunk", false, "svn --non-interactive list");
	}

	public void testCommandLineWithoutTag() throws Exception {
		testCommandLine("scm:svn:http://foo.com/svn/trunk", false, "svn --non-interactive list");
	}

	public void testCommandLineTag() throws Exception {
		testCommandLine("scm:svn:http://anonymous@foo.com/svn/trunk", false, "10",
				"svn --username anonymous --no-auth-cache --non-interactive list -r 10");
	}

	public void testCommandLineWithUsernameAndTag() throws Exception {
		testCommandLine("scm:svn:http://anonymous@foo.com/svn/trunk", false, "10",
				"svn --username anonymous --no-auth-cache --non-interactive list -r 10");
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	private SvnScmProviderRepository getSvnRepository(String scmUrl) throws Exception {
		ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

		return (SvnScmProviderRepository) repository.getProviderRepository();
	}

	private void testCommandLine(String scmUrl, boolean recursive, String commandLine) throws Exception {
		testCommandLine(scmUrl, recursive, null, commandLine);
	}

	private void testCommandLine(String scmUrl, boolean recursive, String revision, String commandLine)
			throws Exception {
		ScmFileSet fileSet = new ScmFileSet(new File("."), new File("."));

		Commandline cl = SvnListCommand.createCommandLine(getSvnRepository(scmUrl), fileSet, recursive,
				new ScmRevision(revision));

		assertCommandLine(commandLine + " http://foo.com/svn/trunk/.", new File(System.getProperty("java.io.tmpdir")),
				cl);
	}
}
