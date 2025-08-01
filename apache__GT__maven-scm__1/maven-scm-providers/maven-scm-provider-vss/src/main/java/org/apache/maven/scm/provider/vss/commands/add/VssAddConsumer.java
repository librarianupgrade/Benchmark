package org.apache.maven.scm.provider.vss.commands.add;

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
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class VssAddConsumer implements StreamConsumer {
	private ScmLogger logger;

	private List<ScmFile> addedFiles = new ArrayList<ScmFile>();

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	public VssAddConsumer(ScmLogger logger) {
		this.logger = logger;
	}

	// ----------------------------------------------------------------------
	// StreamConsumer Implementation
	// ----------------------------------------------------------------------

	/** {@inheritDoc} */
	public void consumeLine(String line) {
		if (line.length() <= 3) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unexpected input, the line must be at least three characters long. Line: '" + line + "'.");
			}

			return;
		}

		String statusString = line.substring(0, 1);

		String file = line.substring(3);

		ScmFileStatus status;

		if (statusString.equals("A")) {
			status = ScmFileStatus.ADDED;
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Unknown file status: '" + statusString + "'.");
			}

			return;
		}

		addedFiles.add(new ScmFile(file, status));
	}

	public List<ScmFile> getAddedFiles() {
		return addedFiles;
	}

}
