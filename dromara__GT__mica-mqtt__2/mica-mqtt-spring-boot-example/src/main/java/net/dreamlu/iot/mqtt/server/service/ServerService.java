package net.dreamlu.iot.mqtt.server.service;

import net.dreamlu.iot.mqtt.spring.server.MqttServerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

/**
 * @author wsq
 */
@Service
public class ServerService {
	private static final Logger logger = LoggerFactory.getLogger(ServerService.class);
	@Autowired
	private MqttServerTemplate server;

	public boolean publish(String body) {
		boolean result = server.publishAll("/test/123", ByteBuffer.wrap(body.getBytes()));
		logger.info("Mqtt publishAll result:{}", result);
		return result;
	}
}
