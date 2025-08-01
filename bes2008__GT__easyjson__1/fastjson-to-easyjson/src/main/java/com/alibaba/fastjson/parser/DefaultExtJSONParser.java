/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson.parser;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
@Deprecated
public class DefaultExtJSONParser extends DefaultJSONParser {

	public DefaultExtJSONParser(String input) {
		this(input, ParserConfig.getGlobalInstance());
	}

	public DefaultExtJSONParser(String input, ParserConfig mapping) {
		super(input, mapping);
	}

	public DefaultExtJSONParser(String input, ParserConfig mapping, int features) {
		super(input, mapping, features);
	}

	public DefaultExtJSONParser(char[] input, int length, ParserConfig mapping, int features) {
		super(input, length, mapping, features);
	}

}
