package org.smartboot.mqtt.client;

import org.smartboot.mqtt.common.enums.MqttQoS;
import org.smartboot.mqtt.common.enums.MqttVersion;
import org.smartboot.mqtt.common.message.MqttMessage;
import org.smartboot.mqtt.common.message.WillMessage;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import java.util.Properties;

public class MqttClientConfigure {
	/**
	 * The default keep alive interval in seconds if one is not specified
	 */
	public static final int KEEP_ALIVE_INTERVAL_DEFAULT = 60;
	/**
	 * The default max inflight if one is not specified
	 */
	public static final int MAX_INFLIGHT_DEFAULT = 10;
	/**
	 * The default clean session setting if one is not specified
	 */
	public static final boolean CLEAN_SESSION_DEFAULT = true;

	private int keepAliveInterval = KEEP_ALIVE_INTERVAL_DEFAULT;
	private int maxInflight = MAX_INFLIGHT_DEFAULT;
	private WillMessage willMessage;
	private String userName;
	private byte[] password;
	private SocketFactory socketFactory;
	private Properties sslClientProps = null;
	private boolean httpsHostnameVerificationEnabled = true;
	private HostnameVerifier sslHostnameVerifier = null;
	private boolean cleanSession = CLEAN_SESSION_DEFAULT;
	private int connectionTimeout = 5000;
	/**
	 * IO缓冲区大小
	 */
	private int bufferSize = 4 * 1024;

	/**
	 * MQTT最大报文限制字节数
	 */
	private int maxPacketSize = 1048576;

	private int connectAckTimeout = 5;
	private String[] serverURIs = null;
	private MqttVersion mqttVersion = MqttVersion.MQTT_3_1_1;
	/**
	 * 自动重连
	 */
	private boolean automaticReconnect = false;
	private int maxReconnectDelay = 128000;
	private Properties customWebSocketHeaders = null;

	// Client Operation Parameters
	private int executorServiceTimeout = 1; // How long to wait in seconds when terminating the executor service.

	private String host;
	private int port;

	private TopicListener topicListener = new TopicListener() {
		@Override
		public void subscribe(String topicFilter, MqttQoS mqttQoS) {

		}

		@Override
		public void unsubscribe(String topicFilter) {

		}
	};

