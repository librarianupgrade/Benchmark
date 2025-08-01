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
package org.apache.directory.api.ldap.codec.actions.request.add;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action used to initialize the AddRequest response
 * <pre>
 * LdapMessage ::= ... AddRequest ...
 * AddRequest ::= [APPLICATION 8] SEQUENCE { ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitAddRequest extends GrammarAction<LdapMessageContainer<AddRequest>> {
	/** The logger */
	private static final Logger LOG = LoggerFactory.getLogger(InitAddRequest.class);

	/**
	 * Instantiates a new action.
	 */
	public InitAddRequest() {
		super("Init AddRequest");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void action(LdapMessageContainer<AddRequest> container) throws DecoderException {
		// Now, we can allocate the AddRequest Object
		int messageId = container.getMessageId();
		AddRequest addRequest = new AddRequestImpl();
		addRequest.setMessageId(messageId);
		container.setMessage(addRequest);

		// We will check that the request is not null
		TLV tlv = container.getCurrentTLV();

		if (tlv.getLength() == 0) {
			String msg = I18n.err(I18n.ERR_05145_NULL_ADD_REQUEST);
			LOG.error(msg);

			// Will generate a PROTOCOL_ERROR
			throw new DecoderException(msg);
		}
	}
}
