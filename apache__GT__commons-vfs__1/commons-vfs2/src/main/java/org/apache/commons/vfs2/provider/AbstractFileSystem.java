/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.cache.OnCallRefreshFileObject;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;
import org.apache.commons.vfs2.events.ChangedEvent;
import org.apache.commons.vfs2.events.CreateEvent;
import org.apache.commons.vfs2.events.DeleteEvent;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.Messages;

/**
 * A partial {@link org.apache.commons.vfs2.FileSystem} implementation.
 */
public abstract class AbstractFileSystem extends AbstractVfsComponent implements FileSystem {

	private static final FileListener[] EMPTY_FILE_LISTENER_ARRAY = {};

	private static final Log LOG = LogFactory.getLog(AbstractFileSystem.class);

	/**
	 * The "root" of the file system. This is always "/" so it isn't always the "real" root.
	 */
	private final FileName rootName;

	/**
	 * The root URI of the file system. The base path specified as a file system option when the file system was
	 * created.
	 */
	private final String rootURI;

	private final Collection<Capability> capabilities = new HashSet<>();

	private final FileObject parentLayer;

	/**
	 * Map from FileName to an ArrayList of listeners for that file.
	 */
	private final Map<FileName, ArrayList<FileListener>> listenerMap = new HashMap<>();

	/**
	 * FileSystemOptions used for configuration
	 */
	private final FileSystemOptions fileSystemOptions;

	/**
	 * How many fileObjects are handed out
	 */
	private final AtomicLong useCount = new AtomicLong();

	private FileSystemKey cacheKey;

	/**
	 * open streams counter for this file system
	 */
	private final AtomicInteger openStreams = new AtomicInteger(0);

	/** Only provided for Serializable subclasses. */
	AbstractFileSystem() {
		this(null, null, null);
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param rootFileName The root file name of this file system.
	 * @param parentLayer The parent layer of this file system.
	 * @param fileSystemOptions Options to build this file system.
	 */
	protected AbstractFileSystem(final FileName rootFileName, final FileObject parentLayer,
			final FileSystemOptions fileSystemOptions) {
		this.parentLayer = parentLayer;
		this.rootName = rootFileName;
		this.fileSystemOptions = fileSystemOptions;
		final FileSystemConfigBuilder builder = DefaultFileSystemConfigBuilder.getInstance();
		String uri = builder.getRootURI(fileSystemOptions);
		if (uri == null) {
			uri = rootFileName != null ? rootFileName.getURI() : null;
		}
		this.rootURI = uri;
	}

	/**
	 * Adds the capabilities of this file system.
	 *
	 * @param caps collections of Capabilities, can be immutable.
	 */
	protected abstract void addCapabilities(Collection<Capability> caps);

	/**
	 * Adds a junction to this file system.
	 *
	 * @param junctionPoint The junction point.
	 * @param targetFile The target to add.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void addJunction(final String junctionPoint, final FileObject targetFile) throws FileSystemException {
		throw new FileSystemException("vfs.provider/junctions-not-supported.error", rootName);
	}

	/**
	 * Adds a listener on a file in this file system.
	 *
	 * @param file The FileObject to be monitored.
	 * @param listener The FileListener
	 */
	@Override
	public void addListener(final FileObject file, final FileListener listener) {
		synchronized (listenerMap) {
			final ArrayList<FileListener> listeners = listenerMap.computeIfAbsent(file.getName(),
					k -> new ArrayList<>());
			listeners.add(listener);
		}
	}

	/**
	 * Closes this component.
	 */
	@Override
	public void close() {
		closeCommunicationLink();
	}

	/**
	 * Closes the underlying link used to access the files.
	 */
	public void closeCommunicationLink() {
		synchronized (this) {
			doCloseCommunicationLink();
		}
	}

	/**
	 * Creates a file object.
	 * <p>
	 * This method is called only if the requested file is not cached.
	 * </p>
	 *
	 * @param name name referencing the new file.
	 * @return new created FileObject.
	 * @throws Exception might throw an Exception, which is then wrapped in FileSystemException.
	 */
	protected abstract FileObject createFile(AbstractFileName name) throws Exception;

	protected FileObject decorateFileObject(FileObject file) throws FileSystemException {
		if (getFileSystemManager().getCacheStrategy().equals(CacheStrategy.ON_CALL)) {
			file = new OnCallRefreshFileObject(file);
		}

		if (getFileSystemManager().getFileObjectDecoratorConst() != null) {
			try {
				file = (FileObject) getFileSystemManager().getFileObjectDecoratorConst().newInstance(file);
			} catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new FileSystemException("vfs.impl/invalid-decorator.error",
						getFileSystemManager().getFileObjectDecorator().getName(), e);
			}
		}

		return file;
	}

	/**
	 * Closes the underlying link used to access the files.
	 */
	protected void doCloseCommunicationLink() {
		// default is noop.
	}

