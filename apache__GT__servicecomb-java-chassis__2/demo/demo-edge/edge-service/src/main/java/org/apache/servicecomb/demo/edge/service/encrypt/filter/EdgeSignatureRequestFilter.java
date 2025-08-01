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
package org.apache.servicecomb.demo.edge.service.encrypt.filter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.demo.edge.authentication.encrypt.Hcr;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.demo.edge.service.encrypt.EncryptContext;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

@Component
public class EdgeSignatureRequestFilter implements ConsumerFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(EdgeSignatureRequestFilter.class);

	@Override
	public int getOrder(InvocationType invocationType, String microservice) {
		return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1800;
	}

	@Nonnull
	@Override
	public String getName() {
		return "test-edge-signature-request";
	}

	@Override
	public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
		EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
		if (encryptContext == null) {
			return nextNode.onFilter(invocation);
		}
		Hcr hcr = encryptContext.getHcr();

		// signature for query and form
		List<String> names = Collections.list(invocation.getRequestEx().getParameterNames());
		names.sort(Comparator.naturalOrder());

		Hasher hasher = Hashing.sha256().newHasher();
		hasher.putString(hcr.getSignatureKey(), StandardCharsets.UTF_8);
		for (String name : names) {
			hasher.putString(name, StandardCharsets.UTF_8);
			hasher.putString(invocation.getRequestEx().getParameter(name), StandardCharsets.UTF_8);
		}
		LOGGER.info("afterReceiveRequest signature: {}", hasher.hash());

		return nextNode.onFilter(invocation);
	}
}
