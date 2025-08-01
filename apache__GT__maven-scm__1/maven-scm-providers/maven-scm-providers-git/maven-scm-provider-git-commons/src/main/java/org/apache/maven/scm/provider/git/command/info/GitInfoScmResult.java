package org.apache.maven.scm.provider.git.command.info;

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

import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.info.InfoScmResult;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public class GitInfoScmResult extends InfoScmResult {

	private static final long serialVersionUID = -1314905338508176675L;

	public GitInfoScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
		super(commandLine, providerMessage, commandOutput, success);
	}

	public GitInfoScmResult(String commandLine, List<GitInfoItem> files) {
		super(commandLine, null, null, true);

		if (files != null) {
			getInfoItems().addAll(files);
		}
	}

	public GitInfoScmResult(List<GitInfoItem> files, ScmResult result) {
		super(result);

		if (files != null) {
			getInfoItems().addAll(files);
		}
	}

}
