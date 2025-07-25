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

package net.dreamlu.iot.mqtt.core.server;

import net.dreamlu.iot.mqtt.codec.MqttConstant;
import net.dreamlu.iot.mqtt.core.server.auth.IMqttServerAuthHandler;
import net.dreamlu.iot.mqtt.core.server.auth.IMqttServerPublishPermission;
import net.dreamlu.iot.mqtt.core.server.auth.IMqttServerSubscribeValidator;
import net.dreamlu.iot.mqtt.core.server.auth.IMqttServerUniqueIdService;
import net.dreamlu.iot.mqtt.core.server.broker.DefaultMqttBrokerDispatcher;
import net.dreamlu.iot.mqtt.core.server.dispatcher.AbstractMqttMessageDispatcher;
import net.dreamlu.iot.mqtt.core.server.dispatcher.IMqttMessageDispatcher;
import net.dreamlu.iot.mqtt.core.server.event.IMqttConnectStatusListener;
import net.dreamlu.iot.mqtt.core.server.event.IMqttMessageListener;
import net.dreamlu.iot.mqtt.core.server.event.IMqttSessionListener;
import net.dreamlu.iot.mqtt.core.server.http.core.MqttWebServer;
import net.dreamlu.iot.mqtt.core.server.interceptor.IMqttMessageInterceptor;
import net.dreamlu.iot.mqtt.core.server.session.IMqttSessionManager;
import net.dreamlu.iot.mqtt.core.server.session.InMemoryMqttSessionManager;
import net.dreamlu.iot.mqtt.core.server.store.IMqttMessageStore;
import net.dreamlu.iot.mqtt.core.server.store.InMemoryMqttMessageStore;
import net.dreamlu.iot.mqtt.core.server.support.DefaultMqttConnectStatusListener;
import net.dreamlu.iot.mqtt.core.server.support.DefaultMqttServerAuthHandler;
import net.dreamlu.iot.mqtt.core.server.support.DefaultMqttServerProcessor;
import net.dreamlu.iot.mqtt.core.server.support.DefaultMqttServerUniqueIdServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.TioConfig;
import org.tio.core.ssl.ClientAuth;
import org.tio.core.ssl.SslConfig;
import org.tio.http.common.HttpConfig;
import org.tio.server.TioServer;
import org.tio.server.TioServerConfig;
import org.tio.server.intf.TioServerHandler;
import org.tio.server.intf.TioServerListener;
import org.tio.utils.buffer.ByteBufferAllocator;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.JsonAdapter;
import org.tio.utils.json.JsonUtil;
import org.tio.utils.thread.ThreadUtils;
import org.tio.utils.timer.DefaultTimerTaskService;
import org.tio.utils.timer.TimerTaskService;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * mqtt 服务端参数构造
 *
 * @author L.cm
 */
public class MqttServerCreator {
	private static final Logger logger = LoggerFactory.getLogger(MqttServerCreator.class);

