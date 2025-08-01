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
package org.jclouds.googlecomputeengine.compute.predicates;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Sets.filter;
import static org.jclouds.compute.predicates.NodePredicates.all;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import javax.inject.Inject;

import org.jclouds.compute.ComputeService;

import com.google.common.base.Predicate;

public final class GroupIsEmpty implements Predicate<String> {

	private final ComputeService computeService;

	@Inject
	GroupIsEmpty(ComputeService computeService) {
		this.computeService = computeService;
	}

	@Override
	public boolean apply(String groupName) {
		return isEmpty(filter(computeService.listNodesDetailsMatching(all()), inGroup(groupName)));
	}
}
