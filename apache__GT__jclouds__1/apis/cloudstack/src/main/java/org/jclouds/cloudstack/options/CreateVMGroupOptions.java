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
package org.jclouds.cloudstack.options;

import org.jclouds.http.options.BaseHttpRequestOptions;

import com.google.common.collect.ImmutableSet;

/**
 * Options used to control options for creating a VM group
 *
 * @see <a
 *      href="http://download.cloud.com/releases/2.2.8/api/user/createInstanceGroup.html"
 *      />
 */
public class CreateVMGroupOptions extends BaseHttpRequestOptions {

	public static final CreateVMGroupOptions NONE = new CreateVMGroupOptions();

	/**
	* @param account account who owns the VMGroup
	*/
	public CreateVMGroupOptions account(String account) {
		this.queryParameters.replaceValues("account", ImmutableSet.of(account));
		return this;
	}

	/**
	* @param domainId domain ID of the account owning the VMGroup
	*/
	public CreateVMGroupOptions domainId(String domainId) {
		this.queryParameters.replaceValues("domainid", ImmutableSet.of(domainId + ""));
		return this;
	}

	/**
	* @param projectId the project the vm group will be in
	*/
	public CreateVMGroupOptions projectId(String projectId) {
		this.queryParameters.replaceValues("projectid", ImmutableSet.of(projectId + ""));
		return this;
	}

	public static class Builder {
		/**
		 * @see org.jclouds.cloudstack.options.CreateVMGroupOptions#account
		 */
		public static CreateVMGroupOptions account(String account) {
			CreateVMGroupOptions options = new CreateVMGroupOptions();
			return options.account(account);
		}

		/**
		 * @see org.jclouds.cloudstack.options.CreateVMGroupOptions#domainId
		 */
		public static CreateVMGroupOptions domainId(String id) {
			CreateVMGroupOptions options = new CreateVMGroupOptions();
			return options.domainId(id);
		}

		/**
		 * @see org.jclouds.cloudstack.options.CreateVMGroupOptions#projectId(String)
		 */
		public static CreateVMGroupOptions projectId(String id) {
			CreateVMGroupOptions options = new CreateVMGroupOptions();
			return options.projectId(id);
		}
	}

}
