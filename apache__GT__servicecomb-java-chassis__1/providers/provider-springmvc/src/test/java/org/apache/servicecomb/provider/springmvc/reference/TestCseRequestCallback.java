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
package org.apache.servicecomb.provider.springmvc.reference;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RequestCallback;

public class TestCseRequestCallback {
	@Test
	public void testNormal() throws IOException {
		RequestCallback callback = Mockito.mock(RequestCallback.class);
		CseClientHttpRequest request = new CseClientHttpRequest();
		CseRequestCallback cb = new CseRequestCallback(null, callback, null);
		cb.doWithRequest(request);

		Assertions.assertNull(request.getContext());
	}

	@Test
	public void testCseEntity() throws IOException {
		CseHttpEntity<?> entity = Mockito.mock(CseHttpEntity.class);
		RequestCallback callback = Mockito.mock(RequestCallback.class);
		CseClientHttpRequest request = new CseClientHttpRequest();

		entity.addContext("c1", "c2");
		CseRequestCallback cb = new CseRequestCallback(entity, callback, null);
		cb.doWithRequest(request);

		Assertions.assertEquals(entity.getContext(), request.getContext());
	}
}
