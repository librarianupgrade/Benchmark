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
package org.jclouds.aws.s3.blobstore.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.Context;
import org.jclouds.aws.s3.blobstore.AWSS3BlobStore;
import org.jclouds.aws.s3.blobstore.AWSS3BlobStoreContext;
import org.jclouds.blobstore.BlobRequestSigner;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.location.Provider;
import org.jclouds.rest.Utils;
import org.jclouds.s3.blobstore.internal.S3BlobStoreContextImpl;

import com.google.common.reflect.TypeToken;

@Singleton
public class AWSS3BlobStoreContextImpl extends S3BlobStoreContextImpl implements AWSS3BlobStoreContext {

	@Inject
	public AWSS3BlobStoreContextImpl(@Provider Context backend, @Provider TypeToken<? extends Context> backendType,
			Utils utils, ConsistencyModel consistencyModel, BlobStore blobStore, BlobRequestSigner blobRequestSigner) {
		super(backend, backendType, utils, consistencyModel, blobStore, blobRequestSigner);
	}

	@Override
	public AWSS3BlobStore getBlobStore() {
		return AWSS3BlobStore.class.cast(super.getBlobStore());
	}
}
