/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.cache.j2cache;

import com.jfinal.plugin.ehcache.IDataLoader;
import io.jboot.components.cache.JbootCacheBase;
import io.jboot.exception.JbootException;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import net.oschina.j2cache.J2Cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Package io.jboot.core.cache.j2cache
 */
public class J2cacheImpl extends JbootCacheBase {

	@Override
	public <T> T get(String cacheName, Object key) {
		CacheObject cacheObject = J2Cache.getChannel().get(cacheName, key.toString(), false);
		return cacheObject != null ? (T) cacheObject.getValue() : null;
	}

	@Override
	public void put(String cacheName, Object key, Object value) {
		J2Cache.getChannel().set(cacheName, key.toString(), value);
	}

	@Override
	public void put(String cacheName, Object key, Object value, int liveSeconds) {
		J2Cache.getChannel().set(cacheName, key.toString(), value, liveSeconds);
	}

	@Override
	public void remove(String cacheName, Object key) {
		J2Cache.getChannel().evict(cacheName, key.toString());
	}

	@Override
	public void removeAll(String cacheName) {
		J2Cache.getChannel().clear(cacheName);
	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
		Object value = get(cacheName, key);
		if (value == null) {
			value = dataLoader.load();
			if (value != null) {
				put(cacheName, key, value);
			}
		}
		return (T) value;
	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader, int liveSeconds) {
		if (liveSeconds <= 0) {
			return get(cacheName, key, dataLoader);
		}

		Object data = get(cacheName, key);
		if (data == null) {
			data = dataLoader.load();
			put(cacheName, key, data, liveSeconds);
		}
		return (T) data;
	}

	private Method sendEvictCmdMethod;

	@Override
	public void refresh(String cacheName, Object key) {
		if (sendEvictCmdMethod == null) {
			synchronized (this) {
				if (sendEvictCmdMethod == null) {
					sendEvictCmdMethod = getSendEvictCmdMethod();
				}
			}
		}
		try {
			sendEvictCmdMethod.invoke(J2Cache.getChannel(), cacheName, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Method sendClearCmdMethod;

	@Override
	public void refresh(String cacheName) {
		if (sendClearCmdMethod == null) {
			synchronized (this) {
				if (sendClearCmdMethod == null) {
					sendClearCmdMethod = getSendClearCmdMethod();
				}
			}
		}
		try {
			sendClearCmdMethod.invoke(J2Cache.getChannel(), cacheName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List getNames() {
		Collection<CacheChannel.Region> regions = J2Cache.getChannel().getL1Provider().regions();
		return regions != null && !regions.isEmpty()
				? regions.stream().map(CacheChannel.Region::getName).collect(Collectors.toList())
				: null;
	}

	@Override
	public List getKeys(String cacheName) {
		Collection keys = J2Cache.getChannel().keys(cacheName);
		return keys != null ? new ArrayList(keys) : null;
	}

	private Method getSendEvictCmdMethod() {
		try {
			Method method = CacheChannel.class.getDeclaredMethod("sendEvictCmd", String.class, String[].class);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getSendClearCmdMethod() {
		try {
			Method method = CacheChannel.class.getDeclaredMethod("sendClearCmd", String.class);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Integer getTtl(String cacheName, Object key) {
		throw new JbootException("getTtl not support in j2cache");
	}

	@Override
	public void setTtl(String cacheName, Object key, int seconds) {
		throw new JbootException("setTtl not support in j2cache");
	}
}
