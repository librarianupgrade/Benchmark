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

package org.apache.servicecomb.governance.service;

import com.google.common.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GovernanceCacheImpl<K, V> implements GovernanceCache<K, V> {

	private static final Logger LOG = LoggerFactory.getLogger(GovernanceCacheImpl.class);

	private final Cache<K, V> cache;

	public GovernanceCacheImpl(Cache<K, V> cache) {
		this.cache = cache;
	}

	public V getValueFromCache(K cacheKey) {
		try {
			return cache.getIfPresent(cacheKey);
		} catch (Exception exception) {
			LOG.warn("Failed to get a value from Cache", exception);
			return null;
		}
	}

	@Override
	public void putValueIntoCache(K cacheKey, V value) {
		try {
			cache.put(cacheKey, value);
		} catch (Exception exception) {
			LOG.warn("Failed to put a value into Cache {}", exception);
		}
	}
}
