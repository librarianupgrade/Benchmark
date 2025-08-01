/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.core.remote.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.request.ServerLoaderInfoRequest;
import com.alibaba.nacos.api.remote.response.ServerLoaderInfoResponse;
import com.alibaba.nacos.core.remote.ConnectionManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * {@link ServerLoaderInfoRequestHandler} unit test.
 *
 * @author chenglu
 * @date 2021-07-01 12:48
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerLoaderInfoRequestHandlerTest {

	@InjectMocks
	private ServerLoaderInfoRequestHandler handler;

	@Mock
	private ConnectionManager connectionManager;

	@Test
	public void testHandle() {
		Mockito.when(connectionManager.currentClientsCount()).thenReturn(1);
		Mockito.when(connectionManager.currentClientsCount(Mockito.any())).thenReturn(1);

		ServerLoaderInfoRequest request = new ServerLoaderInfoRequest();
		RequestMeta meta = new RequestMeta();

		try {
			ServerLoaderInfoResponse response = handler.handle(request, meta);
			String sdkConCount = response.getMetricsValue("sdkConCount");
			Assert.assertEquals(sdkConCount, "1");

		} catch (NacosException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
