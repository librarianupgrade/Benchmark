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
package org.jclouds.rackspace.cloudidentity.v2_0;

/**
 * An Rackspace service, such as Cloud Load Balancers, DNS, etc.
 * A service provides one or more endpoints through which users can access resources and perform operations.
 */
public final class ServiceType {
	/**
	* Cloud Load Balancers
	*/
	public static final String LOAD_BALANCERS = "rax:load-balancer";

	/**
	* Cloud DNS
	*/
	public static final String DNS = "rax:dns";

	/**
	* Cloud Queues
	*/
	public static final String QUEUES = "rax:queues";

	/**
	* Cloud Files CDN
	*/
	public static final String OBJECT_CDN = "rax:object-cdn";

	/**
	* Auto Scale
	*/
	public static final String AUTO_SCALE = "rax:autoscale";

	/**
	* Cloud Backup
	*/
	public static final String BACKUP = "rax:backup";

	/**
	* Cloud Databases
	*/
	public static final String DATABASES = "rax:database";

	/**
	* Cloud Monitoring
	*/
	public static final String MONITORING = "rax:monitor";

	/**
	* Cloud Big Data
	*/
	public static final String BIG_DATA = "rax:bigdata";

	/**
	* CDN
	*/
	public static final String CDN = "rax:cdn";

	/**
	* Orchestration (Openstack Heat)
	*/
	public static final String ORCHESTRATION = "orchestration";

	private ServiceType() {
		throw new AssertionError("intentionally unimplemented");
	}
}
