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

import java.util.SortedSet;

import javax.inject.Inject;

import org.jclouds.gogrid.domain.LoadBalancer;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseJson;

import com.google.common.base.Function;
import com.google.inject.Singleton;

/**
 * Parses {@link org.jclouds.gogrid.domain.LoadBalancer jobs} from a json
 * string.
 */
@Singleton
public class ParseLoadBalancerListFromJsonResponse implements Function<HttpResponse, SortedSet<LoadBalancer>> {

	private final ParseJson<GenericResponseContainer<LoadBalancer>> json;

	@Inject
	ParseLoadBalancerListFromJsonResponse(ParseJson<GenericResponseContainer<LoadBalancer>> json) {
		this.json = json;
	}

	@Override
	public SortedSet<LoadBalancer> apply(HttpResponse arg0) {
		return json.apply(arg0).getList();
	}

}
