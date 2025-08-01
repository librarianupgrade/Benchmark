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
package org.apache.commons.vfs2;

/**
 * The fileCache interface. Implementations of this interface are expected to be thread safe.
 */
public interface FilesCache {

	/**
	 * Purges the entries corresponding to the FileSystem.
	 *
	 * @param fileSystem The FileSystem.
	 */
	void clear(FileSystem fileSystem);

	/**
	 * Purges the whole cache.
	 */
	void close();

	/**
	 * Retrieves a FileObject from the cache by name.
	 *
	 * @param fileSystem The FileSystem.
	 * @param fileName the name
	 * @return the file object or null if file is not cached
	 */
	FileObject getFile(FileSystem fileSystem, FileName fileName);

	/**
	 * Adds a FileObject to the cache.
	 *
	 * @param file the file
	 */
	void putFile(FileObject file);

	/**
	 * Adds a FileObject to the cache if it isn't already present.
	 *
	 * @param file the file
	 * @return true if the file was stored, false otherwise.
	 */
	boolean putFileIfAbsent(FileObject file);

	/**
	 * Removes a file from cache.
	 *
	 * @param fileSystem file system
	 * @param name file name
	 */
	void removeFile(FileSystem fileSystem, FileName name);

	/*
	  If the cache uses timestamps it could use this method to handle updates of them.
	
	  @param file file name
	 */
	// public void touchFile(final FileObject file);
}
