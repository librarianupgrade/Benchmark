/*
 *
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
 *
 */

package com.alibaba.nacos.naming.cluster.remote.request;

import com.alibaba.nacos.core.cluster.remote.request.AbstractClusterRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractClusterRequestTest {

	@Test
	public void getModule() {
		AbstractClusterRequest request = new AbstractClusterRequest() {
		};
		String actual = request.getModule();
		assertEquals("cluster", actual);
	}
}