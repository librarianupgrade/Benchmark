/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.io.payloads;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class FilePayload extends BasePayload<File> {

	public FilePayload(File content) {
		super(content);
		getContentMetadata().setContentLength(content.length());
		checkNotNull(content, "content");
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public InputStream openStream() throws IOException {
		return new FileInputStream(content);
	}

}
