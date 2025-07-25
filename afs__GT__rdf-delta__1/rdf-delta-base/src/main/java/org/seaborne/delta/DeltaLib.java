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

package org.seaborne.delta;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.seaborne.delta.lib.JSONX;

public class DeltaLib {

	/** Generate a JsonObject that is the esponse to a "ping".
	 *  The object includes a timestamp.
	 *  <pre>
	 *  { "value" : timestamp }
	 *  </pre>
	 */
	public static JsonObject ping() {
		String now = DateTimeUtils.nowAsXSDDateTimeString();
		JsonObject r = JSONX.buildObject(b -> {
			b.pair(DeltaConst.F_VALUE, now);
		});
		return r;
	}
}
