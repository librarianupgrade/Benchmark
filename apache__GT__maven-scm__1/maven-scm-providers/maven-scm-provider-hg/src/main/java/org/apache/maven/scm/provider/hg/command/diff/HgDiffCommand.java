package org.apache.maven.scm.provider.hg.command.diff;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 *
 */
public class HgDiffCommand extends AbstractDiffCommand implements Command {
	/** {@inheritDoc} */
	protected DiffScmResult executeDiffCommand(ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion startRevision,
			ScmVersion endRevision) throws ScmException {
		String[] diffCmd;
		if (startRevision != null && !StringUtils.isEmpty(startRevision.getName())) {
			String revArg = startRevision.getName();
			if (endRevision != null && !StringUtils.isEmpty(endRevision.getName())) {
				revArg += ".." + endRevision;
			}
			diffCmd = new String[] { HgCommandConstants.DIFF_CMD, HgCommandConstants.REVISION_OPTION, revArg };
		} else {
			diffCmd = new String[] { HgCommandConstants.DIFF_CMD };
		}

		diffCmd = HgUtils.expandCommandLine(diffCmd, fileSet);
		HgDiffConsumer consumer = new HgDiffConsumer(getLogger(), fileSet.getBasedir());

		ScmResult result = HgUtils.execute(consumer, getLogger(), fileSet.getBasedir(), diffCmd);

		return new DiffScmResult(consumer.getChangedFiles(), consumer.getDifferences(), consumer.getPatch(), result);
	}
}