	/**
	 * Constructs a new <code>MqttConnectOptions</code> object using the default
	 * values.
	 * <p>
	 * The defaults are:
	 * <ul>
	 * <li>The keepalive interval is 60 seconds</li>
	 * <li>Clean Session is true</li>
	 * <li>The message delivery retry interval is 15 seconds</li>
	 * <li>The connection timeout period is 30 seconds</li>
	 * <li>No Will message is set</li>
	 * <li>A standard SocketFactory is used</li>
	 * </ul>
	 * More information about these values can be found in the setter methods.
	 */
	public MqttClientConfigure() {
		// Initialise Base MqttConnectOptions Object
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	/**
	 * Returns the user name to use for the connection.
	 *
	 * @return the user name to use for the connection.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name to use for the connection.
	 *
	 * @param userName The Username as a String
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Get the maximum time (in millis) to wait between reconnects
	 *
	 * @return Get the maximum time (in millis) to wait between reconnects
	 */
	public int getMaxReconnectDelay() {
		return maxReconnectDelay;
	}

	/**
	 * Set the maximum time to wait between reconnects
	 *
	 * @param maxReconnectDelay the duration (in millis)
	 */
	public void setMaxReconnectDelay(int maxReconnectDelay) {
		this.maxReconnectDelay = maxReconnectDelay;
	}

	/**
	 * Sets the "Last Will and Testament" (LWT) for the connection. In the event
	 * that this client unexpectedly loses its connection to the server, the server
	 * will publish a message to itself using the supplied details.
	 *
	 * @param topic
	 *            the topic to publish to.
	 * @param payload
	 *            the byte payload for the message.
	 * @param qos
	 *            the quality of service to publish the message at (0, 1 or 2).
	 * @param retained
	 *            whether or not the message should be retained.
	 */
	//    public void setWill(String topic, byte[] payload, int qos, boolean retained) {
	//        validateWill(topic, payload);
	//        new MqttPublishMessage();
	//        this.setWill(topic, new MqttMessage(payload), qos, retained);
	//    }

	/**
	 * Validates the will fields.
	 */
	//    private void validateWill(String dest, Object payload) {
	//        if ((dest == null) || (payload == null)) {
	//            throw new IllegalArgumentException();
	//        }
	//
	//        MqttTopic.validate(dest, false/* wildcards NOT allowed */);
	//    }

	/**
	 * Sets up the will information, based on the supplied parameters.
	 *
	 * @param topic
	 *            the topic to send the LWT message to
	 * @param msg
	 *            the {@link MqttMessage} to send
	 * @param qos
	 *            the QoS Level to send the message at
	 * @param retained
	 *            whether the message should be retained or not
	 */
	//    protected void setWill(String topic, MqttMessage msg, int qos, boolean retained) {
	//        willDestination = topic;
	//        willMessage = msg;
	//        willMessage.setQos(qos);
	//        willMessage.setRetained(retained);
	//        // Prevent any more changes to the will message
	//        willMessage.setMutable(false);
	//    }

	/**
	 * Returns the "keep alive" interval.
	 *
	 * @return the keep alive interval.
	 * @see #setKeepAliveInterval(int)
	 */
	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	/**
	 * Sets the "keep alive" interval. This value, measured in seconds, defines the
	 * maximum time interval between messages sent or received. It enables the
	 * client to detect if the server is no longer available, without having to wait
	 * for the TCP/IP timeout. The client will ensure that at least one message
	 * travels across the network within each keep alive period. In the absence of a
	 * data-related message during the time period, the client sends a very small
	 * "ping" message, which the server will acknowledge. A value of 0 disables
	 * keepalive processing in the client.
	 * <p>
	 * The default value is 60 seconds
	 * </p>
	 *
	 * @param keepAliveInterval the interval, measured in seconds, must be &gt;= 0.
	 * @throws IllegalArgumentException if the keepAliveInterval was invalid
	 */
	public MqttClientConfigure setKeepAliveInterval(int keepAliveInterval) throws IllegalArgumentException {
		if (keepAliveInterval < 0) {
			throw new IllegalArgumentException();
		}
		this.keepAliveInterval = keepAliveInterval;
		return this;
	}

	/**
	 * Returns the MQTT version.
	 *
	 * @return the MQTT version.
	 */
	public MqttVersion getMqttVersion() {
		return mqttVersion;
	}

	/**
	 * Sets the MQTT version. The default action is to connect with version 3.1.1,
	 * and to fall back to 3.1 if that fails. Version 3.1.1 or 3.1 can be selected
	 * specifically, with no fall back, by using the MQTT_VERSION_3_1_1 or
	 * MQTT_VERSION_3_1 options respectively.
	 *
	 * @param mqttVersion the version of the MQTT protocol.
	 * @throws IllegalArgumentException If the MqttVersion supplied is invalid
	 */
	public void setMqttVersion(MqttVersion mqttVersion) throws IllegalArgumentException {
		if (mqttVersion != MqttVersion.MQTT_3_1 && mqttVersion != MqttVersion.MQTT_3_1_1) {
			throw new IllegalArgumentException(
					"An incorrect version was used \"" + mqttVersion + "\". Acceptable version options are "
							+ MqttVersion.MQTT_3_1 + " and " + MqttVersion.MQTT_3_1_1 + ".");
		}
		this.mqttVersion = mqttVersion;
	}

	/**
	 * Returns the "max inflight". The max inflight limits to how many messages we
	 * can send without receiving acknowledgments.
	 *
	 * @return the max inflight
	 * @see #setMaxInflight(int)
	 */
	public int getMaxInflight() {
		return maxInflight;
	}

	/**
	 * Sets the "max inflight". please increase this value in a high traffic
	 * environment.
	 * <p>
	 * The default value is 10
	 * </p>
	 *
	 * @param maxInflight the number of maxInfligt messages
	 */
	public void setMaxInflight(int maxInflight) {
		if (maxInflight < 0) {
			throw new IllegalArgumentException();
		}
		this.maxInflight = maxInflight;
	}

	/**
	 * Returns the connection timeout value.
	 *
	 * @return the connection timeout value.
	 * @see #setConnectionTimeout(int)
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		if (connectionTimeout < 0) {
			throw new IllegalArgumentException();
		}
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Returns the socket factory that will be used when connecting, or
	 * <code>null</code> if one has not been set.
	 *
	 * @return The Socket Factory
	 */
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	/**
	 * Sets the <code>SocketFactory</code> to use. This allows an application to
	 * apply its own policies around the creation of network sockets. If using an
	 * SSL connection, an <code>SSLSocketFactory</code> can be used to supply
	 * application-specific security settings.
	 *
	 * @param socketFactory the factory to use.
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	public WillMessage getWillMessage() {
		return willMessage;
	}

	public void setWillMessage(WillMessage willMessage) {
		this.willMessage = willMessage;
	}

	/**
	 * Returns the SSL properties for the connection.
	 *
	 * @return the properties for the SSL connection
	 */
	public Properties getSSLProperties() {
		return sslClientProps;
	}

	/**
	 * Sets the SSL properties for the connection.
	 * <p>
	 * Note that these properties are only valid if an implementation of the Java
	 * Secure Socket Extensions (JSSE) is available. These properties are
	 * <em>not</em> used if a SocketFactory has been set using
	 * {@link #setSocketFactory(SocketFactory)}. The following properties can be
	 * used:
	 * </p>
	 * <dl>
	 * <dt>com.ibm.ssl.protocol</dt>
	 * <dd>One of: SSL, SSLv3, TLS, TLSv1, SSL_TLS.</dd>
	 * <dt>com.ibm.ssl.contextProvider
	 * <dd>Underlying JSSE provider. For example "IBMJSSE2" or "SunJSSE"</dd>
	 *
	 * <dt>com.ibm.ssl.keyStore</dt>
	 * <dd>The name of the file that contains the KeyStore object that you want the
	 * KeyManager to use. For example /mydir/etc/key.p12</dd>
	 *
	 * <dt>com.ibm.ssl.keyStorePassword</dt>
	 * <dd>The password for the KeyStore object that you want the KeyManager to use.
	 * The password can either be in plain-text, or may be obfuscated using the
	 * static method:
	 * <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>. This
	 * obfuscates the password using a simple and insecure XOR and Base64 encoding
	 * mechanism. Note that this is only a simple scrambler to obfuscate clear-text
	 * passwords.</dd>
	 *
	 * <dt>com.ibm.ssl.keyStoreType</dt>
	 * <dd>Type of key store, for example "PKCS12", "JKS", or "JCEKS".</dd>
	 *
	 * <dt>com.ibm.ssl.keyStoreProvider</dt>
	 * <dd>Key store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
	 *
	 * <dt>com.ibm.ssl.trustStore</dt>
	 * <dd>The name of the file that contains the KeyStore object that you want the
	 * TrustManager to use.</dd>
	 *
	 * <dt>com.ibm.ssl.trustStorePassword</dt>
	 * <dd>The password for the TrustStore object that you want the TrustManager to
	 * use. The password can either be in plain-text, or may be obfuscated using the
	 * static method:
	 * <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>. This
	 * obfuscates the password using a simple and insecure XOR and Base64 encoding
	 * mechanism. Note that this is only a simple scrambler to obfuscate clear-text
	 * passwords.</dd>
	 *
	 * <dt>com.ibm.ssl.trustStoreType</dt>
	 * <dd>The type of KeyStore object that you want the default TrustManager to
	 * use. Same possible values as "keyStoreType".</dd>
	 *
	 * <dt>com.ibm.ssl.trustStoreProvider</dt>
	 * <dd>Trust store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
	 *
	 * <dt>com.ibm.ssl.enabledCipherSuites</dt>
	 * <dd>A list of which ciphers are enabled. Values are dependent on the
	 * provider, for example:
	 * SSL_RSA_WITH_AES_128_CBC_SHA;SSL_RSA_WITH_3DES_EDE_CBC_SHA.</dd>
	 *
	 * <dt>com.ibm.ssl.keyManager</dt>
	 * <dd>Sets the algorithm that will be used to instantiate a KeyManagerFactory
	 * object instead of using the default algorithm available in the platform.
	 * Example values: "IbmX509" or "IBMJ9X509".</dd>
	 *
	 * <dt>com.ibm.ssl.trustManager</dt>
	 * <dd>Sets the algorithm that will be used to instantiate a TrustManagerFactory
	 * object instead of using the default algorithm available in the platform.
	 * Example values: "PKIX" or "IBMJ9X509".</dd>
	 * </dl>
	 *
	 * @param props The SSL {@link Properties}
	 */
	public void setSSLProperties(Properties props) {
		this.sslClientProps = props;
	}

	public boolean isHttpsHostnameVerificationEnabled() {
		return httpsHostnameVerificationEnabled;
	}

	public void setHttpsHostnameVerificationEnabled(boolean httpsHostnameVerificationEnabled) {
		this.httpsHostnameVerificationEnabled = httpsHostnameVerificationEnabled;
	}

	/**
	 * Returns the HostnameVerifier for the SSL connection.
	 *
	 * @return the HostnameVerifier for the SSL connection
	 */
	public HostnameVerifier getSSLHostnameVerifier() {
		return sslHostnameVerifier;
	}

	/**
	 * Sets the HostnameVerifier for the SSL connection. Note that it will be used
	 * after handshake on a connection and you should do actions by yourserlf when
	 * hostname is verified error.
	 * <p>
	 * There is no default HostnameVerifier
	 * </p>
	 *
	 * @param hostnameVerifier the {@link HostnameVerifier}
	 */
	public void setSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.sslHostnameVerifier = hostnameVerifier;
	}

