/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.link;

import org.seaborne.delta.Id;
import org.seaborne.delta.Version;
import org.apache.jena.rdfpatch.RDFPatch;

/**
 * {@link DeltaLink} listener.
 * Events occur for DataSource changes and for patch actions.
 */
public interface DeltaLinkListener {
	public default void newDataSource(Id dsRef, String name) {
	}

	public default void copyDataSource(Id dsRef, Id dsRef2, String oldName, String newName) {
	}

	public default void renameDataSource(Id dsRef, Id dsRef2, String oldName, String newName) {
	}

	public default void removeDataSource(Id dsRef) {
	}

	/** {@code patch} is null for "not found". */
	public default void fetchById(Id dsRef, Id patchId, RDFPatch patch) {
	}

	public default void fetchByVersion(Id dsRef, Version version, RDFPatch patch) {
	}

	/** Version.UNSET on error.*/
	public default void append(Id dsRef, Version version, RDFPatch patch) {
	}

}
