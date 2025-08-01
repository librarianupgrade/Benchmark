/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.jobson.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public final class ApplicationConfig extends Configuration {

	@JsonProperty
	@NotNull
	@Valid
	private SpecsConfig specs;

	@JsonProperty
	@NotNull
	@Valid
	private JobsConfig jobs;

	@JsonProperty
	@NotNull
	@Valid
	private WorkingDirsConfig workingDirs;

	@JsonProperty
	@NotNull
	@Valid
	private UsersConfig users;

	@JsonProperty
	@NotNull
	@Valid
	private AuthenticationConfig authentication;

	@JsonProperty
	@Valid
	private ExecutionConfig execution = new ExecutionConfig();

	public SpecsConfig getJobSpecConfiguration() {
		return specs;
	}

	public JobsConfig getJobDataConfiguration() {
		return jobs;
	}

	public AuthenticationConfig getAuthenticationConfiguration() {
		return authentication;
	}

	public UsersConfig getUsersConfiguration() {
		return users;
	}

	public WorkingDirsConfig getWorkingDirs() {
		return workingDirs;
	}

	public ExecutionConfig getExecution() {
		return execution;
	}
}
