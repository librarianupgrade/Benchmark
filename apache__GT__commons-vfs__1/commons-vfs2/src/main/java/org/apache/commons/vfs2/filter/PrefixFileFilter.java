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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * Filters file names for a certain prefix.
 * <p>
 * For example, to print all files and directories in the current directory
 * whose name starts with a {@code .}:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(new PrefixFileFilter(&quot;.&quot;)));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "https://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class PrefixFileFilter implements FileFilter, Serializable {

	private static final long serialVersionUID = 1L;

	/** Whether the comparison is case-sensitive. */
	private final IOCase caseSensitivity;

	/** The file name prefixes to search for. */
	private final List<String> prefixes;

	/**
	 * Constructs a new Prefix file filter for a list of prefixes specifying
	 * case-sensitivity.
	 *
	 * @param caseSensitivity how to handle case sensitivity, null means
	 *                        case-sensitive
	 * @param prefixes        the prefixes to allow, must not be null
	 */
	public PrefixFileFilter(final IOCase caseSensitivity, final List<String> prefixes) {
		if (prefixes == null) {
			throw new IllegalArgumentException("The list of prefixes must not be null");
		}
		this.prefixes = new ArrayList<>(prefixes);
		this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	}

	/**
	 * Constructs a new Prefix file filter for any of an array of prefixes
	 * specifying case-sensitivity.
	 *
	 * @param prefixes        the prefixes to allow, must not be null
	 * @param caseSensitivity how to handle case sensitivity, null means
	 *                        case-sensitive
	 */
	public PrefixFileFilter(final IOCase caseSensitivity, final String... prefixes) {
		if (prefixes == null) {
			throw new IllegalArgumentException("The array of prefixes must not be null");
		}
		this.prefixes = new ArrayList<>(Arrays.asList(prefixes));
		this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	}

	/**
	 * Constructs a new Prefix file filter for a list of prefixes.
	 *
	 * @param prefixes the prefixes to allow, must not be null
	 */
	public PrefixFileFilter(final List<String> prefixes) {
		this(IOCase.SENSITIVE, prefixes);
	}

	/**
	 * Constructs a new Prefix file filter for any of an array of prefixes.
	 * <p>
	 * The array is not cloned, so could be changed after constructing the instance.
	 * This would be inadvisable however.
	 *
	 * @param prefixes the prefixes to allow, must not be null
	 */
	public PrefixFileFilter(final String... prefixes) {
		this(IOCase.SENSITIVE, prefixes);
	}

	/**
	 * Checks to see if the file name starts with the prefix.
	 *
	 * @param fileSelectInfo the File to check
	 *
	 * @return true if the file name starts with one of our prefixes
	 */
	@Override
	public boolean accept(final FileSelectInfo fileSelectInfo) {
		final String name = fileSelectInfo.getFile().getName().getBaseName();
		return prefixes.stream().anyMatch(prefix -> caseSensitivity.checkStartsWith(name, prefix));
	}

	/**
	 * Provide a String representation of this file filter.
	 *
	 * @return a String representation
	 */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(super.toString());
		buffer.append("(");
		if (prefixes != null) {
			buffer.append(String.join(",", prefixes));
		}
		buffer.append(")");
		return buffer.toString();
	}

}
