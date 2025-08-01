package org.apache.maven.scm.provider.perforce.command.edit;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author Mike Perham
 *
 */
public class PerforceEditCommand extends AbstractEditCommand implements PerforceCommand {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ScmResult executeEditCommand(ScmProviderRepository repo, ScmFileSet files) throws ScmException {
		Commandline cl = createCommandLine((PerforceScmProviderRepository) repo, files.getBasedir(), files);
		PerforceEditConsumer consumer = new PerforceEditConsumer();
		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(PerforceScmProvider.clean("Executing " + cl.toString()));
			}

			CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
			int exitCode = CommandLineUtils.executeCommandLine(cl, consumer, err);

			if (exitCode != 0) {
				String cmdLine = CommandLineUtils.toString(cl.getCommandline());

				StringBuilder msg = new StringBuilder("Exit code: " + exitCode + " - " + err.getOutput());
				msg.append('\n');
				msg.append("Command line was:" + cmdLine);

				throw new CommandLineException(msg.toString());
			}
		} catch (CommandLineException e) {
			if (getLogger().isErrorEnabled()) {
				getLogger().error("CommandLineException " + e.getMessage(), e);
			}
		}

		if (consumer.isSuccess()) {
			return new EditScmResult(cl.toString(), consumer.getEdits());
		}

		return new EditScmResult(cl.toString(), "Unable to edit file(s)", consumer.getErrorMessage(), false);
	}

	public static Commandline createCommandLine(PerforceScmProviderRepository repo, File workingDirectory,
			ScmFileSet files) throws ScmException {
		Commandline command = PerforceScmProvider.createP4Command(repo, workingDirectory);

		command.createArg().setValue("edit");

		try {
			String candir = workingDirectory.getCanonicalPath();

			for (File f : files.getFileList()) {
				File file = null;
				if (f.isAbsolute()) {
					file = new File(f.getPath());
				} else {
					file = new File(workingDirectory, f.getPath());
				}
				// I want to use relative paths to add files to make testing
				// simpler.
				// Otherwise the absolute path will be different on everyone's
				// machine
				// and testing will be a little more painful.
				String canfile = file.getCanonicalPath();
				if (canfile.startsWith(candir)) {
					canfile = canfile.substring(candir.length() + 1);
				}
				command.createArg().setValue(canfile);
			}
		} catch (IOException e) {
			throw new ScmException(e.getMessage(), e);
		}
		return command;
	}
}
