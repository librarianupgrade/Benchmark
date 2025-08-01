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
package org.jclouds.openstack.nova.ec2.internal;

import java.util.Date;

import javax.inject.Singleton;

import org.jclouds.date.DateService;
import org.jclouds.date.internal.DateServiceDateCodecFactory.DateServiceIso8601Codec;

import com.google.common.base.Objects;
import com.google.inject.Inject;

@Singleton
public class HyphenToNullIso8601Codec extends DateServiceIso8601Codec {

	@Inject
	public HyphenToNullIso8601Codec(DateService dateService) {
		super(dateService);
	}

	@Override
	public Date toDate(String date) throws IllegalArgumentException {
		if (Objects.equal("-", date))
			return null;
		return super.toDate(date);
	}

	@Override
	public String toString() {
		return "hyphenToNullIso8601()";
	}

}
