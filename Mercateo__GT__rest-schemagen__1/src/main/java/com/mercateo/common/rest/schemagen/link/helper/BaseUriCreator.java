/**
 * Copyright © 2015 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.common.rest.schemagen.link.helper;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface BaseUriCreator {
	/**
	 * create base uri from request origin and raw request headers
	 * @param requestBaseUri default base Uri
	 * @param requestHeaders request headers as string multimap
	 * @return base uri for link targets
	 */
	URI createBaseUri(URI requestBaseUri, Map<String, List<String>> requestHeaders);

	/**
	 * create base uri from request origin and wrapped request headers
	 * @param requestBaseUri default base Uri
	 * @param requestHeaders wrapped request headers
	 * @return base uri for link targets
	 */
	URI createBaseUri(URI requestBaseUri, HttpRequestHeaders requestHeaders);
}
