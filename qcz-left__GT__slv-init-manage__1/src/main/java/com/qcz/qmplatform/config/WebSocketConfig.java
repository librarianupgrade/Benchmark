package com.qcz.qmplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 外置Tomcat注释掉该类
 */
@Configuration
public class WebSocketConfig {
	/**
	 * ServerEndpointExporter 作用
	 * <p>
	 * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
	 */
	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}
}
