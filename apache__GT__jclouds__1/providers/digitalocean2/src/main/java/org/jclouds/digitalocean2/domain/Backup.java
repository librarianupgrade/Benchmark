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

package org.jclouds.digitalocean2.domain;

import java.util.List;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Backup {
	public abstract int id();

	public abstract String name();

	public abstract String distribution();

	@Nullable
	public abstract String slug();

	public abstract boolean isPublic();

	public abstract List<String> regions();

	public abstract int minDiskSize();

	@SerializedNames({ "id", "name", "distribution", "slug", "public", "regions", "min_disk_size" })
	public static Backup create(int id, String name, String distribution, String slug, boolean isPublic,
			List<String> regions, int minDiskSize) {
		return new AutoValue_Backup(id, name, distribution, slug, isPublic, regions, minDiskSize);
	}

	Backup() {
	}
}
