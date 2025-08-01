package org.apache.maven.scm;

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

import java.io.Serializable;

/**
 * @TODO move to a real enum
 * <p/>
 * Typesafe enum for file status
 * </p>
 * <p/>
 * There are two types of status defined in this class: <br/>
 * 1) Status: Changes in the working tree, not yet committed to the repository eg. MODIFIED <br/>
 * 2) Transaction: The file is part of some transaction with the repository eg. CHECKED_IN
 * </p>
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public final class ScmFileStatus implements Serializable {
	private static final long serialVersionUID = -7840223279162817915L;

	/**
	 * File is added to the working tree and does not yet exist in the repository
	 */
	public static final ScmFileStatus ADDED = new ScmFileStatus("added");

	/**
	 * File is removed from the working tree thus not revisioned anymore.<br>
	 * The file is still present in the repository.<br>
	 * The file could be deleted from the filesystem depending on the provider.
	 */
	public static final ScmFileStatus DELETED = new ScmFileStatus("deleted");

	/**
	 * The file has been modified in the working tree.
	 */
	public static final ScmFileStatus MODIFIED = new ScmFileStatus("modified");

	/**
	 * The file has been renamed or moved in the working tree.
	 * @since 1.7
	 */
	public static final ScmFileStatus RENAMED = new ScmFileStatus("renamed");

	/**
	 * The file has been copied in the working tree.
	 * @since 1.7
	 */
	public static final ScmFileStatus COPIED = new ScmFileStatus("copied");

	/**
	 * The file is missing in the working tree.
	 */
	public static final ScmFileStatus MISSING = new ScmFileStatus("missing");

	/**
	 * File from working tree is checked into the repository
	 */
	public static final ScmFileStatus CHECKED_IN = new ScmFileStatus("checked-in");

	/**
	 * File is checked out from the repository and into the working tree
	 */
	public static final ScmFileStatus CHECKED_OUT = new ScmFileStatus("checked-out");

	/**
	 * The file in the working tree has differences to the one in repository that
	 * conflicts ie. it cannot automatically be merged.
	 */
	public static final ScmFileStatus CONFLICT = new ScmFileStatus("conflict");

	/**
	 * The file in the working tree has been updated with changes from the repository.
	 */
	public static final ScmFileStatus PATCHED = new ScmFileStatus("patched");

	/**
	 * The file is added, removed or updated from the repository, thus its
	 * up-to-date with the version in the repository. See also isUpdate()
	 */
	public static final ScmFileStatus UPDATED = new ScmFileStatus("updated");

	/**
	 * The file is part of a tag.
	 */
	public static final ScmFileStatus TAGGED = new ScmFileStatus("tagged");

	/**
	 * The file is locked.
	 */
	public static final ScmFileStatus LOCKED = new ScmFileStatus("locked");

	/**
	 * The file is in the working tree but is not versioned and not ignored either.
	 */
	public static final ScmFileStatus UNKNOWN = new ScmFileStatus("unknown");

	/**
	 * @since 1.5
	 * The file is being edited
	 */
	public static final ScmFileStatus EDITED = new ScmFileStatus("edit");

	/**
	 * The status name
	 */
	private final String name;

	private ScmFileStatus(String name) {
		this.name = name;
	}

	/** {@inheritDoc} */
	public String toString() {
		return name;
	}

	/**
	 * There are changes in the working tree that are not committed to the repository, or <br>
	 * the file is unknown for the working tree.
	 *
	 * @return true on changes in the working tree or if the file is unknown.
	 */
	public boolean isStatus() {
		return this == UNKNOWN || isDiff();
	}

	/**
	 * There are changes in the working tree that are not committed to the repository. <br>
	 *
	 * @return true on changes in the working tree
	 */
	public boolean isDiff() {
		return this == ADDED || this == DELETED || this == MODIFIED;
	}

	/**
	 * @return true if the file was part of a transaction with the repository.
	 */
	public boolean isTransaction() {
		return this == CHECKED_IN || this == CHECKED_OUT || this == LOCKED || this == TAGGED || isUpdate();
	}

	/**
	 * File is part of an update transaction with the repository.<br>
	 * Note: ADDED and REMOVED are not an update status since they indicates
	 * that the working tree has changed.<br>
	 * An update indicates the opposite, that the repository was changed compared to
	 * the working tree and that it is now synchronized unless there are conflicts.
	 *
	 * @return true if the status is conflict, updated or patched.
	 */
	public boolean isUpdate() {
		return this == CONFLICT || this == UPDATED || this == PATCHED;
	}
}
