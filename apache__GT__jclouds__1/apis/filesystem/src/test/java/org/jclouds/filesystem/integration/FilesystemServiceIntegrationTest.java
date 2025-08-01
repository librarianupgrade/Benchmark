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
package org.jclouds.filesystem.integration;

import java.util.Properties;

import org.jclouds.blobstore.integration.internal.BaseBlobStoreIntegrationTest;
import org.jclouds.blobstore.integration.internal.BaseServiceIntegrationTest;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.filesystem.utils.TestUtils;
import org.testng.annotations.Test;

@Test(groups = { "integration", "live" }, testName = "blobstore.FilesystemServiceIntegrationTest")
public class FilesystemServiceIntegrationTest extends BaseServiceIntegrationTest {
	public FilesystemServiceIntegrationTest() {
		provider = "filesystem";
		BaseBlobStoreIntegrationTest.SANITY_CHECK_RETURNED_BUCKET_NAME = true;
	}

	@Override
	protected Properties setupProperties() {
		Properties props = super.setupProperties();
		props.setProperty(FilesystemConstants.PROPERTY_BASEDIR, TestUtils.TARGET_BASE_DIR);
		return props;
	}
}
