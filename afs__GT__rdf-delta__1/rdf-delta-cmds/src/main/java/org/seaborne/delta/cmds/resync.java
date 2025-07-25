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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sys.JenaSystem;

/** Reread the on-disk state. */
public class resync extends DeltaCmd {

	static {
		LogCtl.setLogging();
		JenaSystem.init();
	}

	public static void main(String... args) {
		new resync(args).mainRun();
	}

	public resync(String[] argv) {
		super(argv);
		super.add(argLogName);
		super.add(argDataSourceURI);
	}

	@Override
	protected String getSummary() {
		return getCommandName() + " --server URL";
	}

	@Override
	protected void execCmd() {
	}

	@Override
	protected void checkForMandatoryArgs() {
		//        if ( !contains(argDataSourceName) && ! contains(argDataSourceURI) )
		//            throw new CmdException("Required: one of --"+argDataSourceName.getKeyName()+" or --"+argDataSourceURI.getKeyName());
	}
}
