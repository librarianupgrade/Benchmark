package org.nnsoft.guice.rocoto.converters;

/*
 *    Copyright 2009-2012 The 99 Software Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * Converter implementation for {@code java.net.InetAddress}.
 *
 * @since 3.3
 */
public final class InetAddressConverter extends AbstractConverter<InetAddress> {

	/**
	 * {@inheritDoc}
	 */
	public Object convert(String value, TypeLiteral<?> toType) {
		try {
			return InetAddress.getByName(value);
		} catch (UnknownHostException e) {
			throw new ProvisionException("String value '" + value + "' is not a valid InetAddress", e);
		}
	}

}
