/**
 * Copyright 2012 Comcast Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.cmb.test.tools;

import com.comcast.cmb.common.util.CMBProperties;

public class CMBTestingConstants {

	// base URL of live endpoint conforming with com.comcast.cmb.test.tools.EndpointServlet used by various unit tests

	public static final String HTTP_ENDPOINT_BASE_URL = CMBProperties.getInstance().getCQSServiceUrl() + "Endpoint/";
	//public static final String HTTP_ENDPOINT_BASE_URL = "http://sns-test5.plaxo.com:6060/Endpoint/";

	public static final String HTTPS_ENDPOINT_BASE_URL = "";

	// email endpoint for testing

	public static final String EMAIL_ENDPOINT = "jorge@plaxo.com";
}
