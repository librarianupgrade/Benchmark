/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.api.osgi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.extras.controls.passwordExpired.PasswordExpiredResponse;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyRequest;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyResponse;
import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyRequest;
import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyResponse;
import org.apache.directory.api.ldap.extras.extended.startTls.StartTlsRequest;
import org.apache.directory.api.ldap.extras.extended.startTls.StartTlsResponse;
import org.apache.directory.api.ldap.extras.intermediate.syncrepl.SyncInfoValue;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.IntermediateResponse;
import org.apache.directory.api.ldap.model.message.extended.NoticeOfDisconnect;

public class ApiLdapExtrasCodecOsgiTest extends ApiOsgiTestBase {

	@Inject
	LdapApiService ldapApiService;

	@Override
	protected String getBundleName() {
		return "org.apache.directory.api.ldap.extras.codec";
	}

	@Override
	protected void useBundleClasses() throws Exception {
		Control peResponse = ldapApiService.getResponseControlFactories().get(PasswordExpiredResponse.OID).newControl();
		assertNotNull(peResponse);
		assertTrue(peResponse instanceof PasswordExpiredResponse);

		Control ppRequest = ldapApiService.getRequestControlFactories().get(PasswordPolicyRequest.OID).newControl();
		assertNotNull(ppRequest);
		assertTrue(ppRequest instanceof PasswordPolicyRequest);

		Control ppResponse = ldapApiService.getResponseControlFactories().get(PasswordPolicyResponse.OID).newControl();
		assertNotNull(ppResponse);
		assertTrue(ppResponse instanceof PasswordPolicyResponse);

		ExtendedRequest startTlsRequest = ldapApiService.getExtendedRequestFactories()
				.get(StartTlsRequest.EXTENSION_OID).newRequest();
		assertNotNull(startTlsRequest);
		assertTrue(startTlsRequest instanceof StartTlsRequest);

		ExtendedResponse startTlsResponse = ldapApiService.getExtendedResponseFactories()
				.get(StartTlsRequest.EXTENSION_OID).newResponse();
		assertNotNull(startTlsResponse);
		assertTrue(startTlsResponse instanceof StartTlsResponse);

		ExtendedRequest modifyPasswordRequest = ldapApiService.getExtendedRequestFactories()
				.get(PasswordModifyRequest.EXTENSION_OID).newRequest();
		assertNotNull(modifyPasswordRequest);
		assertTrue(modifyPasswordRequest instanceof PasswordModifyRequest);

		ExtendedResponse passwordModifyResponse = ldapApiService.getExtendedResponseFactories()
				.get(PasswordModifyRequest.EXTENSION_OID).newResponse();
		assertNotNull(passwordModifyResponse);
		assertTrue(passwordModifyResponse instanceof PasswordModifyResponse);

		ExtendedResponse noticeOfDisconnectResponse = ldapApiService.getExtendedResponseFactories()
				.get(NoticeOfDisconnect.EXTENSION_OID).newResponse();
		assertNotNull(noticeOfDisconnectResponse);
		assertTrue(noticeOfDisconnectResponse instanceof NoticeOfDisconnect);

		IntermediateResponse syncInfoResponse = ldapApiService.getIntermediateResponseFactories().get(SyncInfoValue.OID)
				.newResponse();
		assertNotNull(syncInfoResponse);
		assertTrue(syncInfoResponse instanceof SyncInfoValue);
	}
}
