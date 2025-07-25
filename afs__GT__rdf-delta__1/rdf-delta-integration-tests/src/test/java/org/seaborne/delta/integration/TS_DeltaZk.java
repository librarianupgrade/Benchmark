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

package org.seaborne.delta.integration;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.seaborne.delta.lib.LogX;

/** System integration tests of the whole system. */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestDeltaZk.class, TestDeltaZkFuseki.class, TestZkExtra.class })

public class TS_DeltaZk {
	@BeforeClass
	public static void beforeClass() {
		LogX.setJavaLogging("src/test/resources/logging.properties");
	}
}
