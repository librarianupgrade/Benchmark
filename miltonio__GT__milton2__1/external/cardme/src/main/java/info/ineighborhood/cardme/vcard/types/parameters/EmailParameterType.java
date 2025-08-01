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

package info.ineighborhood.cardme.vcard.types.parameters;

import info.ineighborhood.cardme.vcard.VCardType;

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
 * Feb 25, 2010
 *
 */
public enum EmailParameterType {

	INTERNET("INTERNET", "Internet Email"), X_400("X_400", "X.400"), AOL("AOL", "AOL"),
	APPLELINK("APPLELINK", "Apple Link"), ATTMAIL("ATTMAIL", "AT&T Mail"), CIS("CIS", "Compuserv Information Service"),
	EWORLD("EWORLD", "eWorld"), IBMMAIL("IBMMAIL", "IBM Mail"), MCIMAIL("MCIMAIL", "MCI Mail"),
	POWERSHARE("POWERSHARE", "Powershare"), PRODIGY("PRODIGY", "Prodigy Information Service"),
	TLX("TLX", "Telex Number"), HOME("HOME", "Home"), WORK("WORK", "Work"), OTHER("OTHER", "Other"),
	PREF("PREF", "Preferred"), NON_STANDARD("NON_STANDARD", "Non-Standard");

	private String type;
	private String desc;

	EmailParameterType(String t, String d) {
		type = t;
		desc = d;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return desc;
	}

	public VCardType getParentType() {
		return VCardType.EMAIL;
	}

	public void setType(String type) {
		this.type = type.toUpperCase();
	}

	public void setDescription(String desc) {
		this.desc = desc;
	}
}
