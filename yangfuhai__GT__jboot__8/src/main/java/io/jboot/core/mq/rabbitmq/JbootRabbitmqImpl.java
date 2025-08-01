/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.core.mq.rabbitmq;

import com.google.common.collect.Maps;
import com.rabbitmq.client.*;
import io.jboot.Jboot;
import io.jboot.core.cache.ehredis.JbootEhredisCacheImpl;
import io.jboot.exception.JbootException;
import io.jboot.core.mq.Jbootmq;
import io.jboot.core.mq.JbootmqBase;
import io.jboot.utils.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * doc : http://www.rabbitmq.com/api-guide.html
 */
public class JbootRabbitmqImpl extends JbootmqBase implements Jbootmq {

	Connection connection;
	Map<String, Channel> channelMap = Maps.newConcurrentMap();

	public JbootRabbitmqImpl() {

		JbootmqRabbitmqConfig config = Jboot.config(JbootmqRabbitmqConfig.class);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(config.getHost());
		factory.setPort(config.getPortAsInt());

		if (StringUtils.isNotBlank(config.getVirtualHost())) {
			factory.setVirtualHost(config.getVirtualHost());
		}
		if (StringUtils.isNotBlank(config.getUsername())) {
			factory.setUsername(config.getUsername());
		}

		if (StringUtils.isNotBlank(config.getPassword())) {
			factory.setPassword(config.getPassword());
		}

		String channelString = config.getChannel();
		if (StringUtils.isBlank(channelString)) {
			throw new JbootException("jboot.mq.rabbitmq.channel config cannot empty in jboot.properties");
		}

		try {
			connection = factory.newConnection();
		} catch (Exception e) {
			throw new JbootException("can not connection rabbitmq server", e);
		}

		String[] channels = channelString.split(",");
		for (String toChannel : channels) {
			registerListner(getChannel(toChannel), toChannel);
		}

		registerListner(getChannel(JbootEhredisCacheImpl.DEFAULT_NOTIFY_CHANNEL),
				JbootEhredisCacheImpl.DEFAULT_NOTIFY_CHANNEL);
	}

	private void registerListner(final Channel channel, String toChannel) {
		if (channel == null) {
			return;
		}
		try {

			/**
			 * Broadcast listener
			 */
			channel.basicConsume("", true, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					Object o = Jboot.me().getSerializer().deserialize(body);
					notifyListeners(envelope.getExchange(), o);
				}
			});

			/**
			 * Queue listener
			 */
			channel.basicConsume(toChannel, true, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					Object o = Jboot.me().getSerializer().deserialize(body);
					notifyListeners(envelope.getRoutingKey(), o);
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Channel getChannel(String toChannel) {

		Channel channel = channelMap.get(toChannel);
		if (channel == null) {
			try {
				channel = connection.createChannel();
				channel.queueDeclare(toChannel, false, false, false, null);
				channel.exchangeDeclare(toChannel, BuiltinExchangeType.FANOUT);
				String queueName = channel.queueDeclare().getQueue();
				channel.queueBind(queueName, toChannel, toChannel);
			} catch (IOException e) {
				throw new JbootException("can not createChannel", e);
			}

			if (channel != null) {
				channelMap.put(toChannel, channel);
			}
		}

		return channel;
	}

	@Override
	public void enqueue(Object message, String toChannel) {
		Channel channel = getChannel(toChannel);

		if (channel == null) {
			return;
		}

		try {
			byte[] bytes = Jboot.me().getSerializer().serialize(message);
			channel.basicPublish("", toChannel, MessageProperties.BASIC, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publish(Object message, String toChannel) {

		Channel channel = getChannel(toChannel);

		if (channel == null) {
			return;
		}

		try {
			byte[] bytes = Jboot.me().getSerializer().serialize(message);
			channel.basicPublish(toChannel, "", MessageProperties.BASIC, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
