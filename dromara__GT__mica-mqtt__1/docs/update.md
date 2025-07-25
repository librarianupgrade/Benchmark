# 升级指南

## 迁移到 mica-mqtt 2.1.x

- `mica-mqtt-core` 拆分成了 `mica-mqtt-client` 和 `mica-mqtt-server`，避免一些依赖引用问题。
- `ByteBufferUtil` 由 `net.dreamlu.iot.mqtt.codec.ByteBufferUtil` 移动到了 `org.tio.utils.buffer.ByteBufferUtil`。
- `HexUtil` 由 `net.dreamlu.iot.mqtt.core.util.HexUtil` 移动到了 `org.tio.utils.mica.HexUtils`。

### 1. 客户端

#### 1.1 订阅回调接口调整
注意：`mica-mqtt-client-spring-boot-starter` 使用注解订阅可以直升。

`IMqttClientMessageListener#onMessage(ChannelContext context, String topic, MqttPublishMessage message, ByteBuffer payload)` 方法统一添加 `context`、`message` 参数。

订阅系列方法需要调整：
```java
// 消息订阅，同类方法 subxxx
client.subQos0("/test/#", (context, topic, message, payload) -> {
    logger.info(topic + '\t' + ByteBufferUtil.toString(payload));
});
```

#### 1.2 SSL 双向认证支持
```yaml
mica:
  client:
    ssl:
      enabled: false            # 是否开启 ssl 认证，2.1.0 开始支持双向认证
      keystore-path:            # 可选参数：ssl 双向认证 keystore 目录，支持 classpath:/ 路径。
      keystore-pass:            # 可选参数：ssl 双向认证 keystore 密码
      truststore-path:          # 可选参数：ssl 双向认证 truststore 目录，支持 classpath:/ 路径。
      truststore-pass:          # 可选参数：ssl 双向认证 truststore 密码
```

注意： ssl 存在三种情况

| 服务端开启ssl                            | 客户端                                        |
| ---------------------------------------- | --------------------------------------------- |
| ClientAuth 为 NONE（不需要客户端验证）   | 仅仅需要开启 ssl 即可不用配置证书             |
| ClientAuth 为 OPTIONAL（与客户端协商）   | 需开启 ssl 并且配置 truststore 证书           |
| ClientAuth 为 REQUIRE (必须的客户端验证) | 需开启 ssl 并且配置 truststore、 keystore证书 |

### 2. 服务端

#### 2.1 IMqttMessageListener 调整

`IMqttMessageListener` onMessage 参数也做了调整，添加了 topic、qoS，message 改为了原始 MqttPublishMessage，方便自行获取 mqtt5.x 的属性。
```java
/**
 * 监听到消息
 *
 * @param context  ChannelContext
 * @param clientId clientId
 * @param topic    topic
 * @param qoS      MqttQoS
 * @param message  Message
 */
void onMessage(ChannelContext context, String clientId, String topic, MqttQoS qoS, MqttPublishMessage message);
```

#### 2.2 ssl 双向认证支持
```yaml
mica:
  server:
    ssl:                        # mqtt tcp ssl 认证
      enabled: false            # 是否开启 ssl 认证，2.1.0 开始支持双向认证
      keystore-path:            # 必须参数：ssl keystore 目录，支持 classpath:/ 路径。
      keystore-pass:            # 必选参数：ssl keystore 密码
      truststore-path:          # 可选参数：ssl 双向认证 truststore 目录，支持 classpath:/ 路径。
      truststore-pass:          # 可选参数：ssl 双向认证 truststore 密码
      client-auth: none         # 是否需要客户端认证（双向认证），默认：NONE（不需要）
```