	/**
	 * Creates a temporary local copy of a file and its descendants.
	 *
	 * @param file the start of the tree.
	 * @param selector selection what to do with children.
	 * @return replicated root file.
	 * @throws Exception any Exception is wrapped as FileSystemException.
	 */
	protected File doReplicateFile(final FileObject file, final FileSelector selector) throws Exception {
		return getContext().getReplicator().replicateFile(file, selector);
	}

	void fileObjectDestroyed(final FileObject fileObject) {
		useCount.decrementAndGet();
	}

	void fileObjectHanded(final FileObject fileObject) {
		useCount.incrementAndGet();
	}

	/**
	 * Fires an event.
	 */
	private void fireEvent(final AbstractFileChangeEvent event) {
		FileListener[] fileListeners = null;
		final FileObject fileObject = event.getFileObject();

		synchronized (listenerMap) {
			final ArrayList<?> listeners = listenerMap.get(fileObject.getName());
			if (listeners != null) {
				fileListeners = listeners.toArray(EMPTY_FILE_LISTENER_ARRAY);
			}
		}

		if (fileListeners != null) {
			for (final FileListener fileListener : fileListeners) {
				try {
					event.notify(fileListener);
				} catch (final Exception e) {
					final String message = Messages.getString("vfs.provider/notify-listener.warn", fileObject);
					// getLogger().warn(message, e);
					VfsLog.warn(getLogger(), LOG, message, e);
				}
			}
		}
	}

	/**
	 * Fires a file changed event.
	 * <p>
	 * This will only happen if you monitor the file using {@link org.apache.commons.vfs2.FileMonitor}.
	 * </p>
	 *
	 * @param file The FileObject that changed.
	 */
	public void fireFileChanged(final FileObject file) {
		fireEvent(new ChangedEvent(file));
	}

	/**
	 * Fires a file create event.
	 *
	 * @param file The FileObject that was created.
	 */
	public void fireFileCreated(final FileObject file) {
		fireEvent(new CreateEvent(file));
	}

	/**
	 * Fires a file delete event.
	 *
	 * @param file The FileObject that was deleted.
	 */
	public void fireFileDeleted(final FileObject file) {
		fireEvent(new DeleteEvent(file));
	}

	void freeResources() {
		// default is noop.
	}

	/**
	 * Retrieves the attribute with the specified name. The default implementation simply throws an exception.
	 *
	 * @param attrName The name of the attribute.
	 * @return the Object associated with the attribute or null if no object is.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public Object getAttribute(final String attrName) throws FileSystemException {
		throw new FileSystemException("vfs.provider/get-attribute-not-supported.error");
	}

	FileSystemKey getCacheKey() {
		return this.cacheKey;
	}

	/**
	 * Returns a cached file.
	 *
	 * @param name name to search for.
	 * @return file object or null if not found.
	 */
	protected FileObject getFileFromCache(final FileName name) {
		return getFilesCache().getFile(this, name);
	}

	private FilesCache getFilesCache() {
		final FilesCache filesCache = getContext().getFileSystemManager().getFilesCache();
		if (filesCache == null) {
			throw new RuntimeException(Messages.getString("vfs.provider/files-cache-missing.error"));
		}

		return filesCache;
	}

	/**
	 * Returns the FileSystemManager used to instantiate this file system.
	 *
	 * @return the FileSystemManager.
	 */
	@Override
	public FileSystemManager getFileSystemManager() {
		return getContext().getFileSystemManager();
	}

	/**
	 * Returns the FileSystemOptions used to instantiate this file system.
	 *
	 * @return the FileSystemOptions.
	 */
	@Override
	public FileSystemOptions getFileSystemOptions() {
		return fileSystemOptions;
	}

	/**
	 * Returns the accuracy of the last modification time.
	 *
	 * @return ms 0 perfectly accurate, {@literal >0} might be off by this value e.g. sftp 1000ms
	 */
	@Override
	public double getLastModTimeAccuracy() {
		return 0;
	}

	/**
	 * Returns the parent layer if this is a layered file system.
	 *
	 * @return The FileObject for the parent layer.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject getParentLayer() throws FileSystemException {
		return parentLayer;
	}

	/**
	 * Returns the root file of this file system.
	 *
	 * @return The root FileObject of the FileSystem
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject getRoot() throws FileSystemException {
		return resolveFile(rootName);
	}

	/**
	 * Returns the name of the root of this file system.
	 *
	 * @return the root FileName.
	 */
	@Override
	public FileName getRootName() {
		return rootName;
	}

	/**
	 * Returns the root URI specified for this file System.
	 *
	 * @return The root URI used in this file system.
	 * @since 2.0
	 */
	@Override
	public String getRootURI() {
		return rootURI;
	}

