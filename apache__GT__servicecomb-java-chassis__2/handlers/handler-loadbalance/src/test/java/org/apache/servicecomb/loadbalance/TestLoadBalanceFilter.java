/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 *
 *
 */
public class TestLoadBalanceFilter {
	static SCBEngine scbEngine;

	static TransportManager transportManager;

	String microserviceName = "ms";

	LoadBalanceFilter handler;

	Map<String, LoadBalancer> loadBalancerMap;

	@Injectable
	Invocation invocation;

	@Mocked
	Transport restTransport;

	Response sendResponse;

	@Before
	public void setUp() {
		ConfigUtil.installDynamicConfig();
		scbEngine = SCBBootstrap.createSCBEngineForTest().run();
		transportManager = scbEngine.getTransportManager();

		new MockUp<Invocation>(invocation) {
			@Mock
			String getMicroserviceName() {
				return microserviceName;
			}

			@Mock
			void next(AsyncResponse asyncResp) throws Exception {
				asyncResp.handle(sendResponse);
			}

			@Mock
			public <T> T getLocalContext(String key) {
				return (T) null;
			}
		};

		new MockUp<TransportManager>(transportManager) {
			@Mock
			Transport findTransport(String transportName) {
				return restTransport;
			}
		};

		new Expectations(SPIServiceUtils.class) {
			{
				SPIServiceUtils.getSortedService(DiscoveryFilter.class);
				result = Collections.emptyList();
			}
		};

		List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
		extensionsFactories.add(new RuleNameExtentionsFactory());
		ExtensionsManager extensionsManager = new ExtensionsManager(extensionsFactories);

		handler = new LoadBalanceFilter(extensionsManager);
		loadBalancerMap = Deencapsulation.getField(handler, "loadBalancerMap");
	}

	@After
	public void teardown() {
		scbEngine.destroy();
		ArchaiusUtils.resetConfig();
	}

	@Test
	public void testIsFailedResponse() {
		Assertions.assertFalse(handler.isFailedResponse(Response.create(400, "", "")));
		Assertions.assertFalse(handler.isFailedResponse(Response.create(500, "", "")));
		Assertions.assertTrue(handler.isFailedResponse(Response.create(490, "", "")));
		Assertions.assertTrue(handler.isFailedResponse(Response.consumerFailResp(new NullPointerException())));
	}
}
