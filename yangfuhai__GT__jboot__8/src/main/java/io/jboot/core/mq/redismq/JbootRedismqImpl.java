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
package io.jboot.core.mq.redismq;

import com.google.common.collect.Sets;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.component.redis.JbootRedisManager;
import io.jboot.core.cache.ehredis.JbootEhredisCacheImpl;
import io.jboot.component.redis.JbootRedis;
import io.jboot.exception.JbootException;
import io.jboot.core.mq.Jbootmq;
import io.jboot.core.mq.JbootmqBase;
import io.jboot.utils.StringUtils;
import redis.clients.jedis.BinaryJedisPubSub;

import java.util.Set;

public class JbootRedismqImpl extends JbootmqBase implements Jbootmq, Runnable {

	private static final Log LOG = Log.getLog(JbootRedismqImpl.class);

	JbootRedis redis;
	Thread dequeueThread;
	Set<String> channnels;

	public JbootRedismqImpl() {
		JbootmqRedisConfig redisConfig = Jboot.config(JbootmqRedisConfig.class);
		if (redisConfig.isConfigOk()) {
			redis = JbootRedisManager.me().getRedis(redisConfig);
		} else {
			redis = Jboot.me().getRedis();
		}

		if (redis == null) {
			throw new JbootException("can not get redis,please check your jboot.properties");
		}

		String channelString = redisConfig.getChannel();
		if (StringUtils.isBlank(channelString)) {
			throw new JbootException("channel config cannot empty in jboot.properties");
		}

		if (channelString.endsWith(",")) {
			channelString += JbootEhredisCacheImpl.DEFAULT_NOTIFY_CHANNEL;
		} else {
			channelString += "," + JbootEhredisCacheImpl.DEFAULT_NOTIFY_CHANNEL;
		}

		String[] channels = channelString.split(",");
		this.channnels = Sets.newHashSet(channels);
		redis.subscribe(new BinaryJedisPubSub() {
			@Override
			public void onMessage(byte[] channel, byte[] message) {
				notifyListeners(redis.bytesToKey(channel), Jboot.me().getSerializer().deserialize(message));
			}
		}, redis.keysToBytesArray(channels));

		dequeueThread = new Thread(this);
		dequeueThread.start();
	}

	@Override
	public void enqueue(Object message, String toChannel) {
		redis.lpush(toChannel, message);
	}

	@Override
	public void publish(Object message, String toChannel) {
		redis.publish(redis.keyToBytes(toChannel), Jboot.me().getSerializer().serialize(message));
	}

	@Override
	public void run() {
		for (;;) {
			try {
				doExecuteDequeue();
				Thread.sleep(100);
			} catch (Throwable ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}

	private void doExecuteDequeue() {
		for (String channel : this.channnels) {
			Object data = redis.lpop(channel);
			if (data != null) {
				notifyListeners(channel, data);
			}
		}
	}
}
