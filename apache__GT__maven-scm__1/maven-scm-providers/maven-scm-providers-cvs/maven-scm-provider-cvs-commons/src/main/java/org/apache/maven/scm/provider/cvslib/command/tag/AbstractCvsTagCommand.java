package org.apache.maven.scm.provider.cvslib.command.tag;

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
import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.util.CvsUtil;
import org.apache.maven.scm.providers.cvslib.settings.Settings;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public abstract class AbstractCvsTagCommand extends AbstractTagCommand implements CvsCommand {

	public ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message)
			throws ScmException {
		return executeTagCommand(repo, fileSet, tag, new ScmTagParameters(message));
	}

	/** {@inheritDoc} */
	public ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag,
			ScmTagParameters scmTagParameters) throws ScmException {
		CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

		Commandline cl = CvsCommandUtils.getBaseCommand("tag", repository, fileSet, false);

		Settings settings = CvsUtil.getSettings();
		if (settings.isUseForceTag()) {
			cl.createArg().setValue("-F");
		}

		cl.createArg().setValue("-c");

		cl.createArg().setValue(tag);

		if (fileSet.getFileList() != null && !fileSet.getFileList().isEmpty()) {
			for (Iterator<File> it = fileSet.getFileList().iterator(); it.hasNext();) {
				File fileName = it.next();
				cl.createArg().setValue(fileName.toString());
			}
		}

		if (getLogger().isInfoEnabled()) {
			getLogger().info("Executing: " + cl);
			getLogger().info("Working directory: " + cl.getWorkingDirectory().getAbsolutePath());
		}

		return executeCvsCommand(cl);
	}

	protected abstract TagScmResult executeCvsCommand(Commandline cl) throws ScmException;
}
