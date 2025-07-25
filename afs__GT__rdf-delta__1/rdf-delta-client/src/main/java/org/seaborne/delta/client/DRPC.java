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

package org.seaborne.delta.client;

import static org.seaborne.delta.DeltaConst.F_ARG;
import static org.seaborne.delta.DeltaConst.F_OP;
import static org.seaborne.delta.DeltaConst.F_OP_ID;

import java.net.http.HttpRequest.BodyPublishers;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;
import org.seaborne.delta.Delta;
import org.seaborne.delta.DeltaBadRequestException;
import org.seaborne.delta.DeltaException;
import org.seaborne.delta.DeltaNotFoundException;
import org.seaborne.delta.lib.JSONX;
import org.slf4j.Logger;

public class DRPC {
	static private Logger LOG = Delta.DELTA_RPC_LOG;
	static AtomicLong counter = new AtomicLong(0);

	/** Send a JSON argument to a URL+name by POST and received a JSON object in return. */
	public static JsonValue rpc(String url, String opName, JsonValue arg) {
		JsonObject a = JSONX.buildObject((b) -> {
			//            if ( token != null )
			//                b.key(F_TOKEN).value(token.asString());
			b.pair(F_OP, opName);
			b.pair(F_OP_ID, Long.toString(counter.incrementAndGet()));
			b.pair(F_ARG, arg);
		});
		return rpc(url, a);
	}

	/** Send a JSON object to a URL by POST and received a JSON object in return. */
	public static JsonValue rpc(String url, JsonObject object) {
		Objects.requireNonNull(url, "DRPC.rpc: Arg1 URL is null");
		Objects.requireNonNull(object, "DRPC.rpc: Arg2 JSON object is null");

		if (!object.hasKey(F_OP))
			throw new DeltaException();

		String argStr = JSON.toString(object);
		try (TypedInputStream x = HttpOp.httpPostStream(url, WebContent.contentTypeJSON,
				BodyPublishers.ofString(argStr), WebContent.contentTypeJSON)) {
			if (x == null)
				throw new DeltaNotFoundException("Not found: " + JSON.toStringFlat(object));

			if (true) {
				String s = IO.readWholeFileAsUTF8(x);
				return JSON.parseAny(s);
			} else
				return JSON.parseAny(x);
		} catch (HttpException ex) {
			if (HttpSC.BAD_REQUEST_400 == ex.getStatusCode()) {
				throw new DeltaBadRequestException(ex.getMessage());
			}
			throw ex;
		}
	}
}
