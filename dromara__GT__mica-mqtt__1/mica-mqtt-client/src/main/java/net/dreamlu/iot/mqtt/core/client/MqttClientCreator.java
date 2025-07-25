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

package net.dreamlu.iot.mqtt.core.client;

import net.dreamlu.iot.mqtt.codec.MqttConnectReasonCode;
import net.dreamlu.iot.mqtt.codec.MqttConstant;
import net.dreamlu.iot.mqtt.codec.MqttProperties;
import net.dreamlu.iot.mqtt.codec.MqttVersion;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.TioClientConfig;
import org.tio.client.intf.TioClientHandler;
import org.tio.client.intf.TioClientListener;
import org.tio.core.Node;
import org.tio.core.TioConfig;
import org.tio.core.ssl.SslConfig;
import org.tio.utils.buffer.ByteBufferAllocator;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.thread.ThreadUtils;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;
import org.tio.utils.timer.DefaultTimerTaskService;
import org.tio.utils.timer.TimerTaskService;

import java.io.InputStream;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * mqtt 客户端构造器
 *
 * @author L.cm
 */
public final class MqttClientCreator {
	/**
	 * 默认的心跳超时
	 */
	public static final int DEFAULT_KEEP_ALIVE_SECS = 60;
	/**
	 * 名称
	 */
	private String name = "Mica-Mqtt-Client";
	/**
	 * ip，可为空，默认为 127.0.0.1
	 */
	private String ip = "127.0.0.1";
	/**
	 * 端口，默认：1883
	 */
	private int port = 1883;
	/**
	 * 超时时间，t-io 配置，可为 null，默认为：5秒
	 */
	private Integer timeout;
	/**
	 * 接收数据的 buffer size，默认：8k
	 */
	private int readBufferSize = MqttConstant.DEFAULT_MAX_READ_BUFFER_SIZE;
	/**
	 * 消息解析最大 bytes 长度，默认：8092
	 */
	private int maxBytesInMessage = MqttConstant.DEFAULT_MAX_BYTES_IN_MESSAGE;
	/**
	 * mqtt 3.1 会校验此参数为 23，为了减少问题设置成了 64
	 */
	private int maxClientIdLength = MqttConstant.DEFAULT_MAX_CLIENT_ID_LENGTH;
	/**
	 * Keep Alive (s)，如果用户不希望框架层面做心跳相关工作，请把此值设为0或负数
	 */
	private int keepAliveSecs = DEFAULT_KEEP_ALIVE_SECS;
	/**
	 * SSL配置
	 */
	private SslConfig sslConfig;
	/**
	 * 自动重连
	 */
	private boolean reconnect = true;
	/**
	 * 重连的间隔时间，单位毫秒，默认：5000
	 */
	private long reInterval = 5000;
	/**
	 * 连续重连次数，当连续重连这么多次都失败时，不再重连。0和负数则一直重连
	 */
	private int retryCount = 0;
	/**
	 * 重连，重新订阅一个批次大小，默认：20
	 */
	private int reSubscribeBatchSize = 20;
	/**
	 * 客户端 id，默认：随机生成
	 */
	private String clientId;
	/**
	 * mqtt 协议，默认：3_1_1
	 */
	private MqttVersion version = MqttVersion.MQTT_3_1_1;
	/**
	 * 用户名
	 */
	private String username = null;
	/**
	 * 密码
	 */
	private String password = null;
	/**
	 * 清除会话
	 * <p>
	 * false 表示如果订阅的客户机断线了，那么要保存其要推送的消息，如果其重新连接时，则将这些消息推送。
	 * true 表示消除，表示客户机是第一次连接，消息所以以前的连接信息。
	 * </p>
	 */
	private boolean cleanSession = true;
	/**
	 * mqtt 5.0 session 有效期，单位秒
	 */
	private Integer sessionExpiryIntervalSecs;
	/**
	 * 遗嘱消息
	 */
	private MqttWillMessage willMessage;
	/**
	 * mqtt5 properties
	 */
	private MqttProperties properties;
	/**
	 * ByteBuffer Allocator，支持堆内存和堆外内存，默认为：堆内存
	 */
	private ByteBufferAllocator bufferAllocator = ByteBufferAllocator.HEAP;
	/**
	 * 连接监听器
	 */
	private IMqttClientConnectListener connectListener;
	/**
	 * 客户端 session
	 */
	private IMqttClientSession clientSession;
	/**
	 * messageId 生成器
	 */
	private IMqttClientMessageIdGenerator messageIdGenerator;
	/**
	 * 是否开启监控，默认：false 不开启，节省内存
	 */
	private boolean statEnable = false;
	/**
	 * debug
	 */
	private boolean debug = false;
	/**
	 * tioExecutor
	 */
	private SynThreadPoolExecutor tioExecutor;
	/**
	 * groupExecutor
	 */
	private ExecutorService groupExecutor;
	/**
	 * mqttExecutor
	 */
	private ExecutorService mqttExecutor;
	/**
	 * taskService
	 */
	private TimerTaskService taskService;
	/**
	 * TioConfig 自定义配置
	 */
	private Consumer<TioConfig> tioConfigCustomize;

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public int getReadBufferSize() {
		return readBufferSize;
	}

