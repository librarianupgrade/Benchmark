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
package org.jclouds.openstack.services;

/**
 * An OpenStack service, such as Compute (Nova), Object Storage (Swift), or Image Service (Glance).
 * A service provides one or more endpoints through which users can access resources and perform
 * (presumably useful) operations.
 * 
 * @see <a href="http://docs.openstack.org/api/openstack-typeentity-service/2.0/content/Identity-Service-Concepts-e1362.html"
 *      />
 */
public class ServiceType {
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
	* Network Service (Quantum)
	*/
	public static final String NETWORK = "network";

	protected ServiceType() {
		throw new AssertionError("intentionally unimplemented");
	}
}
