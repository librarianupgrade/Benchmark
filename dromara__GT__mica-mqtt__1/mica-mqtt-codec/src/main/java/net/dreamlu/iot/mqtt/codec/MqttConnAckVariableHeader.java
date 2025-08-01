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
 * Variable header of {@link MqttConnectMessage}
 *
 * @author netty
 */
public final class MqttConnAckVariableHeader {
	private final MqttConnectReasonCode connectReturnCode;

	private final boolean sessionPresent;

	private final MqttProperties properties;

	public MqttConnAckVariableHeader(MqttConnectReasonCode connectReturnCode, boolean sessionPresent) {
		this(connectReturnCode, sessionPresent, MqttProperties.NO_PROPERTIES);
	}

	public MqttConnAckVariableHeader(MqttConnectReasonCode connectReturnCode, boolean sessionPresent,
			MqttProperties properties) {
		this.connectReturnCode = connectReturnCode;
		this.sessionPresent = sessionPresent;
		this.properties = MqttProperties.withEmptyDefaults(properties);
	}

	public MqttConnectReasonCode connectReturnCode() {
		return connectReturnCode;
	}

	public boolean isSessionPresent() {
		return sessionPresent;
	}

	public MqttProperties properties() {
		return properties;
	}

	@Override
	public String toString() {
		return "MqttConnAckVariableHeader[" + "connectReturnCode=" + connectReturnCode + ", sessionPresent="
				+ sessionPresent + ']';
	}
}
