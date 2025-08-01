/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.stream.binder.rocketmq.aot.hint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ChengPu raozihao
 */
public class RocketMQConsumerPropertiesHintsTests {

	@Test
	public void shouldRegisterHints() {
		Constructor<RocketMQConsumerProperties> constructor;
		try {
			constructor = RocketMQConsumerProperties.class.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		Method setMessageModel = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "setMessageModel",
				String.class);
		Method getPush = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "getPush");
		Method setSubscription = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "setSubscription",
				String.class);
		RuntimeHints hints = new RuntimeHints();
		new RocketMQConsumerPropertiesHints().registerHints(hints, getClass().getClassLoader());
		assertThat(RuntimeHintsPredicates.reflection().onConstructor(constructor)).accepts(hints);
		assertThat(RuntimeHintsPredicates.reflection().onMethod(setMessageModel)).accepts(hints);
		assertThat(RuntimeHintsPredicates.reflection().onMethod(getPush)).accepts(hints);
		assertThat(RuntimeHintsPredicates.reflection().onMethod(setSubscription)).accepts(hints);
	}
}
