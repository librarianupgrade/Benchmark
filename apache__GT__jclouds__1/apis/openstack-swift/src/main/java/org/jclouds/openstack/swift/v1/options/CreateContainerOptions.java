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
package org.jclouds.openstack.swift.v1.options;

import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.CONTAINER_ACL_ANYBODY_READ;
import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.CONTAINER_METADATA_PREFIX;
import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.CONTAINER_READ;
import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.VERSIONS_LOCATION;

import java.util.Map;

import org.jclouds.http.options.BaseHttpRequestOptions;
import org.jclouds.openstack.swift.v1.binders.BindMetadataToHeaders;

import com.google.common.collect.Multimap;

/**
 * Options for creating a {@link Container} 
 * 
 * @see ContainerApi#create(String, CreateContainerOptions)
 */
public class CreateContainerOptions extends BaseHttpRequestOptions {

	public static final CreateContainerOptions NONE = new CreateContainerOptions();

	/** 
	* Sets the headers on a container at creation.
	*/
	public CreateContainerOptions headers(Multimap<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	/**
	* Sets the metadata on a container at creation. 
	*/
	public CreateContainerOptions metadata(Map<String, String> metadata) {
		this.headers.putAll(bindMetadataToHeaders.toHeaders(metadata));
		return this;
	}

	/**
	* Sets the public ACL on the container so that anybody can read it.
	*/
	public CreateContainerOptions anybodyRead() {
		this.headers.put(CONTAINER_READ, CONTAINER_ACL_ANYBODY_READ);
		return this;
	}

	/**
	* Sets the container that will contain object versions.
	*/
	public CreateContainerOptions versionsLocation(String containerName) {
		this.headers.put(VERSIONS_LOCATION, containerName);
		return this;
	}

	public static class Builder {

		/**
		 * @see CreateContainerOptions#anybodyRead
		 */
		public static CreateContainerOptions anybodyRead() {
			CreateContainerOptions options = new CreateContainerOptions();
			return options.anybodyRead();
		}

		/**
		 * @see CreateContainerOptions#headers
		 */
		public static CreateContainerOptions headers(Multimap<String, String> headers) {
			CreateContainerOptions options = new CreateContainerOptions();
			return options.headers(headers);
		}

		/**
		 * @see CreateContainerOptions#metadata
		 */
		public static CreateContainerOptions metadata(Map<String, String> metadata) {
			CreateContainerOptions options = new CreateContainerOptions();
			return options.metadata(metadata);
		}

		/**
		 * @see CreateContainerOptions#versionsLocation
		 */
		public static CreateContainerOptions versionsLocation(String containerName) {
			CreateContainerOptions options = new CreateContainerOptions();
			return options.versionsLocation(containerName);
		}
	}

	private static final BindMetadataToHeaders bindMetadataToHeaders = new BindMetadataToHeaders(
			CONTAINER_METADATA_PREFIX);
}
