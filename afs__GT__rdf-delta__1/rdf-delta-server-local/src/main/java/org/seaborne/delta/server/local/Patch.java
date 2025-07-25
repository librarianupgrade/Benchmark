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

package org.seaborne.delta.server.local;

import org.apache.jena.graph.Node;
import org.seaborne.delta.Id;
import org.seaborne.delta.server.local.patchstores.filestore.FileEntry;
import org.apache.jena.rdfpatch.PatchHeader;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;

/** Holder for an {@link RDFPatch}. This carries with it system information
 * such as where the patch is stored and any local version number. 
 */
public class Patch implements RDFPatch {
	// PatchWrapper

	private final RDFPatch patch;
	private final DataSource dataSource;
	private final FileEntry fileEntry;

	public Patch(boolean unused, RDFPatch patch, DataSource source, FileEntry entry) {
		this.patch = patch;
		this.dataSource = source;
		this.fileEntry = entry;
	}

	public RDFPatch get() {
		return patch;
	}

	@Override
	public Node getId() {
		return patch.getId();
	}

	@Override
	public Node getPrevious() {
		return patch.getPrevious();
	}

	public Id getIdAsId() {
		return Id.fromNode(getId());
	}

	public Id getPreviousIdAsId() {
		return Id.fromNode(getPrevious());
	}

	@Override
	public void apply(RDFChanges changes) {
		patch.apply(changes);
	}

	@Override
	public PatchHeader header() {
		return patch.header();
	}

	public void play(RDFChanges changes) {
		patch.apply(changes);
	}

	@Override
	public boolean repeatable() {
		return patch.repeatable();
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public long getSourceVersion() {
		return fileEntry.version;
	}
}