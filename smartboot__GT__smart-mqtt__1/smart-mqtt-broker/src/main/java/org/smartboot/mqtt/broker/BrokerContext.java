package org.smartboot.mqtt.broker;

import org.smartboot.mqtt.broker.eventbus.messagebus.MessageBus;
import org.smartboot.mqtt.broker.provider.Providers;
import org.smartboot.mqtt.common.eventbus.EventBus;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author 三刀
 * @version V1.0 , 2018/4/26
 */
public interface BrokerContext {

	/**
	 * 初始化Broker上下文
	 */
	void init() throws IOException;

	BrokerConfigure getBrokerConfigure();

	void addSession(MqttSession session);

	MqttSession removeSession(String clientId);

	MqttSession getSession(String clientId);

	/**
	 * 获取Topic，如果不存在将创建
	 *
	 * @param topic
	 * @return
	 */
	BrokerTopic getOrCreateTopic(String topic);

	/**
	 * 获得当前的Topic列表
	 */
	Collection<BrokerTopic> getTopics();

	/**
	 * 获取消息总线
	 *
	 * @return
	 */
	MessageBus getMessageBus();

	/**
	 * 获取事件总线
	 *
	 * @return
	 */
	EventBus getEventBus();

	ScheduledExecutorService getKeepAliveThreadPool();

	void destroy();

	Providers getProviders();

	/**
	 * 解析配置文件
	 */
	<T> T parseConfig(String path, Class<T> clazz);

	MqttBrokerMessageProcessor getMessageProcessor();
}
