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
package org.jclouds.s3.blobstore.integration;

import java.io.IOException;
import java.net.MalformedURLException;

import org.jclouds.blobstore.integration.internal.BaseBlobStoreIntegrationTest;
import org.jclouds.blobstore.integration.internal.BaseContainerLiveTest;
import org.testng.annotations.Test;

@Test(groups = "live", testName = "S3ContainerLiveTest")
public class S3ContainerLiveTest extends BaseContainerLiveTest {

	public S3ContainerLiveTest() {
		provider = "s3";
		BaseBlobStoreIntegrationTest.SANITY_CHECK_RETURNED_BUCKET_NAME = true;
	}

	@Override
	@Test(groups = { "live", "fails-on-s3proxy" })
	public void testPublicAccess() throws InterruptedException, MalformedURLException, IOException {
		super.testPublicAccess();
	}

	@Override
	@Test(groups = { "live", "fails-on-s3proxy" }, dependsOnMethods = "testPublicAccess")
	public void testPublicAccessInNonDefaultLocation() throws InterruptedException, MalformedURLException, IOException {
		super.testPublicAccessInNonDefaultLocation();
	}

	@Override
	@Test(groups = { "live", "fails-on-s3proxy" }, dependsOnMethods = "testPublicAccess")
	public void testPublicAccessInNonDefaultLocationWithBigBlob()
			throws InterruptedException, MalformedURLException, IOException {
		super.testPublicAccessInNonDefaultLocationWithBigBlob();
	}
}
