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

package org.seaborne.delta.cmds;

import java.util.List;

import org.seaborne.delta.DataSourceDescription;
import org.seaborne.delta.PatchLogInfo;
import org.seaborne.delta.Version;

/** List a new log */
public class list extends DeltaCmd {

	public static void main(String... args) {
		new list(args).mainRun();
	}

	public list(String[] argv) {
		super(argv);
		super.add(argLogName);
		super.add(argDataSourceURI);
	}

	@Override
	protected String getSummary() {
		return getCommandName() + " --server URL [--log=NAME]";
	}

	@Override
	protected void execCmd() {
		// "Op1"
		if (dataSourceName != null) {
			execOneName(dataSourceName);
			return;
		}
		if (dataSourceURI != null) {
			execOneURI(dataSourceURI);
			return;
		}

		if (positionals.isEmpty()) {
			execList();
			return;
		}
		positionals.forEach(this::execOneName);
	}

	private void execOneName(String name) {
		DataSourceDescription dsd = dLink.getDataSourceDescriptionByName(name);
		if (dsd == null) {
			System.out.printf("Not found: %s\n", name);
			return;
		}
		detailsByDSD(dsd);
	}

	private void execOneURI(String uriStr) {
		DataSourceDescription dsd = dLink.getDataSourceDescriptionByURI(uriStr);
		if (dsd == null) {
			System.out.printf("Not found: <%s>\n", uriStr);
			return;
		}
		detailsByDSD(dsd);
	}

	protected void execList() {
		List<DataSourceDescription> all = getDescriptions();
		if (all.isEmpty()) {
			System.out.println("-- No logs --");
			return;
		}
		all.forEach(this::detailsByDSD);

		//        List<PatchLogInfo> logs = getPatchLogInfo();
		//        if ( logs.isEmpty()) {
		//            System.out.println("-- No logs --");
		//            return ;
		//        }
		//        logs.forEach(System.out::println);
	}

	private void detailsByDSD(DataSourceDescription dsd) {
		PatchLogInfo logInfo = dLink.getPatchLogInfo(dsd.getId());
		if (logInfo == null) {
			// Some thing bad somewhere.
			System.out.printf("[%s %s <%s> [no info] %s]\n", dsd.getId(), dsd.getName(), dsd.getUri());
			return;
		}
		details(logInfo);
	}

	private void details(PatchLogInfo logInfo) {
		DataSourceDescription dsd = logInfo.getDataSourceDescr();
		if (Version.INIT.equals(logInfo.getMinVersion()) && Version.INIT.equals(logInfo.getMaxVersion())) {
			if (logInfo.getLatestPatch() != null)
				// Should not happen.
				System.out.printf("[%s %s <%s> [empty] %s]\n", dsd.getId(), dsd.getName(), dsd.getUri(),
						logInfo.getLatestPatch().toString());
			else
				System.out.printf("[%s %s <%s> [empty]]\n", dsd.getId(), dsd.getName(), dsd.getUri());
			return;
		}
		if (logInfo.getMinVersion().isValid()) {
			System.out.printf("[%s %s <%s> [%s,%s] %s]\n", dsd.getId(), dsd.getName(), dsd.getUri(),
					logInfo.getMinVersion(), logInfo.getMaxVersion(),
					(logInfo.getLatestPatch() == null) ? "<no patches>" : logInfo.getLatestPatch().toString());
		}

	}

	@Override
	protected void checkForMandatoryArgs() {
	}
}
