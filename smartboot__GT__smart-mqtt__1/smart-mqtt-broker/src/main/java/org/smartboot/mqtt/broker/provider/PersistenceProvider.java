package org.smartboot.mqtt.broker.provider;

import org.smartboot.mqtt.broker.provider.impl.message.PersistenceMessage;
import org.smartboot.mqtt.common.message.MqttPublishMessage;

/**
 * 消息持久化Provider
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/6/25
 */
public interface PersistenceProvider {

	/**
	 * 保存消息
	 */
	void doSave(MqttPublishMessage message);

	/**
	 * 删除指定topic的所有消息
	 */
	void delete(String topic);

	/**
	 * 获取指定位置的消息，若不存在，则获取之后最近的一条
	 */
	PersistenceMessage get(String topic, long startOffset);

	/**
	 * 获取存储topic消息最早的点位
	 */
	long getOldestOffset(String topic);

	/**
	 * 获取存储topic消息最近的点位
	 */
	long getLatestOffset(String topic);

}
