package org.apache.maven.scm.provider.starteam.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @author Olivier Lamy
 *
 */
public class StarteamCheckOutConsumer implements StreamConsumer {
	private ScmLogger logger;

	private String workingDirectory;

	private String currentDir = "";

	private List<ScmFile> files = new ArrayList<ScmFile>();

	/**
	 * Marks current directory data
	 */
	private static final String DIR_MARKER = "(working dir: ";

	/**
	 * Marks current file data
	 */
	private static final String CHECKOUT_MARKER = ": checked out";

	/**
	 * Marks skipped file during update
	 */
	private static final String SKIPPED_MARKER = ": skipped";

	public StarteamCheckOutConsumer(ScmLogger logger, File workingDirectory) {
		this.logger = logger;

		this.workingDirectory = workingDirectory.getPath().replace('\\', '/');
	}

	/** {@inheritDoc} */
	public void consumeLine(String line) {
		if (logger.isDebugEnabled()) {
			logger.debug(line);
		}

		int pos = 0;

		if ((pos = line.indexOf(CHECKOUT_MARKER)) != -1) {
			processCheckedOutFile(line, pos);
		} else if ((pos = line.indexOf(DIR_MARKER)) != -1) {
			processDirectory(line, pos);
		} else if ((pos = line.indexOf(CHECKOUT_MARKER)) != -1) {
			processCheckedOutFile(line, pos);
		} else if ((pos = line.indexOf(SKIPPED_MARKER)) != -1) {
			processSkippedFile(line, pos);
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("Unknown checkout ouput: " + line);
			}
		}
	}

	public List<ScmFile> getCheckedOutFiles() {
		return files;
	}

	private void processDirectory(String line, int pos) {
		String dirPath = line.substring(pos + DIR_MARKER.length(), line.length() - 1).replace('\\', '/');

		try {
			this.currentDir = StarteamCommandLineUtils.getRelativeChildDirectory(this.workingDirectory, dirPath);
		} catch (IllegalStateException e) {
			String error = "Working and checkout directories are not on the same tree";

			if (logger.isErrorEnabled()) {
				logger.error(error);
				logger.error("Working directory: " + workingDirectory);
				logger.error("Checked out directory: " + dirPath);
			}

			throw new IllegalStateException(error);
		}

	}

	private void processCheckedOutFile(String line, int pos) {
		String checkedOutFilePath = this.currentDir + "/" + line.substring(0, pos);

		this.files.add(new ScmFile(checkedOutFilePath, ScmFileStatus.CHECKED_OUT));

		if (logger.isInfoEnabled()) {
			logger.info("Checked out: " + checkedOutFilePath);
		}
	}

	private void processSkippedFile(String line, int pos) {
		String skippedFilePath = this.currentDir + "/" + line.substring(0, pos);

		if (logger.isDebugEnabled()) {
			logger.debug("Skipped: " + skippedFilePath);
		}
	}

}
