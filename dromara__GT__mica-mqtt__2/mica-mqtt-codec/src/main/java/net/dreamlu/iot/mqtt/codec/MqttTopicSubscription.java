/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.dreamlu.iot.mqtt.codec;

/**
 * Contains a topic name and Qos Level.
 * This is part of the {@link MqttSubscribePayload}
 *
 * @author netty
 */
public final class MqttTopicSubscription {

	private final String topicFilter;
	private final MqttSubscriptionOption option;

	public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService) {
		this.topicFilter = topicFilter;
		this.option = MqttSubscriptionOption.onlyFromQos(qualityOfService);
	}

	public MqttTopicSubscription(String topicFilter, MqttSubscriptionOption option) {
		this.topicFilter = topicFilter;
		this.option = option;
	}

	public String topicName() {
		return topicFilter;
	}

	public MqttQoS qualityOfService() {
		return option.qos();
	}

	public MqttSubscriptionOption option() {
		return option;
	}

	@Override
	public String toString() {
		return "MqttTopicSubscription[" + "topicFilter=" + topicFilter + ", option=" + this.option + ']';
	}
}
