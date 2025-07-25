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
package org.apache.directory.api.ldap.extras.extended.ads_impl.cancel;

import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.AbstractExtendedOperationFactory;
import org.apache.directory.api.ldap.codec.api.ExtendedOperationFactory;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.extras.extended.cancel.CancelRequest;
import org.apache.directory.api.ldap.extras.extended.cancel.CancelRequestImpl;
import org.apache.directory.api.ldap.extras.extended.cancel.CancelResponse;
import org.apache.directory.api.ldap.extras.extended.cancel.CancelResponseImpl;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;

/**
 * An {@link ExtendedOperationFactory} for creating cancel extended request response 
 * pairs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CancelFactory extends AbstractExtendedOperationFactory {
	/**
	 * Creates a new instance of CancelFactory.
	 *
	 * @param codec The codec for this factory.
	 */
	public CancelFactory(LdapApiService codec) {
		super(codec, CancelRequest.EXTENSION_OID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CancelRequest newRequest() {
		return new CancelRequestImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CancelRequest newRequest(byte[] encodedValue) throws DecoderException {
		CancelRequest cancelRequest = new CancelRequestImpl();
		decodeValue(cancelRequest, encodedValue);

		return cancelRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CancelResponse newResponse() {
		return new CancelResponseImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decodeValue(ExtendedRequest extendedRequest, byte[] requestValue) throws DecoderException {
		ByteBuffer bb = ByteBuffer.wrap(requestValue);
		CancelRequestContainer container = new CancelRequestContainer();
		container.setCancelRequest((CancelRequest) extendedRequest);
		Asn1Decoder.decode(bb, container);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encodeValue(Asn1Buffer buffer, ExtendedRequest extendedRequest) {
		int start = buffer.getPos();
		CancelRequest cancelRequest = (CancelRequest) extendedRequest;

		// the ID
		BerValue.encodeInteger(buffer, cancelRequest.getCancelId());

		// The sequence
		BerValue.encodeSequence(buffer, start);

	}
}
