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
package org.jclouds.atmos.domain;

import java.net.URI;

import org.jclouds.atmos.domain.internal.DelegatingMutableContentMetadata;

import com.google.inject.ImplementedBy;

/**
 * metadata of the object
 */
@ImplementedBy(DelegatingMutableContentMetadata.class)
public interface MutableContentMetadata extends org.jclouds.io.MutableContentMetadata {

	String getName();

	void setName(String name);

	URI getUri();

	void setUri(URI uri);

	String getPath();

	void setPath(String path);
}
