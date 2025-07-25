/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.net.dreamlu.net).
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

package net.dreamlu.iot.mqtt.broker;

import net.dreamlu.iot.mqtt.core.client.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 设备 A，这里默认 APP 应用端
 *
 * @author L.cm
 */
public class DeviceA {
	private static final Logger logger = LoggerFactory.getLogger(DeviceA.class);

	public static void main(String[] args) {
		// 初始化 mqtt 客户端
		MqttClient client = MqttClient.create().ip("127.0.0.1").port(1883).username("admin").password("123456")
				.connectSync();

		client.subQos0("/a/door/open", (context, topic, message, payload) -> {
			logger.info(topic + '\t' + new String(payload, StandardCharsets.UTF_8));
		});
	}

}
