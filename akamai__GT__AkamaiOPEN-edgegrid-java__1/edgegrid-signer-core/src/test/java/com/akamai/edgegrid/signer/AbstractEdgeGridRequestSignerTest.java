/*
 * Copyright 2018 Akamai Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akamai.edgegrid.signer;

import com.akamai.edgegrid.signer.exceptions.NoMatchingCredentialException;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;

import org.testng.annotations.Test;

import java.net.URI;

/**
 * Unit tests for {@link AbstractEdgeGridRequestSigner}.
 *
 * @author mgawinec@akamai.com
 */

public class AbstractEdgeGridRequestSignerTest {

	@Test(expectedExceptions = NoMatchingCredentialException.class)
	public void shouldTerminateSigningForMissingCredential() throws RequestSigningException {
		AbstractEdgeGridRequestSigner mockedSigner = mockedSigner(new ClientCredentialProvider() {
			@Override
			public ClientCredential getClientCredential(Request request) throws NoMatchingCredentialException {
				throw new NoMatchingCredentialException();
			}
		});
		mockedSigner.sign(new Object(), new Object());
	}

	@Test(expectedExceptions = NoMatchingCredentialException.class)
	public void shouldTerminateSigningForNullCredential() throws RequestSigningException {
		AbstractEdgeGridRequestSigner mockedSigner = mockedSigner(new ClientCredentialProvider() {
			@Override
			public ClientCredential getClientCredential(Request request) throws NoMatchingCredentialException {
				return null;
			}
		});
		mockedSigner.sign(new Object(), new Object());
	}

	@Test
	public void shouldNotTerminateSigningForValidCredentialAndRequest() throws RequestSigningException {
		AbstractEdgeGridRequestSigner mockedSigner = mockedSigner(new ClientCredentialProvider() {
			@Override
			public ClientCredential getClientCredential(Request request) throws NoMatchingCredentialException {
				return ClientCredential.builder().accessToken("accessToken").clientSecret("clientSecret")
						.clientToken("clientToken").host("host").build();
			}
		});
		mockedSigner.sign(new Object(), new Object());
	}

	public AbstractEdgeGridRequestSigner mockedSigner(ClientCredentialProvider clientCredentialProvider) {

		return new AbstractEdgeGridRequestSigner(clientCredentialProvider) {

			@Override
			protected URI requestUri(Object request) {
				return URI.create("http://request/test");
			}

			@Override
			protected Request map(Object request) {
				return Request.builder().method("GET").uri("http://request/test/").body("".getBytes()).build();
			}

			@Override
			protected void setAuthorization(Object request, String signature) {
			}

			@Override
			protected void setHost(Object request, String host, URI uri) {
			}

		};
	}

}
