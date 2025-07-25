package org.smartboot.mqtt.client.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.mqtt.client.MqttClient;
import org.smartboot.mqtt.client.Subscribe;
import org.smartboot.mqtt.common.TopicToken;
import org.smartboot.mqtt.common.enums.MqttQoS;
import org.smartboot.mqtt.common.message.MqttPubAckMessage;
import org.smartboot.mqtt.common.message.MqttPubAckVariableHeader;
import org.smartboot.mqtt.common.message.MqttPubCompMessage;
import org.smartboot.mqtt.common.message.MqttPubRecMessage;
import org.smartboot.mqtt.common.message.MqttPubRelMessage;
import org.smartboot.mqtt.common.message.MqttPublishMessage;
import org.smartboot.mqtt.common.message.MqttPublishVariableHeader;
import org.smartboot.mqtt.common.util.TopicTokenUtil;

import java.util.function.Consumer;

/**
 * 发布Topic
 *
 * @author 三刀
 * @version V1.0 , 2018/4/25
 */
public class PublishProcessor implements MqttProcessor<MqttPublishMessage> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublishProcessor.class);

	@Override
	public void process(MqttClient session, MqttPublishMessage mqttPublishMessage) {
		MqttQoS mqttQoS = mqttPublishMessage.getFixedHeader().getQosLevel();
		switch (mqttQoS) {
		case AT_MOST_ONCE:
			processQos0(session, mqttPublishMessage);
			break;
		case AT_LEAST_ONCE:
			processQos1(session, mqttPublishMessage);
			break;
		case EXACTLY_ONCE:
			processQos2(session, mqttPublishMessage);
			break;
		default:
			LOGGER.warn("unSupport mqttQos:{}", mqttQoS);
			break;
		}

	}

	private void processQos0(MqttClient mqttClient, MqttPublishMessage mqttPublishMessage) {
		//        LOGGER.info("receive publish:{}", mqttPublishMessage);
		processPublishMessage(mqttPublishMessage, mqttClient);
	}

	private void processPublishMessage(MqttPublishMessage mqttPublishMessage, MqttClient mqttClient) {
		MqttPublishVariableHeader header = mqttPublishMessage.getVariableHeader();
		Subscribe subscribe = mqttClient.getSubscribes().get(header.getTopicName());

		//尝试通配符匹配
		if (subscribe == null) {
			subscribe = matchWildcardsSubscribe(mqttClient, header.getTopicName());
		}
		// If unsubscribed, maybe null.
		if (subscribe != null && !subscribe.getUnsubscribed()) {
			subscribe.getConsumer().accept(mqttClient, mqttPublishMessage);
		}
	}

	private static Subscribe matchWildcardsSubscribe(MqttClient mqttClient, String topicName) {
		TopicToken publicTopicToken = new TopicToken(topicName);
		TopicToken matchToken = mqttClient.getWildcardsToken().stream()
				.filter(topicToken -> TopicTokenUtil.match(publicTopicToken, topicToken)).findFirst().orElse(null);
		return matchToken != null ? mqttClient.getSubscribes().get(matchToken.getTopicFilter()) : null;
	}

	private void processQos1(MqttClient mqttClient, MqttPublishMessage mqttPublishMessage) {
		processPublishMessage(mqttPublishMessage, mqttClient);
		MqttPubAckVariableHeader variableHeader = new MqttPubAckVariableHeader(
				mqttPublishMessage.getVariableHeader().getPacketId(), null);
		MqttPubAckMessage pubAckMessage = new MqttPubAckMessage(variableHeader);
		mqttClient.write(pubAckMessage);
	}

	private void processQos2(MqttClient session, MqttPublishMessage mqttPublishMessage) {
		final int messageId = mqttPublishMessage.getVariableHeader().getPacketId();
		MqttPubRecMessage pubRecMessage = new MqttPubRecMessage(messageId);
		session.write(pubRecMessage, (Consumer<MqttPubRelMessage>) message -> {
			MqttPubCompMessage pubRelMessage = new MqttPubCompMessage(message.getVariableHeader().getPacketId());
			session.write(pubRelMessage);

			processPublishMessage(mqttPublishMessage, session);
		});
	}

}