	/**
	 * Returns whether the client and server should remember state for the client
	 * across reconnects.
	 *
	 * @return the clean session flag
	 */
	public boolean isCleanSession() {
		return this.cleanSession;
	}

	/**
	 * Sets whether the client and server should remember state across restarts and
	 * reconnects.
	 * <ul>
	 * <li>If set to false both the client and server will maintain state across
	 * restarts of the client, the server and the connection. As state is
	 * maintained:
	 * <ul>
	 * <li>Message delivery will be reliable meeting the specified QOS even if the
	 * client, server or connection are restarted.
	 * <li>The server will treat a subscription as durable.
	 * </ul>
	 * <li>If set to true the client and server will not maintain state across
	 * restarts of the client, the server or the connection. This means
	 * <ul>
	 * <li>Message delivery to the specified QOS cannot be maintained if the client,
	 * server or connection are restarted
	 * <li>The server will treat a subscription as non-durable
	 * </ul>
	 * </ul>
	 *
	 * @param cleanSession Set to True to enable cleanSession
	 */
	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	/**
	 * Set a list of one or more serverURIs the client may connect to.
	 * <p>
	 * Each <code>serverURI</code> specifies the address of a server that the client
	 * may connect to. Two types of connection are supported <code>tcp://</code> for
	 * a TCP connection and <code>ssl://</code> for a TCP connection secured by
	 * SSL/TLS. For example:
	 * <ul>
	 * <li><code>tcp://localhost:1883</code></li>
	 * <li><code>ssl://localhost:8883</code></li>
	 * </ul>
	 * If the port is not specified, it will default to 1883 for
	 * <code>tcp://</code>" URIs, and 8883 for <code>ssl://</code> URIs.
	 * <p>
	 * If serverURIs is set then it overrides the serverURI parameter passed in on
	 * the constructor of the MQTT client.
	 * <p>
	 * When an attempt to connect is initiated the client will start with the first
	 * serverURI in the list and work through the list until a connection is
	 * established with a server. If a connection cannot be made to any of the
	 * servers then the connect attempt fails.
	 * <p>
	 * Specifying a list of servers that a client may connect to has several uses:
	 * <ol>
	 * <li>High Availability and reliable message delivery
	 * <p>
	 * Some MQTT servers support a high availability feature where two or more
	 * "equal" MQTT servers share state. An MQTT client can connect to any of the
	 * "equal" servers and be assured that messages are reliably delivered and
	 * durable subscriptions are maintained no matter which server the client
	 * connects to.
	 * </p>
	 * <p>
	 * The cleansession flag must be set to false if durable subscriptions and/or
	 * reliable message delivery is required.
	 * </p>
	 * </li>
	 * <li>Hunt List
	 * <p>
	 * A set of servers may be specified that are not "equal" (as in the high
	 * availability option). As no state is shared across the servers reliable
	 * message delivery and durable subscriptions are not valid. The cleansession
	 * flag must be set to true if the hunt list mode is used
	 * </p>
	 * </li>
	 * </ol>
	 *
	 * @param serverURIs
	 *            to be used by the client
	 */
	//    public void setServerURIs(String[] serverURIs) {
	//        for (String serverURI : serverURIs) {
	//            NetworkModuleService.validateURI(serverURI);
	//        }
	//        this.serverURIs = serverURIs.clone();
	//    }

