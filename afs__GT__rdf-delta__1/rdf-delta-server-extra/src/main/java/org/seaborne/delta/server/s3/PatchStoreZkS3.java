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

package org.seaborne.delta.server.s3;

import org.apache.curator.framework.CuratorFramework;
import org.seaborne.delta.DataSourceDescription;
import org.seaborne.delta.server.local.LocalServerConfig;
import org.seaborne.delta.server.local.PatchStore;
import org.seaborne.delta.server.local.PatchStoreProvider;
import org.seaborne.delta.server.local.patchstores.PatchStorage;
import org.seaborne.delta.server.local.patchstores.zk.PatchStoreZk;
import org.seaborne.delta.server.s3.PatchStoreProviderZkS3.DetailsS3;

public class PatchStoreZkS3 extends PatchStoreZk {

	private final DetailsS3 s3;

	PatchStoreZkS3(CuratorFramework client, PatchStoreProvider psp, DetailsS3 s3, String rootDirName) {
		super(client, psp, rootDirName);
		this.s3 = s3;
	}

	public DetailsS3 access() {
		return s3;
	}

	// Same "newPatchLog" and "newPatchIndex"

	@Override
	public PatchStorage newPatchStorage(DataSourceDescription dsd, PatchStore patchStore,
			LocalServerConfig configuration) {
		DetailsS3 s3 = ((PatchStoreZkS3) patchStore).access();
		String logPrefix = s3.prefix + dsd.getName() + "/";
		return new PatchStorageS3(s3.client, s3.bucketName, logPrefix);
	}
}
