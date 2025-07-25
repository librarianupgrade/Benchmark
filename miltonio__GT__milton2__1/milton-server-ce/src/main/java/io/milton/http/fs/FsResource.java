/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.http.fs;

import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.*;
import java.io.File;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class FsResource
		implements Resource, MoveableResource, CopyableResource, LockableResource, DigestResource {

	private static final Logger log = LoggerFactory.getLogger(FsResource.class);
	File file;
	final FileSystemResourceFactory factory;
	final String host;
	String ssoPrefix;

	protected abstract void doCopy(File dest) throws NotAuthorizedException;

	public FsResource(String host, FileSystemResourceFactory factory, File file) {
		this.host = host;
		this.file = file;
		this.factory = factory;
	}

	public File getFile() {
		return file;
	}

	@Override
	public String getUniqueId() {
		String s = file.lastModified() + "_" + file.length() + "_" + file.getAbsolutePath();
		return s.hashCode() + "";
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public Object authenticate(String user, String password) {
		return factory.getSecurityManager().authenticate(user, password);
	}

	public Object authenticate(DigestResponse digestRequest) {
		return factory.getSecurityManager().authenticate(digestRequest);
	}

	public boolean isDigestAllowed() {
		return factory.isDigestAllowed();
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		boolean b = factory.getSecurityManager().authorise(request, method, auth, this);
		if (log.isTraceEnabled()) {
			log.trace("authorise: result=" + b);
		}
		return b;
	}

	@Override
	public String getRealm() {
		String r = factory.getRealm(this.host);
		if (r == null) {
			throw new NullPointerException("Got null realm from: " + factory.getClass() + " for host=" + this.host);
		}
		return r;
	}

	@Override
	public Date getModifiedDate() {
		return new Date(file.lastModified());
	}

	public Date getCreateDate() {
		return null;
	}

	public int compareTo(Resource o) {
		return this.getName().compareTo(o.getName());
	}

	public void moveTo(CollectionResource newParent, String newName) {
		if (newParent instanceof FsDirectoryResource) {
			FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
			File dest = new File(newFsParent.getFile(), newName);
			boolean ok = this.file.renameTo(dest);
			if (!ok) {
				throw new RuntimeException("Failed to move to: " + dest.getAbsolutePath());
			}
			this.file = dest;
			factory.getWsManager().ifPresent(wsManager -> wsManager
					.notifyMoved(factory.toResourcePath(newFsParent.getFile()), factory.toResourcePath(file)));
		} else {
			throw new RuntimeException(
					"Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
		}
	}

	public void copyTo(CollectionResource newParent, String newName) throws NotAuthorizedException {
		if (newParent instanceof FsDirectoryResource) {
			FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
			File dest = new File(newFsParent.getFile(), newName);
			doCopy(dest);
			factory.getWsManager().ifPresent(wsManager -> wsManager.notifyCreated(factory.toResourcePath(dest)));
		} else {
			throw new RuntimeException(
					"Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
		}
	}

	public void delete() {
		boolean ok = file.delete();
		if (!ok) {
			throw new RuntimeException("Failed to delete");
		}
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyDeleted(factory.toResourcePath(file)));
	}

	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
		final LockResult lockResult = factory.getLockManager().lock(timeout, lockInfo, this);
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyLocked(factory.toResourcePath(file)));
		return lockResult;
	}

	public LockResult refreshLock(String token, LockTimeout timeout) throws NotAuthorizedException {
		final LockResult lockResult = factory.getLockManager().refresh(token, timeout, this);
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyLocked(factory.toResourcePath(file)));
		return lockResult;
	}

	public void unlock(String tokenId) throws NotAuthorizedException {
		factory.getLockManager().unlock(tokenId, this);
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyUnlocked(factory.toResourcePath(file)));
	}

	public LockToken getCurrentLock() {
		if (factory.getLockManager() != null) {
			return factory.getLockManager().getCurrentToken(this);
		} else {
			log.warn("getCurrentLock called, but no lock manager: file: " + file.getAbsolutePath());
			return null;
		}
	}
}
