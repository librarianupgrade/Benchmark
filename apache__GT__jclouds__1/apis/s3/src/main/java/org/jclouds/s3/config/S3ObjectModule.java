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
package org.jclouds.s3.config;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jclouds.blobstore.config.BlobStoreObjectModule;
import org.jclouds.s3.domain.MutableObjectMetadata;
import org.jclouds.s3.domain.S3Object;
import org.jclouds.s3.domain.internal.S3ObjectImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Configures the domain object mappings needed for all S3 implementations
 */
public class S3ObjectModule extends AbstractModule {

	/**
	* explicit factories are created here as it has been shown that Assisted Inject is extremely
	* inefficient. http://code.google.com/p/google-guice/issues/detail?id=435
	*/
	@Override
	protected void configure() {
		// for converters
		install(new BlobStoreObjectModule());
		bind(S3Object.Factory.class).to(S3ObjectFactory.class).asEagerSingleton();
	}

	private static class S3ObjectFactory implements S3Object.Factory {
		@Inject
		Provider<MutableObjectMetadata> metadataProvider;

		public S3Object create(MutableObjectMetadata metadata) {
			return new S3ObjectImpl(metadata != null ? metadata : metadataProvider.get());
		}
	}

	@Provides
	final S3Object provideS3Object(S3Object.Factory factory) {
		return factory.create(null);
	}

}
