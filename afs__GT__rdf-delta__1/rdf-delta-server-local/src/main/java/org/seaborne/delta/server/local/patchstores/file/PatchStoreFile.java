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

package org.seaborne.delta.server.local.patchstores.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.seaborne.delta.DataSourceDescription;
import org.seaborne.delta.DeltaConst;
import org.seaborne.delta.Id;
import org.seaborne.delta.server.local.LocalServerConfig;
import org.seaborne.delta.server.local.PatchLog;
import org.seaborne.delta.server.local.PatchStore;
import org.seaborne.delta.server.local.PatchStoreProvider;
import org.seaborne.delta.server.local.patchstores.PatchLogIndex;
import org.seaborne.delta.server.local.patchstores.PatchStorage;
import org.seaborne.delta.server.local.patchstores.filestore.FileArea;
import org.seaborne.delta.server.local.patchstores.filestore.FileStore;

public class PatchStoreFile extends PatchStore {
	/*   Server Root
	 *      delta.cfg
	 *      /NAME ... per DataSource.
	 *          /source.cfg
	 *          /data -- TDB database (optional)
	 *          /disabled -- if this file is present, then the datasource is not accessible.
	 */
	// Singleton.
	// "static" so two PatchStoreFile's go to the same log.
	private static Map<Id, LogIndexFile> logIndexes = new ConcurrentHashMap<>();

	private final Path patchLogDirectory;

	public PatchStoreFile(String patchLogDirectory, PatchStoreProvider provider) {
		super(provider);
		Objects.requireNonNull(patchLogDirectory);
		this.patchLogDirectory = Paths.get(patchLogDirectory);
	}

	public static void resetTracked() {
		logIndexes.clear();
	}

	/*package*/ LogIndexFile getLogIndex(Id id) {
		return logIndexes.get(id);
	}

	@Override
	protected void initialize(LocalServerConfig config) {
		return;
	}

	@Override
	protected List<DataSourceDescription> initialDataSources() {
		return FileArea.scanForLogs(patchLogDirectory);
	}

	@Override
	protected PatchLog newPatchLog(DataSourceDescription dsd) {
		Id id = dsd.getId();
		logIndexes.computeIfAbsent(id, x -> {
			Path fileStoreDir = patchLogDirectory.resolve(dsd.getName());
			if (!Files.exists(fileStoreDir))
				FileArea.setupDataSourceByFile(patchLogDirectory, this, dsd);
			FileStore fileStore = FileStore.attach(fileStoreDir, DeltaConst.FilePatchBasename);
			return LogIndexFile.create(fileStore);
		});
		// The LogIndexFile will be picked up by newPatchLogIndex, newPatchStorage
		// calls in PatchStoreProviderFile.
		PatchLog newPatchLog = newPatchLogFromIndexAndStorage(dsd);
		return newPatchLog;
	}

	@Override
	protected PatchLogIndex newPatchLogIndex(DataSourceDescription dsd, PatchStore patchStore,
			LocalServerConfig configuration) {
		PatchStoreFile patchStoreFile = (PatchStoreFile) patchStore;
		LogIndexFile filePatchIdx = patchStoreFile.getLogIndex(dsd.getId());
		return new PatchLogIndexFile(filePatchIdx);
	}

	@Override
	protected PatchStorage newPatchStorage(DataSourceDescription dsd, PatchStore patchStore,
			LocalServerConfig configuration) {
		PatchStoreFile patchStoreFile = (PatchStoreFile) patchStore;
		LogIndexFile logIndexFile = patchStoreFile.getLogIndex(dsd.getId());
		return new PatchStorageFile(logIndexFile.fileStore(), logIndexFile::idToVersion);
	}

	@Override
	protected void delete(PatchLog patchLog) {
		Id id = patchLog.getDescription().getId();
		LogIndexFile logIndexFile = logIndexes.remove(id);
		logIndexFile.fileStore().release();
		Path path = logIndexFile.getPath();
		FileArea.retire(path);
	}

	@Override
	protected void shutdownSub() {
	}

}
