/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dreamlu.iot.mqtt.spring.server.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.iot.mqtt.core.server.MqttServer;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.tio.core.Tio;
import org.tio.server.ServerGroupStat;
import org.tio.server.TioServerConfig;

import java.util.Collections;

/**
 * mica mqtt Metrics
 *
 * @author L.cm
 */
@RequiredArgsConstructor
public class MqttServerMetrics implements ApplicationListener<ApplicationStartedEvent> {
	/**
	 * Prefix used for all mica-mqtt metric names.
	 */
	public static final String MQTT_METRIC_NAME_PREFIX = "mqtt";
	/**
	 * 连接统计
	 */
	private static final String MQTT_CONNECTIONS_ACCEPTED = MQTT_METRIC_NAME_PREFIX + ".connections.accepted";
	private static final String MQTT_CONNECTIONS_SIZE = MQTT_METRIC_NAME_PREFIX + ".connections.size";
	private static final String MQTT_CONNECTIONS_CLOSED = MQTT_METRIC_NAME_PREFIX + ".connections.closed";
	/**
	 * 消息统计
	 */
	private static final String MQTT_MESSAGES_HANDLED_PACKETS = MQTT_METRIC_NAME_PREFIX + ".messages.handled.packets";
	private static final String MQTT_MESSAGES_HANDLED_BYTES = MQTT_METRIC_NAME_PREFIX + ".messages.handled.bytes";
	private static final String MQTT_MESSAGES_RECEIVED_PACKETS = MQTT_METRIC_NAME_PREFIX + ".messages.received.packets";
	private static final String MQTT_MESSAGES_RECEIVED_BYTES = MQTT_METRIC_NAME_PREFIX + ".messages.received.bytes";
	private static final String MQTT_MESSAGES_SEND_PACKETS = MQTT_METRIC_NAME_PREFIX + ".messages.send.packets";
	private static final String MQTT_MESSAGES_SEND_BYTES = MQTT_METRIC_NAME_PREFIX + ".messages.send.bytes";

	private final Iterable<Tag> tags;

	public MqttServerMetrics() {
		this.tags = Collections.emptyList();
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		MqttServer mqttServer = applicationContext.getBean(MqttServer.class);
		MeterRegistry registry = applicationContext.getBean(MeterRegistry.class);
		TioServerConfig serverConfig = mqttServer.getServerConfig();
		bindTo(registry, serverConfig);
	}

	private void bindTo(MeterRegistry meterRegistry, TioServerConfig serverConfig) {
		// 连接统计
		Gauge.builder(MQTT_CONNECTIONS_ACCEPTED, serverConfig,
				(config) -> ((ServerGroupStat) config.getGroupStat()).accepted.sum())
				.description("Mqtt server connections accepted").tags(tags).register(meterRegistry);
		Gauge.builder(MQTT_CONNECTIONS_SIZE, serverConfig, (config) -> Tio.getAll(config).size())
				.description("Mqtt server connections size").tags(tags).register(meterRegistry);
		Gauge.builder(MQTT_CONNECTIONS_CLOSED, serverConfig, (config) -> config.getGroupStat().getClosed().sum())
				.description("Mqtt server connections closed").tags(tags).register(meterRegistry);
		// 消息统计
		Gauge.builder(MQTT_MESSAGES_HANDLED_PACKETS, serverConfig,
				(config) -> config.getGroupStat().getHandledPackets().sum()).description("Mqtt server handled packets")
				.tags(tags).register(meterRegistry);
		Gauge.builder(MQTT_MESSAGES_HANDLED_BYTES, serverConfig,
				(config) -> config.getGroupStat().getHandledBytes().sum()).description("Mqtt server handled bytes")
				.tags(tags).register(meterRegistry);
		// 接收的消息
		Gauge.builder(MQTT_MESSAGES_RECEIVED_PACKETS, serverConfig,
				(config) -> config.getGroupStat().getReceivedPackets().sum())
				.description("Mqtt server received packets").tags(tags).register(meterRegistry);
		Gauge.builder(MQTT_MESSAGES_RECEIVED_BYTES, serverConfig,
				(config) -> config.getGroupStat().getReceivedBytes().sum()).description("Mqtt server received bytes")
				.tags(tags).register(meterRegistry);
		// 发送的消息
		Gauge.builder(MQTT_MESSAGES_SEND_PACKETS, serverConfig,
				(config) -> config.getGroupStat().getSentPackets().sum()).description("Mqtt server send packets")
				.tags(tags).register(meterRegistry);
		Gauge.builder(MQTT_MESSAGES_SEND_BYTES, serverConfig, (config) -> config.getGroupStat().getSentPackets().sum())
				.description("Mqtt server send bytes").tags(tags).register(meterRegistry);
	}

}