	public int getMaxBytesInMessage() {
		return maxBytesInMessage;
	}

	public int getMaxClientIdLength() {
		return maxClientIdLength;
	}

	public int getKeepAliveSecs() {
		return keepAliveSecs;
	}

	public SslConfig getSslConfig() {
		return sslConfig;
	}

	public boolean isReconnect() {
		return reconnect;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public long getReInterval() {
		return reInterval;
	}

	public int getReSubscribeBatchSize() {
		return reSubscribeBatchSize;
	}

	public String getClientId() {
		return clientId;
	}

	public MqttVersion getVersion() {
		return version;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public Integer getSessionExpiryIntervalSecs() {
		return sessionExpiryIntervalSecs;
	}

	public MqttWillMessage getWillMessage() {
		return willMessage;
	}

	public MqttProperties getProperties() {
		return properties;
	}

	public ByteBufferAllocator getBufferAllocator() {
		return bufferAllocator;
	}

	public IMqttClientConnectListener getConnectListener() {
		return connectListener;
	}

	public IMqttClientSession getClientSession() {
		return clientSession;
	}

	public IMqttClientMessageIdGenerator getMessageIdGenerator() {
		return messageIdGenerator;
	}

	public boolean isStatEnable() {
		return statEnable;
	}

	public boolean isDebug() {
		return debug;
	}

	public SynThreadPoolExecutor getTioExecutor() {
		return tioExecutor;
	}

	public ExecutorService getGroupExecutor() {
		return groupExecutor;
	}

	public ExecutorService getMqttExecutor() {
		return mqttExecutor;
	}

	public TimerTaskService getTaskService() {
		return taskService;
	}

	public MqttClientCreator name(String name) {
		this.name = name;
		return this;
	}

	public MqttClientCreator ip(String ip) {
		this.ip = ip;
		return this;
	}

	public MqttClientCreator port(int port) {
		this.port = port;
		return this;
	}

	public MqttClientCreator timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public MqttClientCreator readBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
		return this;
	}

	public MqttClientCreator maxBytesInMessage(int maxBytesInMessage) {
		this.maxBytesInMessage = maxBytesInMessage;
		return this;
	}

	public MqttClientCreator maxClientIdLength(int maxClientIdLength) {
		this.maxClientIdLength = maxClientIdLength;
		return this;
	}

	public MqttClientCreator keepAliveSecs(int keepAliveSecs) {
		this.keepAliveSecs = keepAliveSecs;
		return this;
	}

	public MqttClientCreator useSsl() {
		return sslConfig(SslConfig.forClient());
	}

	public MqttClientCreator useSsl(String trustStoreFile, String trustPassword) {
		return sslConfig(SslConfig.forClient(trustStoreFile, trustPassword));
	}

	public MqttClientCreator useSsl(String keyStoreFile, String keyPasswd, String trustStoreFile,
			String trustPassword) {
		return sslConfig(SslConfig.forClient(keyStoreFile, keyPasswd, trustStoreFile, trustPassword));
	}

	public MqttClientCreator useSsl(InputStream trustStoreInputStream, String trustPassword) {
		return sslConfig(SslConfig.forClient(trustStoreInputStream, trustPassword));
	}

	public MqttClientCreator useSsl(InputStream keyStoreInputStream, String keyPasswd,
			InputStream trustStoreInputStream, String trustPassword) {
		return sslConfig(SslConfig.forClient(keyStoreInputStream, keyPasswd, trustStoreInputStream, trustPassword));
	}

	public MqttClientCreator sslConfig(SslConfig sslConfig) {
		this.sslConfig = sslConfig;
		return this;
	}

	public MqttClientCreator reconnect(boolean reconnect) {
		this.reconnect = reconnect;
		return this;
	}

	public MqttClientCreator retryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}

