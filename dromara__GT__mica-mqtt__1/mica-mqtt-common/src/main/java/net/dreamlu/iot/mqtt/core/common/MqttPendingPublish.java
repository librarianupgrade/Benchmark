package net.dreamlu.iot.mqtt.core.common;

import net.dreamlu.iot.mqtt.codec.MqttMessage;
import net.dreamlu.iot.mqtt.codec.MqttPublishMessage;
import net.dreamlu.iot.mqtt.codec.MqttQoS;
import org.tio.utils.timer.TimerTaskService;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * MqttPendingPublish，参考于 netty-mqtt-client
 *
 * @author netty
 */
public final class MqttPendingPublish {
	private final byte[] payload;
	private final MqttPublishMessage message;
	private final MqttQoS qos;
	private final RetryProcessor<MqttPublishMessage> pubRetryProcessor = new RetryProcessor<>();
	private final RetryProcessor<MqttMessage> pubRelRetryProcessor = new RetryProcessor<>();

	public MqttPendingPublish(byte[] payload, MqttPublishMessage message, MqttQoS qos) {
		this.payload = payload;
		this.message = message;
		this.qos = qos;
		this.pubRetryProcessor.setOriginalMessage(message);
	}

	public byte[] getPayload() {
		return payload;
	}

	public MqttPublishMessage getMessage() {
		return message;
	}

	public MqttQoS getQos() {
		return qos;
	}

	public void startPublishRetransmissionTimer(TimerTaskService taskService, Consumer<MqttMessage> sendPacket) {
		this.pubRetryProcessor.setHandle(((fixedHeader, originalMessage) -> {
			sendPacket.accept(new MqttPublishMessage(fixedHeader, originalMessage.variableHeader(), this.payload));
		}));
		this.pubRetryProcessor.start(taskService);
	}

	public void onPubAckReceived() {
		this.pubRetryProcessor.stop();
	}

	public void setPubRelMessage(MqttMessage pubRelMessage) {
		this.pubRelRetryProcessor.setOriginalMessage(pubRelMessage);
	}

	public void startPubRelRetransmissionTimer(TimerTaskService taskService, Consumer<MqttMessage> sendPacket) {
		this.pubRelRetryProcessor.setHandle((fixedHeader, originalMessage) -> sendPacket
				.accept(new MqttMessage(fixedHeader, originalMessage.variableHeader())));
		this.pubRelRetryProcessor.start(taskService);
	}

	public void onPubCompReceived() {
		this.pubRelRetryProcessor.stop();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MqttPendingPublish that = (MqttPendingPublish) o;
		return Objects.equals(payload, that.payload) && Objects.equals(message, that.message) && qos == that.qos;
	}

	@Override
	public int hashCode() {
		return Objects.hash(payload, message, qos);
	}

}
