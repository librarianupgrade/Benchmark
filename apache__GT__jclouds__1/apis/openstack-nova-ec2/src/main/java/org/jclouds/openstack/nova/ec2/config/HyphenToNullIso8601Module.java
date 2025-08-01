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
package org.jclouds.openstack.nova.ec2.config;

import org.jclouds.date.internal.DateServiceDateCodecFactory.DateServiceIso8601Codec;
import org.jclouds.openstack.nova.ec2.internal.HyphenToNullIso8601Codec;

import com.google.inject.AbstractModule;

public class HyphenToNullIso8601Module extends AbstractModule {

	@Override
	protected void configure() {
		bind(DateServiceIso8601Codec.class).to(HyphenToNullIso8601Codec.class);
	}

}
