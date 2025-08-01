package org.apache.maven.scm.provider.starteam.command.tag;

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
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @author Olivier Lamy
 *
 */
public class StarteamTagCommand extends AbstractTagCommand implements StarteamCommand {
	// ----------------------------------------------------------------------
	// AbstractTagCommand Implementation
	// ----------------------------------------------------------------------

	protected ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message)
			throws ScmException {
		return executeTagCommand(repo, fileSet, tag, new ScmTagParameters(message));
	}

	/** {@inheritDoc} */
	protected ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag,
			ScmTagParameters scmTagParameters) throws ScmException {
		if (!fileSet.getFileList().isEmpty()) {
			throw new ScmException("This provider doesn't support tagging subsets of a directory");
		}

		if (tag == null || tag.trim().length() == 0) {
			throw new ScmException("tag must be specified");
		}

		if (getLogger().isInfoEnabled()) {
			getLogger().info("Working directory: " + fileSet.getBasedir().getAbsolutePath());
		}

		StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

		StarteamTagConsumer consumer = new StarteamTagConsumer(getLogger());

		CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

		Commandline cl = createCommandLine(repository, fileSet.getBasedir(), tag);

		int exitCode = StarteamCommandLineUtils.executeCommandline(cl, consumer, stderr, getLogger());

		if (exitCode != 0) {
			return new TagScmResult(cl.toString(), "The starteam command failed.", stderr.getOutput(), false);
		}

		return new TagScmResult(cl.toString(), consumer.getTaggedFiles());
	}

	// ----------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------

	public static Commandline createCommandLine(StarteamScmProviderRepository repo, File workingDirectory, String tag)
			throws ScmException {
		Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine("label", repo);

		cl.createArg().setValue("-p");

		cl.createArg().setValue(repo.getFullUrl());

		cl.createArg().setValue("-nl");

		cl.createArg().setValue(tag);

		cl.createArg().setValue("-b");

		return cl;
	}
}
