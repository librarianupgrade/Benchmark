/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.sftp;

import org.apache.commons.vfs2.NamingTests;
import org.apache.commons.vfs2.PermissionsTests;
import org.apache.commons.vfs2.ProviderDeleteTests;
import org.apache.commons.vfs2.ProviderReadTests;
import org.apache.commons.vfs2.ProviderRenameTests;
import org.apache.commons.vfs2.ProviderWriteTests;

import junit.framework.Test;

public class SftpProviderClosedExecChannelTestCase extends AbstractSftpProviderTestCase {

	/**
	 * Creates the test suite for the sftp file system.
	 */
	public static Test suite() throws Exception {
		final SftpProviderTestSuite suite = new SftpProviderTestSuite(new SftpProviderClosedExecChannelTestCase()) {
			@Override
			protected void addBaseTests() throws Exception {
				addTests(ProviderReadTests.class);
				addTests(ProviderWriteTests.class);
				addTests(ProviderDeleteTests.class);
				addTests(ProviderRenameTests.class);
				addTests(NamingTests.class);
				// VFS-405: set/get permissions
				addTests(PermissionsTests.class);
			}
		};
		return suite;
	}

	@Override
	protected boolean isExecChannelClosed() {
		return true;
	}

}
