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
package org.jclouds.s3.internal;

import org.jclouds.date.TimeStamp;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.internal.BaseRestApiExpectTest;
import org.jclouds.s3.S3ApiMetadata;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.config.S3HttpApiModule;

import com.google.common.base.Supplier;
import com.google.inject.Module;

public abstract class BaseS3ClientExpectTest extends BaseRestApiExpectTest<S3Client> {

	protected static final String CONSTANT_DATE = "2009-11-08T15:54:08.897Z";

	@ConfiguresHttpApi
	private static final class TestS3HttpApiModule extends S3HttpApiModule<S3Client> {

		@Override
		protected String provideTimeStamp(@TimeStamp Supplier<String> cache) {
			return CONSTANT_DATE;
		}
	}

	@Override
	protected Module createModule() {
		return new TestS3HttpApiModule();
	}

	@Override
	public S3ApiMetadata createApiMetadata() {
		return new S3ApiMetadata();
	}

}
