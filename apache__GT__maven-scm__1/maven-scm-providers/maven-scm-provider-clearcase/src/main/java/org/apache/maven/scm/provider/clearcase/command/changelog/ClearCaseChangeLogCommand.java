package org.apache.maven.scm.provider.clearcase.command.changelog;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.apache.maven.scm.provider.clearcase.util.ClearCaseUtil;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:frederic.mura@laposte.net">Frederic Mura</a>
 * @author <a href="mailto:m.holster@anva.nl">Mark Holster</a>
 * @author Olivier Lamy
 *
 */
public class ClearCaseChangeLogCommand extends AbstractChangeLogCommand implements ClearCaseCommand {
	// ----------------------------------------------------------------------
	// AbstractChangeLogCommand Implementation
	// ----------------------------------------------------------------------

	/** {@inheritDoc} */
	protected ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, ScmFileSet fileSet,
			Date startDate, Date endDate, ScmBranch branch, String datePattern) throws ScmException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("executing changelog command...");
		}
		Commandline cl = createCommandLine(fileSet.getBasedir(), branch, startDate);

		ClearCaseChangeLogConsumer consumer = new ClearCaseChangeLogConsumer(getLogger(), datePattern);

		CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

		int exitCode;

		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString());
			}
			exitCode = CommandLineUtils.executeCommandLine(cl, consumer, stderr);
		} catch (CommandLineException ex) {
			throw new ScmException("Error while executing cvs command.", ex);
		}

		if (exitCode != 0) {
			return new ChangeLogScmResult(cl.toString(), "The cleartool command failed.", stderr.getOutput(), false);
		}

		return new ChangeLogScmResult(cl.toString(), new ChangeLogSet(consumer.getModifications(), startDate, endDate));
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	/**
	 * ClearCase LT version doesn't support the attribut -fmt and -since for command lhistory.
	 *
	 * @param workingDirectory
	 * @param branch
	 * @param startDate
	 * @return The command line
	 */
	public static Commandline createCommandLine(File workingDirectory, ScmBranch branch, Date startDate) {
		Commandline command = new Commandline();
		command.setExecutable("cleartool");
		command.createArg().setValue("lshistory");

		command.setWorkingDirectory(workingDirectory.getAbsolutePath());

		Settings settings = ClearCaseUtil.getSettings();
		String userFormat = StringUtils.isEmpty(settings.getChangelogUserFormat()) ? ""
				: settings.getChangelogUserFormat();

		StringBuilder format = new StringBuilder();
		format.append("NAME:%En\\n");
		format.append("DATE:%Nd\\n");
		format.append("COMM:%-12.12o - %o - %c - Activity: %[activity]p\\n");
		format.append("USER:%" + userFormat + "u\\n");
		format.append("REVI:%Ln\\n");

		command.createArg().setValue("-fmt");
		command.createArg().setValue(format.toString());
		command.createArg().setValue("-recurse");
		command.createArg().setValue("-nco");

		if (startDate != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

			String start = sdf.format(startDate);

			command.createArg().setValue("-since");

			command.createArg().setValue(start);
		}

		// TODO: End date?

		if (branch != null && StringUtils.isNotEmpty(branch.getName())) {
			command.createArg().setValue("-branch");

			command.createArg().setValue(branch.getName());
		}

		return command;
	}
}
