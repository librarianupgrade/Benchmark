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
package org.jclouds.profitbricks.binder.loadbalancer;

import static java.lang.String.format;
import org.jclouds.profitbricks.binder.BaseProfitBricksRequestBinder;
import org.jclouds.profitbricks.domain.LoadBalancer;

public class DeregisterLoadBalancerRequestBinder
		extends BaseProfitBricksRequestBinder<LoadBalancer.Request.DeregisterPayload> {

	protected final StringBuilder requestBuilder;

	DeregisterLoadBalancerRequestBinder() {
		super("loadbalancer");
		this.requestBuilder = new StringBuilder(128 * 4);
	}

	@Override
	protected String createPayload(LoadBalancer.Request.DeregisterPayload payload) {
		requestBuilder.append("<ws:deregisterServersOnLoadBalancer>");
		for (String s : payload.serverIds())
			requestBuilder.append(format("<serverIds>%s</serverIds>", s));
		requestBuilder.append(format("<loadBalancerId>%s</loadBalancerId>", payload.id()))
				.append("</ws:deregisterServersOnLoadBalancer>");

		return requestBuilder.toString();
	}
}
