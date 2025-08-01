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
package org.apache.commons.vfs2.provider.res;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.url.UrlFileSystem;

/**
 * The config BUILDER for various FTP configuration options.
 */
public final class ResourceFileSystemConfigBuilder extends FileSystemConfigBuilder {

	private static final ResourceFileSystemConfigBuilder BUILDER = new ResourceFileSystemConfigBuilder();

	private ResourceFileSystemConfigBuilder() {
		super("resource.");
	}

	/**
	 * Gets the singleton builder.
	 *
	 * @return the singleton builder.
	 */
	public static ResourceFileSystemConfigBuilder getInstance() {
		return BUILDER;
	}

	/**
	 * Gets the class loader from the FileSystemOptions.
	 *
	 * @param fileSystemOptions The source FileSystemOptions.
	 * @return the class loader from the FileSystemOptions.
	 */
	public ClassLoader getClassLoader(final FileSystemOptions fileSystemOptions) {
		return getParam(fileSystemOptions, ClassLoader.class.getName());
	}

	@Override
	protected Class<? extends FileSystem> getConfigClass() {
		return UrlFileSystem.class;
	}

	/**
	 * Sets the class loader in the FileSystemOptions.
	 *
	 * @param fileSystemOptions Target FileSystemOptions.
	 * @param classLoader The data to set.
	 */
	public void setClassLoader(final FileSystemOptions fileSystemOptions, final ClassLoader classLoader) {
		setParam(fileSystemOptions, ClassLoader.class.getName(), classLoader);
	}
}
