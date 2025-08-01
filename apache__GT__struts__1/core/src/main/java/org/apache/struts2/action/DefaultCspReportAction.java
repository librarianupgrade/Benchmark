/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The default implementation of {@link CspReportAction} that simply logs the JSON object
 * that contains the details of the CSP violation.
 *
 * @see CspReportAction
 */
public class DefaultCspReportAction extends CspReportAction {

	protected static final Logger LOG = LogManager.getLogger(DefaultCspReportAction.class);

	@Override
	void processReport(String jsonCspReport) {
		LOG.error(jsonCspReport);
	}
}
