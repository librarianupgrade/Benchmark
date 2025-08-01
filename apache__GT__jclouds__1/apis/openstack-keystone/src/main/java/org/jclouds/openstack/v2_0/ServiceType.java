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
package org.jclouds.openstack.v2_0;

/**
 * An OpenStack service, such as Compute (Nova), Object Storage (Swift), or Image Service (Glance).
 * A service provides one or more endpoints through which users can access resources and perform
 * (presumably useful) operations.
 */
public final class ServiceType {
	/**
	* Object Storage (Swift)
	*/
	public static final String OBJECT_STORE = "object-store";

	/**
	* Compute (Nova)
	*/
	public static final String COMPUTE = "compute";

	/**
	* Image Service (Glance)
	*/
	public static final String IMAGE = "image";

	/**
	* Identity Service (Keystone)
	*/
	public static final String IDENTITY = "identity";

	/**
	* Network Service (Neutron)
	*/
	public static final String NETWORK = "network";

	/**
	* Block Storage (Cinder)
	*/
	public static final String BLOCK_STORAGE = "volume";

	/**
	* Database Service (Trove)
	*/
	public static final String DATABASE = "database";

	/**
	* @deprecated use {@link #MESSAGING} instead.
	* Queues Service (Marconi)
	*/
	@Deprecated
	public static final String QUEUES = "queuing";

	/**
	* Orchestration Service (Heat)
	*/
	public static final String ORCHESTRATION = "orchestration";

	/**
	* CDN Service (Poppy)
	*/
	public static final String CDN = "cdn";

	/**
	* Shared Filsystem Service (Manila)
	*/
	public static final String SHARED_FILESYSTEM = "share";

	/**
	* Messaging Service (Zaqar)
	*/
	public static final String MESSAGING = "messaging";

	private ServiceType() {
		throw new AssertionError("intentionally unimplemented");
	}
}
