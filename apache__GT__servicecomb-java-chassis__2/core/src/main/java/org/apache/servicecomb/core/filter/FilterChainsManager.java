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
package org.apache.servicecomb.core.filter;

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;
import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.deleteLast;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.springframework.beans.factory.annotation.Autowired;

public class FilterChainsManager {
	private final InvocationFilterChains consumerChains = new InvocationFilterChains(InvocationType.CONSUMER);

	private final InvocationFilterChains producerChains = new InvocationFilterChains(InvocationType.PRODUCER);

	@Autowired
	public FilterChainsManager addFilters(List<Filter> filters) {
		for (Filter filter : filters) {
			if (filter.isEnabledForInvocationType(InvocationType.CONSUMER)) {
				consumerChains.addFilter(filter);
			}

			if (filter.isEnabledForInvocationType(InvocationType.PRODUCER)) {
				producerChains.addFilter(filter);
			}
		}

		return this;
	}

	public FilterChainsManager init() {
		return this;
	}

	public FilterNode findConsumerChain(String microserviceName) {
		return consumerChains.findChain(microserviceName);
	}

	public FilterNode findProducerChain(String microserviceName) {
		return producerChains.findChain(microserviceName);
	}

	public String collectResolvedChains() {
		StringBuilder sb = new StringBuilder();

		appendLine(sb, "consumer: ");
		appendLine(sb, "  filters: %s", collectFilterNames(consumerChains, InvocationType.CONSUMER));

		appendLine(sb, "producer: ");
		appendLine(sb, "  filters: %s", collectFilterNames(producerChains, InvocationType.PRODUCER));

		return deleteLast(sb, 1).toString();
	}

	private List<String> collectFilterNames(InvocationFilterChains chains, InvocationType invocationType) {
		return chains.getFilters().stream()
				.map(filter -> filter.getName() + "(" + filter.getOrder(invocationType, null) + ")")
				.collect(Collectors.toList());
	}
}
