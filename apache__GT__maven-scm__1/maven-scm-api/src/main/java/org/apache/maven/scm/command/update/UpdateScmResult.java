package org.apache.maven.scm.command.update;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class UpdateScmResult extends ScmResult {

	private static final long serialVersionUID = 1L;

	private List<ScmFile> updatedFiles;

	private List<ChangeSet> changes;

	public UpdateScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
		super(commandLine, providerMessage, commandOutput, success);
	}

	public UpdateScmResult(String commandLine, List<ScmFile> updatedFiles) {
		super(commandLine, null, null, true);

		this.updatedFiles = updatedFiles;

	}

	public UpdateScmResult(List<ScmFile> updatedFiles, List<ChangeSet> changes, ScmResult result) {
		super(result);

		this.updatedFiles = updatedFiles;

		this.changes = changes;
	}

	/**
	 * 
	 * @return List of {@link ScmFile}
	 */
	public List<ScmFile> getUpdatedFiles() {
		return updatedFiles;
	}

	/**
	 * @return {@link List} of {@link ChangeSet}
	 */
	public List<ChangeSet> getChanges() {
		if (changes == null) {
			return new ArrayList<ChangeSet>();
		}
		return changes;
	}

	public void setChanges(List<ChangeSet> changes) {
		this.changes = changes;
	}
}
