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
package org.apache.commons.vfs2.provider.tar;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.provider.CompositeFileProvider;

/**
 * A file system provider for Tar files. Provides read-only file systems.
 */
public class TgzFileProvider extends CompositeFileProvider {

	/** The provider's capabilities */
	protected static final Collection<Capability> capabilities = TarFileProvider.capabilities;

	private static final String[] SCHEMES = { "gz", "tar" };

	/**
	 * Constructs a new instance.
	 */
	public TgzFileProvider() {
	}

	@Override
	public Collection<Capability> getCapabilities() {
		return capabilities;
	}

	@Override
	protected String[] getSchemes() {
		return SCHEMES;
	}
}
