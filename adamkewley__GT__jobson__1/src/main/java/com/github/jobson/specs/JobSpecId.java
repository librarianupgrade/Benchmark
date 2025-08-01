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

package com.github.jobson.specs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "A unique identifier for a job spec")
public final class JobSpecId {

	private final String jobSpecId;

	@JsonCreator
	public JobSpecId(String jobSpecId) {
		this.jobSpecId = jobSpecId;
	}

	@Override
	@JsonValue
	public String toString() {
		return this.jobSpecId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		JobSpecId jobSpecId1 = (JobSpecId) o;

		return jobSpecId != null ? jobSpecId.equals(jobSpecId1.jobSpecId) : jobSpecId1.jobSpecId == null;

	}

	@Override
	public int hashCode() {
		return jobSpecId != null ? jobSpecId.hashCode() : 0;
	}
}
