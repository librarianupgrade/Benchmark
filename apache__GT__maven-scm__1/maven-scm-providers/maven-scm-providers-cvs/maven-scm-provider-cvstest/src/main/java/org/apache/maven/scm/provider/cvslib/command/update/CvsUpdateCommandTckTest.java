package org.apache.maven.scm.provider.cvslib.command.update;

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

import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;
import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class CvsUpdateCommandTckTest extends UpdateCommandTckTest {
	/** {@inheritDoc} */
	public String getScmUrl() {
		return CvsScmTestUtils.getScmUrl(getRepositoryRoot(), getModule());
	}

	/** {@inheritDoc} */
	protected String getModule() {
		return "test-repo/module";
	}

	/** {@inheritDoc} */
	public void initRepo() throws Exception {
		CvsScmTestUtils.initRepo("src/test/tck-repository/", getRepositoryRoot(), getWorkingDirectory());
	}
}
