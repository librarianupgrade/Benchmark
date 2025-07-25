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

package net.dreamlu.iot.mqtt.spring.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.iot.mqtt.core.client.IMqttClientMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * MqttClient 订阅监听器
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
public class MqttClientSubscribeDetector implements BeanPostProcessor {
	private final ApplicationContext applicationContext;

	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {
		Class<?> userClass = ClassUtils.getUserClass(bean);
		// 1. 查找类上的 MqttClientSubscribe 注解
		if (bean instanceof IMqttClientMessageListener) {
			MqttClientSubscribe subscribe = AnnotationUtils.findAnnotation(userClass, MqttClientSubscribe.class);
			if (subscribe != null) {
				MqttClientTemplate clientTemplate = getMqttClientTemplate(applicationContext,
						subscribe.clientTemplateBean());
				String[] topicFilters = getTopicFilters(applicationContext, subscribe.value());
				clientTemplate.subscribe(topicFilters, subscribe.qos(), (IMqttClientMessageListener) bean);
			}
		} else {
			// 2. 查找方法上的 MqttClientSubscribe 注解
			ReflectionUtils.doWithMethods(userClass, method -> {
				MqttClientSubscribe subscribe = AnnotationUtils.findAnnotation(method, MqttClientSubscribe.class);
				if (subscribe != null) {
					// 1. 校验必须为 public 和非 static 的方法
					int modifiers = method.getModifiers();
					if (Modifier.isStatic(modifiers)) {
						throw new IllegalArgumentException(
								"@MqttClientSubscribe on method " + method + " must not static.");
					}
					if (!Modifier.isPublic(modifiers)) {
						throw new IllegalArgumentException(
								"@MqttClientSubscribe on method " + method + " must public.");
					}
					// 2. 校验 method 入参数必须等于2
					int paramCount = method.getParameterCount();
					if (paramCount != 2) {
						throw new IllegalArgumentException(
								"@MqttClientSubscribe on method " + method + " parameter count must equal to 2.");
					}
					// 3. 校验 method 入参类型必须为 String、ByteBuffer
					Class<?>[] parameterTypes = method.getParameterTypes();
					Class<?> topicParamType = parameterTypes[0];
					Class<?> payloadParamType = parameterTypes[1];
					if (String.class != topicParamType || ByteBuffer.class != payloadParamType) {
						throw new IllegalArgumentException("@MqttClientSubscribe on method " + method
								+ " parameter type must String and ByteBuffer.");
					}
					// 4. 订阅
					MqttClientTemplate clientTemplate = getMqttClientTemplate(applicationContext,
							subscribe.clientTemplateBean());
					String[] topicFilters = getTopicFilters(applicationContext, subscribe.value());
					clientTemplate.subscribe(topicFilters, subscribe.qos(),
							(topic, payload) -> ReflectionUtils.invokeMethod(method, bean, topic, payload));
				}
			}, ReflectionUtils.USER_DECLARED_METHODS);
		}
		return bean;
	}

	private static MqttClientTemplate getMqttClientTemplate(ApplicationContext applicationContext, String beanName) {
		return applicationContext.getBean(beanName, MqttClientTemplate.class);
	}

	private static String[] getTopicFilters(ApplicationContext applicationContext, String[] values) {
		return Arrays.stream(values).map(applicationContext.getEnvironment()::resolvePlaceholders)
				.toArray(String[]::new);
	}

}
