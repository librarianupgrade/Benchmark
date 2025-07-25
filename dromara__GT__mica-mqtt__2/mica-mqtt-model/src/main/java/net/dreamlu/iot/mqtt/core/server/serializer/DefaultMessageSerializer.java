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

package net.dreamlu.iot.mqtt.core.server.serializer;

import net.dreamlu.iot.mqtt.core.server.enums.MessageType;
import net.dreamlu.iot.mqtt.core.server.model.Message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * mica mqtt 消息序列化
 *
 * @author L.cm
 */
public enum DefaultMessageSerializer implements IMessageSerializer {

	/**
	 * 单利
	 */
	INSTANCE;

	/**
	 * 空 byte 数组
	 */
	private static final byte[] EMPTY_BYTES = new byte[0];
	/**
	 * 空 short byte 数组，2 个长度
	 */
	private static final byte[] EMPTY_SHORT_BYTES = new byte[2];
	/**
	 * 空 int byte 数组，4 个长度
	 */
	private static final byte[] EMPTY_INT_BYTES = new byte[4];
	/**
	 * 空 long byte 数组，8 个长度
	 */
	private static final byte[] EMPTY_LONG_BYTES = new byte[8];

	@Override
	public byte[] serialize(Message message) {
		if (message == null) {
			return EMPTY_BYTES;
		}
		MessageType messageType = message.getMessageType();
		if (messageType == null) {
			throw new IllegalArgumentException("message type is null.");
		}
		switch (messageType) {
		case CONNECT:
		case DISCONNECT:
			return serializeConnect(messageType, message);
		case SUBSCRIBE:
			return serializeSubscribe(messageType, message);
		case UNSUBSCRIBE:
			return serializeUnsubscribe(messageType, message);
		case UP_STREAM:
			return serializeUpStream(messageType, message);
		case DOWN_STREAM:
			return serializeDownStream(messageType, message);
		default:
			throw new IllegalArgumentException("unknown message type: " + messageType);
		}
	}

