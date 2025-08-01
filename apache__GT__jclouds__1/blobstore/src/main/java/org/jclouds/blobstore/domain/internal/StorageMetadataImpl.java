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
package org.jclouds.blobstore.domain.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.domain.Location;
import org.jclouds.domain.internal.ResourceMetadataImpl;
import org.jclouds.javax.annotation.Nullable;

/**
 * Idpayload of the object
 */
public class StorageMetadataImpl extends ResourceMetadataImpl<StorageType> implements StorageMetadata {

	@Nullable
	private final String eTag;
	@Nullable
	private final Date creationDate;
	@Nullable
	private final Date lastModified;
	private final StorageType type;
	@Nullable
	private final Long size;

	public StorageMetadataImpl(StorageType type, @Nullable String id, @Nullable String name,
			@Nullable Location location, @Nullable URI uri, @Nullable String eTag, @Nullable Date creationDate,
			@Nullable Date lastModified, Map<String, String> userMetadata, @Nullable Long size) {
		super(id, name, location, uri, userMetadata);
		this.eTag = eTag;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
		this.type = checkNotNull(type, "type");
		this.size = size;
	}

	/** @deprecated call StorageMetadataImpl(StorageType.class, String.class, String.class, Location.class, URI.class, String.class, Date.class, Date.class, Map.class, Long.class) */
	@Deprecated
	public StorageMetadataImpl(StorageType type, @Nullable String id, @Nullable String name,
			@Nullable Location location, @Nullable URI uri, @Nullable String eTag, @Nullable Date creationDate,
			@Nullable Date lastModified, Map<String, String> userMetadata) {
		this(type, id, name, location, uri, eTag, creationDate, lastModified, userMetadata, null);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public StorageType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), eTag, creationDate, lastModified, type, size);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageMetadataImpl other = (StorageMetadataImpl) obj;
		if (!Objects.equal(eTag, other.eTag)) {
			return false;
		}
		if (!Objects.equal(creationDate, other.creationDate)) {
			return false;
		}
		if (!Objects.equal(lastModified, other.lastModified)) {
			return false;
		}
		if (!Objects.equal(type, other.type)) {
			return false;
		}
		if (!Objects.equal(size, other.size)) {
			return false;
		}
		return true;
	}

	@Override
	protected ToStringHelper string() {
		return super.string().add("eTag", eTag).add("creationDate", creationDate).add("lastModified", lastModified)
				.add("type", type).add("size", size);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String getETag() {
		return eTag;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public Long getSize() {
		return size;
	}

}
