package org.apache.maven.scm.provider.svn.svnexe.command.remoteinfo;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;

/**
 * @author Olivier Lamy
 */
public class SvnRemoteInfoCommandTest extends ScmTestCase {
	public void testExist() throws Exception {
		SvnRemoteInfoCommand svnRemoteInfoCommand = new SvnRemoteInfoCommand();

		SvnScmProviderRepository svnScmProviderRepository = new SvnScmProviderRepository(
				"http://svn.apache.org/repos/asf/maven/scm/trunk/");
		assertTrue(svnRemoteInfoCommand.remoteUrlExist(svnScmProviderRepository, null));
	}

	public void testNotExist() throws Exception {
		SvnRemoteInfoCommand svnRemoteInfoCommand = new SvnRemoteInfoCommand();

		SvnScmProviderRepository svnScmProviderRepository = new SvnScmProviderRepository(
				"http://svn.apache.org/repos/asf/maven/scm/trunk/foo/bar/beer");
		assertFalse(svnRemoteInfoCommand.remoteUrlExist(svnScmProviderRepository, null));
	}
}
