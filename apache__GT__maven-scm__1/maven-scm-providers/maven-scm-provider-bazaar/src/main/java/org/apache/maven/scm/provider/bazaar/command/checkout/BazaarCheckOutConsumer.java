package org.apache.maven.scm.provider.bazaar.command.checkout;

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
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjorn Eikli Smorgrav</a>
 * @author Olivier Lamy
 *
 */
public class BazaarCheckOutConsumer extends BazaarConsumer {

	private final File workingDirectory;

	private final List<ScmFile> checkedOut = new ArrayList<ScmFile>();

	public BazaarCheckOutConsumer(ScmLogger logger, File workingDirectory) {
		super(logger);
		this.workingDirectory = workingDirectory;
	}

	/** {@inheritDoc} */
	public void doConsume(ScmFileStatus status, String line) {
		File file = new File(workingDirectory, line);
		if (file.isFile()) {
			checkedOut.add(new ScmFile(line, ScmFileStatus.CHECKED_OUT));
		}
	}

	public List<ScmFile> getCheckedOutFiles() {
		return checkedOut;
	}
}
