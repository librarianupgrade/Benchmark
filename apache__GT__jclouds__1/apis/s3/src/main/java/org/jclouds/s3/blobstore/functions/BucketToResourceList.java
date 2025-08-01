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
package org.jclouds.s3.blobstore.functions;

import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.s3.domain.ListBucketResponse;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Singleton
public class BucketToResourceList implements Function<ListBucketResponse, PageSet<? extends StorageMetadata>> {
	private final ObjectToBlobMetadata object2blobMd;
	private final CommonPrefixesToResourceMetadata prefix2ResourceMd;

	protected final Function<StorageMetadata, String> indexer = new Function<StorageMetadata, String>() {
		@Override
		public String apply(StorageMetadata from) {
			return from.getName();
		}
	};

	@Inject
	public BucketToResourceList(ObjectToBlobMetadata object2blobMd,
			CommonPrefixesToResourceMetadata prefix2ResourceMd) {
		this.object2blobMd = object2blobMd;
		this.prefix2ResourceMd = prefix2ResourceMd;
	}

	public PageSet<? extends StorageMetadata> apply(ListBucketResponse from) {
		// S3 lists keys in sorted order; use sorted set to order relative paths correctly
		SortedSet<StorageMetadata> contents = Sets
				.<StorageMetadata>newTreeSet(Iterables.transform(from, object2blobMd));

		for (String prefix : from.getCommonPrefixes()) {
			contents.add(prefix2ResourceMd.apply(prefix));
		}
		return new PageSetImpl<StorageMetadata>(contents, from.getNextMarker());
	}
}
