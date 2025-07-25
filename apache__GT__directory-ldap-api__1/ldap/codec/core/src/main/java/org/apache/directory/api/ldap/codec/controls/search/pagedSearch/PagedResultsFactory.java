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
package org.apache.directory.api.ldap.codec.controls.search.pagedSearch;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.AbstractControlFactory;
import org.apache.directory.api.ldap.codec.api.ControlFactory;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;

/**
 * A {@link ControlFactory} for {@link PagedResults} controls.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PagedResultsFactory extends AbstractControlFactory<PagedResults> {
	/**
	 * Creates a new instance of PagedResultsFactory.
	 *
	 * @param codec The LDAP codec.
	 */
	public PagedResultsFactory(LdapApiService codec) {
		super(codec, PagedResults.OID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagedResults newControl() {
		return new PagedResultsImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encodeValue(Asn1Buffer buffer, Control control) {
		int start = buffer.getPos();

		// The cookie
		BerValue.encodeOctetString(buffer, ((PagedResults) control).getCookie());

		// The size
		BerValue.encodeInteger(buffer, ((PagedResults) control).getSize());

		// The sequence
		BerValue.encodeSequence(buffer, start);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decodeValue(Control control, byte[] controlBytes) throws DecoderException {
		decodeValue(new PagedResultsContainer(control), control, controlBytes);
	}
}
