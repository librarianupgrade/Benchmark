/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & dreamlu.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dreamlu.iot.mqtt.broker.cluster;

import lombok.RequiredArgsConstructor;
import net.dreamlu.iot.mqtt.broker.enums.RedisKeys;
import net.dreamlu.iot.mqtt.broker.util.RedisUtil;
import net.dreamlu.iot.mqtt.core.server.model.Message;
import net.dreamlu.iot.mqtt.core.server.serializer.IMessageSerializer;
import net.dreamlu.iot.mqtt.core.server.store.IMqttMessageStore;
import net.dreamlu.iot.mqtt.core.util.TopicUtil;
import net.dreamlu.mica.redis.cache.MicaRedisCache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * redis mqtt 遗嘱和保留消息存储
 *
 * @author L.cm
 */
@RequiredArgsConstructor
public class RedisMqttMessageStore implements IMqttMessageStore {
	private final MicaRedisCache redisCache;
	private final IMessageSerializer messageSerializer;

	@Override
	public boolean addWillMessage(String clientId, Message message) {
		redisCache.set(RedisKeys.MESSAGE_STORE_WILL.getKey(clientId), message, messageSerializer::serialize);
		return true;
	}

	@Override
	public boolean clearWillMessage(String clientId) {
		redisCache.del(RedisKeys.MESSAGE_STORE_WILL.getKey(clientId));
		return true;
	}

	@Override
	public Message getWillMessage(String clientId) {
		return redisCache.get(RedisKeys.MESSAGE_STORE_WILL.getKey(clientId), messageSerializer::deserialize);
	}

	@Override
	public boolean addRetainMessage(String topic, Message message) {
		redisCache.set(RedisKeys.MESSAGE_STORE_RETAIN.getKey(topic), message, messageSerializer::serialize);
		return true;
	}

	@Override
	public boolean clearRetainMessage(String topic) {
		redisCache.del(RedisKeys.MESSAGE_STORE_RETAIN.getKey(topic));
		return true;
	}

	@Override
	public List<Message> getRetainMessage(String topicFilter) {
		List<Message> retainMessageList = new ArrayList<>();
		Pattern topicPattern = TopicUtil.getTopicPattern(topicFilter);
		RedisKeys redisKey = RedisKeys.MESSAGE_STORE_RETAIN;
		String redisKeyPrefix = redisKey.getKey();
		String redisKeyPattern = redisKeyPrefix.concat(RedisUtil.getTopicPattern(topicFilter));
		int keyPrefixLength = redisKeyPrefix.length();
		redisCache.scan(redisKeyPattern, (key) -> {
			String keySuffix = key.substring(keyPrefixLength);
			if (topicPattern.matcher(keySuffix).matches()) {
				Message message = redisCache.get(key, messageSerializer::deserialize);
				if (message != null) {
					retainMessageList.add(message);
				}
			}
		});
		return retainMessageList;
	}

}
