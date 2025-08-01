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
package org.apache.servicecomb.core.filter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.core.tracing.TraceIdLogger;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.MDC;

import jakarta.ws.rs.core.Response.Status;

public class ScheduleFilter extends AbstractFilter implements ProviderFilter {
	public static final String NAME = "schedule";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return Filter.PROVIDER_SCHEDULE_FILTER_ORDER;
	}

	@Override
	public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode next) {
		invocation.getInvocationStageTrace().startProviderQueue();
		Executor executor = invocation.getOperationMeta().getExecutor();
		return CompletableFuture.completedFuture(null).thenComposeAsync(response -> runInExecutor(invocation, next),
				executor);
	}

	protected CompletableFuture<Response> runInExecutor(Invocation invocation, FilterNode next) {
		invocation.getInvocationStageTrace().finishProviderQueue();
		MDC.put(TraceIdLogger.KEY_TRACE_ID, invocation.getTraceId());
		checkInQueueTimeout(invocation);
		return next.onFilter(invocation);
	}

	private void checkInQueueTimeout(Invocation invocation) {
		long nanoTimeout = invocation.getOperationMeta().getConfig()
				.getNanoRequestWaitInPoolTimeout(invocation.getTransport().getName());

		if (invocation.getInvocationStageTrace().calcQueue() > nanoTimeout) {
			throw new InvocationException(Status.REQUEST_TIMEOUT, "Request in the queue timed out.");
		}
	}
}
