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

package net.dreamlu.iot.mqtt.spring.server.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.iot.mqtt.core.server.event.IMqttConnectStatusListener;
import org.springframework.context.ApplicationEventPublisher;
import org.tio.core.ChannelContext;
import org.tio.core.Node;

import java.util.concurrent.TimeUnit;

/**
 * spring event mqtt 连接状态
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
public class SpringEventMqttConnectStatusListener implements IMqttConnectStatusListener {
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void online(ChannelContext context, String clientId, String username) {
		log.info("Mqtt clientId:{} username:{} online.", clientId, username);
		MqttClientOnlineEvent onlineEvent = new MqttClientOnlineEvent();
		onlineEvent.setClientId(clientId);
		onlineEvent.setUsername(username);
		// clientNode
		Node clientNode = context.getClientNode();
		onlineEvent.setIpAddress(clientNode.getIp());
		onlineEvent.setPort(clientNode.getPort());
		// keepalive
		long keepalive = context.heartbeatTimeout == null ? 60L
				: TimeUnit.MILLISECONDS.toSeconds(context.heartbeatTimeout);
		onlineEvent.setKeepalive(keepalive);
		onlineEvent.setTs(context.stat.timeCreated);
		eventPublisher.publishEvent(onlineEvent);
	}

	@Override
	public void offline(ChannelContext context, String clientId, String username, String reason) {
		log.info("Mqtt clientId:{} username:{} offline reason:{}.", clientId, username, reason);
		MqttClientOfflineEvent offlineEvent = new MqttClientOfflineEvent();
		offlineEvent.setClientId(clientId);
		offlineEvent.setUsername(username);
		offlineEvent.setReason(reason);
		offlineEvent.setTs(context.stat.timeClosed);
		eventPublisher.publishEvent(offlineEvent);
	}

}
