package org.apache.maven.scm.command.list;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * Result of {@link org.apache.maven.scm.provider.ScmProvider#list(org.apache.maven.scm.repository.ScmRepository,
 * org.apache.maven.scm.ScmFileSet, boolean, org.apache.maven.scm.ScmVersion)} operation
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 *
 */
public class ListScmResult extends ScmResult {

	private static final long serialVersionUID = 5402161066844465281L;
	private List<ScmFile> files;

	public ListScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
		super(commandLine, providerMessage, commandOutput, success);

		files = new ArrayList<ScmFile>(0);
	}

	public ListScmResult(String commandLine, List<ScmFile> files) {
		super(commandLine, null, null, true);

		this.files = files;
	}

	public ListScmResult(List<ScmFile> files, ScmResult result) {
		super(result);

		this.files = files;
	}

	public List<ScmFile> getFiles() {
		return files;
	}

}