	/**
	 * Determines if this file system has a particular capability.
	 *
	 * @param capability the Capability to check for.
	 * @return true if the FileSystem has the Capability, false otherwise.
	 */
	@Override
	public boolean hasCapability(final Capability capability) {
		return capabilities.contains(capability);
	}

	/**
	 * Initializes this component.
	 *
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void init() throws FileSystemException {
		addCapabilities(capabilities);
	}

	/**
	 * Checks if this file system has open streams.
	 *
	 * @return true if the FileSystem has open streams.
	 */
	public boolean isOpen() {
		return openStreams.get() > 0;
	}

	/**
	 * Returns true if no file is using this FileSystem.
	 *
	 * @return true if no file is using this FileSystem.
	 */
	public boolean isReleaseable() {
		return useCount.get() < 1;
	}

	/**
	 * Called after all file-objects closed their streams.
	 */
	protected void notifyAllStreamsClosed() {
		// default is noop.
	}

	/**
	 * Adds a file object to the cache.
	 *
	 * @param file the file to add.
	 */
	protected void putFileToCache(final FileObject file) {
		getFilesCache().putFile(file);
	}

	/**
	 * Removes a cached file.
	 *
	 * @param name The file name to remove.
	 */
	protected void removeFileFromCache(final FileName name) {
		getFilesCache().removeFile(this, name);
	}

	/**
	 * Removes a junction from this file system.
	 *
	 * @param junctionPoint The junction point.
	 * @throws FileSystemException if an error occurs
	 */
	@Override
	public void removeJunction(final String junctionPoint) throws FileSystemException {
		throw new FileSystemException("vfs.provider/junctions-not-supported.error", rootName);
	}

	/**
	 * Removes a listener from a file in this file system.
	 *
	 * @param file The FileObject to be monitored.
	 * @param listener The FileListener
	 */
	@Override
	public void removeListener(final FileObject file, final FileListener listener) {
		synchronized (listenerMap) {
			final ArrayList<?> listeners = listenerMap.get(file.getName());
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty()) {
					listenerMap.remove(file.getName());
				}
			}
		}
	}

	/**
	 * Creates a temporary local copy of a file and its descendants.
	 *
	 * @param file The FileObject to replicate.
	 * @param selector The FileSelector.
	 * @return The replicated File.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public File replicateFile(final FileObject file, final FileSelector selector) throws FileSystemException {
		if (!FileObjectUtils.exists(file)) {
			throw new FileSystemException("vfs.provider/replicate-missing-file.error", file.getName());
		}

		try {
			return doReplicateFile(file, selector);
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/replicate-file.error", file.getName(), e);
		}
	}

	/**
	 * Finds a file in this file system.
	 *
	 * @param name The name of the file to locate.
	 * @return The located FileObject or null if none could be located.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject resolveFile(final FileName name) throws FileSystemException {
		return resolveFile(name, true);
	}

	private synchronized FileObject resolveFile(final FileName name, final boolean useCache)
			throws FileSystemException {
		if (!rootName.getRootURI().equals(name.getRootURI())) {
			throw new FileSystemException("vfs.provider/mismatched-fs-for-name.error", name, rootName,
					name.getRootURI());
		}

		// imario@apache.org ==> use getFileFromCache
		FileObject file;
		if (useCache) {
			file = getFileFromCache(name);
		} else {
			file = null;
		}

		if (file == null) {
			try {
				file = createFile((AbstractFileName) name);
			} catch (final Exception e) {
				throw new FileSystemException("vfs.provider/resolve-file.error", name, e);
			}

			file = decorateFileObject(file);

			// imario@apache.org ==> use putFileToCache
			if (useCache) {
				putFileToCache(file);
			}
		}

		/*
		  resync the file information if requested
		 */
		if (getFileSystemManager().getCacheStrategy().equals(CacheStrategy.ON_RESOLVE)) {
			file.refresh();
		}
		return file;
	}

	/**
	 * Finds a file in this file system.
	 *
	 * @param nameStr The name of the file to resolve.
	 * @return The located FileObject or null if none could be located.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject resolveFile(final String nameStr) throws FileSystemException {
		// Resolve the name, and create the file
		return resolveFile(getFileSystemManager().resolveName(rootName, nameStr));
	}

	/**
	 * Sets the attribute with the specified name. The default implementation simply throws an exception.
	 *
	 * @param attrName the attribute name.
	 * @param value The object to associate with the attribute.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void setAttribute(final String attrName, final Object value) throws FileSystemException {
		throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
	}

	void setCacheKey(final FileSystemKey cacheKey) {
		this.cacheKey = cacheKey;
	}

	void streamClosed() {
		if (openStreams.decrementAndGet() == 0) {
			notifyAllStreamsClosed();
		}
	}

	void streamOpened() {
		openStreams.incrementAndGet();
	}
}
