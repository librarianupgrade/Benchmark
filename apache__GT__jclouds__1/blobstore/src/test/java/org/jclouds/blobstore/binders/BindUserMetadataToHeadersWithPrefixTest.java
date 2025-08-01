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
package org.jclouds.blobstore.binders;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.http.HttpRequest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

@Test(groups = "unit")
public class BindUserMetadataToHeadersWithPrefixTest {

	@Test
	public void testCorrect() throws SecurityException, NoSuchMethodException {

		HttpRequest request = HttpRequest.builder().method("GET").endpoint("http://momma").build();
		BindUserMetadataToHeadersWithPrefix binder = new BindUserMetadataToHeadersWithPrefix(
				new BindMapToHeadersWithPrefix("prefix:"));

		Blob blob = ContextBuilder.newBuilder("transient").buildInjector().getInstance(Blob.Factory.class).create(null);

		blob.getMetadata().setUserMetadata(ImmutableMap.of("imageName", "foo", "serverId", "2"));

		assertEquals(binder.bindToRequest(request, blob), HttpRequest.builder().method("GET").endpoint("http://momma")
				.addHeader("prefix:imagename", "foo").addHeader("prefix:serverid", "2").build());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testMustBeBlob() {
		BindUserMetadataToHeadersWithPrefix binder = new BindUserMetadataToHeadersWithPrefix(
				new BindMapToHeadersWithPrefix("prefix:"));
		HttpRequest request = HttpRequest.builder().method("POST").endpoint("http://localhost").build();
		binder.bindToRequest(request, new File("foo"));
	}

	@Test(expectedExceptions = { NullPointerException.class, IllegalStateException.class })
	public void testNullIsBad() {
		BindUserMetadataToHeadersWithPrefix binder = new BindUserMetadataToHeadersWithPrefix(
				new BindMapToHeadersWithPrefix("prefix:"));
		HttpRequest request = HttpRequest.builder().method("GET").endpoint("http://momma").build();
		binder.bindToRequest(request, null);
	}
}
