/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.compute.events;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.scriptbuilder.InitScript;

import com.google.common.annotations.Beta;

/**
 * An init script was submitted to a node for execution.
 * <p/>
 * Note this does not guarantee that there was success, nor that the init script started.
 */
@Beta
public class InitScriptOnNodeSubmission extends StatementOnNodeSubmission {

	public InitScriptOnNodeSubmission(InitScript statement, NodeMetadata node) {
		super(statement, node);
	}

	public InitScript getStatement() {
		return InitScript.class.cast(statement);
	}

}
