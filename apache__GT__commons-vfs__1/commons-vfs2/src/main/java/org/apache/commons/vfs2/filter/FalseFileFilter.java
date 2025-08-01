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
package org.apache.commons.vfs2.filter;

import java.io.Serializable;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * A file filter that always returns false.
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "https://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class FalseFileFilter implements FileFilter, Serializable {

	/**
	 * Singleton instance of false filter.
	 *
	 * @since 2.10.0
	 */
	public static final FileFilter INSTANCE = new FalseFileFilter();

	/**
	 * Singleton instance of false filter.
	 * @deprecated Use {@link #INSTANCE}.
	 */
	@Deprecated
	public static final FileFilter FALSE = INSTANCE;

	private static final long serialVersionUID = 1L;

	/**
	 * Restrictive constructor.
	 */
	protected FalseFileFilter() {
	}

	/**
	 * Returns false.
	 *
	 * @param fileSelectInfo the file to check (ignored)
	 *
	 * @return Always {@code false}
	 */
	@Override
	public boolean accept(final FileSelectInfo fileSelectInfo) {
		return false;
	}

}
