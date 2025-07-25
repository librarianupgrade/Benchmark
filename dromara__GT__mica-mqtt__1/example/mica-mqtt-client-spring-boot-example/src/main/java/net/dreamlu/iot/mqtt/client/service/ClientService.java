package net.dreamlu.iot.mqtt.client.service;

import net.dreamlu.iot.mqtt.spring.client.MqttClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author wsq
 */
@Service
public class ClientService {
	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
	@Autowired
	private MqttClientTemplate client;

	public boolean publish(String body) {
		client.publish("/test/client", body.getBytes(StandardCharsets.UTF_8));
		return true;
	}

	public boolean sub() {
		client.subQos0("/test/#", (context, topic, message, payload) -> {
			logger.info(topic + '\t' + new String(payload, StandardCharsets.UTF_8));
		});
		return true;
	}

}
