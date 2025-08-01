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
package org.apache.commons.vfs2.provider.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * A wrapper to the FTPClient to allow automatic reconnect on connection loss.
 * <p>
 * I decided to not to use eg. noop() to determine the state of the connection to avoid unnecessary server round-trips.
 * </p>
 */
public class FTPClientWrapper implements FtpClient {

	private static final Log LOG = LogFactory.getLog(FTPClientWrapper.class);

	/**
	 * Authentication options.
	 */
	protected final FileSystemOptions fileSystemOptions;
	private FTPClient ftpClient;
	private final GenericFileName root;

	protected FTPClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions)
			throws FileSystemException {
		this.root = root;
		this.fileSystemOptions = fileSystemOptions;
		getFtpClient(); // fail-fast
	}

	@Override
	public boolean abort() throws IOException {
		try {
			// imario@apache.org: 2005-02-14
			// it should be better to really "abort" the transfer, but
			// currently I didn't manage to make it work - so lets "abort" the hard way.
			// return getFtpClient().abort();

			disconnect();
			return true;
		} catch (final IOException e) {
			disconnect();
		}
		return true;
	}

	@Override
	public OutputStream appendFileStream(final String relPath) throws IOException {
		try {
			return getFtpClient().appendFileStream(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().appendFileStream(relPath);
		}
	}

	@Override
	public boolean completePendingCommand() throws IOException {
		if (ftpClient != null) {
			return getFtpClient().completePendingCommand();
		}

		return true;
	}

	private FTPClient createClient() throws FileSystemException {
		final GenericFileName rootName = getRoot();

		UserAuthenticationData authData = null;
		try {
			authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, FtpFileProvider.AUTHENTICATOR_TYPES);

			return createClient(rootName, authData);
		} finally {
			UserAuthenticatorUtils.cleanup(authData);
		}
	}

	protected FTPClient createClient(final GenericFileName rootName, final UserAuthenticationData authData)
			throws FileSystemException {
		return FtpClientFactory.createConnection(rootName.getHostName(), rootName.getPort(),
				UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
						UserAuthenticatorUtils.toChar(rootName.getUserName())),
				UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
						UserAuthenticatorUtils.toChar(rootName.getPassword())),
				rootName.getPath(), getFileSystemOptions());
	}

	@Override
	public boolean deleteFile(final String relPath) throws IOException {
		try {
			return getFtpClient().deleteFile(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().deleteFile(relPath);
		}
	}

	@Override
	public void disconnect() throws IOException {
		try {
			getFtpClient().quit();
		} catch (final IOException e) {
			LOG.debug("I/O exception while trying to quit, probably it's a timed out connection, ignoring.", e);
		} finally {
			try {
				getFtpClient().disconnect();
			} catch (final IOException e) {
				LOG.warn("I/O exception while trying to disconnect, probably it's a closed connection, ignoring.", e);
			} finally {
				ftpClient = null;
			}
		}
	}

	/**
	 * Gets the FileSystemOptions.
	 *
	 * @return the FileSystemOptions.
	 */
	public FileSystemOptions getFileSystemOptions() {
		return fileSystemOptions;
	}

	private FTPClient getFtpClient() throws FileSystemException {
		if (ftpClient == null) {
			ftpClient = createClient();
		}

		return ftpClient;
	}

	@Override
	public int getReplyCode() throws IOException {
		return getFtpClient().getReplyCode();
	}

	@Override
	public String getReplyString() throws IOException {
		return getFtpClient().getReplyString();
	}

	/**
	 * Gets the root file name.
	 *
	 * @return  the root file name.
	 */
	public GenericFileName getRoot() {
		return root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasFeature(final String feature) throws IOException {
		try {
			return getFtpClient().hasFeature(feature);
		} catch (final IOException ex) {
			disconnect();
			return getFtpClient().hasFeature(feature);
		}
	}

	@Override
	public boolean isConnected() throws FileSystemException {
		return ftpClient != null && ftpClient.isConnected();
	}

	@Override
	public FTPFile[] listFiles(final String relPath) throws IOException {
		try {
			// VFS-210: return getFtpClient().listFiles(relPath);
			return listFilesInDirectory(relPath);
		} catch (final IOException e) {
			disconnect();
			return listFilesInDirectory(relPath);
		}
	}

	private FTPFile[] listFilesInDirectory(final String relPath) throws IOException {
		// VFS-307: no check if we can simply list the files, this might fail if there are spaces in the path
		FTPFile[] ftpFiles = getFtpClient().listFiles(relPath);
		if (FTPReply.isPositiveCompletion(getFtpClient().getReplyCode())) {
			return ftpFiles;
		}

		// VFS-307: now try the hard way by cd'ing into the directory, list and cd back
		// if VFS is required to fallback here the user might experience a real bad FTP performance
		// as then every list requires 4 FTP commands.
		String workingDirectory = null;
		if (relPath != null) {
			workingDirectory = getFtpClient().printWorkingDirectory();
			if (!getFtpClient().changeWorkingDirectory(relPath)) {
				return null;
			}
		}

		ftpFiles = getFtpClient().listFiles();

		if (relPath != null && !getFtpClient().changeWorkingDirectory(workingDirectory)) {
			throw new FileSystemException("vfs.provider.ftp.wrapper/change-work-directory-back.error",
					workingDirectory);
		}
		return ftpFiles;
	}

	@Override
	public boolean makeDirectory(final String relPath) throws IOException {
		try {
			return getFtpClient().makeDirectory(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().makeDirectory(relPath);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instant mdtmInstant(final String relPath) throws IOException {
		try {
			return getFtpClient().mdtmCalendar(relPath).toInstant();
		} catch (final IOException ex) {
			disconnect();
			return getFtpClient().mdtmCalendar(relPath).toInstant();
		}
	}

	@Override
	public boolean removeDirectory(final String relPath) throws IOException {
		try {
			return getFtpClient().removeDirectory(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().removeDirectory(relPath);
		}
	}

	@Override
	public boolean rename(final String oldName, final String newName) throws IOException {
		try {
			return getFtpClient().rename(oldName, newName);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().rename(oldName, newName);
		}
	}

	@Override
	public InputStream retrieveFileStream(final String relPath) throws IOException {
		try {
			return getFtpClient().retrieveFileStream(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().retrieveFileStream(relPath);
		}
	}

	@Override
	public InputStream retrieveFileStream(final String relPath, final int bufferSize) throws IOException {
		try {
			final FTPClient client = getFtpClient();
			client.setBufferSize(bufferSize);
			return client.retrieveFileStream(relPath);
		} catch (final IOException e) {
			disconnect();
			final FTPClient client = getFtpClient();
			client.setBufferSize(bufferSize);
			return client.retrieveFileStream(relPath);
		}
	}

	@Override
	public InputStream retrieveFileStream(final String relPath, final long restartOffset) throws IOException {
		try {
			final FTPClient client = getFtpClient();
			client.setRestartOffset(restartOffset);
			return client.retrieveFileStream(relPath);
		} catch (final IOException e) {
			disconnect();
			final FTPClient client = getFtpClient();
			client.setRestartOffset(restartOffset);
			return client.retrieveFileStream(relPath);
		}
	}

	@Override
	public void setBufferSize(final int bufferSize) throws FileSystemException {
		getFtpClient().setBufferSize(bufferSize);
	}

	@Override
	public OutputStream storeFileStream(final String relPath) throws IOException {
		try {
			return getFtpClient().storeFileStream(relPath);
		} catch (final IOException e) {
			disconnect();
			return getFtpClient().storeFileStream(relPath);
		}
	}
}
