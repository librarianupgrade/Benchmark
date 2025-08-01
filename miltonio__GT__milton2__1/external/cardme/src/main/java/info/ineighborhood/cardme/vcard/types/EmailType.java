/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package info.ineighborhood.cardme.vcard.types;

import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCardType;
import info.ineighborhood.cardme.vcard.features.EmailFeature;
import info.ineighborhood.cardme.vcard.types.parameters.EmailParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.ParameterTypeStyle;
import info.ineighborhood.cardme.vcard.types.parameters.XEmailParameterType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Feb 4, 2010
 *
 */
public class EmailType extends Type implements EmailFeature {

	private String email = null;
	private List<EmailParameterType> emailParameterTypes = null;
	private List<XEmailParameterType> xtendedEmailParameterTypes = null;

	public EmailType() {
		this(null);
	}

	public EmailType(String email) {
		super(EncodingType.EIGHT_BIT, ParameterTypeStyle.PARAMETER_VALUE_LIST);
		emailParameterTypes = new ArrayList<EmailParameterType>();
		xtendedEmailParameterTypes = new ArrayList<XEmailParameterType>();
		setEmail(email);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<EmailParameterType> getEmailParameterTypes() {
		return emailParameterTypes.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EmailParameterType> getEmailParameterTypesList() {
		return Collections.unmodifiableList(emailParameterTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getEmailParameterSize() {
		return emailParameterTypes.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addEmailParameterType(EmailParameterType emailParameterType) {
		emailParameterTypes.add(emailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeEmailParameterType(EmailParameterType emailParameterType) {
		emailParameterTypes.remove(emailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEmailParameterType(EmailParameterType emailParameterType) {
		return emailParameterTypes.contains(emailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasEmailParameterTypes() {
		return !emailParameterTypes.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearEmailParameterTypes() {
		emailParameterTypes.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<XEmailParameterType> getExtendedEmailParameterTypes() {
		return xtendedEmailParameterTypes.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<XEmailParameterType> getExtendedEmailParameterTypesList() {
		return Collections.unmodifiableList(xtendedEmailParameterTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getExtendedEmailParameterSize() {
		return xtendedEmailParameterTypes.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType) {
		xtendedEmailParameterTypes.add(xtendedEmailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType) {
		xtendedEmailParameterTypes.remove(xtendedEmailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType) {
		return xtendedEmailParameterTypes.contains(xtendedEmailParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasExtendedEmailParameterTypes() {
		return !xtendedEmailParameterTypes.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearExtendedEmailParameterTypes() {
		xtendedEmailParameterTypes.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasEmail() {
		return email != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTypeString() {
		return VCardType.EMAIL.getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj instanceof EmailType) {
				if (this == obj || ((EmailType) obj).hashCode() == this.hashCode()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Util.generateHashCode(toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[ ");
		if (encodingType != null) {
			sb.append(encodingType.getType());
			sb.append(",");
		}

		if (email != null) {
			sb.append(email);
			sb.append(",");
		}

		if (!emailParameterTypes.isEmpty()) {
			for (int i = 0; i < emailParameterTypes.size(); i++) {
				sb.append(emailParameterTypes.get(i).getType());
				sb.append(",");
			}
		}

		if (!xtendedEmailParameterTypes.isEmpty()) {
			for (int i = 0; i < xtendedEmailParameterTypes.size(); i++) {
				sb.append(xtendedEmailParameterTypes.get(i).getType());
				sb.append(",");
			}
		}

		if (super.id != null) {
			sb.append(super.id);
			sb.append(",");
		}

		sb.deleteCharAt(sb.length() - 1); //Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmailFeature clone() {
		EmailType cloned = new EmailType();

		if (email != null) {
			cloned.setEmail(email);
		}

		if (!emailParameterTypes.isEmpty()) {
			for (EmailParameterType emailParameterType : emailParameterTypes) {
				cloned.addEmailParameterType(emailParameterType);
			}
		}

		if (!xtendedEmailParameterTypes.isEmpty()) {
			for (XEmailParameterType xtendedEmailParameterType : xtendedEmailParameterTypes) {
				cloned.addExtendedEmailParameterType(xtendedEmailParameterType);
			}
		}

		cloned.setParameterTypeStyle(getParameterTypeStyle());
		cloned.setEncodingType(getEncodingType());
		cloned.setID(getID());
		return cloned;
	}
}