	/**
	 * Return a list of serverURIs the client may connect to
	 *
	 * @return the serverURIs or null if not set
	 */
	public String[] getServerURIs() {
		return serverURIs;
	}

	/**
	 * Returns whether the client will automatically attempt to reconnect to the
	 * server if the connection is lost
	 *
	 * @return the automatic reconnection flag.
	 */
	public boolean isAutomaticReconnect() {
		return automaticReconnect;
	}

	/**
	 * Sets whether the client will automatically attempt to reconnect to the server
	 * if the connection is lost.
	 * <ul>
	 * <li>If set to false, the client will not attempt to automatically reconnect
	 * to the server in the event that the connection is lost.</li>
	 * <li>If set to true, in the event that the connection is lost, the client will
	 * attempt to reconnect to the server. It will initially wait 1 second before it
	 * attempts to reconnect, for every failed reconnect attempt, the delay will
	 * double until it is at 2 minutes at which point the delay will stay at 2
	 * minutes.</li>
	 * </ul>
	 *
	 * @param automaticReconnect If set to True, Automatic Reconnect will be enabled
	 */
	public void setAutomaticReconnect(boolean automaticReconnect) {
		this.automaticReconnect = automaticReconnect;
	}

	public int getExecutorServiceTimeout() {
		return executorServiceTimeout;
	}

