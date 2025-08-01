package org.apache.maven.scm.provider.starteam.command.tag;

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
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class StarteamTagCommandTest extends ScmTestCase {
	public void testTagCommandLine() throws Exception {

		File workDir = new File("target");

		testCommandLine("scm:starteam:myusername:mypassword@myhost:1234/projecturl", workDir, "myTag",
				"stcmd label -x -nologo -stop " + "-p myusername:mypassword@myhost:1234/projecturl " + "-nl myTag -b");
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	private void testCommandLine(String scmUrl, File workDir, String tag, String commandLine) throws Exception {
		ScmRepository repo = getScmManager().makeScmRepository(scmUrl);

		StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

		Commandline cl = StarteamTagCommand.createCommandLine(repository, workDir, tag);

		assertCommandLine(commandLine, null, cl);
	}
}
