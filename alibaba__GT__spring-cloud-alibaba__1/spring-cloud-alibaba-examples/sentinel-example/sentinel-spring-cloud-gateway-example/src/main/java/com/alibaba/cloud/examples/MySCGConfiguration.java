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

package com.alibaba.cloud.examples;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
public class MySCGConfiguration {

	@Bean
	public BlockRequestHandler blockRequestHandler() {
		return new BlockRequestHandler() {
			@Override
			public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
				return ServerResponse.status(444).contentType(MediaType.APPLICATION_JSON)
						.body(fromValue("SCS Sentinel block"));
			}
		};
	}

}
