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

package org.seaborne.delta.server.patchstores;

import org.seaborne.delta.DataSourceDescription;
import org.seaborne.delta.Id;
import org.seaborne.delta.server.local.*;
import org.seaborne.delta.server.local.patchstores.mem.PatchStoreProviderMem;

public class TestPatchLogMem extends AbstractTestPatchLog {

	@Override
	protected PatchLog patchLog() {
		DataSourceDescription dsd = new DataSourceDescription(Id.create(), "ABC", "http://test/ABC");
		PatchStoreProvider psp = new PatchStoreProviderMem();
		LocalServerConfig config = LocalServers.configMem();
		PatchStore patchStore = psp.create(config);
		patchStore.initialize(new DataSourceRegistry("mem"), config);
		return patchStore.createLog(dsd);
	}
}
