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
package org.jclouds.googlecomputeengine.domain;

import java.net.URI;
import java.util.Date;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

/**
 * Represents a network used to enable instance communication.
 */
@AutoValue
public abstract class Network {

	public abstract String id();

	public abstract Date creationTimestamp();

	public abstract URI selfLink();

	public abstract String name();

	@Nullable
	public abstract String description();

	/**
	* The range of internal addresses that are legal on this network. This range is a CIDR
	* specification, for example: {@code 192.168.0.0/16}.
	*/
	public abstract String rangeIPv4();

	/**
	* This must be within the range specified by IPv4Range, and is typically the first usable address in that range.
	* If not specified, the default value is the first usable address in IPv4Range.
	*/
	@Nullable
	public abstract String gatewayIPv4();

	@SerializedNames({ "id", "creationTimestamp", "selfLink", "name", "description", "IPv4Range", "gatewayIPv4" })
	public static Network create(String id, Date creationTimestamp, URI selfLink, String name, String description,
			String rangeIPv4, String gatewayIPv4) {
		return new AutoValue_Network(id, creationTimestamp, selfLink, name, description, rangeIPv4, gatewayIPv4);
	}

	Network() {
	}
}
