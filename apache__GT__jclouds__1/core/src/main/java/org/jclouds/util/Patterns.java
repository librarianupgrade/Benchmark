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
package org.jclouds.util;

import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Patterns {

	public static final Pattern NEWLINE_PATTERN = Pattern.compile("\r?\n");

	public static final LoadingCache<String, Pattern> TOKEN_TO_PATTERN = CacheBuilder.newBuilder()
			.<String, Pattern>build(new CacheLoader<String, Pattern>() {
				@Override
				public Pattern load(String tokenValue) {
					return Pattern.compile("\\{" + tokenValue + "\\}");
				}
			});
}
