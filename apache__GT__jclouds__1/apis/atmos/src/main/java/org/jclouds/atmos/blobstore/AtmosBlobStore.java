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
package org.jclouds.atmos.blobstore;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.atmos.options.PutOptions.Builder.publicRead;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jclouds.atmos.AtmosClient;
import org.jclouds.atmos.blobstore.functions.BlobStoreListOptionsToListOptions;
import org.jclouds.atmos.blobstore.functions.BlobToObject;
import org.jclouds.atmos.blobstore.functions.DirectoryEntryListToResourceMetadataList;
import org.jclouds.atmos.blobstore.functions.ObjectToBlob;
import org.jclouds.atmos.blobstore.functions.ObjectToBlobMetadata;
import org.jclouds.atmos.options.ListOptions;
import org.jclouds.atmos.util.AtmosUtils;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.functions.BlobToHttpGetOptions;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.strategy.internal.FetchBlobMetadata;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.crypto.Crypto;
import org.jclouds.domain.Location;
import org.jclouds.http.options.GetOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Singleton
public class AtmosBlobStore extends BaseBlobStore {
	private final AtmosClient sync;
	private final ObjectToBlob object2Blob;
	private final ObjectToBlobMetadata object2BlobMd;
	private final BlobToObject blob2Object;
	private final BlobStoreListOptionsToListOptions container2ContainerListOptions;
	private final DirectoryEntryListToResourceMetadataList container2ResourceList;
	private final Crypto crypto;
	private final BlobToHttpGetOptions blob2ObjectGetOptions;
	private final Provider<FetchBlobMetadata> fetchBlobMetadataProvider;
	private final LoadingCache<String, Boolean> isPublic;