	/**
	 * 名称
	 */
	private String name = "Mica-Mqtt-Server";
	/**
	 * 服务端 ip，默认为空，可不设置
	 */
	private String ip;
	/**
	 * 端口
	 */
	private int port = 1883;
	/**
	 * 心跳超时时间(单位: 毫秒 默认: 1000 * 120)，如果用户不希望框架层面做心跳相关工作，请把此值设为0或负数
	 */
	private Long heartbeatTimeout;
	/**
	 * MQTT 客户端 keepalive 系数，连接超时缺省为连接设置的 keepalive * keepaliveBackoff * 2，默认：0.75
	 * <p>
	 * 如果读者想对该值做一些调整，可以在此进行配置。比如设置为 0.75，则变为 keepalive * 1.5。但是该值不得小于 0.5，否则将小于 keepalive 设定的时间。
	 */
	private float keepaliveBackoff = 0.75F;
	/**
	 * 接收数据的 buffer size，默认：8k
	 */
	private int readBufferSize = MqttConstant.DEFAULT_MAX_READ_BUFFER_SIZE;
	/**
	 * 消息解析最大 bytes 长度，默认：10M
	 */
	private int maxBytesInMessage = MqttConstant.DEFAULT_MAX_BYTES_IN_MESSAGE;
	/**
	 * 堆内存和堆外内存
	 */
	private ByteBufferAllocator bufferAllocator = ByteBufferAllocator.HEAP;
	/**
	 * ssl 证书配置
	 */
	private SslConfig sslConfig;
	/**
	 * 认证处理器
	 */
	private IMqttServerAuthHandler authHandler;
	/**
	 * 唯一 id 服务
	 */
	private IMqttServerUniqueIdService uniqueIdService;
	/**
	 * 订阅校验器
	 */
	private IMqttServerSubscribeValidator subscribeValidator;
	/**
	 * 发布权限校验
	 */
	private IMqttServerPublishPermission publishPermission;
	/**
	 * 消息处理器
	 */
	private IMqttMessageDispatcher messageDispatcher;
	/**
	 * 消息存储
	 */
	private IMqttMessageStore messageStore;
	/**
	 * session 管理
	 */
	private IMqttSessionManager sessionManager;
	/**
	 * session 监听
	 */
	private IMqttSessionListener sessionListener;
	/**
	 * 消息监听
	 */
	private IMqttMessageListener messageListener;
	/**
	 * 连接状态监听
	 */
	private IMqttConnectStatusListener connectStatusListener;
	/**
	 * debug
	 */
	private boolean debug = false;
	/**
	 * mqtt 3.1 会校验此参数为 23，为了减少问题设置成了 64
	 */
	private int maxClientIdLength = MqttConstant.DEFAULT_MAX_CLIENT_ID_LENGTH;
	/**
	 * http、websocket 端口，默认：8083
	 */
	private int webPort = 8083;
	/**
	 * 开启 websocket 服务，默认：true
	 */
	private boolean websocketEnable = true;
	/**
	 * 开启 http 服务，默认：true
	 */
	private boolean httpEnable = false;
	/**
	 * http Basic 认证账号
	 */
	private String httpBasicUsername;
	/**
	 * http Basic 认证密码
	 */
	private String httpBasicPassword;
	/**
	 * 节点名称，用于处理集群
	 */
	private String nodeName;
	/**
	 * 是否用队列发送
	 */
	private boolean useQueueSend = true;
	/**
	 * 是否用队列解码（系统初始化时确定该值，中途不要变更此值，否则在切换的时候可能导致消息丢失）
	 */
	private boolean useQueueDecode = false;
	/**
	 * 是否开启监控，不开启可节省内存，默认：true
	 */
	private boolean statEnable = true;
	/**
	 * TioConfig 自定义配置
	 */
	private Consumer<TioServerConfig> tioConfigCustomize;
	/**
	 * TioConfig 自定义配置
	 */
	private BiConsumer<TioServerConfig, HttpConfig> webConfigCustomize;
	/**
	 * 消息拦截器
	 */
	private final MqttMessageInterceptors messageInterceptors = new MqttMessageInterceptors();
	/**
	 * taskService
	 */
	private TimerTaskService taskService;
	/**
	 * 业务消费线程
	 */
	private ExecutorService mqttExecutor;
	/**
	 * json 处理器
	 */
	private JsonAdapter jsonAdapter;

	public String getName() {
		return name;
	}

	public MqttServerCreator name(String name) {
		this.name = name;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public MqttServerCreator ip(String ip) {
		this.ip = ip;
		return this;
	}

	public int getPort() {
		return port;
	}

	public MqttServerCreator port(int port) {
		this.port = port;
		return this;
	}

	public Long getHeartbeatTimeout() {
		return heartbeatTimeout;
	}

	public MqttServerCreator heartbeatTimeout(Long heartbeatTimeout) {
		this.heartbeatTimeout = heartbeatTimeout;
		return this;
	}

	public float getKeepaliveBackoff() {
		return keepaliveBackoff;
	}

	public MqttServerCreator keepaliveBackoff(float keepaliveBackoff) {
		if (keepaliveBackoff <= 0.5) {
			throw new IllegalArgumentException("keepalive backoff must greater than 0.5");
		}
		this.keepaliveBackoff = keepaliveBackoff;
		return this;
	}

	public int getReadBufferSize() {
		return readBufferSize;
	}

	public MqttServerCreator readBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
		return this;
	}

