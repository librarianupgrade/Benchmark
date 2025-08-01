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
package org.apache.servicecomb.transport.highway;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpData;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighwayClientFilter extends AbstractFilter implements ConsumerFilter, EdgeFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(HighwayClientFilter.class);

	public static final String NAME = "highway-client";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean enabledForTransport(String transport) {
		return CoreConst.HIGHWAY.equals(transport);
	}

	@Override
	public int getOrder() {
		return Filter.CONSUMER_LOAD_BALANCE_ORDER + 2000;
	}

	@Override
	public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
		LOGGER.debug("Sending request by highway, operation={}, endpoint={}.",
				invocation.getMicroserviceQualifiedName(), invocation.getEndpoint().getEndpoint());

		OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(invocation);
		return send(invocation, operationProtobuf)
				.thenApply(tcpData -> convertToResponse(invocation, operationProtobuf, tcpData))
				.thenApply(this::convertFailedResponseToException);
	}

	protected CompletableFuture<TcpData> send(Invocation invocation, OperationProtobuf operationProtobuf) {
		invocation.getInvocationStageTrace().startConsumerConnection();
		HighwayClient highwayClient = ((HighwayTransport) invocation.getTransport()).getHighwayClient();
		HighwayClientPackage clientPackage = highwayClient.createClientPackage(invocation, operationProtobuf);
		HighwayClientConnection clientConnection = highwayClient.findClientPool(invocation);
		invocation.getInvocationStageTrace().finishConsumerConnection();
		invocation.onStartSendRequest();
		invocation.getInvocationStageTrace().startConsumerSendRequest();
		CompletableFuture<TcpData> sendFuture = clientConnection.send(clientPackage)
				.whenComplete((r, e) -> invocation.getInvocationStageTrace().finishWaitResponse());
		invocation.getInvocationStageTrace().finishConsumerSendRequest();
		invocation.getInvocationStageTrace().startWaitResponse();
		return invocation.optimizeSyncConsumerThread(sendFuture);
	}

	protected Response convertToResponse(Invocation invocation, OperationProtobuf operationProtobuf, TcpData tcpData) {
		try {
			invocation.getInvocationStageTrace().startConsumerDecodeResponse();
			Response result = HighwayCodec.decodeResponse(invocation, operationProtobuf, tcpData);
			invocation.getInvocationStageTrace().finishConsumerDecodeResponse();
			return result;
		} catch (Exception e) {
			throw AsyncUtils.rethrow(e);
		}
	}

	protected Response convertFailedResponseToException(Response response) {
		if (response.isFailed()) {
			Object errorData = response.getResult();
			if (errorData instanceof InvocationException) {
				errorData = ((InvocationException) errorData).getErrorData();
			}
			throw Exceptions.create(response.getStatus(), errorData);
		}

		return response;
	}
}