	/**
	 * Set the time in seconds that the executor service should wait when
	 * terminating before forcefully terminating. It is not recommended to change
	 * this value unless you are absolutely sure that you need to.
	 *
	 * @param executorServiceTimeout the time in seconds to wait when shutting down.Ï
	 */
	public void setExecutorServiceTimeout(int executorServiceTimeout) {
		this.executorServiceTimeout = executorServiceTimeout;
	}

	public Properties getCustomWebSocketHeaders() {
		return customWebSocketHeaders;
	}

	/**
	 * Sets the Custom WebSocket Headers for the WebSocket Connection.
	 *
	 * @param props The custom websocket headers {@link Properties}
	 */

	public void setCustomWebSocketHeaders(Properties props) {
		this.customWebSocketHeaders = props;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public TopicListener getTopicListener() {
		return topicListener;
	}

	public void setTopicListener(TopicListener topicListener) {
		this.topicListener = topicListener;
	}

	public int getConnectAckTimeout() {
		return connectAckTimeout;
	}

	public void setConnectAckTimeout(int connectAckTimeout) {
		this.connectAckTimeout = connectAckTimeout;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public MqttClientConfigure setMaxPacketSize(int maxPacketSize) {
		this.maxPacketSize = maxPacketSize;
		return this;
	}
}
