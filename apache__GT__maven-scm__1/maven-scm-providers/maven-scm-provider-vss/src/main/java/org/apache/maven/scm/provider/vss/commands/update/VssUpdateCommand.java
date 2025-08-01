package org.apache.maven.scm.provider.vss.commands.update;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.commands.changelog.VssHistoryCommand;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 *
 */
public class VssUpdateCommand extends AbstractUpdateCommand {
	// TODO handle deleted files from VSS
	/** {@inheritDoc} */
	protected UpdateScmResult executeUpdateCommand(ScmProviderRepository repository, ScmFileSet fileSet,
			ScmVersion version) throws ScmException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("executing update command...");
		}

		VssScmProviderRepository repo = (VssScmProviderRepository) repository;

		Commandline cl = buildCmdLine(repo, fileSet, version);

		VssUpdateConsumer consumer = new VssUpdateConsumer(repo, getLogger());

		// TODO handle deleted files from VSS
		// TODO identify local files
		CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

		int exitCode;

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString());
		}

		exitCode = VssCommandLineUtils.executeCommandline(cl, consumer, stderr, getLogger());

		if (exitCode != 0) {
			String error = stderr.getOutput();

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("VSS returns error: [" + error + "] return code: [" + exitCode + "]");
			}
			if (error.indexOf("A writable copy of") < 0) {
				return new UpdateScmResult(cl.toString(), "The vss command failed.", error, false);
			}
			// print out the writable copy for manual handling
			if (getLogger().isWarnEnabled()) {
				getLogger().warn(error);
			}
		}

		return new UpdateScmResult(cl.toString(), consumer.getUpdatedFiles());
	}

	public Commandline buildCmdLine(VssScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version)
			throws ScmException {

		Commandline command = new Commandline();

		command.setWorkingDirectory(fileSet.getBasedir().getAbsolutePath());

		try {
			command.addSystemEnvironment();
		} catch (Exception e) {
			throw new ScmException("Can't add system environment.", e);
		}

		command.addEnvironment("SSDIR", repo.getVssdir());

		String ssDir = VssCommandLineUtils.getSsDir();

		command.setExecutable(ssDir + VssConstants.SS_EXE);

		command.createArg().setValue(VssConstants.COMMAND_GET);

		command.createArg().setValue(VssConstants.PROJECT_PREFIX + repo.getProject());

		//User identification to get access to vss repository
		if (repo.getUserPassword() != null) {
			command.createArg().setValue(VssConstants.FLAG_LOGIN + repo.getUserPassword());
		}

		//Display the history of an entire project list
		command.createArg().setValue(VssConstants.FLAG_RECURSION);

		//Ignore: Do not ask for input under any circumstances.
		command.createArg().setValue(VssConstants.FLAG_AUTORESPONSE_DEF);

		// FIXME Update command only works if there is no file checked out
		// or no file is dirty locally. It's better than overwriting
		// checked out files
		//Ignore: Do not touch local writable files.
		command.createArg().setValue(VssConstants.FLAG_SKIP_WRITABLE);

		if (version != null) {
			command.createArg().setValue(VssConstants.FLAG_VERSION_LABEL + version);
		}

		return command;
	}

	/** {@inheritDoc} */
	protected ChangeLogCommand getChangeLogCommand() {
		VssHistoryCommand command = new VssHistoryCommand();

		command.setLogger(getLogger());

		return command;
	}

}
