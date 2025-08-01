/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.common.rest.codec.header;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

public class HeaderCodecsUtils {
	private static final Map<String, HeaderCodec> CODECS;

	static {
		CODECS = new HashMap<>();
		CODECS.put(HeaderCodecSimple.NAME, new HeaderCodecSimple());
		CODECS.put(HeaderCodecMulti.NAME, new HeaderCodecMulti());
		CODECS.put(HeaderCodecCsv.CODEC_NAME, new HeaderCodecCsv());
		CODECS.put(HeaderCodecPipes.CODEC_NAME, new HeaderCodecPipes());
		CODECS.put(HeaderCodecSsv.CODEC_NAME, new HeaderCodecSsv());
	}

	private HeaderCodecsUtils() {
	}

	public static HeaderCodec find(Parameter.StyleEnum styleEnum, Boolean explode) {
		return CODECS.get(formatName(styleEnum, explode));
	}

	private static String formatName(StyleEnum styleEnum, Boolean explode) {
		if (styleEnum == null) {
			return HeaderCodecSimple.NAME;
		}
		return styleEnum + ":" + (explode != null && explode ? "1" : "0");
	}
}
