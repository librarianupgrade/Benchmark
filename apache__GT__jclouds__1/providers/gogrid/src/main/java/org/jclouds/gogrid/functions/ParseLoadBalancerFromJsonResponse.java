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
package org.jclouds.gogrid.functions;

import javax.inject.Inject;

import org.jclouds.gogrid.domain.LoadBalancer;
import org.jclouds.http.HttpResponse;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;

/**
 * Parses the single load balancer out of the response.
 * 
 * This class delegates parsing to {@link ParseLoadBalancerListFromJsonResponse}
 * .
 */
@Singleton
public class ParseLoadBalancerFromJsonResponse implements Function<HttpResponse, LoadBalancer> {
	private final ParseLoadBalancerListFromJsonResponse parser;

	@Inject
	ParseLoadBalancerFromJsonResponse(ParseLoadBalancerListFromJsonResponse parser) {
		this.parser = parser;
	}

	@Override
	public LoadBalancer apply(HttpResponse arg0) {
		return Iterables.getOnlyElement(parser.apply(arg0));
	}

}
