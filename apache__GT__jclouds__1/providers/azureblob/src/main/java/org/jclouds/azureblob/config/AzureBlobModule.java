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
package org.jclouds.azureblob.config;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.azureblob.domain.MutableBlobProperties;
import org.jclouds.azureblob.domain.internal.AzureBlobImpl;
import org.jclouds.blobstore.config.BlobStoreObjectModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/**
 * Configures the domain object mappings needed for all Azure Blob implementations
 */
public class AzureBlobModule extends AbstractModule {

	/**
	* explicit factories are created here as it has been shown that Assisted Inject is extremely
	* inefficient. http://code.google.com/p/google-guice/issues/detail?id=435
	*/
	@Override
	protected void configure() {
		// for converters
		install(new BlobStoreObjectModule());
		bind(AzureBlob.Factory.class).to(AzureBlobFactory.class).in(Scopes.SINGLETON);
	}

	private static class AzureBlobFactory implements AzureBlob.Factory {
		@Inject
		Provider<MutableBlobProperties> metadataProvider;

		public AzureBlob create(MutableBlobProperties metadata) {
			return new AzureBlobImpl(metadata != null ? metadata : metadataProvider.get());
		}
	}

	@Provides
	final AzureBlob provideAzureBlob(AzureBlob.Factory factory) {
		return factory.create(null);
	}

}