	public int getMaxBytesInMessage() {
		return maxBytesInMessage;
	}

	public MqttServerCreator maxBytesInMessage(int maxBytesInMessage) {
		if (maxBytesInMessage < 1) {
			throw new IllegalArgumentException("maxBytesInMessage must be greater than 0.");
		}
		this.maxBytesInMessage = maxBytesInMessage;
		return this;
	}

	public ByteBufferAllocator getBufferAllocator() {
		return bufferAllocator;
	}

	public MqttServerCreator bufferAllocator(ByteBufferAllocator bufferAllocator) {
		this.bufferAllocator = bufferAllocator;
		return this;
	}

	public SslConfig getSslConfig() {
		return sslConfig;
	}

	public MqttServerCreator useSsl(InputStream keyStoreInputStream, String keyPasswd) {
		return sslConfig(SslConfig.forServer(keyStoreInputStream, keyPasswd));
	}

	public MqttServerCreator useSsl(InputStream keyStoreInputStream, String keyPasswd, ClientAuth clientAuth) {
		return sslConfig(SslConfig.forServer(keyStoreInputStream, keyPasswd, clientAuth));
	}

	public MqttServerCreator useSsl(InputStream keyStoreInputStream, String keyPasswd,
			InputStream trustStoreInputStream, String trustPassword, ClientAuth clientAuth) {
		return sslConfig(
				SslConfig.forServer(keyStoreInputStream, keyPasswd, trustStoreInputStream, trustPassword, clientAuth));
	}

	public MqttServerCreator useSsl(String keyStoreFile, String keyPasswd) {
		return sslConfig(SslConfig.forServer(keyStoreFile, keyPasswd));
	}

	public MqttServerCreator useSsl(String keyStoreFile, String keyPasswd, ClientAuth clientAuth) {
		return sslConfig(SslConfig.forServer(keyStoreFile, keyPasswd, clientAuth));
	}

	public MqttServerCreator useSsl(String keyStoreFile, String keyPasswd, String trustStoreFile, String trustPassword,
			ClientAuth clientAuth) {
		return sslConfig(SslConfig.forServer(keyStoreFile, keyPasswd, trustStoreFile, trustPassword, clientAuth));
	}

	public MqttServerCreator sslConfig(SslConfig sslConfig) {
		this.sslConfig = sslConfig;
		return this;
	}

	public IMqttServerAuthHandler getAuthHandler() {
		return authHandler;
	}

	public MqttServerCreator authHandler(IMqttServerAuthHandler authHandler) {
		this.authHandler = authHandler;
		return this;
	}

	public MqttServerCreator usernamePassword(String username, String password) {
		return authHandler(new DefaultMqttServerAuthHandler(username, password));
	}

	public IMqttServerUniqueIdService getUniqueIdService() {
		return uniqueIdService;
	}

	public MqttServerCreator uniqueIdService(IMqttServerUniqueIdService uniqueIdService) {
		this.uniqueIdService = uniqueIdService;
		return this;
	}

	public IMqttServerSubscribeValidator getSubscribeValidator() {
		return subscribeValidator;
	}

	public MqttServerCreator subscribeValidator(IMqttServerSubscribeValidator subscribeValidator) {
		this.subscribeValidator = subscribeValidator;
		return this;
	}

	public IMqttServerPublishPermission getPublishPermission() {
		return publishPermission;
	}

	public MqttServerCreator publishPermission(IMqttServerPublishPermission publishPermission) {
		this.publishPermission = publishPermission;
		return this;
	}

	public IMqttMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public MqttServerCreator messageDispatcher(IMqttMessageDispatcher messageDispatcher) {
		this.messageDispatcher = messageDispatcher;
		return this;
	}

	public IMqttMessageStore getMessageStore() {
		return messageStore;
	}

	public MqttServerCreator messageStore(IMqttMessageStore messageStore) {
		this.messageStore = messageStore;
		return this;
	}

