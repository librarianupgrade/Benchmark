/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.api.ldap.codec.compare;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.CompareResponse;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.controls.EntryChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test the CompareResponse codec
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class CompareResponseTest extends AbstractCodecServiceTest {
	/**
	 * Test the decoding of a CompareResponse
	 */
	@Test
	public void testDecodeCompareResponseSuccess() throws DecoderException, EncoderException {
		ByteBuffer stream = ByteBuffer.allocate(0x0E);

		stream.put(new byte[] { 0x30, 0x0C, // LDAPMessage ::=SEQUENCE {
				0x02, 0x01, 0x01, // messageID MessageID
				0x6F, 0x07, // CHOICE { ..., compareResponse CompareResponse,
				// ...
				// CompareResponse ::= [APPLICATION 15] LDAPResult
				0x0A, 0x01, 0x00, // LDAPResult ::= SEQUENCE {
				// resultCode ENUMERATED {
				// success (0), ...
				// },
				0x04, 0x00, // matchedDN LDAPDN,
				0x04, 0x00 // errorMessage LDAPString,
							// referral [3] Referral OPTIONAL }
							// }
		});

		stream.flip();

		// Allocate a LdapMessage Container
		LdapMessageContainer<CompareResponse> container = new LdapMessageContainer<>(codec);

		// Decode the CompareResponse PDU
		Asn1Decoder.decode(stream, container);

		// Check the decoded CompareResponse PDU
		CompareResponse compareResponse = container.getMessage();

		assertEquals(1, compareResponse.getMessageId());
		assertEquals(ResultCodeEnum.SUCCESS, compareResponse.getLdapResult().getResultCode());
		assertEquals("", compareResponse.getLdapResult().getMatchedDn().getName());
		assertEquals("", compareResponse.getLdapResult().getDiagnosticMessage());

		// Check encode reverse
		Asn1Buffer buffer = new Asn1Buffer();

		LdapEncoder.encodeMessage(buffer, codec, compareResponse);

		assertArrayEquals(stream.array(), buffer.getBytes().array());
	}

	/**
	 * Test the decoding of a CompareResponse with controls
	 */
	@Test
	public void testDecodeCompareResponseSuccessWithControls() throws DecoderException, EncoderException {
		ByteBuffer stream = ByteBuffer.allocate(0x32);

		stream.put(new byte[] { 0x30, 0x30, // LDAPMessage ::=SEQUENCE {
				0x02, 0x01, 0x01, // messageID MessageID
				0x6F, 0x07, // CHOICE { ..., compareResponse CompareResponse,
				// ...
				// CompareResponse ::= [APPLICATION 15] LDAPResult
				0x0A, 0x01, 0x00, // LDAPResult ::= SEQUENCE {
				// resultCode ENUMERATED {
				// success (0), ...
				// },
				0x04, 0x00, // matchedDN LDAPDN,
				0x04, 0x00, // errorMessage LDAPString,
				// referral [3] Referral OPTIONAL }
				// }
				(byte) 0xA0, 0x22, // A control
				0x30, 0x20, 0x04, 0x17, '2', '.', '1', '6', '.', '8', '4', '0', '.', '1', '.', '1', '1', '3', '7', '3',
				'0', '.', '3', '.', '4', '.', '7', 0x04, 0x05, // Control value
				0x30, 0x03, // EntryChangeNotification ::= SEQUENCE {
				0x0A, 0x01, 0x01 //     changeType ENUMERATED {
		});

		stream.flip();

		// Allocate a LdapMessage Container
		LdapMessageContainer<CompareResponse> container = new LdapMessageContainer<>(codec);

		// Decode the CompareResponse PDU
		Asn1Decoder.decode(stream, container);

		// Check the decoded CompareResponse PDU
		CompareResponse compareResponse = container.getMessage();

		assertEquals(1, compareResponse.getMessageId());
		assertEquals(ResultCodeEnum.SUCCESS, compareResponse.getLdapResult().getResultCode());
		assertEquals("", compareResponse.getLdapResult().getMatchedDn().getName());
		assertEquals("", compareResponse.getLdapResult().getDiagnosticMessage());

		// Check the Control
		Map<String, Control> controls = compareResponse.getControls();

		assertEquals(1, controls.size());

		Control control = controls.get("2.16.840.1.113730.3.4.7");
		assertEquals("2.16.840.1.113730.3.4.7", control.getOid());
		assertTrue(control instanceof EntryChange);

		// Check the encoding
		Asn1Buffer buffer = new Asn1Buffer();

		LdapEncoder.encodeMessage(buffer, codec, compareResponse);

		assertArrayEquals(stream.array(), buffer.getBytes().array());
	}

	/**
	 * Test the decoding of a CompareResponse with no LdapResult
	 */
	@Test
	public void testDecodeCompareResponseEmptyResult() throws DecoderException {
		ByteBuffer stream = ByteBuffer.allocate(0x07);

		stream.put(new byte[] { 0x30, 0x05, // LDAPMessage ::=SEQUENCE {
				0x02, 0x01, 0x01, // messageID MessageID
				0x6F, 0x00 // CHOICE { ..., compareResponse CompareResponse,
							// ...
		});

		stream.flip();

		// Allocate a LdapMessage Container
		LdapMessageContainer<CompareResponse> container = new LdapMessageContainer<>(codec);

		// Decode a CompareResponse message
		assertThrows(DecoderException.class, () -> {
			Asn1Decoder.decode(stream, container);
		});
	}
}
