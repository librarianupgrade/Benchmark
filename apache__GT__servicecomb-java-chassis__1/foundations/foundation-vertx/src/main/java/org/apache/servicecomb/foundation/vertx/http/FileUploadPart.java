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

package org.apache.servicecomb.foundation.vertx.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.common.part.AbstractPart;

import io.vertx.ext.web.FileUpload;

public class FileUploadPart extends AbstractPart {
	private final FileUpload fileUpload;

	public FileUploadPart(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(new File(fileUpload.uploadedFileName()).toPath());
	}

	@Override
	public String getContentType() {
		return fileUpload.contentType();
	}

	@Override
	public String getName() {
		return fileUpload.name();
	}

	@Override
	public String getSubmittedFileName() {
		return fileUpload.fileName();
	}

	@Override
	public long getSize() {
		return fileUpload.size();
	}

	@Override
	public void write(String fileName) throws IOException {
		FileUtils.copyFile(new File(fileUpload.uploadedFileName()), new File(fileName));
	}
}
