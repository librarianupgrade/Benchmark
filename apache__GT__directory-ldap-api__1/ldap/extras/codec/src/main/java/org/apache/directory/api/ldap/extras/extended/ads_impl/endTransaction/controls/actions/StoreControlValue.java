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
package org.apache.directory.api.ldap.extras.extended.ads_impl.endTransaction.controls.actions;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.ControlFactory;
import org.apache.directory.api.ldap.extras.extended.ads_impl.endTransaction.controls.ControlsContainer;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action used to set the value of a control. This is an extension point
 * where different controls can be plugged in (at least eventually). For now we
 * hard code controls.
 * <pre>
 * Control ::= SEQUENCE {
 *     ...
 *     controlValue OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreControlValue extends GrammarAction<ControlsContainer> {
	/** The logger */
	private static final Logger LOG = LoggerFactory.getLogger(StoreControlValue.class);

	/**
	 * Instantiates a new StoreControlValue action.
	 */
	public StoreControlValue() {
		super("Store the control value");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void action(ControlsContainer container) throws DecoderException {
		TLV tlv = container.getCurrentTLV();

		Control control = container.getCurrentControl();
		ControlFactory<?> controlFactory = container.getFactory();

		// Get the current control
		BerValue value = tlv.getValue();

		// Store the value - have to handle the special case of a 0 length value
		try {
			if (tlv.getLength() == 0) {
				controlFactory.decodeValue(control, Strings.EMPTY_BYTES);
			} else {
				controlFactory.decodeValue(control, value.getData());
			}
		} catch (DecoderException de) {
			String message = I18n.err(I18n.ERR_08109_BAD_CONTROL_VALUE, Strings.dumpBytes(value.getData()));
			LOG.error(message);

			// This will generate a PROTOCOL_ERROR
			throw new DecoderException(message, de);

		}

		// We can have an END transition
		container.setGrammarEndAllowed(true);

		if (LOG.isDebugEnabled()) {
			LOG.debug(I18n.msg(I18n.MSG_08203_CONTROL_VALUE, Strings.dumpBytes(value.getData())));
		}
	}
}
