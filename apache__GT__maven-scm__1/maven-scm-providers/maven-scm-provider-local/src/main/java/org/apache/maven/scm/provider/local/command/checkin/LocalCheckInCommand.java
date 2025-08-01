package org.apache.maven.scm.provider.local.command.checkin;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class LocalCheckInCommand extends AbstractCheckInCommand implements LocalCommand {
	/** {@inheritDoc} */
	protected CheckInScmResult executeCheckInCommand(ScmProviderRepository repo, ScmFileSet fileSet, String message,
			ScmVersion version) throws ScmException {
		LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

		if (version != null && StringUtils.isNotEmpty(version.getName())) {
			throw new ScmException("The local scm doesn't support tags.");
		}

		File root = new File(repository.getRoot());

		String module = repository.getModule();

		File source = new File(root, module);

		File baseDestination = fileSet.getBasedir();

		if (!baseDestination.exists()) {
			throw new ScmException("The working directory doesn't exist (" + baseDestination.getAbsolutePath() + ").");
		}

		if (!root.exists()) {
			throw new ScmException("The base directory doesn't exist (" + root.getAbsolutePath() + ").");
		}

		if (!source.exists()) {
			throw new ScmException("The module directory doesn't exist (" + source.getAbsolutePath() + ").");
		}

		List<ScmFile> checkedInFiles = new ArrayList<ScmFile>();

		try {
			// Only copy files newer than in the repo
			File repoRoot = new File(repository.getRoot(), repository.getModule());

			List<File> files = fileSet.getFileList();

			if (files.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<File> listFiles = FileUtils.getFiles(baseDestination, "**", null, false);

				files = listFiles;
			}

			for (File file : files) {
				String path = file.getPath().replace('\\', '/');
				File repoFile = new File(repoRoot, path);
				file = new File(baseDestination, path);

				ScmFileStatus status;

				if (repoFile.exists()) {
					String repoFileContents = FileUtils.fileRead(repoFile);

					String fileContents = FileUtils.fileRead(file);

					if (getLogger().isDebugEnabled()) {
						getLogger().debug("fileContents:" + fileContents);
						getLogger().debug("repoFileContents:" + repoFileContents);
					}
					if (fileContents.equals(repoFileContents)) {
						continue;
					}

					status = ScmFileStatus.CHECKED_IN;
				} else if (repository.isFileAdded(path)) {
					status = ScmFileStatus.CHECKED_IN;
				} else {
					if (getLogger().isWarnEnabled()) {
						getLogger().warn("skipped unknown file in checkin:" + path);
					}
					// unknown file, skip
					continue;
				}

				FileUtils.copyFile(file, repoFile);
				ScmFile scmFile = new ScmFile(path, status);
				getLogger().info(scmFile.toString());
				checkedInFiles.add(scmFile);
			}
		} catch (IOException ex) {
			throw new ScmException("Error while checking in the files.", ex);
		}

		return new CheckInScmResult(null, checkedInFiles);
	}
}
