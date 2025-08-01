package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dump changelog contents to console. It is mainly used to test maven-scm-api's changelog command.
 *
 * @author <a href="dantran@gmail.com">Dan Tran</a>
 * @author Olivier Lamy
 */
@Mojo(name = "changelog", aggregator = true)
public class ChangeLogMojo extends AbstractScmMojo {
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * Start Date.
	 */
	@Parameter(property = "startDate")
	private String startDate;

	/**
	 * End Date.
	 */
	@Parameter(property = "endDate")
	private String endDate;

	/**
	 * Start Scm Version.
	 */
	@Parameter(property = "startScmVersion")
	private String startScmVersion;

	/**
	 * End Scm Version.
	 */
	@Parameter(property = "endScmVersion")
	private String endScmVersion;

	/**
	 * Start Scm Version Type.
	 */
	@Parameter(property = "startScmVersionType")
	private String startScmVersionType;

	/**
	 * End Scm Version Type.
	 */
	@Parameter(property = "endScmVersionType")
	private String endScmVersionType;

	/**
	 * Date Format in changelog output of scm tool.
	 */
	@Parameter(property = "dateFormat")
	private String dateFormat;

	/**
	 * Date format to use for the specified startDate and/or endDate.
	 */
	@Parameter(property = "userDateFormat", defaultValue = "yyyy-MM-dd")
	private String userDateFormat = DEFAULT_DATE_FORMAT;

	/**
	 * The version type (branch/tag) of scmVersion.
	 */
	@Parameter(property = "scmVersionType")
	private String scmVersionType;

	/**
	 * The version (revision number/branch name/tag name).
	 */
	@Parameter(property = "scmVersion")
	private String scmVersion;

	/**
	 * {@inheritDoc}
	 */
	public void execute() throws MojoExecutionException {
		super.execute();

		SimpleDateFormat localFormat = new SimpleDateFormat(userDateFormat);

		try {
			ScmRepository repository = getScmRepository();

			ScmProvider provider = getScmManager().getProviderByRepository(repository);

			ScmVersion startRev = getScmVersion(
					StringUtils.isEmpty(startScmVersionType) ? "revision" : startScmVersionType, startScmVersion);
			ScmVersion endRev = getScmVersion(StringUtils.isEmpty(endScmVersionType) ? "revision" : endScmVersionType,
					endScmVersion);

			ChangeLogScmResult result;
			if (startRev != null || endRev != null) {
				result = provider.changeLog(repository, getFileSet(), startRev, endRev, dateFormat);
			} else {
				result = provider.changeLog(repository, getFileSet(), this.parseDate(localFormat, this.startDate),
						this.parseDate(localFormat, this.endDate), 0,
						(ScmBranch) getScmVersion(scmVersionType, scmVersion), dateFormat);
			}
			checkResult(result);

			ChangeLogSet changeLogSet = result.getChangeLog();

			for (ChangeSet changeSet : changeLogSet.getChangeSets()) {
				getLog().info(changeSet.toString());
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Cannot run changelog command : ", e);
		} catch (ScmException e) {
			throw new MojoExecutionException("Cannot run changelog command : ", e);
		}
	}

	/**
	 * Converts the localized date string pattern to date object.
	 *
	 * @return A date
	 */
	private Date parseDate(SimpleDateFormat format, String date) throws MojoExecutionException {
		if (date == null || date.trim().length() == 0) {
			return null;
		}

		try {
			return format.parse(date.toString());
		} catch (ParseException e) {
			throw new MojoExecutionException("Please use this date pattern: " + format.toLocalizedPattern().toString(),
					e);
		}
	}
}
