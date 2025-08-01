package org.apache.maven.scm.command.changelog;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.Date;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public abstract class AbstractChangeLogCommand extends AbstractCommand implements ChangeLogCommand {
	@Deprecated
	protected abstract ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, ScmFileSet fileSet,
			Date startDate, Date endDate, ScmBranch branch, String datePattern) throws ScmException;

	@Deprecated
	protected ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, ScmFileSet fileSet,
			ScmVersion startVersion, ScmVersion endVersion, String datePattern) throws ScmException {
		throw new ScmException("Unsupported method for this provider.");
	}

	@Deprecated
	protected ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, ScmFileSet fileSet,
			ScmVersion version, String datePattern) throws ScmException {
		throw new ScmException("Unsupported method for this provider.");
	}

	/**
	 * {@inheritDoc}
	 */
	public ScmResult executeCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
			throws ScmException {
		Date startDate = parameters.getDate(CommandParameter.START_DATE, null);

		Date endDate = parameters.getDate(CommandParameter.END_DATE, null);

		int numDays = parameters.getInt(CommandParameter.NUM_DAYS, 0);

		Integer limit = parameters.getInt(CommandParameter.LIMIT, -1);
		if (limit < 1) {
			limit = null;
		}

		ScmBranch branch = (ScmBranch) parameters.getScmVersion(CommandParameter.BRANCH, null);

		ScmVersion version = parameters.getScmVersion(CommandParameter.SCM_VERSION, null);

		ScmVersion startVersion = parameters.getScmVersion(CommandParameter.START_SCM_VERSION, null);

		ScmVersion endVersion = parameters.getScmVersion(CommandParameter.END_SCM_VERSION, null);

		String datePattern = parameters.getString(CommandParameter.CHANGELOG_DATE_PATTERN, null);

		boolean versionOnly = startVersion == null && endVersion == null && version != null;

		if (versionOnly) {
			return executeChangeLogCommand(repository, fileSet, version, datePattern);
		} else if (startVersion != null || endVersion != null) {
			return executeChangeLogCommand(repository, fileSet, startVersion, endVersion, datePattern);
		} else {
			if (numDays != 0 && (startDate != null || endDate != null)) {
				throw new ScmException("Start or end date cannot be set if num days is set.");
			}

			if (endDate != null && startDate == null) {
				throw new ScmException("The end date is set but the start date isn't.");
			}

			if (numDays > 0) {
				@SuppressWarnings("checkstyle:magicnumber")
				int day = 24 * 60 * 60 * 1000;
				startDate = new Date(System.currentTimeMillis() - (long) numDays * day);

				endDate = new Date(System.currentTimeMillis() + (long) day);
			} else if (endDate == null) {
				endDate = new Date();
			}

			return executeChangeLogCommand(repository, fileSet, startDate, endDate, branch, datePattern);
		}
	}

	protected ChangeLogScmResult executeChangeLogCommand(ChangeLogScmRequest request) throws ScmException {
		throw new ScmException("Unsupported method for this provider.");
	}
}
