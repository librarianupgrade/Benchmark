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
package org.jclouds.ec2.xml;

import javax.inject.Inject;

import org.jclouds.date.DateService;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.location.Region;

import com.google.common.base.Supplier;

/**
 * Parses the following XML document:
 * <p/>
 * RunInstancesResponse xmlns="http:
 * 
 * @see <a href="http: />
 */
public class RunInstancesResponseHandler extends BaseReservationHandler<Reservation<? extends RunningInstance>> {

	@Inject
	public RunInstancesResponseHandler(DateService dateService, @Region Supplier<String> defaultRegion) {
		super(dateService, defaultRegion);
	}

	@Override
	public Reservation<? extends RunningInstance> getResult() {
		return newReservation();
	}

}
