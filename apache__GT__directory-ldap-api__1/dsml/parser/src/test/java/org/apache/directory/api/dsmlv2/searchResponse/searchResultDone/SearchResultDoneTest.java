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

package org.apache.directory.api.dsmlv2.searchResponse.searchResultDone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Map;

import org.apache.directory.api.dsmlv2.AbstractResponseTest;
import org.apache.directory.api.dsmlv2.DsmlControl;
import org.apache.directory.api.dsmlv2.Dsmlv2ResponseParser;
import org.apache.directory.api.dsmlv2.response.SearchResponse;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Tests for the Search Result Done Response parsing
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class SearchResultDoneTest extends AbstractResponseTest {
	/**
	 * Test parsing of a response with a (optional) Control element
	 */
	@Test
	public void testResponseWith1Control() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_1_control.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();
		Map<String, Control> controls = searchResultDone.getControls();

		assertEquals(1, searchResultDone.getControls().size());

		Control control = controls.get("1.2.840.113556.1.4.643");

		assertNotNull(control);
		assertTrue(control.isCritical());
		assertEquals("1.2.840.113556.1.4.643", control.getOid());
		assertEquals("Some text", Strings.utf8ToString(((DsmlControl<?>) control).getValue()));
	}

	/**
	 * Test parsing of a response with a (optional) Control element with empty value
	 */
	@Test
	public void testResponseWith1ControlEmptyValue() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(
					SearchResultDoneTest.class.getResource("response_with_1_control_empty_value.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();
		Map<String, Control> controls = searchResultDone.getControls();

		assertEquals(1, searchResultDone.getControls().size());

		Control control = controls.get("1.2.840.113556.1.4.643");

		assertNotNull(control);
		assertTrue(control.isCritical());
		assertEquals("1.2.840.113556.1.4.643", control.getOid());
		assertFalse(((DsmlControl<?>) control).hasValue());
	}

	/**
	 * Test parsing of a response with 2 (optional) Control elements
	 */
	@Test
	public void testResponseWith2Controls() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_2_controls.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();
		Map<String, Control> controls = searchResultDone.getControls();

		assertEquals(2, searchResultDone.getControls().size());

		Control control = controls.get("1.2.840.113556.1.4.789");

		assertNotNull(control);
		assertFalse(control.isCritical());
		assertEquals("1.2.840.113556.1.4.789", control.getOid());
		assertEquals("Some other text", Strings.utf8ToString(((DsmlControl<?>) control).getValue()));
	}

	/**
	 * Test parsing of a response with 3 (optional) Control elements without value
	 */
	@Test
	public void testResponseWith3ControlsWithoutValue() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(
					SearchResultDoneTest.class.getResource("response_with_3_controls_without_value.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();
		Map<String, Control> controls = searchResultDone.getControls();

		assertEquals(3, searchResultDone.getControls().size());

		Control control = controls.get("1.2.840.113556.1.4.456");

		assertNotNull(control);
		assertTrue(control.isCritical());
		assertEquals("1.2.840.113556.1.4.456", control.getOid());
		assertFalse(((DsmlControl<?>) control).hasValue());
	}

	/**
	 * Test parsing of a Response with the (optional) requestID attribute
	 */
	@Test
	public void testResponseWithRequestId() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(
					SearchResultDoneTest.class.getResource("response_with_requestID_attribute.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		assertEquals(456, searchResultDone.getMessageId());
	}

	/**
	 * Test parsing of a Response with the (optional) requestID attribute below 0
	 */
	@Test
	public void testResponseWithRequestIdBelow0() {
		testParsingFail(SearchResultDoneTest.class, "response_with_requestID_below_0.xml");
	}

	/**
	 * Test parsing of a response without Result Code element
	 */
	@Test
	public void testResponseWithoutResultCode() {
		testParsingFail(SearchResultDoneTest.class, "response_without_result_code.xml");
	}

	/**
	 * Test parsing of a response with Result Code element but a not integer value
	 */
	@Test
	public void testResponseWithResultCodeNotInteger() {
		testParsingFail(SearchResultDoneTest.class, "response_with_result_code_not_integer.xml");
	}

	/**
	 * Test parsing of a response with Result Code
	 */
	@Test
	public void testResponseWithResultCode() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_result_code.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		assertEquals(ResultCodeEnum.PROTOCOL_ERROR, ldapResult.getResultCode());
	}

	/**
	 * Test parsing of a response with Error Message
	 */
	@Test
	public void testResponseWithErrorMessage() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_error_message.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		assertEquals("Unrecognized extended operation EXTENSION_OID: 1.2.6.1.4.1.18060.1.1.1.100.2",
				ldapResult.getDiagnosticMessage());
	}

	/**
	 * Test parsing of a response with empty Error Message
	 */
	@Test
	public void testResponseWithEmptyErrorMessage() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(
					SearchResultDoneTest.class.getResource("response_with_empty_error_message.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		assertNull(ldapResult.getDiagnosticMessage());
	}

	/**
	 * Test parsing of a response with a Referral
	 */
	@Test
	public void testResponseWith1Referral() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_1_referral.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

		assertEquals(1, referrals.size());

		try {
			assertTrue(referrals.contains(new LdapUrl("ldap://www.apache.org/").toString()));
		} catch (LdapURLEncodingException e) {
			fail();
		}
	}

	/**
	 * Test parsing of a response with an empty Referral
	 */
	@Test
	public void testResponseWith1EmptyReferral() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_1_empty_referral.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

		assertEquals(0, referrals.size());
	}

	/**
	 * Test parsing of a response with 2 Referral elements
	 */
	@Test
	public void testResponseWith2Referrals() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_2_referrals.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

		assertEquals(2, referrals.size());

		try {
			assertTrue(referrals.contains(new LdapUrl("ldap://www.apache.org/").toString()));
		} catch (LdapURLEncodingException e) {
			fail();
		}

		try {
			assertTrue(referrals.contains(new LdapUrl("ldap://www.apple.com/").toString()));
		} catch (LdapURLEncodingException e) {
			fail();
		}
	}

	/**
	 * Test parsing of a response with a Referral and an Error Message
	 */
	@Test
	public void testResponseWith1ReferralAndAnErrorMessage() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(SearchResultDoneTest.class.getResource("response_with_1_referral_and_error_message.xml")
					.openStream(), "UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

		assertEquals(1, referrals.size());

		try {
			assertTrue(referrals.contains(new LdapUrl("ldap://www.apache.org/").toString()));
		} catch (LdapURLEncodingException e) {
			fail();
		}
	}

	/**
	 * Test parsing of a response with MatchedDN attribute
	 */
	@Test
	public void testResponseWithMatchedDNAttribute() {
		Dsmlv2ResponseParser parser = null;
		try {
			parser = new Dsmlv2ResponseParser(getCodec());

			parser.setInput(
					SearchResultDoneTest.class.getResource("response_with_matchedDN_attribute.xml").openStream(),
					"UTF-8");

			parser.parse();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		SearchResultDone searchResultDone = ((SearchResponse) parser.getBatchResponse().getCurrentResponse()
				.getDecorated()).getSearchResultDone();

		LdapResult ldapResult = searchResultDone.getLdapResult();

		assertTrue(ldapResult.getMatchedDn().equals("cn=Bob Rush,ou=Dev,dc=Example,dc=COM"));
	}

	/**
	 * Test parsing of a response with wrong matched Dn
	 */
	@Test
	public void testResponseWithWrongMatchedDN() {
		testParsingFail(SearchResultDoneTest.class, "response_with_wrong_matchedDN_attribute.xml");
	}

	/**
	 * Test parsing of a response with wrong Descr attribute
	 */
	@Test
	public void testResponseWithWrongDescr() {
		testParsingFail(SearchResultDoneTest.class, "response_with_wrong_descr.xml");
	}
}
