package org.apache.maven.scm.provider.perforce.command.remove;

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
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.List;

/**
 * @author Mike Perham
 * @author Olivier Lamy
 *
 */
public class PerforceRemoveCommand extends AbstractRemoveCommand implements PerforceCommand {
	/**
	 * {@inheritDoc}
	 */
	protected ScmResult executeRemoveCommand(ScmProviderRepository repo, ScmFileSet files, String message)
			throws ScmException {
		Commandline cl = createCommandLine((PerforceScmProviderRepository) repo, files.getBasedir(), files);
		PerforceRemoveConsumer consumer = new PerforceRemoveConsumer();
		try {
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
			throw new ScmException("CommandLineException " + e.getMessage(), e);

		}

		return new RemoveScmResult(cl.toString(), consumer.getRemovals());
	}

	public static Commandline createCommandLine(PerforceScmProviderRepository repo, File workingDirectory,
			ScmFileSet files) {
		Commandline command = PerforceScmProvider.createP4Command(repo, workingDirectory);
		command.createArg().setValue("delete");

		List<File> fs = files.getFileList();
		for (int i = 0; i < fs.size(); i++) {
			File file = (File) fs.get(i);
			command.createArg().setValue(file.getName());
		}
		return command;
	}
}