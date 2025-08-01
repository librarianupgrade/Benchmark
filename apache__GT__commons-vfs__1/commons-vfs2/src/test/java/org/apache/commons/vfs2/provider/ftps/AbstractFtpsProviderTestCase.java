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
package org.apache.commons.vfs2.provider.ftps;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.Assertions;

/**
 * Abstract tests for FTP file systems.
 */
abstract class AbstractFtpsProviderTestCase extends AbstractProviderTestConfig {

	static final class FtpProviderTestSuite extends ProviderTestSuite {
		private final boolean implicit;

		public FtpProviderTestSuite(final AbstractFtpsProviderTestCase providerConfig) throws Exception {
			super(providerConfig);
			this.implicit = providerConfig.isImplicit();
		}

		@Override
		protected void setUp() throws Exception {
			if (getSystemTestUriOverride() == null) {
				setUpClass(implicit);
			}
			super.setUp();
		}

		@Override
		protected void tearDown() throws Exception {
			try {
				// This will report running threads of the FTP server.
				// However, shutting down the FTP server first will always
				// report an exception closing the manager, because the
				// server is already down
				super.tearDown();
			} finally {
				tearDownClass();
			}
		}
	}

	private static int SocketPort;

	/**
	 * Use %40 for @ in URLs
	 */
	private static String ConnectionUri;

	private static FtpServer Server;

	private static final String TEST_URI = "test.ftps.uri";

	private static final String USER_PROPS_RES = "org.apache.ftpsserver/users.properties";

	private static final String SERVER_JKS_RES = "org.apache.ftpsserver/ftpserver.jks";

	protected FileSystemOptions fileSystemOptions;

	static String getConnectionUri() {
		return ConnectionUri;
	}

	static int getSocketPort() {
		return SocketPort;
	}

	static String getSystemTestUriOverride() {
		return System.getProperty(TEST_URI);
	}

	/**
	 * Creates and starts an embedded Apache FTP Server (MINA).
	 *
	 * @param implicit FTPS connection mode
	 * @throws FtpException
	 */
	static void setUpClass(final boolean implicit) throws FtpException {
		if (Server != null) {
			return;
		}
		final FtpServerFactory serverFactory = new FtpServerFactory();
		final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
		final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
		Assertions.assertNotNull(userPropsResource, USER_PROPS_RES);
		propertiesUserManagerFactory.setUrl(userPropsResource);
		final UserManager userManager = propertiesUserManagerFactory.createUserManager();
		final BaseUser user = (BaseUser) userManager.getUserByName("test");
		// Pickup the home dir value at runtime even though we have it set in the user prop file
		// The user prop file requires the "homedirectory" to be set
		user.setHomeDirectory(getTestDirectory());
		serverFactory.setUserManager(userManager);
		final ListenerFactory factory = new ListenerFactory();
		// Let the OS find use an ephemeral port by using 0 here.
		factory.setPort(0);

		// define SSL configuration
		final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES);
		Assertions.assertNotNull(serverJksResource, SERVER_JKS_RES);
		final SslConfigurationFactory ssl = new SslConfigurationFactory();
		final File keyStoreFile = FileUtils.toFile(serverJksResource);
		Assertions.assertTrue(keyStoreFile.exists(), keyStoreFile.toString());
		ssl.setKeystoreFile(keyStoreFile);
		ssl.setKeystorePassword("password");

		// set the SSL configuration for the listener
		SslConfiguration sslConfiguration = ssl.createSslConfiguration();
		NoProtocolSslConfigurationProxy noProtocolSslConfigurationProxy = new NoProtocolSslConfigurationProxy(
				sslConfiguration);
		factory.setSslConfiguration(noProtocolSslConfigurationProxy);
		factory.setImplicitSsl(implicit);

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());

		// start the server
		Server = serverFactory.createServer();
		Server.start();
		SocketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) Server).getListener("default").getPort();
		ConnectionUri = "ftps://test:test@localhost:" + SocketPort;
	}

	/**
	 * Stops the embedded Apache FTP Server (MINA).
	 */
	static void tearDownClass() {
		if (Server != null) {
			Server.stop();
			Server = null;
		}
	}

	/**
	 * Returns the base folder for tests. You can override the DEFAULT_URI by using the system property name defined by
	 * TEST_URI.
	 */
	@Override
	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
		String uri = getSystemTestUriOverride();
		if (uri == null) {
			uri = ConnectionUri;
		}
		return manager.resolveFile(uri, getFileSystemOptions());
	}

	protected FileSystemOptions getFileSystemOptions() {
		if (fileSystemOptions == null) {
			fileSystemOptions = new FileSystemOptions();
			setupOptions(FtpsFileSystemConfigBuilder.getInstance());
		}
		return fileSystemOptions;
	}

	protected abstract boolean isImplicit();

	/**
	 * Prepares the file system manager.
	 */
	@Override
	public void prepare(final DefaultFileSystemManager manager) throws Exception {
		manager.addProvider("ftps", new FtpsFileProvider());
	}

	protected void setupOptions(final FtpsFileSystemConfigBuilder builder) {
		builder.setConnectTimeout(fileSystemOptions, Duration.ofSeconds(10));
		builder.setDataTimeout(fileSystemOptions, Duration.ofSeconds(10));
	}

}
