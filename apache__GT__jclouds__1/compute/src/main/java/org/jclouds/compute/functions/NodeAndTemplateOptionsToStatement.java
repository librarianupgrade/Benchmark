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
package org.jclouds.compute.functions;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.inject.ImplementedBy;

/**
 * Returns the statement to be executed on the node.
 */
@ImplementedBy(InstallKeysAndRunScript.class)
public interface NodeAndTemplateOptionsToStatement {

	/**
	* Returns the script that has to be executed in the given node.
	* 
	* @return The script to be executed or <code>null</code> if no script needs
	*         to be run.
	*/
	@Nullable
	Statement apply(NodeMetadata node, TemplateOptions options);
}
