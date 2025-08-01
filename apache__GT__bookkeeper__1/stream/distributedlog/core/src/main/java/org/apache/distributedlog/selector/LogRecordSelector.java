/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.selector;

import org.apache.distributedlog.LogRecordWithDLSN;

/**
 * Visitor interface to process a set of records, and return some result.
 */
public interface LogRecordSelector {
	/**
	 * Process a given <code>record</code>.
	 *
	 * @param record
	 *          log record to process
	 */
	void process(LogRecordWithDLSN record);

	/**
	 * Returned the selected log record after processing a set of records.
	 *
	 * @return the selected log record.
	 */
	LogRecordWithDLSN result();
}