	private static byte[] serializeConnect(MessageType messageType, Message message) {
		// messageType, node, clientId, username, peerHost, timestamp
		// 1 + 2 * 3 + 1 + 8
		int protocolLength = 16;
		// 事件触发所在节点
		String node = message.getNode();
		byte[] nodeBytes = null;
		if (node != null) {
			nodeBytes = node.getBytes(StandardCharsets.UTF_8);
			protocolLength += nodeBytes.length;
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		String clientId = message.getClientId();
		byte[] clientIdBytes = null;
		if (clientId != null) {
			clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += clientIdBytes.length;
		}
		// 消息目的用户名，主要是在遗嘱消息用
		String username = message.getUsername();
		byte[] usernameBytes = null;
		if (username != null) {
			usernameBytes = username.getBytes(StandardCharsets.UTF_8);
			protocolLength += usernameBytes.length;
		}
		// 客户端的 IPAddress
		String peerHost = message.getPeerHost();
		byte[] peerHostBytes = null;
		if (peerHost != null) {
			peerHostBytes = peerHost.getBytes(StandardCharsets.UTF_8);
			protocolLength += peerHostBytes.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(protocolLength);
		// 消息类型
		buffer.put((byte) messageType.getValue());
		// 事件触发所在节点
		if (nodeBytes != null) {
			buffer.putShort((short) nodeBytes.length);
			buffer.put(nodeBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		if (clientIdBytes != null) {
			buffer.putShort((short) clientIdBytes.length);
			buffer.put(clientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息来源 用户名
		if (usernameBytes != null) {
			buffer.putShort((short) usernameBytes.length);
			buffer.put(usernameBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 客户端的 IPAddress
		if (peerHostBytes != null) {
			buffer.put((byte) peerHostBytes.length);
			buffer.put(peerHostBytes);
		} else {
			buffer.put(EMPTY_BYTES);
		}
		// 存储时间
		buffer.putLong(message.getTimestamp());
		return buffer.array();
	}

	private static byte[] serializeSubscribe(MessageType messageType, Message message) {
		// messageType, node, clientId, topic, qos, peerHost, timestamp
		// 1 + 2 * 3 + 1 + 1 + 8
		int protocolLength = 17;
		// 事件触发所在节点
		String node = message.getNode();
		byte[] nodeBytes = null;
		if (node != null) {
			nodeBytes = node.getBytes(StandardCharsets.UTF_8);
			protocolLength += nodeBytes.length;
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		String clientId = message.getClientId();
		byte[] clientIdBytes = null;
		if (clientId != null) {
			clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += clientIdBytes.length;
		}
		// topic
		String topic = message.getTopic();
		byte[] topicBytes = null;
		if (topic != null) {
			topicBytes = topic.getBytes(StandardCharsets.UTF_8);
			protocolLength += topicBytes.length;
		}
		// 客户端的 IPAddress
		String peerHost = message.getPeerHost();
		byte[] peerHostBytes = null;
		if (peerHost != null) {
			peerHostBytes = peerHost.getBytes(StandardCharsets.UTF_8);
			protocolLength += peerHostBytes.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(protocolLength);
		// 消息类型
		buffer.put((byte) messageType.getValue());
		// 事件触发所在节点
		if (nodeBytes != null) {
			buffer.putShort((short) nodeBytes.length);
			buffer.put(nodeBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		if (clientIdBytes != null) {
			buffer.putShort((short) clientIdBytes.length);
			buffer.put(clientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// topic
		if (topicBytes != null) {
			buffer.putShort((short) topicBytes.length);
			buffer.put(topicBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// qos
		buffer.put((byte) message.getQos());
		// 客户端的 IPAddress
		if (peerHostBytes != null) {
			buffer.put((byte) peerHostBytes.length);
			buffer.put(peerHostBytes);
		} else {
			buffer.put(EMPTY_BYTES);
		}
		// 存储时间
		buffer.putLong(message.getTimestamp());
		return buffer.array();
	}

	private static byte[] serializeUnsubscribe(MessageType messageType, Message message) {
		// messageType, node, clientId, topic, peerHost, timestamp
		// 1 + 2 * 3 + 1 + 8
		int protocolLength = 16;
		// 事件触发所在节点
		String node = message.getNode();
		byte[] nodeBytes = null;
		if (node != null) {
			nodeBytes = node.getBytes(StandardCharsets.UTF_8);
			protocolLength += nodeBytes.length;
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		String clientId = message.getClientId();
		byte[] clientIdBytes = null;
		if (clientId != null) {
			clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += clientIdBytes.length;
		}
		// topic
		String topic = message.getTopic();
		byte[] topicBytes = null;
		if (topic != null) {
			topicBytes = topic.getBytes(StandardCharsets.UTF_8);
			protocolLength += topicBytes.length;
		}
		// 客户端的 IPAddress
		String peerHost = message.getPeerHost();
		byte[] peerHostBytes = null;
		if (peerHost != null) {
			peerHostBytes = peerHost.getBytes(StandardCharsets.UTF_8);
			protocolLength += peerHostBytes.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(protocolLength);
		// 消息类型
		buffer.put((byte) messageType.getValue());
		// 事件触发所在节点
		if (nodeBytes != null) {
			buffer.putShort((short) nodeBytes.length);
			buffer.put(nodeBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		if (clientIdBytes != null) {
			buffer.putShort((short) clientIdBytes.length);
			buffer.put(clientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// topic
		if (topicBytes != null) {
			buffer.putShort((short) topicBytes.length);
			buffer.put(topicBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 客户端的 IPAddress
		if (peerHostBytes != null) {
			buffer.put((byte) peerHostBytes.length);
			buffer.put(peerHostBytes);
		} else {
			buffer.put(EMPTY_BYTES);
		}
		// 存储时间
		buffer.putLong(message.getTimestamp());
		return buffer.array();
	}

	private static byte[] serializeUpStream(MessageType messageType, Message message) {
		// 1 + 2 + 2 + 2 * 5 + 1 + 4 + 1 + 8 + 8
		int protocolLength = 37;
		String fromClientId = message.getFromClientId();
		// 消息来源 客户端 id
		byte[] fromClientIdBytes = null;
		if (fromClientId != null) {
			fromClientIdBytes = fromClientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += fromClientIdBytes.length;
		}
		// 消息来源 用户名
		String fromUsername = message.getFromUsername();
		// 消息来源 客户端 id
		byte[] fromUsernameBytes = null;
		if (fromUsername != null) {
			fromUsernameBytes = fromUsername.getBytes(StandardCharsets.UTF_8);
			protocolLength += fromUsernameBytes.length;
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		String clientId = message.getClientId();
		byte[] clientIdBytes = null;
		if (clientId != null) {
			clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += clientIdBytes.length;
		}
		// 消息目的用户名，主要是在遗嘱消息用
		String username = message.getUsername();
		byte[] usernameBytes = null;
		if (username != null) {
			usernameBytes = username.getBytes(StandardCharsets.UTF_8);
			protocolLength += usernameBytes.length;
		}
		// topic
		String topic = message.getTopic();
		byte[] topicBytes = null;
		if (topic != null) {
			topicBytes = topic.getBytes(StandardCharsets.UTF_8);
			protocolLength += topicBytes.length;
		}
		// 消息内容
		ByteBuffer payload = message.getPayload();
		byte[] payloadBytes = null;
		if (payload != null) {
			payloadBytes = payload.array();
			protocolLength += payloadBytes.length;
		}
		// 客户端的 IPAddress
		String peerHost = message.getPeerHost();
		byte[] peerHostBytes = null;
		if (peerHost != null) {
			peerHostBytes = peerHost.getBytes(StandardCharsets.UTF_8);
			protocolLength += peerHostBytes.length;
		}
		// 事件触发所在节点
		String node = message.getNode();
		byte[] nodeBytes = null;
		if (node != null) {
			nodeBytes = node.getBytes(StandardCharsets.UTF_8);
			protocolLength += nodeBytes.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(protocolLength);
		// 消息类型
		buffer.put((byte) messageType.getValue());
		// 事件触发所在节点
		if (nodeBytes != null) {
			buffer.putShort((short) nodeBytes.length);
			buffer.put(nodeBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// MQTT 消息 ID
		Integer messageId = message.getId();
		if (messageId != null) {
			buffer.putShort(messageId.shortValue());
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息来源 客户端 id
		if (fromClientIdBytes != null) {
			buffer.putShort((short) fromClientIdBytes.length);
			buffer.put(fromClientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息来源 用户名
		if (fromUsernameBytes != null) {
			buffer.putShort((short) fromUsernameBytes.length);
			buffer.put(fromUsernameBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		if (clientIdBytes != null) {
			buffer.putShort((short) clientIdBytes.length);
			buffer.put(clientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息来源 用户名
		if (usernameBytes != null) {
			buffer.putShort((short) usernameBytes.length);
			buffer.put(usernameBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// topic
		if (topicBytes != null) {
			buffer.putShort((short) topicBytes.length);
			buffer.put(topicBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// dup、qos、retain
		int byte1 = 0;
		if (message.isDup()) {
			byte1 |= 0x08;
		}
		byte1 |= message.getQos() << 1;
		if (message.isRetain()) {
			byte1 |= 0x01;
		}
		buffer.put((byte) byte1);
		// 消息内容
		if (payloadBytes != null) {
			buffer.putInt(payloadBytes.length);
			buffer.put(payloadBytes);
		} else {
			buffer.put(EMPTY_INT_BYTES);
		}
		// 客户端的 IPAddress
		if (peerHostBytes != null) {
			buffer.put((byte) peerHostBytes.length);
			buffer.put(peerHostBytes);
		} else {
			buffer.put(EMPTY_BYTES);
		}
		// 存储时间
		buffer.putLong(message.getTimestamp());
		// PUBLISH 消息到达 Broker 的时间 (ms)
		Long publishReceivedAt = message.getPublishReceivedAt();
		if (publishReceivedAt != null) {
			buffer.putLong(publishReceivedAt);
		} else {
			buffer.put(EMPTY_LONG_BYTES);
		}
		return buffer.array();
	}

	private static byte[] serializeDownStream(MessageType messageType, Message message) {
		// 1 + 2 + 2 + 2 * 3 + 1 + 4 + 1 + 8 + 8
		int protocolLength = 33;
		// 事件触发所在节点
		String node = message.getNode();
		byte[] nodeBytes = null;
		if (node != null) {
			nodeBytes = node.getBytes(StandardCharsets.UTF_8);
			protocolLength += nodeBytes.length;
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		String clientId = message.getClientId();
		byte[] clientIdBytes = null;
		if (clientId != null) {
			clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
			protocolLength += clientIdBytes.length;
		}
		// 消息目的用户名，主要是在遗嘱消息用
		String username = message.getUsername();
		byte[] usernameBytes = null;
		if (username != null) {
			usernameBytes = username.getBytes(StandardCharsets.UTF_8);
			protocolLength += usernameBytes.length;
		}
		// topic
		String topic = message.getTopic();
		byte[] topicBytes = null;
		if (topic != null) {
			topicBytes = topic.getBytes(StandardCharsets.UTF_8);
			protocolLength += topicBytes.length;
		}
		// 消息内容
		ByteBuffer payload = message.getPayload();
		byte[] payloadBytes = null;
		if (payload != null) {
			payloadBytes = payload.array();
			protocolLength += payloadBytes.length;
		}
		// 客户端的 IPAddress
		String peerHost = message.getPeerHost();
		byte[] peerHostBytes = null;
		if (peerHost != null) {
			peerHostBytes = peerHost.getBytes(StandardCharsets.UTF_8);
			protocolLength += peerHostBytes.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(protocolLength);
		// 消息类型
		buffer.put((byte) messageType.getValue());
		// 事件触发所在节点
		if (nodeBytes != null) {
			buffer.putShort((short) nodeBytes.length);
			buffer.put(nodeBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// MQTT 消息 ID
		Integer messageId = message.getId();
		if (messageId != null) {
			buffer.putShort(messageId.shortValue());
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		if (clientIdBytes != null) {
			buffer.putShort((short) clientIdBytes.length);
			buffer.put(clientIdBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// 消息来源 用户名
		if (usernameBytes != null) {
			buffer.putShort((short) usernameBytes.length);
			buffer.put(usernameBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// topic
		if (topicBytes != null) {
			buffer.putShort((short) topicBytes.length);
			buffer.put(topicBytes);
		} else {
			buffer.put(EMPTY_SHORT_BYTES);
		}
		// dup、qos、retain
		int byte1 = 0;
		if (message.isDup()) {
			byte1 |= 0x08;
		}
		byte1 |= message.getQos() << 1;
		if (message.isRetain()) {
			byte1 |= 0x01;
		}
		buffer.put((byte) byte1);
		// 消息内容
		if (payloadBytes != null) {
			buffer.putInt(payloadBytes.length);
			buffer.put(payloadBytes);
		} else {
			buffer.put(EMPTY_INT_BYTES);
		}
		// 客户端的 IPAddress
		if (peerHostBytes != null) {
			buffer.put((byte) peerHostBytes.length);
			buffer.put(peerHostBytes);
		} else {
			buffer.put(EMPTY_BYTES);
		}
		// 存储时间
		buffer.putLong(message.getTimestamp());
		// PUBLISH 消息到达 Broker 的时间 (ms)
		Long publishReceivedAt = message.getPublishReceivedAt();
		if (publishReceivedAt != null) {
			buffer.putLong(publishReceivedAt);
		} else {
			buffer.put(EMPTY_LONG_BYTES);
		}
		return buffer.array();
	}

	@Override
	public Message deserialize(byte[] data) {
		// 1. null 或者空 byte 数组
		if (data == null || data.length < 1) {
			return null;
		}
		Message message = new Message();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		byte messageTypeByte = buffer.get();
		MessageType messageType = MessageType.valueOf(messageTypeByte);
		message.setMessageType(messageType);
		switch (messageType) {
		case CONNECT:
		case DISCONNECT:
			return deserializeConnect(buffer, message);
		case SUBSCRIBE:
			return deserializeSubscribe(buffer, message);
		case UNSUBSCRIBE:
			return deserializeUnsubscribe(buffer, message);
		case UP_STREAM:
			return deserializeUpStream(buffer, message);
		case DOWN_STREAM:
			return deserializeDownStream(buffer, message);
		default:
			throw new IllegalArgumentException("unknown message type: " + messageType);
		}
	}

	private static Message deserializeConnect(ByteBuffer buffer, Message message) {
		// 事件触发所在节点
		short nodeLength = buffer.getShort();
		if (nodeLength > 0) {
			byte[] nodeBytes = new byte[nodeLength];
			buffer.get(nodeBytes);
			message.setNode(new String(nodeBytes, StandardCharsets.UTF_8));
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		short clientIdLen = buffer.getShort();
		if (clientIdLen > 0) {
			byte[] clientIdBytes = new byte[clientIdLen];
			buffer.get(clientIdBytes);
			message.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
		}
		// 消息目的用户名，主要是在遗嘱消息用
		short usernameLen = buffer.getShort();
		if (usernameLen > 0) {
			byte[] usernameBytes = new byte[usernameLen];
			buffer.get(usernameBytes);
			message.setUsername(new String(usernameBytes, StandardCharsets.UTF_8));
		}
		// 客户端的 peerHost IPAddress
		byte peerHostLen = buffer.get();
		if (peerHostLen > 0) {
			byte[] peerHostBytes = new byte[peerHostLen];
			buffer.get(peerHostBytes);
			message.setPeerHost(new String(peerHostBytes, StandardCharsets.UTF_8));
		}
		// 存储时间
		long timestamp = buffer.getLong();
		message.setTimestamp(timestamp);
		return message;
	}

	private static Message deserializeSubscribe(ByteBuffer buffer, Message message) {
		// 事件触发所在节点
		short nodeLength = buffer.getShort();
		if (nodeLength > 0) {
			byte[] nodeBytes = new byte[nodeLength];
			buffer.get(nodeBytes);
			message.setNode(new String(nodeBytes, StandardCharsets.UTF_8));
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		short clientIdLen = buffer.getShort();
		if (clientIdLen > 0) {
			byte[] clientIdBytes = new byte[clientIdLen];
			buffer.get(clientIdBytes);
			message.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
		}
		// topic
		short topicLength = buffer.getShort();
		if (topicLength > 0) {
			byte[] topicBytes = new byte[topicLength];
			buffer.get(topicBytes);
			message.setTopic(new String(topicBytes, StandardCharsets.UTF_8));
		}
		byte qos = buffer.get();
		message.setQos(qos);
		// 客户端的 peerHost IPAddress
		byte peerHostLen = buffer.get();
		if (peerHostLen > 0) {
			byte[] peerHostBytes = new byte[peerHostLen];
			buffer.get(peerHostBytes);
			message.setPeerHost(new String(peerHostBytes, StandardCharsets.UTF_8));
		}
		// 存储时间
		long timestamp = buffer.getLong();
		message.setTimestamp(timestamp);
		return message;
	}

	private static Message deserializeUnsubscribe(ByteBuffer buffer, Message message) {
		// 事件触发所在节点
		short nodeLength = buffer.getShort();
		if (nodeLength > 0) {
			byte[] nodeBytes = new byte[nodeLength];
			buffer.get(nodeBytes);
			message.setNode(new String(nodeBytes, StandardCharsets.UTF_8));
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		short clientIdLen = buffer.getShort();
		if (clientIdLen > 0) {
			byte[] clientIdBytes = new byte[clientIdLen];
			buffer.get(clientIdBytes);
			message.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
		}
		// topic
		short topicLength = buffer.getShort();
		if (topicLength > 0) {
			byte[] topicBytes = new byte[topicLength];
			buffer.get(topicBytes);
			message.setTopic(new String(topicBytes, StandardCharsets.UTF_8));
		}
		// 客户端的 peerHost IPAddress
		byte peerHostLen = buffer.get();
		if (peerHostLen > 0) {
			byte[] peerHostBytes = new byte[peerHostLen];
			buffer.get(peerHostBytes);
			message.setPeerHost(new String(peerHostBytes, StandardCharsets.UTF_8));
		}
		// 存储时间
		long timestamp = buffer.getLong();
		message.setTimestamp(timestamp);
		return message;
	}

	private static Message deserializeUpStream(ByteBuffer buffer, Message message) {
		// 事件触发所在节点
		short nodeLength = buffer.getShort();
		if (nodeLength > 0) {
			byte[] nodeBytes = new byte[nodeLength];
			buffer.get(nodeBytes);
			message.setNode(new String(nodeBytes, StandardCharsets.UTF_8));
		}
		// MQTT 消息 ID
		int messageId = getMessageId(buffer);
		if (messageId > 0) {
			message.setId(messageId);
		}
		// 消息来源 客户端 id
		short fromClientIdLen = buffer.getShort();
		if (fromClientIdLen > 0) {
			byte[] fromClientIdBytes = new byte[fromClientIdLen];
			buffer.get(fromClientIdBytes);
			message.setFromClientId(new String(fromClientIdBytes, StandardCharsets.UTF_8));
		}
		// 消息来源 用户名
		short fromUsernameLen = buffer.getShort();
		if (fromUsernameLen > 0) {
			byte[] fromUsernameBytes = new byte[fromUsernameLen];
			buffer.get(fromUsernameBytes);
			message.setFromUsername(new String(fromUsernameBytes, StandardCharsets.UTF_8));
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		short clientIdLen = buffer.getShort();
		if (clientIdLen > 0) {
			byte[] clientIdBytes = new byte[clientIdLen];
			buffer.get(clientIdBytes);
			message.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
		}
		// 消息目的用户名，主要是在遗嘱消息用
		short usernameLen = buffer.getShort();
		if (usernameLen > 0) {
			byte[] usernameBytes = new byte[usernameLen];
			buffer.get(usernameBytes);
			message.setUsername(new String(usernameBytes, StandardCharsets.UTF_8));
		}
		// topic
		short topicLength = buffer.getShort();
		if (topicLength > 0) {
			byte[] topicBytes = new byte[topicLength];
			buffer.get(topicBytes);
			message.setTopic(new String(topicBytes, StandardCharsets.UTF_8));
		}
		// 消息类型、dup、qos、retain
		short byte1 = readUnsignedByte(buffer);
		boolean isDup = (byte1 & 0x08) == 0x08;
		message.setDup(isDup);
		// qos
		int qosLevel = (byte1 & 0x06) >> 1;
		message.setQos(qosLevel);
		// retain
		boolean retain = (byte1 & 0x01) != 0;
		message.setRetain(retain);
		// 消息内容
		int payloadLen = buffer.getInt();
		if (payloadLen > 0) {
			byte[] payloadBytes = new byte[payloadLen];
			buffer.get(payloadBytes);
			message.setPayload(ByteBuffer.wrap(payloadBytes));
		}
		// 客户端的 peerHost IPAddress
		byte peerHostLen = buffer.get();
		if (peerHostLen > 0) {
			byte[] peerHostBytes = new byte[peerHostLen];
			buffer.get(peerHostBytes);
			message.setPeerHost(new String(peerHostBytes, StandardCharsets.UTF_8));
		}
		// 存储时间
		long timestamp = buffer.getLong();
		message.setTimestamp(timestamp);
		// PUBLISH 消息到达 Broker 的时间 (ms)
		long publishReceivedAt = buffer.getLong();
		if (publishReceivedAt > 0) {
			message.setPublishReceivedAt(publishReceivedAt);
		}
		return message;
	}

	private static Message deserializeDownStream(ByteBuffer buffer, Message message) {
		// 事件触发所在节点
		short nodeLength = buffer.getShort();
		if (nodeLength > 0) {
			byte[] nodeBytes = new byte[nodeLength];
			buffer.get(nodeBytes);
			message.setNode(new String(nodeBytes, StandardCharsets.UTF_8));
		}
		// MQTT 消息 ID
		int messageId = getMessageId(buffer);
		if (messageId > 0) {
			message.setId(messageId);
		}
		// 消息目的 Client ID，主要是在遗嘱消息用
		short clientIdLen = buffer.getShort();
		if (clientIdLen > 0) {
			byte[] clientIdBytes = new byte[clientIdLen];
			buffer.get(clientIdBytes);
			message.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
		}
		// 消息目的用户名，主要是在遗嘱消息用
		short usernameLen = buffer.getShort();
		if (usernameLen > 0) {
			byte[] usernameBytes = new byte[usernameLen];
			buffer.get(usernameBytes);
			message.setUsername(new String(usernameBytes, StandardCharsets.UTF_8));
		}
		// topic
		short topicLength = buffer.getShort();
		if (topicLength > 0) {
			byte[] topicBytes = new byte[topicLength];
			buffer.get(topicBytes);
			message.setTopic(new String(topicBytes, StandardCharsets.UTF_8));
		}
		// 消息类型、dup、qos、retain
		short byte1 = readUnsignedByte(buffer);
		boolean isDup = (byte1 & 0x08) == 0x08;
		message.setDup(isDup);
		// qos
		int qosLevel = (byte1 & 0x06) >> 1;
		message.setQos(qosLevel);
		// retain
		boolean retain = (byte1 & 0x01) != 0;
		message.setRetain(retain);
		// 消息内容
		int payloadLen = buffer.getInt();
		if (payloadLen > 0) {
			byte[] payloadBytes = new byte[payloadLen];
			buffer.get(payloadBytes);
			message.setPayload(ByteBuffer.wrap(payloadBytes));
		}
		// 客户端的 peerHost IPAddress
		byte peerHostLen = buffer.get();
		if (peerHostLen > 0) {
			byte[] peerHostBytes = new byte[peerHostLen];
			buffer.get(peerHostBytes);
			message.setPeerHost(new String(peerHostBytes, StandardCharsets.UTF_8));
		}
		// 存储时间
		long timestamp = buffer.getLong();
		message.setTimestamp(timestamp);
		// PUBLISH 消息到达 Broker 的时间 (ms)
		long publishReceivedAt = buffer.getLong();
		if (publishReceivedAt > 0) {
			message.setPublishReceivedAt(publishReceivedAt);
		}
		return message;
	}

	/**
	 * read unsigned byte
	 *
	 * @param buffer ByteBuffer
	 * @return short
	 */
	private static short readUnsignedByte(ByteBuffer buffer) {
		return (short) (buffer.get() & 0xFF);
	}

	/**
	 * MessageId numberOfBytesConsumed = 2. return decoded result.
	 */
	private static int getMessageId(ByteBuffer buffer) {
		int min = 0;
		int max = 65535;
		short msbSize = readUnsignedByte(buffer);
		short lsbSize = readUnsignedByte(buffer);
		int result = msbSize << 8 | lsbSize;
		if (result < min || result > max) {
			result = -1;
		}
		return result;
	}

}
