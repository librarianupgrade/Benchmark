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

package net.dreamlu.iot.mqtt.core.client;

import net.dreamlu.iot.mqtt.core.common.MqttPendingPublish;
import net.dreamlu.iot.mqtt.core.common.MqttPendingQos2Publish;

import java.util.List;

/**
 * 客户端 session
 *
 * @author L.cm
 */
public interface IMqttClientSession {

	/**
	 * 添加订阅
	 *
	 * @param messageId           messageId
	 * @param pendingSubscription MqttPendingSubscription
	 */
	void addPaddingSubscribe(int messageId, MqttPendingSubscription pendingSubscription);

	/**
	 * 获取过程订阅
	 *
	 * @param messageId messageId
	 * @return MqttPendingSubscription
	 */
	MqttPendingSubscription getPaddingSubscribe(int messageId);

	/**
	 * 删除过程订阅
	 *
	 * @param messageId messageId
	 * @return MqttPendingSubscription
	 */
	MqttPendingSubscription removePaddingSubscribe(int messageId);

	/**
	 * 添加订阅
	 *
	 * @param subscriptionList MqttClientSubscription 集合
	 */
	void addSubscriptionList(List<MqttClientSubscription> subscriptionList);

	/**
	 * 判断是否已经订阅过
	 *
	 * @param clientSubscription MqttClientSubscription
	 * @return 是否已经订阅过
	 */
	boolean isSubscribed(MqttClientSubscription clientSubscription);

	/**
	 * 获取并清除订阅
	 *
	 * @return 订阅集合
	 */
	List<MqttClientSubscription> getAndCleanSubscription();

	/**
	 * 获取匹配的订阅
	 *
	 * @param topicName topicName
	 * @return 订阅信息集合
	 */
	List<MqttClientSubscription> getMatchedSubscription(String topicName);

	/**
	 * 删除订阅过程消息
	 *
	 * @param topicFilters topicFilter 集合
	 */
	void removeSubscriptions(List<String> topicFilters);

	/**
	 * 添加取消订阅过程消息
	 *
	 * @param messageId             messageId
	 * @param pendingUnSubscription MqttPendingUnSubscription
	 */
	void addPaddingUnSubscribe(int messageId, MqttPendingUnSubscription pendingUnSubscription);

	/**
	 * 获取取消订阅过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingUnSubscription
	 */
	MqttPendingUnSubscription getPaddingUnSubscribe(int messageId);

	/**
	 * 删除取消订阅过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingUnSubscription
	 */
	MqttPendingUnSubscription removePaddingUnSubscribe(int messageId);

	/**
	 * 添加过程消息
	 *
	 * @param messageId      messageId
	 * @param pendingPublish MqttPendingPublish
	 */
	void addPendingPublish(int messageId, MqttPendingPublish pendingPublish);

	/**
	 * 获取过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingPublish
	 */
	MqttPendingPublish getPendingPublish(int messageId);

	/**
	 * 删除过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingPublish
	 */
	MqttPendingPublish removePendingPublish(int messageId);

	/**
	 * 添加 qos2 过程消息
	 *
	 * @param messageId          messageId
	 * @param pendingQos2Publish MqttPendingQos2Publish
	 */
	void addPendingQos2Publish(int messageId, MqttPendingQos2Publish pendingQos2Publish);

	/**
	 * 获取 qos2 过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingQos2Publish
	 */
	MqttPendingQos2Publish getPendingQos2Publish(int messageId);

	/**
	 * 删除 qos2 过程消息
	 *
	 * @param messageId messageId
	 * @return MqttPendingQos2Publish
	 */
	MqttPendingQos2Publish removePendingQos2Publish(int messageId);

	/**
	 * 资源清理
	 */
	void clean();

}
