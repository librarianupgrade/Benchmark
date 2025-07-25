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

package net.dreamlu.iot.mqtt.core.server.session;

import net.dreamlu.iot.mqtt.core.common.MqttPendingPublish;
import net.dreamlu.iot.mqtt.core.common.MqttPendingQos2Publish;
import net.dreamlu.iot.mqtt.core.server.model.Subscribe;
import net.dreamlu.iot.mqtt.core.util.TopicUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存 session 管理
 *
 * @author L.cm
 */
public class InMemoryMqttSessionManager implements IMqttSessionManager {
	/**
	 * messageId 存储 clientId: messageId
	 */
	private final ConcurrentMap<String, AtomicInteger> messageIdStore = new ConcurrentHashMap<>();
	/**
	 * 订阅存储 topicFilter: {clientId: qos}
	 */
	private final ConcurrentMap<String, ConcurrentMap<String, Integer>> subscribeStore = new ConcurrentHashMap<>();
	/**
	 * qos1 消息过程存储 clientId: {msgId: Object}
	 */
	private final ConcurrentMap<String, Map<Integer, MqttPendingPublish>> pendingPublishStore = new ConcurrentHashMap<>();
	/**
	 * qos2 消息过程存储 clientId: {msgId: Object}
	 */
	private final ConcurrentMap<String, Map<Integer, MqttPendingQos2Publish>> pendingQos2PublishStore = new ConcurrentHashMap<>();

	@Override
	public void addSubscribe(String topicFilter, String clientId, int mqttQoS) {
		Map<String, Integer> data = subscribeStore.computeIfAbsent(topicFilter, (key) -> new ConcurrentHashMap<>(16));
		// 如果不存在或者老的订阅 qos 比较小也重新设置
		Integer existingQos = data.get(clientId);
		if (existingQos == null || existingQos < mqttQoS) {
			data.put(clientId, mqttQoS);
		}
	}

	@Override
	public void removeSubscribe(String topicFilter, String clientId) {
		ConcurrentMap<String, Integer> map = subscribeStore.get(topicFilter);
		if (map == null) {
			return;
		}
		map.remove(clientId);
	}

	public void removeSubscribe(String clientId) {
		subscribeStore.forEach((key, value) -> value.remove(clientId));
	}

	@Override
	public Integer searchSubscribe(String topicName, String clientId) {
		// 服务端发布时查找是否有订阅，只要证明有订阅即可
		// 1. 如果订阅的就是普通的 topic
		ConcurrentMap<String, Integer> subscribeData = subscribeStore.get(topicName);
		if (subscribeData != null && !subscribeData.isEmpty()) {
			Integer qos = subscribeData.get(clientId);
			if (qos != null) {
				return qos;
			}
		}
		// 2. 如果订阅的事通配符
		Integer qosValue = null;
		Set<String> topicFilterSet = subscribeStore.keySet();
		for (String topicFilter : topicFilterSet) {
			if (TopicUtil.getTopicPattern(topicFilter).matcher(topicName).matches()) {
				ConcurrentMap<String, Integer> data = subscribeStore.get(topicFilter);
				if (data != null && !data.isEmpty()) {
					Integer mqttQoS = data.get(clientId);
					if (mqttQoS != null) {
						if (qosValue == null) {
							qosValue = mqttQoS;
						} else {
							qosValue = Math.min(qosValue, mqttQoS);
						}
					}
				}
			}
		}
		return qosValue;
	}

	@Override
	public List<Subscribe> searchSubscribe(String topicName) {
		// 排除重复订阅，例如： /test/# 和 /# 只发一份
		Map<String, Integer> subscribeMap = new HashMap<>(32);
		Set<String> topicFilterSet = subscribeStore.keySet();
		for (String topicFilter : topicFilterSet) {
			if (TopicUtil.match(topicFilter, topicName)) {
				ConcurrentMap<String, Integer> data = subscribeStore.get(topicFilter);
				if (data != null && !data.isEmpty()) {
					data.forEach((clientId, qos) -> {
						subscribeMap.merge(clientId, qos, Math::min);
					});
				}
			}
		}
		List<Subscribe> subscribeList = new ArrayList<>();
		subscribeMap.forEach((clientId, qos) -> {
			subscribeList.add(new Subscribe(clientId, qos));
		});
		subscribeMap.clear();
		return subscribeList;
	}

	@Override
	public void addPendingPublish(String clientId, int messageId, MqttPendingPublish pendingPublish) {
		Map<Integer, MqttPendingPublish> data = pendingPublishStore.computeIfAbsent(clientId,
				(key) -> new ConcurrentHashMap<>(16));
		data.put(messageId, pendingPublish);
	}

	@Override
	public MqttPendingPublish getPendingPublish(String clientId, int messageId) {
		Map<Integer, MqttPendingPublish> data = pendingPublishStore.get(clientId);
		if (data == null) {
			return null;
		}
		return data.get(messageId);
	}

	@Override
	public void removePendingPublish(String clientId, int messageId) {
		Map<Integer, MqttPendingPublish> data = pendingPublishStore.get(clientId);
		if (data != null) {
			data.remove(messageId);
		}
	}

	@Override
	public void addPendingQos2Publish(String clientId, int messageId, MqttPendingQos2Publish pendingQos2Publish) {
		Map<Integer, MqttPendingQos2Publish> data = pendingQos2PublishStore.computeIfAbsent(clientId,
				(key) -> new ConcurrentHashMap<>());
		data.put(messageId, pendingQos2Publish);
	}

	@Override
	public MqttPendingQos2Publish getPendingQos2Publish(String clientId, int messageId) {
		Map<Integer, MqttPendingQos2Publish> data = pendingQos2PublishStore.get(clientId);
		if (data == null) {
			return null;
		}
		return data.get(messageId);
	}

	@Override
	public void removePendingQos2Publish(String clientId, int messageId) {
		Map<Integer, MqttPendingQos2Publish> data = pendingQos2PublishStore.get(clientId);
		if (data != null) {
			data.remove(messageId);
		}
	}

	@Override
	public int getMessageId(String clientId) {
		AtomicInteger value = messageIdStore.computeIfAbsent(clientId, (key) -> new AtomicInteger(1));
		value.compareAndSet(0xffff, 1);
		return value.getAndIncrement();
	}

	@Override
	public boolean hasSession(String clientId) {
		return pendingQos2PublishStore.containsKey(clientId) || pendingPublishStore.containsKey(clientId)
				|| messageIdStore.containsKey(clientId)
				|| subscribeStore.values().stream().anyMatch(data -> data.containsKey(clientId));
	}

	@Override
	public boolean expire(String clientId, int sessionExpirySeconds) {
		return false;
	}

	@Override
	public boolean active(String clientId) {
		return false;
	}

	@Override
	public void remove(String clientId) {
		removeSubscribe(clientId);
		pendingPublishStore.remove(clientId);
		pendingQos2PublishStore.remove(clientId);
		messageIdStore.remove(clientId);
	}

	@Override
	public void clean() {
		subscribeStore.clear();
		pendingPublishStore.clear();
		pendingQos2PublishStore.clear();
		messageIdStore.clear();
	}

}
