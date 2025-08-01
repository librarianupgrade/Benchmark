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

package org.apache.directory.api.ldap.model.ldif.anonymizer;

import java.util.Map;

/**
 * An anonymizer for the TelephoneNumber attribute. We simply replace the digits by random digits.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TelephoneNumberAnonymizer extends IntegerAnonymizer {

	/**
	 * Creates a new instance of TelephoneNumberAnonymizer.
	 */
	public TelephoneNumberAnonymizer() {
		super();
	}

	/**
	 * Creates a new instance of TelephoneNumberAnonymizer.
	 * 
	 * @param latestIntegerMap The map containing the latest integer value for each length 
	 */
	public TelephoneNumberAnonymizer(Map<Integer, String> latestIntegerMap) {
		super(latestIntegerMap);
	}
}
