package org.smartboot.mqtt.common.enums;

public enum MqttVersion {
	MQTT_3_1(MqttProtocolEnum.MQIsdp, (byte) 3), MQTT_3_1_1(MqttProtocolEnum.MQTT, (byte) 4),
	MQTT_5(MqttProtocolEnum.MQTT, (byte) 5);

	private final MqttProtocolEnum protocol;
	private final byte level;

	MqttVersion(MqttProtocolEnum protocolName, byte protocolLevel) {
		protocol = protocolName;
		level = protocolLevel;
	}

	public static MqttVersion getByProtocolWithVersion(MqttProtocolEnum protocolEnum, byte protocolLevel) {
		for (MqttVersion version : values()) {
			if (protocolEnum == version.protocol && version.level == protocolLevel) {
				return version;
			}
		}
		return null;
	}

	public String protocolName() {
		return protocol.getName();
	}

	public byte protocolLevel() {
		return level;
	}
}
