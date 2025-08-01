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

package io.milton.zsync;

import io.milton.http.Range;
import java.io.InputStream;

/**
 * A simple container for a Range and a reference to an InputStream. 
 * 
 * @author Administrator
 */
public class ByteRange {

	private final Range range;
	private final InputStream dataQueue;

	/**
	 * Constructs a ByteRange with the specified Range and InputStream. The dataQueue field
	 * will simply reference the specified InputStream rather than copying from it.
	 * 
	 * @param range 
	 * @param queue 
	 */
	public ByteRange(Range range, InputStream queue) {
		this.range = range;
		this.dataQueue = queue;
	}

	public Range getRange() {
		return range;
	}

	public InputStream getDataQueue() {
		return dataQueue;
	}
}