	public IMqttSessionManager getSessionManager() {
		return sessionManager;
	}

	public MqttServerCreator sessionManager(IMqttSessionManager sessionManager) {
		this.sessionManager = sessionManager;
		return this;
	}

	public IMqttSessionListener getSessionListener() {
		return sessionListener;
	}

	public MqttServerCreator sessionListener(IMqttSessionListener sessionListener) {
		this.sessionListener = sessionListener;
		return this;
	}

	public IMqttMessageListener getMessageListener() {
		return messageListener;
	}

	public MqttServerCreator messageListener(IMqttMessageListener messageListener) {
		this.messageListener = messageListener;
		return this;
	}

	public IMqttConnectStatusListener getConnectStatusListener() {
		return connectStatusListener;
	}

	public MqttServerCreator connectStatusListener(IMqttConnectStatusListener connectStatusListener) {
		this.connectStatusListener = connectStatusListener;
		return this;
	}

	public boolean isDebug() {
		return debug;
	}

	public MqttServerCreator debug() {
		this.debug = true;
		return this;
	}

	public int getMaxClientIdLength() {
		return maxClientIdLength;
	}

	public MqttServerCreator maxClientIdLength(int maxClientIdLength) {
		this.maxClientIdLength = maxClientIdLength;
		return this;
	}

	public int getWebPort() {
		return webPort;
	}

	public MqttServerCreator webPort(int webPort) {
		this.webPort = webPort;
		return this;
	}

	public boolean isWebsocketEnable() {
		return websocketEnable;
	}

	public MqttServerCreator websocketEnable(boolean websocketEnable) {
		this.websocketEnable = websocketEnable;
		return this;
	}

	public boolean isHttpEnable() {
		return httpEnable;
	}

	public MqttServerCreator httpEnable(boolean httpEnable) {
		this.httpEnable = httpEnable;
		return this;
	}

	public String getHttpBasicUsername() {
		return httpBasicUsername;
	}

