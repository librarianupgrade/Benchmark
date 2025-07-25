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

import java.util.Objects;

/**
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#fixed-header">
 * MQTTV3.1/fixed-header</a>
 *
 * @author netty
 */
public final class MqttFixedHeader {

	private final MqttMessageType messageType;
	private final boolean isDup;
	private final MqttQoS qosLevel;
	private final boolean isRetain;
	private final int remainingLength;

	public MqttFixedHeader(MqttMessageType messageType, boolean isDup, MqttQoS qosLevel, boolean isRetain,
			int remainingLength) {
		this.messageType = Objects.requireNonNull(messageType, "messageType is null.");
		this.isDup = isDup;
		this.qosLevel = Objects.requireNonNull(qosLevel, "qosLevel is null.");
		this.isRetain = isRetain;
		this.remainingLength = remainingLength;
	}

	public MqttMessageType messageType() {
		return messageType;
	}

	public boolean isDup() {
		return isDup;
	}

	public MqttQoS qosLevel() {
		return qosLevel;
	}

	public boolean isRetain() {
		return isRetain;
	}

	public int remainingLength() {
		return remainingLength;
	}

	@Override
	public String toString() {
		return "MqttFixedHeader[" + "messageType=" + messageType + ", isDup=" + isDup + ", qosLevel=" + qosLevel
				+ ", isRetain=" + isRetain + ", remainingLength=" + remainingLength + ']';
	}
}
