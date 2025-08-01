package org.apache.maven.scm.command.branch;

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

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class BranchScmResult extends ScmResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4241972929129557932L;
	private List<ScmFile> branchedFiles;

	public BranchScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
		super(commandLine, providerMessage, commandOutput, success);
	}

	public BranchScmResult(String commandLine, List<ScmFile> branchedFiles) {
		super(commandLine, null, null, true);

		this.branchedFiles = branchedFiles;
	}

	public BranchScmResult(List<ScmFile> branchedFiles, ScmResult result) {
		super(result);

		this.branchedFiles = branchedFiles;
	}

	public List<ScmFile> getBranchedFiles() {
		return branchedFiles;
	}
}
