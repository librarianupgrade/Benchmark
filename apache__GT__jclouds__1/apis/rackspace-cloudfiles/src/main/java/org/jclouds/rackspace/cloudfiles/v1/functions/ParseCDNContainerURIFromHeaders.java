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
package org.jclouds.rackspace.cloudfiles.v1.functions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import org.jclouds.http.HttpResponse;
import org.jclouds.rackspace.cloudfiles.v1.reference.CloudFilesHeaders;

import com.google.common.base.Function;

/**
 * Parses the {@link CDNContainer} from the response headers.
 */
public class ParseCDNContainerURIFromHeaders implements Function<HttpResponse, URI> {

	/**
	* parses the http response headers to provide the CDN URI string.
	*/
	public URI apply(final HttpResponse from) {
		String cdnUri = checkNotNull(from.getFirstHeaderOrNull(CloudFilesHeaders.CDN_URI), CloudFilesHeaders.CDN_URI);
		return URI.create(cdnUri);
	}
}
