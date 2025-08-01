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
package org.jclouds.chef.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.chef.util.CollectionUtils.copyOfOrEmpty;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jclouds.domain.JsonBall;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;

/**
 * Role object.
 */
public class Role {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String name;
		private String description;
		private ImmutableMap.Builder<String, JsonBall> overrideAttributes = ImmutableMap.builder();
		private ImmutableMap.Builder<String, JsonBall> defaultAttributes = ImmutableMap.builder();
		private ImmutableList.Builder<String> runList = ImmutableList.builder();
		// envRunList is a nested set of collections. The Immutable* classes in google collections don't appear to
		// support this nested immutability, so the builder will utilize native collections as the envRunList is
		// assembled. An immutable, nested map of collections will be assembled in the build() method.
		private Map<String, List<String>> envRunList = new HashMap<String, List<String>>();

		public Builder name(String name) {
			this.name = checkNotNull(name, "name");
			return this;
		}

		public Builder description(String description) {
			this.description = checkNotNull(description, "description");
			return this;
		}

		public Builder overrideAttribute(String key, JsonBall value) {
			this.overrideAttributes.put(checkNotNull(key, "key"), checkNotNull(value, "value"));
			return this;
		}

		public Builder overrideAttributes(Map<String, JsonBall> overrideAttributes) {
			this.overrideAttributes.putAll(checkNotNull(overrideAttributes, "overrideAttributes"));
			return this;
		}

		public Builder defaultAttribute(String key, JsonBall value) {
			this.defaultAttributes.put(checkNotNull(key, "key"), checkNotNull(value, "value"));
			return this;
		}

		public Builder defaultAttributes(Map<String, JsonBall> defaultAttributes) {
			this.defaultAttributes.putAll(checkNotNull(defaultAttributes, "defaultAttributes"));
			return this;
		}

		public Builder runListElement(String element) {
			this.runList.add(checkNotNull(element, "element"));
			return this;
		}

		public Builder runList(Iterable<String> runList) {
			this.runList.addAll(checkNotNull(runList, "runList"));
			return this;
		}

		public Builder envRunList(Map<String, List<String>> envRunList) {
			this.envRunList.putAll(checkNotNull(envRunList, "envRunList"));
			return this;
		}

		public Builder envRunList(String name, List<String> runList) {
			this.envRunList.put(checkNotNull(name, "name"), checkNotNull(runList, "runList"));
			return this;
		}

		public Builder envRunListElement(String name, String value) {
			checkNotNull(name, "name");
			checkNotNull(value, "value");
			List<String> runList = this.envRunList.get(name);
			if (runList == null) {
				runList = new ArrayList<String>();
				this.envRunList.put(name, runList);
			}
			runList.add(value);
			return this;
		}

		public Role build() {
			// Assemble an immutable envRunList where each entry is an immutable list of entries.
			Map<String, List<String>> immutableEnvRunList = Maps.transformValues(envRunList,
					new Function<List<String>, List<String>>() {
						@Override
						public List<String> apply(List<String> input) {
							return ImmutableList.copyOf(input);
						}
					});

			return new Role(name, description, defaultAttributes.build(), runList.build(), overrideAttributes.build(),
					immutableEnvRunList);
		}
	}

	private final String name;
	private final String description;
	@SerializedName("override_attributes")
	private final Map<String, JsonBall> overrideAttributes;
	@SerializedName("default_attributes")
	private final Map<String, JsonBall> defaultAttributes;
	@SerializedName("run_list")
	private final List<String> runList;
	@SerializedName("env_run_lists")
	private Map<String, List<String>> envRunList;

	// internal
	@SerializedName("json_class")
	private final String _jsonClass = "Chef::Role";
	@SerializedName("chef_type")
	private final String _chefType = "role";

	@ConstructorProperties({ "name", "description", "default_attributes", "run_list", "override_attributes",
			"env_run_lists" })
	protected Role(String name, String description, @Nullable Map<String, JsonBall> defaultAttributes,
			@Nullable List<String> runList, @Nullable Map<String, JsonBall> overrideAttributes,
			@Nullable Map<String, List<String>> envRunList) {
		this.name = name;
		this.description = description;
		this.defaultAttributes = copyOfOrEmpty(defaultAttributes);
		this.runList = copyOfOrEmpty(runList);
		this.overrideAttributes = copyOfOrEmpty(overrideAttributes);
		this.envRunList = envRunList;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, JsonBall> getOverrideAttributes() {
		return overrideAttributes;
	}

	public Map<String, JsonBall> getDefaultAttributes() {
		return defaultAttributes;
	}

	public List<String> getRunList() {
		return runList;
	}

	public Map<String, List<String>> getEnvRunList() {
		return envRunList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_chefType == null) ? 0 : _chefType.hashCode());
		result = prime * result + ((_jsonClass == null) ? 0 : _jsonClass.hashCode());
		result = prime * result + ((defaultAttributes == null) ? 0 : defaultAttributes.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((overrideAttributes == null) ? 0 : overrideAttributes.hashCode());
		result = prime * result + ((runList == null) ? 0 : runList.hashCode());
		result = prime * result + ((envRunList == null) ? 0 : envRunList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (_chefType == null) {
			if (other._chefType != null)
				return false;
		} else if (!_chefType.equals(other._chefType))
			return false;
		if (_jsonClass == null) {
			if (other._jsonClass != null)
				return false;
		} else if (!_jsonClass.equals(other._jsonClass))
			return false;
		if (defaultAttributes == null) {
			if (other.defaultAttributes != null)
				return false;
		} else if (!defaultAttributes.equals(other.defaultAttributes))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (overrideAttributes == null) {
			if (other.overrideAttributes != null)
				return false;
		} else if (!overrideAttributes.equals(other.overrideAttributes))
			return false;
		if (runList == null) {
			if (other.runList != null)
				return false;
		} else if (!runList.equals(other.runList))
			return false;
		if (envRunList == null) {
			if (other.envRunList != null)
				return false;
		} else if (!envRunList.equals(other.envRunList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Role [name=" + name + ", description=" + description + ", defaultAttributes=" + defaultAttributes
				+ ", overrideAttributes=" + overrideAttributes + ", runList=" + runList + ", envRunList="
				+ this.envRunList + "]";
	}

}
