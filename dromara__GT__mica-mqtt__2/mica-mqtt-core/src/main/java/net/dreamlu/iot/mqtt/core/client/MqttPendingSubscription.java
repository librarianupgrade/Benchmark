package net.dreamlu.iot.mqtt.core.client;

import net.dreamlu.iot.mqtt.codec.MqttMessage;
import net.dreamlu.iot.mqtt.codec.MqttSubscribeMessage;
import net.dreamlu.iot.mqtt.core.common.RetryProcessor;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * MqttPendingSubscription，参考于 netty-mqtt-client
 */
final class MqttPendingSubscription {
	private final List<MqttClientSubscription> subscriptionList;
	private final RetryProcessor<MqttSubscribeMessage> retryProcessor = new RetryProcessor<>();

	MqttPendingSubscription(List<MqttClientSubscription> subscriptionList, MqttSubscribeMessage message) {
		this.subscriptionList = subscriptionList;
		this.retryProcessor.setOriginalMessage(message);
	}

	public List<MqttClientSubscription> getSubscriptionList() {
		return subscriptionList;
	}

	protected void startRetransmitTimer(ScheduledThreadPoolExecutor executor, Consumer<MqttMessage> sendPacket) {
		this.retryProcessor.setHandle((fixedHeader, originalMessage) -> sendPacket.accept(
				new MqttSubscribeMessage(fixedHeader, originalMessage.variableHeader(), originalMessage.payload())));
		this.retryProcessor.start(executor);
	}

	protected void onSubAckReceived() {
		this.retryProcessor.stop();
	}

}
