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

package com.alibaba.cloud.integration.provider.controller;

import com.alibaba.cloud.integration.provider.message.PraiseMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TrevorLink
 */
@RestController
@RequestMapping("/praise")
public class PraiseController {
	private static final String BINDING_NAME = "praise-output";
	@Autowired
	private StreamBridge streamBridge;

	@GetMapping({ "/rocketmq", "/sentinel" })
	public boolean praise(@RequestParam Integer itemId) {
		PraiseMessage message = new PraiseMessage();
		message.setItemId(itemId);
		Message<PraiseMessage> praiseMessage = MessageBuilder.withPayload(message).build();
		return streamBridge.send(BINDING_NAME, praiseMessage);
	}

}