	@Inject
	AtmosBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
			@Memoized Supplier<Set<? extends Location>> locations, PayloadSlicer slicer, AtmosClient sync,
			ObjectToBlob object2Blob, ObjectToBlobMetadata object2BlobMd, BlobToObject blob2Object,
			BlobStoreListOptionsToListOptions container2ContainerListOptions,
			DirectoryEntryListToResourceMetadataList container2ResourceList, Crypto crypto,
			BlobToHttpGetOptions blob2ObjectGetOptions, Provider<FetchBlobMetadata> fetchBlobMetadataProvider,
			LoadingCache<String, Boolean> isPublic) {
		super(context, blobUtils, defaultLocation, locations, slicer);
		this.blob2ObjectGetOptions = checkNotNull(blob2ObjectGetOptions, "blob2ObjectGetOptions");
		this.sync = checkNotNull(sync, "sync");
		this.container2ContainerListOptions = checkNotNull(container2ContainerListOptions,
				"container2ContainerListOptions");
		this.container2ResourceList = checkNotNull(container2ResourceList, "container2ResourceList");
		this.object2Blob = checkNotNull(object2Blob, "object2Blob");
		this.blob2Object = checkNotNull(blob2Object, "blob2Object");
		this.object2BlobMd = checkNotNull(object2BlobMd, "object2BlobMd");
		this.crypto = checkNotNull(crypto, "crypto");
		this.fetchBlobMetadataProvider = checkNotNull(fetchBlobMetadataProvider, "fetchBlobMetadataProvider");
		this.isPublic = checkNotNull(isPublic, "isPublic");
	}

	/**
	* This implementation invokes {@link AtmosClient#headFile}
	*/
	@Override
	public BlobMetadata blobMetadata(String container, String key) {
		return object2BlobMd.apply(sync.headFile(container + "/" + key));
	}

	/**
	* This implementation invokes {@link AtmosClient#deletePath} followed by
	* {@link AtmosClient#pathExists} until it is true.
	*/
	protected boolean deleteAndVerifyContainerGone(final String container) {
		sync.deletePath(container + "/");
		return !sync.pathExists(container + "/");
	}

	/**
	* This implementation invokes {@link AtmosClient#createDirectory}
	* 
	* @param location
	*           currently ignored
	* @param container
	*           directory name
	*/
	@Override
	public boolean createContainerInLocation(Location location, String container) {
		return sync.createDirectory(container) != null;
	}

	@Override
	public ContainerAccess getContainerAccess(String container) {
		if (sync.isPublic(container)) {
			return ContainerAccess.PUBLIC_READ;
		} else {
			return ContainerAccess.PRIVATE;
		}
	}

	@Override
	public void setContainerAccess(String container, ContainerAccess access) {
		org.jclouds.atmos.options.PutOptions options = new org.jclouds.atmos.options.PutOptions();
		if (access == ContainerAccess.PUBLIC_READ) {
			options.publicRead();
		} else {
			options.publicNone();
		}
		sync.setGroupAcl(container, options);
	}

	/**
	* This implementation invokes {@link AtmosClient#createDirectory}
	* 
	* @param container
	*           directory name
	*/
	@Override
	public void createDirectory(String container, String directory) {
		sync.createDirectory(container + "/" + directory);
	}

	/**
	* This implementation invokes {@link #removeBlob}
	*/
	@Override
	public void deleteDirectory(String containerName, String directory) {
		removeBlob(containerName, directory + "/");
	}

	/**
	* This implementation invokes {@link AtmosClient#pathExists}
	*/
	@Override
	public boolean containerExists(String container) {
		return sync.pathExists(container + "/");
	}

	/**
	* This implementation invokes {@link AtmosClient#pathExists}
	*/
	@Override
	public boolean directoryExists(String container, String directory) {
		return sync.pathExists(container + "/" + directory + "/");
	}

	/**
	* This implementation invokes {@link AtmosClient#pathExists}
	* 
	* @param container
	*           container
	* @param key
	*           file name
	*/
	@Override
	public boolean blobExists(String container, String key) {
		return sync.pathExists(container + "/" + key);
	}

	/**
	* This implementation invokes {@link AtmosClient#readFile}
	*/
	@Override
	public Blob getBlob(String container, String key, org.jclouds.blobstore.options.GetOptions options) {
		GetOptions httpOptions = blob2ObjectGetOptions.apply(options);
		return object2Blob.apply(sync.readFile(container + "/" + key, httpOptions));
	}

	/**
	* This implementation invokes {@link AtmosClient#listDirectories}
	*/
	@Override
	public PageSet<? extends StorageMetadata> list() {
		return container2ResourceList.apply(sync.listDirectories());
	}

	/**
	* This implementation invokes {@link AtmosClient#listDirectory}
	*/
	@Override
	public PageSet<? extends StorageMetadata> list(String container,
			org.jclouds.blobstore.options.ListContainerOptions options) {
		container = AtmosUtils.adjustContainerIfDirOptionPresent(container, options);
		ListOptions nativeOptions = container2ContainerListOptions.apply(options);
		// until includeMeta() option works for namespace interface
		PageSet<? extends StorageMetadata> list = container2ResourceList
				.apply(sync.listDirectory(container, nativeOptions));
		return options.isDetailed() ? fetchBlobMetadataProvider.get().setContainerName(container).apply(list) : list;
	}

	/**
	* This implementation invokes {@link AtmosClient#createFile}
	* <p/>
	* Since there is no etag support in atmos, we just return the path.
	*/
	@Override
	public String putBlob(final String container, final Blob blob) {
		return putBlob(container, blob, PutOptions.NONE);
	}

	/**
	* This implementation invokes {@link AtmosClient#createFile}
	* <p/>
	* Since there is no etag support in atmos, we just return the path.
	*/
	@Override
	public String putBlob(String container, Blob blob, PutOptions options) {
		if (options.isMultipart()) {
			throw new UnsupportedOperationException("Atmos does not support multipart uploads");
		}
		org.jclouds.atmos.options.PutOptions atmosOptions = new org.jclouds.atmos.options.PutOptions();
		atmosOptions.publicNone();
		try {
			// TODO: not needed
			if (isPublic.getUnchecked(container + "/"))
				atmosOptions.publicRead();
		} catch (CacheLoader.InvalidCacheLoadException e) {
			// nulls not permitted
		}
		if (options.getBlobAccess() == BlobAccess.PUBLIC_READ) {
			atmosOptions.publicRead();
		}
		return AtmosUtils.putBlob(sync, crypto, blob2Object, container, blob, atmosOptions);
	}

	/**
	* This implementation invokes {@link AtmosClient#deletePath}
	*/
	@Override
	public void removeBlob(String container, String key) {
		sync.deletePath(container + "/" + key);
	}

	@Override
	public BlobAccess getBlobAccess(String container, String key) {
		if (sync.isPublic(container + "/" + key)) {
			return BlobAccess.PUBLIC_READ;
		} else {
			return BlobAccess.PRIVATE;
		}
	}

	@Override
	public void setBlobAccess(String container, String key, BlobAccess access) {
		org.jclouds.atmos.options.PutOptions options = new org.jclouds.atmos.options.PutOptions();
		if (access == BlobAccess.PUBLIC_READ) {
			options.publicRead();
		} else {
			options.publicNone();
		}
		sync.setGroupAcl(container + "/" + key, options);
	}

	@Override
	public boolean createContainerInLocation(Location location, String container, CreateContainerOptions options) {
		if (options.isPublicRead()) {
			return sync.createDirectory(container, publicRead()) != null;
		}
		return createContainerInLocation(location, container);
	}

	@Override
	public MultipartUpload initiateMultipartUpload(String container, BlobMetadata blobMetadata, PutOptions options) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public void abortMultipartUpload(MultipartUpload mpu) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public String completeMultipartUpload(MultipartUpload mpu, List<MultipartPart> parts) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public MultipartPart uploadMultipartPart(MultipartUpload mpu, int partNumber, Payload payload) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public List<MultipartPart> listMultipartUpload(MultipartUpload mpu) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public List<MultipartUpload> listMultipartUploads(String container) {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public long getMinimumMultipartPartSize() {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public long getMaximumMultipartPartSize() {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public int getMaximumNumberOfParts() {
		throw new UnsupportedOperationException("Atmos does not support multipart uploads");
	}

	@Override
	public String copyBlob(String fromContainer, String fromName, String toContainer, String toName,
			CopyOptions options) {
		if (options.ifMatch() != null) {
			throw new UnsupportedOperationException("Atmos does not support ifMatch");
		}
		if (options.ifNoneMatch() != null) {
			throw new UnsupportedOperationException("Atmos does not support ifNoneMatch");
		}
		return super.copyBlob(fromContainer, fromName, toContainer, toName, options);
	}
}
