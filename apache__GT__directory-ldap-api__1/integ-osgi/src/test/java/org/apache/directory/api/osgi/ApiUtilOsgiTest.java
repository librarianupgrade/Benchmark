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
package org.apache.directory.api.osgi;

import java.util.Date;

import org.apache.directory.api.util.EmptyEnumeration;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.api.util.Hex;
import org.apache.directory.api.util.SingletonEnumeration;
import org.apache.directory.api.util.exception.MultiException;

public class ApiUtilOsgiTest extends ApiOsgiTestBase {

	@Override
	protected String getBundleName() {
		return "org.apache.directory.api.util";
	}

	@Override
	protected void useBundleClasses() throws Exception {
		new GeneralizedTime(new Date()).getHour();
		new MultiException();
		new EmptyEnumeration<String>();
		new SingletonEnumeration<String>("foo");
		Hex.decodeHexString("#60");
	}

}
