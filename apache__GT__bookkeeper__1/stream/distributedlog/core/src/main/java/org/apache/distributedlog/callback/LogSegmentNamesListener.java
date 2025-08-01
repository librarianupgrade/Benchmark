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
package org.apache.distributedlog.callback;

import java.util.List;
import org.apache.bookkeeper.versioning.Versioned;

/**
 * Listener on list of log segments changes for a given stream used by
 * {@link org.apache.distributedlog.logsegment.LogSegmentMetadataStore}.
 */
public interface LogSegmentNamesListener {
	/**
	 * Notified when <i>segments</i> updated. The new log segments
	 * list is returned in this method.
	 *
	 * @param segments
	 *          updated list of segments.
	 */
	void onSegmentsUpdated(Versioned<List<String>> segments);

	/**
	 * Notified when the log stream is deleted.
	 */
	void onLogStreamDeleted();
}
