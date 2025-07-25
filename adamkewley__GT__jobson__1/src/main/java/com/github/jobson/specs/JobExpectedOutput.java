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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class JobExpectedOutput {

	@JsonProperty
	@NotNull
	private RawTemplateString id;

	@JsonProperty
	@NotNull
	private String path;

	@JsonProperty
	private Optional<String> mimeType = Optional.empty();

	@JsonProperty
	private Optional<String> name = Optional.empty();

	@JsonProperty
	private Optional<String> description = Optional.empty();

	@JsonProperty
	private Map<String, String> metadata = new HashMap<>();

	/**
	 * @deprecated Used by JSON deserializer.
	 */
	public JobExpectedOutput() {
	}

	public JobExpectedOutput(RawTemplateString id, String path, String mimeType) {
		this.id = id;
		this.path = path;
		this.mimeType = Optional.of(mimeType);
	}

	public JobExpectedOutput(RawTemplateString id, String path, String mimeType, Optional<String> name,
			Optional<String> description, Map<String, String> metadata) {
		this.id = id;
		this.path = path;
		this.mimeType = Optional.of(mimeType);
		this.name = name;
		this.description = description;
		this.metadata = metadata;
	}

	public RawTemplateString getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public Optional<String> getMimeType() {
		return mimeType;
	}

	public Optional<String> getName() {
		return name;
	}

	public Optional<String> getDescription() {
		return description;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		JobExpectedOutput jobExpectedOutput = (JobExpectedOutput) o;

		if (path != null ? !path.equals(jobExpectedOutput.path) : jobExpectedOutput.path != null)
			return false;
		if (mimeType != null ? !mimeType.equals(jobExpectedOutput.mimeType) : jobExpectedOutput.mimeType != null)
			return false;
		if (name != null ? !name.equals(jobExpectedOutput.name) : jobExpectedOutput.name != null)
			return false;
		if (description != null ? !description.equals(jobExpectedOutput.description)
				: jobExpectedOutput.description != null)
			return false;
		return metadata != null ? metadata.equals(jobExpectedOutput.metadata) : jobExpectedOutput.metadata == null;
	}

	@Override
	public int hashCode() {
		int result = path != null ? path.hashCode() : 0;
		result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
		return result;
	}
}
