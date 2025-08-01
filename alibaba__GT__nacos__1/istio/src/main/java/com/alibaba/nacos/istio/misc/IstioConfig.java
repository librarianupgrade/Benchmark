/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.istio.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stores some configurations for Istio integration.
 *
 * @author nkorange
 * @since 1.1.4
 */
@Component
public class IstioConfig {

	@Value("${nacos.istio.mcp.server.enabled:false}")
	private boolean serverEnabled = false;
	@Value("${nacos.istio.mcp.server.port:18848}")
	private int serverPort = 18848;
	@Value("${nacos.istio.mcp.push.interval:3000}")
	private int mcpPushInterval;
	@Value("${nacos.istio.domain.suffix:nacos}")
	private String domainSuffix;

	public boolean isServerEnabled() {
		return serverEnabled;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getDomainSuffix() {
		return domainSuffix;
	}

	public int getMcpPushInterval() {
		return mcpPushInterval;
	}

}
