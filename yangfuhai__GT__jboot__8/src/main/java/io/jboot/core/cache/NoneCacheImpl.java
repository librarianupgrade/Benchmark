/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.core.cache;

import com.jfinal.plugin.ehcache.IDataLoader;

import java.util.List;

public class NoneCacheImpl extends JbootCacheBase {
	@Override
	public List getKeys(String cacheName) {
		return null;
	}

	@Override
	public <T> T get(String cacheName, Object key) {
		return null;
	}

	@Override
	public void put(String cacheName, Object key, Object value) {

	}

	@Override
	public void put(String cacheName, Object key, Object value, int liveSeconds) {

	}

	@Override
	public void remove(String cacheName, Object key) {

	}

	@Override
	public void removeAll(String cacheName) {

	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
		return (T) dataLoader.load();
	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader, int liveSeconds) {
		return (T) dataLoader.load();
	}

	@Override
	public boolean isNoneCache() {
		return true;
	}
}
