package org.smartboot.mqtt.common.message.properties;

import org.smartboot.mqtt.common.message.MqttCodecUtil;
import org.smartboot.mqtt.common.util.MqttPropertyConstant;
import org.smartboot.mqtt.common.util.ValidateUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/7
 */
public class MqttProperties {
	/**
	 * 认证方法
	 */
	private String authenticationMethod;
	/**
	 * 会话过期间隔.
	 * 如果会话过期间隔（Session Expiry Interval）值未指定，则使用0。
	 * 如果设置为0或者未指定，会话将在网络连接（Network Connection）关闭时结束
	 */
	private int sessionExpiryInterval;
	/**
	 * 订阅标识符
	 */
	private int subscriptionIdentifier;

	/**
	 * 遗嘱延时间隔
	 * 如果没有设置遗嘱延时间隔，遗嘱延时间隔默认值将为0，即不用延时发布遗嘱消息（Will Message）
	 */
	private int willDelayInterval;

	/**
	 * 载荷格式指示
	 */
	private byte payloadFormatIndicator;

	/**
	 * 消息过期间隔
	 */
	private int messageExpiryInterval;

	/**
	 * 内容类型
	 */
	private String contentType;

	/**
	 * 响应主题
	 */
	private String responseTopic;

	/**
	 * 对比数据
	 */
	private byte[] correlationData;
	/**
	 * 分配客户标识符
	 */
	private String assignedClientIdentifier;
	/**
	 * 服务端保持连接
	 */
	private int serverKeepAlive;
	/**
	 * 用户属性
	 */
	private final List<UserProperty> userProperties = new ArrayList<>();
	/**
	 * 请求问题信息
	 * 如果没有请求问题信息（Request Problem Information），则请求问题默认值为1
	 */
	private byte requestProblemInformation = 1;
	/**
	 * 认证数据
	 */
	private byte[] authenticationData;
	/**
	 * 请求响应信息
	 * 如果没有请求响应信息（Request Response Information），则请求响应默认值为0
	 */
	private byte requestResponseInformation;
	/**
	 * 响应信息
	 */
	private String responseInformation;

	/**
	 * 服务端参考
	 */
	private String serverReference;
	/**
	 * 原因字符串
	 */
	private String reasonString;

	/**
	 * 主题别名最大值
	 */
	private int topicAliasMaximum;

	/**
	 * 接收最大值只将被应用在当前网络连接。如果没有设置最大接收值，将使用默认值65535。
	 */
	private int receiveMaximum = 65535;
	/**
	 * 主题别名
	 */
	private int topicAlias;

	/**
	 * 最大服务质量
	 */
	private int maximumQoS;
	/**
	 * 保留可用
	 */
	private byte retainAvailable;
	/**
	 * 最大报文长度
	 */
	private Integer maximumPacketSize;
	/**
	 * 通配符订阅可用
	 */
	private byte wildcardSubscriptionAvailable;
	/**
	 * 订阅标识符可用
	 */
	private byte subscriptionIdentifierAvailable;
	/**
	 * 共享订阅可用
	 */
	private byte sharedSubscriptionAvailable;

