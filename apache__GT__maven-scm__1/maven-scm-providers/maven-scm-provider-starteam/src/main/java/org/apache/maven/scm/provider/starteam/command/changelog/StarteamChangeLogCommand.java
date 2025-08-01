package org.apache.maven.scm.provider.starteam.command.changelog;

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
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class StarteamChangeLogCommand extends AbstractChangeLogCommand implements StarteamCommand {
	// ----------------------------------------------------------------------
	// AbstractChangeLogCommand Implementation
	// ----------------------------------------------------------------------

	/** {@inheritDoc} */
	protected ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repo, ScmFileSet fileSet, Date startDate,
			Date endDate, ScmBranch branch, String datePattern) throws ScmException {
		if ((branch != null || StringUtils.isNotEmpty((branch == null) ? null : branch.getName()))
				&& (getLogger().isWarnEnabled())) {
			getLogger().warn("This provider doesn't support changelog with on a given branch.");
		}

		StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

		// TODO: revision
		Commandline cl = createCommandLine(repository, fileSet, startDate);

		StarteamChangeLogConsumer consumer = new StarteamChangeLogConsumer(fileSet.getBasedir(), getLogger(), startDate,
				endDate, datePattern);

		CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

		int exitCode;

		try {
			exitCode = CommandLineUtils.executeCommandLine(cl, consumer, stderr);
		} catch (CommandLineException ex) {
			throw new ScmException("Error while executing command.", ex);
		}

		if (exitCode != 0) {
			return new ChangeLogScmResult(cl.toString(), "The 'stcmd' command failed.", stderr.getOutput(), false);
		}

		return new ChangeLogScmResult(cl.toString(), new ChangeLogSet(consumer.getModifications(), startDate, endDate));
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	public static Commandline createCommandLine(StarteamScmProviderRepository repo, ScmFileSet workingDirectory,
			Date startDate) {
		//return the full history since we dont know to get hist log from
		//  creation date to a specific date yet
		return StarteamCommandLineUtils.createStarteamCommandLine("hist", null, workingDirectory, repo);

		/**
		 * unfortunately the below option only gives the hist from view creation date to
		 * the specified date.  What good is that?????
		 */

		/*
		if ( startDate != null )
		{
		    SimpleDateFormat localFormat = new SimpleDateFormat();
		
		    cl.createArg().setValue( "-cfgd" );
		
		    cl.createArg().setValue( localFormat.format( startDate ).toString() );
		}
		*/

	}
}