	public MqttServerCreator httpBasicAuth(String username, String password) {
		if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
			throw new IllegalArgumentException("Mqtt http basic auth username or password is blank.");
		}
		this.httpBasicUsername = username;
		this.httpBasicPassword = password;
		return this;
	}

	public String getHttpBasicPassword() {
		return httpBasicPassword;
	}

	public String getNodeName() {
		return nodeName;
	}

	public MqttServerCreator nodeName(String nodeName) {
		this.nodeName = nodeName;
		return this;
	}

	public boolean isUseQueueSend() {
		return useQueueSend;
	}

	public MqttServerCreator useQueueSend(boolean useQueueSend) {
		this.useQueueSend = useQueueSend;
		return this;
	}

	public boolean isUseQueueDecode() {
		return useQueueDecode;
	}

	public MqttServerCreator useQueueDecode(boolean useQueueDecode) {
		this.useQueueDecode = useQueueDecode;
		return this;
	}

	public boolean isStatEnable() {
		return statEnable;
	}

	public MqttServerCreator statEnable() {
		return statEnable(true);
	}

	public MqttServerCreator statEnable(boolean enable) {
		this.statEnable = enable;
		return this;
	}

	public MqttServerCreator tioConfigCustomize(Consumer<TioServerConfig> tioConfigCustomize) {
		this.tioConfigCustomize = tioConfigCustomize;
		return this;
	}

	public BiConsumer<TioServerConfig, HttpConfig> getWebConfigCustomize() {
		return webConfigCustomize;
	}

	public MqttServerCreator webConfigCustomize(BiConsumer<TioServerConfig, HttpConfig> webConfigCustomize) {
		this.webConfigCustomize = webConfigCustomize;
		return this;
	}

	public MqttMessageInterceptors getMessageInterceptors() {
		return messageInterceptors;
	}

	public MqttServerCreator addInterceptor(IMqttMessageInterceptor interceptor) {
		this.messageInterceptors.add(interceptor);
		return this;
	}

	public MqttServerCreator taskService(TimerTaskService taskService) {
		this.taskService = taskService;
		return this;
	}

	public ExecutorService getMqttExecutor() {
		return mqttExecutor;
	}

	public MqttServerCreator mqttExecutor(ExecutorService mqttExecutor) {
		this.mqttExecutor = mqttExecutor;
		return this;
	}

	public JsonAdapter getJsonAdapter() {
		return jsonAdapter;
	}

	public MqttServerCreator jsonAdapter(JsonAdapter jsonAdapter) {
		this.jsonAdapter = JsonUtil.getJsonAdapter(jsonAdapter);
		return this;
	}

	public MqttServer build() {
		// 默认的节点名称，用于集群
		if (StrUtil.isBlank(this.nodeName)) {
			this.nodeName = ManagementFactory.getRuntimeMXBean().getName() + ':' + port;
		}
		if (this.uniqueIdService == null) {
			this.uniqueIdService = new DefaultMqttServerUniqueIdServiceImpl();
		}
		if (this.messageDispatcher == null) {
			this.messageDispatcher = new DefaultMqttBrokerDispatcher();
		}
		if (this.sessionManager == null) {
			this.sessionManager = new InMemoryMqttSessionManager();
		}
		if (this.messageStore == null) {
			this.messageStore = new InMemoryMqttMessageStore();
		}
		if (this.connectStatusListener == null) {
			this.connectStatusListener = new DefaultMqttConnectStatusListener();
		}
		// taskService
		if (this.taskService == null) {
			this.taskService = new DefaultTimerTaskService(200L, 60);
		}
		// 业务线程池
		if (this.mqttExecutor == null) {
			this.mqttExecutor = ThreadUtils.getBizExecutor(ThreadUtils.MAX_POOL_SIZE_FOR_TIO);
		}
		// AckService
		DefaultMqttServerProcessor serverProcessor = new DefaultMqttServerProcessor(this, this.taskService,
				mqttExecutor);
		// 1. 处理消息
		TioServerHandler handler = new MqttServerAioHandler(this, serverProcessor);
		// 2. t-io 监听
		TioServerListener listener = new MqttServerAioListener(this, mqttExecutor);
		// 3. t-io 配置
		TioServerConfig tioConfig = new TioServerConfig(this.name, handler, listener);
		tioConfig.setUseQueueDecode(this.useQueueDecode);
		tioConfig.setUseQueueSend(this.useQueueSend);
		// 4. mqtt 消息最大长度，小于 1 则使用默认的，可通过 property tio.default.read.buffer.size 设置默认大小
		if (this.readBufferSize > 0) {
			tioConfig.setReadBufferSize(this.readBufferSize);
		}
		// 5. 是否开启监控
		tioConfig.statOn = this.statEnable;
		// 6. 设置 t-io 心跳 timeout
		if (this.heartbeatTimeout != null) {
			tioConfig.setHeartbeatTimeout(this.heartbeatTimeout);
		}
		if (this.sslConfig != null) {
			tioConfig.setSslConfig(this.sslConfig);
		}
		if (this.debug) {
			tioConfig.debug = true;
		}
		// 自定义处理
		if (this.tioConfigCustomize != null) {
			this.tioConfigCustomize.accept(tioConfig);
		}
		TioServer tioServer = new TioServer(tioConfig);
		// 9 配置 mqtt http/websocket server
		MqttWebServer webServer;
		logger.info("Mica mqtt http api enable:{} websocket enable:{}", this.httpEnable, this.websocketEnable);
		if (this.httpEnable || this.websocketEnable) {
			webServer = MqttWebServer.config(this, tioConfig);
		} else {
			webServer = null;
		}
		// MqttServer
		MqttServer mqttServer = new MqttServer(tioServer, webServer, this, this.taskService);
		// 9. 如果是默认的消息转发器，设置 mqttServer
		if (this.messageDispatcher instanceof AbstractMqttMessageDispatcher) {
			((AbstractMqttMessageDispatcher) this.messageDispatcher).config(mqttServer);
		}
		return mqttServer;
	}

	public MqttServer start() {
		MqttServer mqttServer = this.build();
		mqttServer.start();
		return mqttServer;
	}
}