	public void decode(ByteBuffer buffer, int validBites) {
		int remainingLength = MqttCodecUtil.decodeVariableByteInteger(buffer);
		int position;
		while (remainingLength > 0) {
			position = buffer.position();
			switch (buffer.get()) {
			//载荷格式指示
			case MqttPropertyConstant.PAYLOAD_FORMAT_INDICATOR:
				ValidateUtils.isTrue((MqttPropertyConstant.PAYLOAD_FORMAT_INDICATOR_BIT & validBites) > 0, "");
				//包含多个载荷格式指示（Payload Format Indicator）将造成协议错误（Protocol Error）
				validBites &= ~MqttPropertyConstant.PAYLOAD_FORMAT_INDICATOR_BIT;
				payloadFormatIndicator = buffer.get();
				ValidateUtils.isTrue(payloadFormatIndicator == 0 || payloadFormatIndicator == 1, "");
				break;
			//消息过期间隔
			case MqttPropertyConstant.MESSAGE_EXPIRY_INTERVAL:
				ValidateUtils.isTrue((MqttPropertyConstant.MESSAGE_EXPIRY_INTERVAL_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.MESSAGE_EXPIRY_INTERVAL_BIT;
				//包含多个消息过期间隔将导致协议错误（Protocol Error）
				ValidateUtils.isTrue(messageExpiryInterval == -1, "");
				messageExpiryInterval = buffer.getInt();
				ValidateUtils.isTrue(messageExpiryInterval > 0, "");
				break;
			//内容类型
			case MqttPropertyConstant.CONTENT_TYPE:
				ValidateUtils.isTrue((MqttPropertyConstant.CONTENT_TYPE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.CONTENT_TYPE_BIT;
				contentType = MqttCodecUtil.decodeString(buffer);
				break;
			//响应主题
			case MqttPropertyConstant.RESPONSE_TOPIC:
				ValidateUtils.isTrue((MqttPropertyConstant.RESPONSE_TOPIC_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.RESPONSE_TOPIC_BIT;
				responseTopic = MqttCodecUtil.decodeString(buffer);
				break;
			//对比数据
			case MqttPropertyConstant.CORRELATION_DATA:
				ValidateUtils.isTrue((MqttPropertyConstant.CORRELATION_DATA_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.CORRELATION_DATA_BIT;
				correlationData = MqttCodecUtil.decodeByteArray(buffer);
				break;
			//订阅标识符
			case MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER:
				//包含多个订阅标识符将造成协议错误（Protocol Error）
				ValidateUtils.isTrue((MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER_BIT;
				subscriptionIdentifier = buffer.getInt();
				//订阅标识符取值范围从1到268,435,455
				ValidateUtils.isTrue(subscriptionIdentifier >= 1 && subscriptionIdentifier <= 268435455, "");
				break;
			//会话过期间隔
			case MqttPropertyConstant.SESSION_EXPIRY_INTERVAL:
				//包含多个会话过期间隔（Session Expiry Interval）将造成协议错误（Protocol Error）
				ValidateUtils.isTrue((MqttPropertyConstant.SESSION_EXPIRY_INTERVAL_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SESSION_EXPIRY_INTERVAL_BIT;
				sessionExpiryInterval = buffer.getInt();
				break;
			//分配客户标识符
			case MqttPropertyConstant.ASSIGNED_CLIENT_IDENTIFIER:
				//包含多个分配客户标识符将造成协议错误（Protocol Error）
				ValidateUtils.isTrue((MqttPropertyConstant.ASSIGNED_CLIENT_IDENTIFIER_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.ASSIGNED_CLIENT_IDENTIFIER_BIT;
				assignedClientIdentifier = MqttCodecUtil.decodeString(buffer);
				break;
			//服务端保持连接
			case MqttPropertyConstant.SERVER_KEEP_ALIVE:
				ValidateUtils.isTrue((MqttPropertyConstant.SERVER_KEEP_ALIVE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SERVER_KEEP_ALIVE_BIT;
				serverKeepAlive = MqttCodecUtil.decodeMsbLsb(buffer);
				ValidateUtils.isTrue(serverKeepAlive >= 0, "");
				break;
			//认证方法
			case MqttPropertyConstant.AUTHENTICATION_METHOD:
				//包含多个认证方法将造成协议错误
				ValidateUtils.isTrue((MqttPropertyConstant.AUTHENTICATION_METHOD_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.AUTHENTICATION_METHOD_BIT;
				authenticationMethod = MqttCodecUtil.decodeString(buffer);
				break;
			//认证数据
			case MqttPropertyConstant.AUTHENTICATION_DATA:
				//包含多个认证数据（Authentication Data）将造成协议错误
				ValidateUtils.isTrue((MqttPropertyConstant.AUTHENTICATION_DATA_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.AUTHENTICATION_DATA_BIT;
				authenticationData = MqttCodecUtil.decodeByteArray(buffer);
				break;
			//请求问题信息
			case MqttPropertyConstant.REQUEST_PROBLEM_INFORMATION:
				ValidateUtils.isTrue((MqttPropertyConstant.REQUEST_PROBLEM_INFORMATION_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.REQUEST_PROBLEM_INFORMATION_BIT;
				requestProblemInformation = buffer.get();
				ValidateUtils.isTrue(requestProblemInformation == 0 || requestProblemInformation == 1, "");
				break;
			//遗嘱延时间隔
			case MqttPropertyConstant.WILL_DELAY_INTERVAL:
				ValidateUtils.isTrue((MqttPropertyConstant.WILL_DELAY_INTERVAL_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.WILL_DELAY_INTERVAL_BIT;
				//包含多个遗嘱延时间隔将造成协议错误（Protocol Error）
				willDelayInterval = buffer.getInt();
				ValidateUtils.isTrue(willDelayInterval >= 0, "");
				break;
			//请求响应信息
			case MqttPropertyConstant.REQUEST_RESPONSE_INFORMATION:
				ValidateUtils.isTrue((MqttPropertyConstant.REQUEST_RESPONSE_INFORMATION_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.REQUEST_RESPONSE_INFORMATION_BIT;
				requestResponseInformation = buffer.get();
				ValidateUtils.isTrue(requestResponseInformation == 0 || requestResponseInformation == 1, "");
				break;
			//响应信息
			case MqttPropertyConstant.RESPONSE_INFORMATION:
				ValidateUtils.isTrue((MqttPropertyConstant.RESPONSE_INFORMATION_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.RESPONSE_INFORMATION_BIT;
				//包含多个响应信息将造成协议错误（Protocol Error）。
				responseInformation = MqttCodecUtil.decodeString(buffer);
				break;
			//服务端参考
			case MqttPropertyConstant.SERVER_REFERENCE:
				//包含多个响应信息将造成协议错误（Protocol Error）。
				ValidateUtils.isTrue((MqttPropertyConstant.SERVER_REFERENCE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SERVER_REFERENCE_BIT;
				serverReference = MqttCodecUtil.decodeString(buffer);
				break;
			//原因字符串
			case MqttPropertyConstant.REASON_STRING:
				//包含多个原因字符串将造成协议错误（Protocol Error）。
				ValidateUtils.isTrue((MqttPropertyConstant.REASON_STRING_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.REASON_STRING_BIT;
				reasonString = MqttCodecUtil.decodeString(buffer);
				break;
			//接收最大值
			case MqttPropertyConstant.RECEIVE_MAXIMUM:
				ValidateUtils.isTrue((MqttPropertyConstant.RECEIVE_MAXIMUM_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.RECEIVE_MAXIMUM_BIT;
				receiveMaximum = MqttCodecUtil.decodeMsbLsb(buffer);
				break;
			//主题别名最大值
			case MqttPropertyConstant.TOPIC_ALIAS_MAXIMUM:
				ValidateUtils.isTrue((MqttPropertyConstant.TOPIC_ALIAS_MAXIMUM_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.TOPIC_ALIAS_MAXIMUM_BIT;
				//跟随其后的是用双字节整数表示的主题别名最大值（Topic Alias Maximum）。
				// 包含多个主题别名最大值（Topic Alias Maximum）将造成协议错误（Protocol Error）。
				topicAliasMaximum = MqttCodecUtil.decodeMsbLsb(buffer);
				ValidateUtils.isTrue(topicAliasMaximum >= 0, "");
				break;
			//最大报文长度
			case MqttPropertyConstant.TOPIC_ALIAS:
				//包含多个主题别名值将造成协议错误（Protocol Error）。
				ValidateUtils.isTrue((MqttPropertyConstant.TOPIC_ALIAS_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.TOPIC_ALIAS_BIT;
				topicAlias = MqttCodecUtil.decodeMsbLsb(buffer);
				//todo
				break;
			//最大服务质量
			case MqttPropertyConstant.MAXIMUM_QOS:
				//包含多个最大报文长度（Maximum Packet Size）或者最大报文长度（Maximum Packet Size）值为0将造成协议错误。
				ValidateUtils.isTrue((MqttPropertyConstant.MAXIMUM_QOS_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.MAXIMUM_QOS_BIT;
				maximumQoS = buffer.get();
				ValidateUtils.isTrue(maximumQoS == 0 || maximumQoS == 1, "");
				break;
			//保留可用
			case MqttPropertyConstant.RETAIN_AVAILABLE:
				ValidateUtils.isTrue((MqttPropertyConstant.RETAIN_AVAILABLE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.RETAIN_AVAILABLE_BIT;
				retainAvailable = buffer.get();
				ValidateUtils.isTrue(retainAvailable == 0 || retainAvailable == 1, "");
				break;
			//用户属性
			case MqttPropertyConstant.USER_PROPERTY:
				ValidateUtils.isTrue((MqttPropertyConstant.USER_PROPERTY_BIT & validBites) > 0, "");
				String key = MqttCodecUtil.decodeString(buffer);
				String value = MqttCodecUtil.decodeString(buffer);
				userProperties.add(new UserProperty(key, value));
				break;
			//最大报文长度
			case MqttPropertyConstant.MAXIMUM_PACKET_SIZE:
				//包含多个最大报文长度（Maximum Packet Size）或者最大报文长度（Maximum Packet Size）值为0将造成协议错误。
				ValidateUtils.isTrue((MqttPropertyConstant.MAXIMUM_PACKET_SIZE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.MAXIMUM_PACKET_SIZE_BIT;
				maximumPacketSize = buffer.getInt();
				ValidateUtils.isTrue(maximumPacketSize > 0, "");
				break;
			//通配符订阅可用
			case MqttPropertyConstant.WILDCARD_SUBSCRIPTION_AVAILABLE:
				ValidateUtils.isTrue((MqttPropertyConstant.WILDCARD_SUBSCRIPTION_AVAILABLE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.WILDCARD_SUBSCRIPTION_AVAILABLE_BIT;
				wildcardSubscriptionAvailable = buffer.get();
				ValidateUtils.isTrue(wildcardSubscriptionAvailable == 0 || wildcardSubscriptionAvailable == 1, "");
				break;
			//订阅标识符可用
			case MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER_AVAILABLE:
				ValidateUtils.isTrue((MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER_AVAILABLE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SUBSCRIPTION_IDENTIFIER_AVAILABLE_BIT;
				subscriptionIdentifierAvailable = buffer.get();
				ValidateUtils.isTrue(subscriptionIdentifierAvailable == 0 || subscriptionIdentifierAvailable == 1, "");
				break;
			//共享订阅可用
			case MqttPropertyConstant.SHARED_SUBSCRIPTION_AVAILABLE:
				ValidateUtils.isTrue((MqttPropertyConstant.SHARED_SUBSCRIPTION_AVAILABLE_BIT & validBites) > 0, "");
				validBites &= ~MqttPropertyConstant.SHARED_SUBSCRIPTION_AVAILABLE_BIT;
				sharedSubscriptionAvailable = buffer.get();
				ValidateUtils.isTrue(sharedSubscriptionAvailable == 0 || sharedSubscriptionAvailable == 1, "");
				break;
			}
			remainingLength -= buffer.position() - position;
		}
	}

	public String getAuthenticationMethod() {
		return authenticationMethod;
	}

	public int getSessionExpiryInterval() {
		return sessionExpiryInterval;
	}

	public int getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}

	public int getWillDelayInterval() {
		return willDelayInterval;
	}

	public byte getPayloadFormatIndicator() {
		return payloadFormatIndicator;
	}

	public int getMessageExpiryInterval() {
		return messageExpiryInterval;
	}

	public String getContentType() {
		return contentType;
	}

	public String getResponseTopic() {
		return responseTopic;
	}

	public byte[] getCorrelationData() {
		return correlationData;
	}

	public String getAssignedClientIdentifier() {
		return assignedClientIdentifier;
	}

	public int getServerKeepAlive() {
		return serverKeepAlive;
	}

	public List<UserProperty> getUserProperties() {
		return userProperties;
	}

	public byte getRequestProblemInformation() {
		return requestProblemInformation;
	}

	public byte[] getAuthenticationData() {
		return authenticationData;
	}

	public byte getRequestResponseInformation() {
		return requestResponseInformation;
	}

	public String getResponseInformation() {
		return responseInformation;
	}

	public String getServerReference() {
		return serverReference;
	}

	public String getReasonString() {
		return reasonString;
	}

	public int getTopicAliasMaximum() {
		return topicAliasMaximum;
	}

	public int getReceiveMaximum() {
		return receiveMaximum;
	}

	public int getTopicAlias() {
		return topicAlias;
	}

	public int getMaximumQoS() {
		return maximumQoS;
	}

	public byte getRetainAvailable() {
		return retainAvailable;
	}

	public Integer getMaximumPacketSize() {
		return maximumPacketSize;
	}

	public byte getWildcardSubscriptionAvailable() {
		return wildcardSubscriptionAvailable;
	}

	public byte getSubscriptionIdentifierAvailable() {
		return subscriptionIdentifierAvailable;
	}

	public byte getSharedSubscriptionAvailable() {
		return sharedSubscriptionAvailable;
	}

	public void setAuthenticationMethod(String authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

	public void setSessionExpiryInterval(int sessionExpiryInterval) {
		this.sessionExpiryInterval = sessionExpiryInterval;
	}

	public void setSubscriptionIdentifier(int subscriptionIdentifier) {
		this.subscriptionIdentifier = subscriptionIdentifier;
	}

	public void setWillDelayInterval(int willDelayInterval) {
		this.willDelayInterval = willDelayInterval;
	}

	public void setPayloadFormatIndicator(byte payloadFormatIndicator) {
		this.payloadFormatIndicator = payloadFormatIndicator;
	}

	public void setMessageExpiryInterval(int messageExpiryInterval) {
		this.messageExpiryInterval = messageExpiryInterval;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}

	public void setCorrelationData(byte[] correlationData) {
		this.correlationData = correlationData;
	}

	public void setAssignedClientIdentifier(String assignedClientIdentifier) {
		this.assignedClientIdentifier = assignedClientIdentifier;
	}

	public void setServerKeepAlive(int serverKeepAlive) {
		this.serverKeepAlive = serverKeepAlive;
	}

	public void setRequestProblemInformation(byte requestProblemInformation) {
		this.requestProblemInformation = requestProblemInformation;
	}

	public void setAuthenticationData(byte[] authenticationData) {
		this.authenticationData = authenticationData;
	}

	public void setRequestResponseInformation(byte requestResponseInformation) {
		this.requestResponseInformation = requestResponseInformation;
	}

	public void setResponseInformation(String responseInformation) {
		this.responseInformation = responseInformation;
	}

	public void setServerReference(String serverReference) {
		this.serverReference = serverReference;
	}

	public void setReasonString(String reasonString) {
		this.reasonString = reasonString;
	}

	public void setTopicAliasMaximum(int topicAliasMaximum) {
		this.topicAliasMaximum = topicAliasMaximum;
	}

	public void setReceiveMaximum(int receiveMaximum) {
		this.receiveMaximum = receiveMaximum;
	}

	public void setTopicAlias(int topicAlias) {
		this.topicAlias = topicAlias;
	}

	public void setMaximumQoS(int maximumQoS) {
		this.maximumQoS = maximumQoS;
	}

	public void setRetainAvailable(byte retainAvailable) {
		this.retainAvailable = retainAvailable;
	}

	public void setMaximumPacketSize(Integer maximumPacketSize) {
		this.maximumPacketSize = maximumPacketSize;
	}

	public void setWildcardSubscriptionAvailable(byte wildcardSubscriptionAvailable) {
		this.wildcardSubscriptionAvailable = wildcardSubscriptionAvailable;
	}

	public void setSubscriptionIdentifierAvailable(byte subscriptionIdentifierAvailable) {
		this.subscriptionIdentifierAvailable = subscriptionIdentifierAvailable;
	}

	public void setSharedSubscriptionAvailable(byte sharedSubscriptionAvailable) {
		this.sharedSubscriptionAvailable = sharedSubscriptionAvailable;
	}
}
