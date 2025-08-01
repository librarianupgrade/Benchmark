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

public final class JobOutputId {

	private final String jobOutputId;

	@JsonCreator
	public JobOutputId(String jobOutputId) {
		this.jobOutputId = jobOutputId;
	}

	public String getJobOutputId() {
		return jobOutputId;
	}

	@Override
	@JsonValue
	public String toString() {
		return jobOutputId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		JobOutputId that = (JobOutputId) o;

		return jobOutputId != null ? jobOutputId.equals(that.jobOutputId) : that.jobOutputId == null;
	}

	@Override
	public int hashCode() {
		return jobOutputId != null ? jobOutputId.hashCode() : 0;
	}
}