	public MqttClientCreator reInterval(long reInterval) {
		this.reInterval = reInterval;
		return this;
	}

	public MqttClientCreator reSubscribeBatchSize(int reSubscribeBatchSize) {
		this.reSubscribeBatchSize = reSubscribeBatchSize;
		return this;
	}

	public MqttClientCreator clientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public MqttClientCreator version(MqttVersion version) {
		this.version = version;
		return this;
	}

	public MqttClientCreator username(String username) {
		this.username = username;
		return this;
	}

	public MqttClientCreator password(String password) {
		this.password = password;
		return this;
	}

	public MqttClientCreator cleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
		return this;
	}

	public MqttClientCreator sessionExpiryIntervalSecs(Integer sessionExpiryIntervalSecs) {
		this.sessionExpiryIntervalSecs = sessionExpiryIntervalSecs;
		return this;
	}

	public MqttClientCreator willMessage(MqttWillMessage willMessage) {
		this.willMessage = willMessage;
		return this;
	}

	public MqttClientCreator willMessage(Consumer<MqttWillMessage.Builder> consumer) {
		MqttWillMessage.Builder builder = MqttWillMessage.builder();
		consumer.accept(builder);
		return willMessage(builder.build());
	}

	public MqttClientCreator properties(MqttProperties properties) {
		this.properties = properties;
		return this;
	}

	public MqttClientCreator bufferAllocator(ByteBufferAllocator allocator) {
		this.bufferAllocator = allocator;
		return this;
	}

	public MqttClientCreator connectListener(IMqttClientConnectListener connectListener) {
		this.connectListener = connectListener;
		return this;
	}

	public MqttClientCreator clientSession(IMqttClientSession clientSession) {
		this.clientSession = clientSession;
		return this;
	}

	public MqttClientCreator messageIdGenerator(IMqttClientMessageIdGenerator messageIdGenerator) {
		this.messageIdGenerator = messageIdGenerator;
		return this;
	}

	public MqttClientCreator statEnable() {
		return statEnable(true);
	}

	public MqttClientCreator statEnable(boolean enable) {
		this.statEnable = enable;
		return this;
	}

	public MqttClientCreator debug() {
		this.debug = true;
		return this;
	}

	public MqttClientCreator tioExecutor(SynThreadPoolExecutor tioExecutor) {
		this.tioExecutor = tioExecutor;
		return this;
	}

	public MqttClientCreator groupExecutor(ExecutorService groupExecutor) {
		this.groupExecutor = groupExecutor;
		return this;
	}

	public MqttClientCreator mqttExecutor(ExecutorService mqttExecutor) {
		this.mqttExecutor = mqttExecutor;
		return this;
	}

	public MqttClientCreator taskService(TimerTaskService taskService) {
		this.taskService = taskService;
		return this;
	}

	public MqttClientCreator tioConfigCustomize(Consumer<TioConfig> tioConfigCustomize) {
		this.tioConfigCustomize = tioConfigCustomize;
		return this;
	}

	private MqttClient build() {
		// 1. clientId 为空，生成默认的 clientId
		if (StrUtil.isBlank(this.clientId)) {
			// 默认为：MICA-MQTT- 前缀和 36进制的纳秒数
			this.clientId("MICA-MQTT-" + Long.toString(System.nanoTime(), 36));
		}
		// 2. 客户端 session
		if (this.clientSession == null) {
			this.clientSession = new DefaultMqttClientSession();
		}
		// 3. 消息id 生成器
		if (this.messageIdGenerator == null) {
			this.messageIdGenerator = new DefaultMqttClientMessageIdGenerator();
		}
		// tioExecutor
		if (this.tioExecutor == null) {
			this.tioExecutor = ThreadUtils.getTioExecutor(3);
		}
		// groupExecutor
		if (this.groupExecutor == null) {
			this.groupExecutor = ThreadUtils.getGroupExecutor(2);
		}
		// mqttExecutor
		if (this.mqttExecutor == null) {
			this.mqttExecutor = ThreadUtils.getBizExecutor(2);
		}
		// taskService
		if (this.taskService == null) {
			this.taskService = new DefaultTimerTaskService();
		}
		IMqttClientProcessor processor = new DefaultMqttClientProcessor(this);
		// 4. 初始化 mqtt 处理器
		TioClientHandler clientAioHandler = new MqttClientAioHandler(this, processor);
		TioClientListener clientAioListener = new MqttClientAioListener(this);
		// 5. 重连配置
		ReconnConf reconnConf = null;
		if (this.reconnect) {
			reconnConf = new ReconnConf(this.reInterval, this.retryCount);
		}
		// 6. tioConfig
		TioClientConfig tioConfig = new TioClientConfig(clientAioHandler, clientAioListener, reconnConf, tioExecutor,
				groupExecutor);
		tioConfig.setName(this.name);
		// 7. 心跳超时时间，关闭默认的心跳检测，不符合 emqx
		tioConfig.setHeartbeatTimeout(0);
		// 8. mqtt 消息最大长度，小于 1 则使用默认的，可通过 property tio.default.read.buffer.size 设置默认大小
		if (this.readBufferSize > 0) {
			tioConfig.setReadBufferSize(this.readBufferSize);
		}
		// 9. ssl 证书设置
		tioConfig.setSslConfig(this.sslConfig);
		// 10. 是否开启监控
		tioConfig.statOn = this.statEnable;
		if (this.debug) {
			tioConfig.debug = true;
		}
		// 11. 自定义处理
		if (this.tioConfigCustomize != null) {
			this.tioConfigCustomize.accept(tioConfig);
		}
		// 12. tioClient
		try {
			TioClient tioClient = new TioClient(tioConfig);
			return new MqttClient(tioClient, this);
		} catch (Exception e) {
			throw new IllegalStateException("Mica mqtt client start fail.", e);
		}
	}

	/**
	 * 默认异步连接
	 *
	 * @return TioClient
	 */
	public MqttClient connect() {
		return this.build().start(false);
	}

	/**
	 * 同步连接
	 *
	 * @return TioClient
	 */
	public MqttClient connectSync() {
		return this.build().start(true);
	}

	/**
	 * 连接测试
	 *
	 * @return MqttConnectReasonCode
	 */
	public MqttConnectReasonCode connectTest() {
		return connectTest(3, TimeUnit.SECONDS);
	}

	/**
	 * 连接测试
	 *
	 * @return MqttConnectReasonCode
	 */
	public MqttConnectReasonCode connectTest(long timeout, TimeUnit timeUnit) {
		// 1. clientId 为空，生成默认的 clientId
		if (StrUtil.isBlank(this.clientId)) {
			// 默认为：MICA-MQTT- 前缀和 36进制的纳秒数
			this.clientId("MICA-MQTT-" + Long.toString(System.nanoTime(), 36));
		}
		CompletableFuture<MqttConnectReasonCode> future = new CompletableFuture<>();
		IMqttClientProcessor processor = new MqttClientConnectTestProcessor(future);
		// 2. 初始化 mqtt 处理器
		TioClientHandler clientAioHandler = new MqttClientAioHandler(this, processor);
		TioClientListener clientAioListener = new MqttClientAioListener(this);
		// 3. tioConfig
		TioClientConfig tioConfig = new TioClientConfig(clientAioHandler, clientAioListener);
		tioConfig.setName(this.name);
		// 4. 心跳超时时间，关闭心跳检测
		tioConfig.setHeartbeatTimeout(0);
		TioClient tioClient;
		try {
			tioClient = new TioClient(tioConfig);
			tioClient.asyncConnect(new Node(this.getIp(), this.getPort()));
		} catch (Exception e) {
			throw new IllegalStateException("Mica mqtt client start fail.", e);
		}
		try {
			return future.get(timeout, timeUnit);
		} catch (Exception e) {
			// 超时，一般为服务器不可用
			return MqttConnectReasonCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
		} finally {
			tioClient.stop();
		}
	}

}
