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

package net.dreamlu.iot.mqtt.broker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * redis key 汇总，方便统一处理
 *
 * @author L.cm
 */
@Getter
@RequiredArgsConstructor
public enum RedisKeys {

	/**
	 * mqtt 服务端节点
	 */
	SERVER_NODES("mqtt:server:nodes:"),
	/**
	 * mqtt <-> redis pug/sub 集群内消息交互
	 */
	REDIS_CHANNEL_EXCHANGE("mqtt:channel:exchange"),
	/**
	 * 设备 -> 云端 redis pug/sub 上行消息通道，适合 mq 的集群模式消费
	 */
	REDIS_CHANNEL_UP("mqtt:channel:up"),
	/**
	 * 云端 -> 设备 redis pug/sub 下行数据通道，广播到 mqtt 集群
	 */
	REDIS_CHANNEL_DOWN("mqtt:channel:down"),
	/**
	 * 连接状态存储
	 */
	CONNECT_STATUS("mqtt:connect:status:"),
	/**
	 * 遗嘱消息存储
	 */
	MESSAGE_STORE_WILL("mqtt:messages:will:"),
	/**
	 * 保留消息存储
	 */
	MESSAGE_STORE_RETAIN("mqtt:messages:retain:"),;

	private final String key;

	/**
	 * 用于拼接后缀
	 *
	 * @param suffix 后缀
	 * @return 完整的 redis key
	 */
	public String getKey(String suffix) {
		return this.key.concat(suffix);
	}

}
