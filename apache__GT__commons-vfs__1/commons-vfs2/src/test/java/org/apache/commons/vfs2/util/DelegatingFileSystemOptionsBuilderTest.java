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
package org.apache.commons.vfs2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.TrustEveryoneUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Some tests for the DelegatingFileSystemOptionsBuilder
 */
public class DelegatingFileSystemOptionsBuilderTest {

	private static final String[] schemes = { "http", "ftp", "file", "zip", "tar", "tgz", "bz2", "gz", "jar", "tmp",
			"ram" };

	private StandardFileSystemManager fsm;

	@BeforeEach
	public void setUp() throws Exception {

		// get a full-blown, fully functional manager
		fsm = new StandardFileSystemManager();
		fsm.init();
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (fsm != null) {
			fsm.close();
		}
	}

	@Test
	public void testConfiguration() throws Exception {
		for (final String scheme : schemes) {
			assertTrue("Missing " + scheme + " provider", fsm.hasProvider(scheme));
		}
	}

	@Test
	public void testDelegatingBad() throws Throwable {
		final FileSystemOptions opts = new FileSystemOptions();
		final DelegatingFileSystemOptionsBuilder delegate = new DelegatingFileSystemOptionsBuilder(fsm);

		try {
			delegate.setConfigString(opts, "http", "proxyPort", "wrong_port");
			fail();
		} catch (final FileSystemException e) {
			assertSame(e.getCause().getClass(), InvocationTargetException.class);
			assertSame(((InvocationTargetException) e.getCause()).getTargetException().getClass(),
					NumberFormatException.class);
		}

		try {
			delegate.setConfigClass(opts, "sftp", "userinfo", String.class);
			fail();
		} catch (final FileSystemException e) {
			assertEquals(e.getCode(), "vfs.provider/config-value-invalid.error");
		}
	}

	@Test
	public void testDelegatingGood() throws Throwable {
		final String[] identityPaths = { "/file1", "/file2", };

		final FileSystemOptions opts = new FileSystemOptions();
		final DelegatingFileSystemOptionsBuilder delegate = new DelegatingFileSystemOptionsBuilder(fsm);

		delegate.setConfigString(opts, "http", "proxyHost", "proxy");
		delegate.setConfigString(opts, "http", "proxyPort", "8080");
		delegate.setConfigClass(opts, "sftp", "userinfo", TrustEveryoneUserInfo.class);
		delegate.setConfigStrings(opts, "sftp", "identities", identityPaths);

		assertEquals("http.proxyHost", HttpFileSystemConfigBuilder.getInstance().getProxyHost(opts), "proxy");
		assertEquals("http.proxyPort", HttpFileSystemConfigBuilder.getInstance().getProxyPort(opts), 8080);
		assertSame("sftp.userInfo", SftpFileSystemConfigBuilder.getInstance().getUserInfo(opts).getClass(),
				TrustEveryoneUserInfo.class);

		final File[] identities = SftpFileSystemConfigBuilder.getInstance().getIdentities(opts);
		assertNotNull("sftp.identities", identities);
		assertEquals("sftp.identities size", identities.length, identityPaths.length);
		for (int iterIdentities = 0; iterIdentities < identities.length; iterIdentities++) {
			assertEquals("sftp.identities #" + iterIdentities, identities[iterIdentities].getAbsolutePath(),
					new File(identityPaths[iterIdentities]).getAbsolutePath());
		}
	}

}
