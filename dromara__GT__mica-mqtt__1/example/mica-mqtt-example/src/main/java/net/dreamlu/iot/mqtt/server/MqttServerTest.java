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

package net.dreamlu.iot.mqtt.server;

import net.dreamlu.iot.mqtt.core.server.MqttServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * mqtt 服务端测试
 *
 * @author L.cm
 */
public class MqttServerTest {
	private static final Logger logger = LoggerFactory.getLogger(MqttServerTest.class);

	public static void main(String[] args) {
		// 注意：为了能接受更多链接（降低内存），请添加 jvm 参数 -Xss129k
		MqttServer mqttServer = MqttServer.create()
				// 服务端 ip 默认为空，0.0.0.0，建议不要设置
				.ip("0.0.0.0")
				// 默认：1883
				.port(1883)
				// mqtt 用户名密码
				//			.usernamePassword("mica", "mica")
				// 默认为： 8192（mqtt 默认最大消息大小），为了降低内存可以减小小此参数，如果消息过大 t-io 会尝试解析多次（建议根据实际业务情况而定）
				.readBufferSize(8192)
				//			最大包体长度
				//			.maxBytesInMessage(1024 * 100)
				//			mqtt 3.1 协议会校验 clientId 长度。
				//			.maxClientIdLength(64)
				.messageListener((context, clientId, topic, qos, message) -> {
					logger.info("clientId:{} payload:{}", clientId,
							new String(message.payload(), StandardCharsets.UTF_8));
				})
				// 客户端连接状态监听
				.connectStatusListener(new MqttConnectStatusListener())
				// 自定义消息拦截器
				//			.addInterceptor(new MqttMessageInterceptor())
				// 开启 http
				.httpEnable(true)
				// http basic 认证，自定义认证，实现 HttpFilter， 注册到 MqttHttpRoutes 即可
				.httpBasicAuth("mica", "mica")
				// 开启 websocket
				.websocketEnable(true)
				// 开始 stat 监控
				.statEnable()
				// 开启 debug 信息日志
				.debug().start();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				String message = "mica最牛皮 " + System.currentTimeMillis();
				mqttServer.publishAll("/test/123", message.getBytes(StandardCharsets.UTF_8));
			}
		}, 1000, 2000);
	}
}
