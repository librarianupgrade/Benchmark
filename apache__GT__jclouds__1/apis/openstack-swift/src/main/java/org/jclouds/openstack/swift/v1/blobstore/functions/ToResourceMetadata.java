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
package org.jclouds.openstack.swift.v1.blobstore.functions;

import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.MutableStorageMetadataImpl;
import org.jclouds.domain.Location;
import org.jclouds.openstack.swift.v1.domain.Container;

import com.google.common.base.Function;

public class ToResourceMetadata implements Function<Container, StorageMetadata> {
	private Location region;

	public ToResourceMetadata(Location region) {
		this.region = region;
	}

	@Override
	public StorageMetadata apply(Container from) {
		MutableStorageMetadata to = new MutableStorageMetadataImpl();
		to.setName(from.getName());
		to.setLocation(region);
		to.setType(StorageType.CONTAINER);
		to.setUserMetadata(from.getMetadata());
		return to;
	}
}
