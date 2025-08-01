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
package org.jclouds.ec2.predicates;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.jclouds.aws.AWSResponseException;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.logging.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * 
 * Tests to see if a task succeeds.
 */
@Singleton
public class InstanceHasIpAddress implements Predicate<RunningInstance> {

	private final EC2Api client;

	@Resource
	protected Logger logger = Logger.NULL;

	@Inject
	public InstanceHasIpAddress(EC2Api client) {
		this.client = client;
	}

	public boolean apply(RunningInstance instance) {
		logger.trace("looking for ipAddress on instance %s", instance);
		try {
			instance = refresh(instance);
			return instance.getIpAddress() != null;
		} catch (AWSResponseException e) {
			if (e.getError().getCode().equals("InvalidInstanceID.NotFound"))
				return false;
			throw e;
		}
	}

	private RunningInstance refresh(RunningInstance instance) {
		return Iterables.getOnlyElement(Iterables.getOnlyElement(
				client.getInstanceApi().get().describeInstancesInRegion(instance.getRegion(), instance.getId())));
	}
}
