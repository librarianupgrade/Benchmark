/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.core.cache.ehredis;

import com.jfinal.plugin.ehcache.IDataLoader;
import io.jboot.Jboot;
import io.jboot.core.cache.JbootCacheBase;
import io.jboot.core.cache.ehcache.JbootEhcacheImpl;
import io.jboot.core.cache.redis.JbootRedisCacheImpl;
import io.jboot.core.mq.JbootmqMessageListener;
import io.jboot.utils.StringUtils;

import java.util.List;

/**
 * 基于 ehcache和redis做的二级缓存
 * 优点是：减少高并发下redis的io瓶颈
 */
public class JbootEhredisCacheImpl extends JbootCacheBase implements JbootmqMessageListener {

	public static final String DEFAULT_NOTIFY_CHANNEL = "jboot_ehredis_channel";

	private JbootEhcacheImpl ehcacheImpl;
	private JbootRedisCacheImpl redisCacheImpl;

	private String channel = DEFAULT_NOTIFY_CHANNEL;
	private String clientId;

	public JbootEhredisCacheImpl() {
		this.ehcacheImpl = new JbootEhcacheImpl();
		this.redisCacheImpl = new JbootRedisCacheImpl();
		this.clientId = StringUtils.uuid();

		Jboot.me().getMq().addMessageListener(this, channel);
	}

	@Override
	public List getKeys(String cacheName) {
		List list = ehcacheImpl.getKeys(cacheName);
		if (list == null || list.isEmpty()) {
			list = redisCacheImpl.getKeys(cacheName);
		}
		return list;
	}

	@Override
	public <T> T get(String cacheName, Object key) {
		T obj = ehcacheImpl.get(cacheName, key);
		if (obj == null) {
			obj = redisCacheImpl.get(cacheName, key);
			if (obj != null) {
				ehcacheImpl.put(cacheName, key, obj);
			}
		}
		return obj;
	}

	@Override
	public void put(String cacheName, Object key, Object value) {
		try {
			ehcacheImpl.put(cacheName, key, value);
			redisCacheImpl.put(cacheName, key, value);
		} finally {
			publishMessage(JbootEhredisMessage.ACTION_PUT, cacheName, key);
		}
	}

	@Override
	public void put(String cacheName, Object key, Object value, int liveSeconds) {
		try {
			ehcacheImpl.put(cacheName, key, value, liveSeconds);
			redisCacheImpl.put(cacheName, key, value, liveSeconds);
		} finally {
			publishMessage(JbootEhredisMessage.ACTION_PUT, cacheName, key);
		}
	}

	@Override
	public void remove(String cacheName, Object key) {
		try {
			ehcacheImpl.remove(cacheName, key);
			redisCacheImpl.remove(cacheName, key);
		} finally {
			publishMessage(JbootEhredisMessage.ACTION_REMOVE, cacheName, key);
		}
	}

	@Override
	public void removeAll(String cacheName) {
		try {
			ehcacheImpl.removeAll(cacheName);
			redisCacheImpl.removeAll(cacheName);
		} finally {
			publishMessage(JbootEhredisMessage.ACTION_REMOVE_ALL, cacheName, null);
		}
	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
		T obj = get(cacheName, key);
		if (obj != null) {
			return obj;
		}

		obj = (T) dataLoader.load();
		if (obj != null) {
			put(cacheName, key, obj);
		}
		return obj;
	}

	@Override
	public <T> T get(String cacheName, Object key, IDataLoader dataLoader, int liveSeconds) {
		T obj = get(cacheName, key);
		if (obj != null) {
			return obj;
		}

		obj = (T) dataLoader.load();
		if (obj != null) {
			put(cacheName, key, obj, liveSeconds);
		}
		return obj;
	}

	private void publishMessage(int action, String cacheName, Object key) {
		Jboot.me().getMq().publish(new JbootEhredisMessage(clientId, action, cacheName, key), channel);
	}

	@Override
	public void onMessage(String channel, Object obj) {

		JbootEhredisMessage message = (JbootEhredisMessage) obj;
		/**
		 * 不处理自己发送的消息
		 */
		if (clientId.equals(message.getClientId())) {
			return;
		}

		switch (message.getAction()) {
		case JbootEhredisMessage.ACTION_PUT:
			ehcacheImpl.remove(message.getCacheName(), message.getKey());
			break;
		case JbootEhredisMessage.ACTION_REMOVE:
			ehcacheImpl.remove(message.getCacheName(), message.getKey());
			break;
		case JbootEhredisMessage.ACTION_REMOVE_ALL:
			ehcacheImpl.removeAll(message.getCacheName());
			break;
		}
	}

	public JbootEhcacheImpl getEhcacheImpl() {
		return ehcacheImpl;
	}

	public JbootRedisCacheImpl getRedisCacheImpl() {
		return redisCacheImpl;
	}
}
