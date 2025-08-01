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
package org.jclouds.osgi;

import java.util.Set;

import org.jclouds.apis.ApiMetadata;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

/**
 * A registry for holding {@link org.jclouds.apis.ApiMetadata}.
 */
@Beta
public class ApiRegistry {

	private static final Set<ApiMetadata> apis = Sets.newHashSet();

	public static void registerApi(ApiMetadata api) {
		apis.add(api);
	}

	public static void unRegisterApi(ApiMetadata api) {
		apis.remove(api);
	}

	public static Iterable<ApiMetadata> fromRegistry() {
		return apis;
	}

	public static void clear() {
		apis.clear();
	}
}
