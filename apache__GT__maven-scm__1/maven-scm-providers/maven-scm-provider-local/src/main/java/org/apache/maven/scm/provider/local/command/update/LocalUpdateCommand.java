package org.apache.maven.scm.provider.local.command.update;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.command.changelog.LocalChangeLogCommand;
import org.apache.maven.scm.provider.local.metadata.LocalScmMetadata;
import org.apache.maven.scm.provider.local.metadata.LocalScmMetadataUtils;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public class LocalUpdateCommand extends AbstractUpdateCommand implements LocalCommand {
	/** {@inheritDoc} */
	protected UpdateScmResult executeUpdateCommand(ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version)
			throws ScmException {
		LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

		if (version != null) {
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

		if (!baseDestination.exists() && !baseDestination.isDirectory()) {
			throw new ScmException("The destination directory isn't a directory or doesn't exist ("
					+ baseDestination.getAbsolutePath() + ").");
		}

		List<ScmFile> updatedFiles;

		try {
			if (getLogger().isInfoEnabled()) {
				getLogger().info("Updating '" + baseDestination.getAbsolutePath() + "' from '"
						+ source.getAbsolutePath() + "'.");
			}

			@SuppressWarnings("unchecked")
			List<File> fileList = FileUtils.getFiles(source.getAbsoluteFile(), "**", null);
			List<File> list = fileList;
			updatedFiles = update(source, baseDestination, list);

			// process deletions in repository
			LocalScmMetadataUtils metadataUtils = new LocalScmMetadataUtils(getLogger());
			LocalScmMetadata originalMetadata = metadataUtils.readMetadata(baseDestination);
			if (originalMetadata != null) {
				LocalScmMetadata newMetadata = metadataUtils.buildMetadata(source);
				for (Iterator<String> it = originalMetadata.getRepositoryFileNames().iterator(); it.hasNext();) {
					String filename = it.next();
					if (!newMetadata.getRepositoryFileNames().contains(filename)) {
						File localFile = new File(baseDestination, filename);
						if (localFile.exists()) {
							localFile.delete();
							updatedFiles.add(new ScmFile("/" + filename, ScmFileStatus.UPDATED));
						}
					}
				}
			}

			// rewrite metadata file
			metadataUtils.writeMetadata(baseDestination, metadataUtils.buildMetadata(source));

		} catch (IOException ex) {
			throw new ScmException("Error while checking out the files.", ex);
		}

		return new LocalUpdateScmResult(null, updatedFiles);
	}

	private List<ScmFile> update(File source, File baseDestination, List<File> files) throws ScmException, IOException {
		String sourcePath = source.getAbsolutePath();

		List<ScmFile> updatedFiles = new ArrayList<ScmFile>();

		for (Iterator<File> i = files.iterator(); i.hasNext();) {
			File repositoryFile = i.next();

			File repositoryDirectory = repositoryFile.getParentFile();

			// TODO: Add more excludes here
			if (repositoryDirectory != null && repositoryDirectory.getName().equals("CVS")) {
				continue;
			}

			String dest = repositoryFile.getAbsolutePath().substring(sourcePath.length() + 1);

			File destinationFile = new File(baseDestination, dest);

			String repositoryFileContents = FileUtils.fileRead(repositoryFile);

			if (destinationFile.exists()) {
				String destionationFileContents = FileUtils.fileRead(destinationFile);

				if (repositoryFileContents.equals(destionationFileContents)) {
					continue;
				}
			}

			File destinationDirectory = destinationFile.getParentFile();

			if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
				throw new ScmException(
						"Could not create destination directory '" + destinationDirectory.getAbsolutePath() + "'.");
			}

			ScmFileStatus status;

			if (destinationFile.exists()) {
				status = ScmFileStatus.UPDATED;
			} else {
				status = ScmFileStatus.ADDED;
			}

			FileUtils.copyFileToDirectory(repositoryFile, destinationDirectory);

			int chop = baseDestination.getAbsolutePath().length();

			String fileName = "/" + destinationFile.getAbsolutePath().substring(chop + 1);

			updatedFiles.add(new ScmFile(fileName, status));
		}

		return updatedFiles;
	}

	/** {@inheritDoc} */
	protected ChangeLogCommand getChangeLogCommand() {
		return new LocalChangeLogCommand();
	}
}
