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
package org.jclouds.openstack.swift.v1;

import org.jclouds.View;
import org.jclouds.apis.internal.BaseApiMetadataTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

@Test(groups = "unit", testName = "SwiftApiMetadataTest")
// public class SwiftApiMetadataTest extends BaseBlobStoreApiMetadataTest {
public class SwiftApiMetadataTest extends BaseApiMetadataTest {
	public SwiftApiMetadataTest() {
		super(new SwiftApiMetadata(), ImmutableSet.<TypeToken<? extends View>>of());
	}
}
