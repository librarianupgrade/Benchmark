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

import com.google.common.base.Throwables;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class DelegatingPayload implements Payload {

	private final Payload delegate;
	private boolean isSensitive;

	public DelegatingPayload(Payload delegate) {
		this.delegate = checkNotNull(delegate, "delegate");
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public InputStream openStream() throws IOException {
		return delegate.openStream();
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public InputStream getInput() {
		try {
			return openStream();
		} catch (IOException ioe) {
			throw Throwables.propagate(ioe);
		}
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Object getRawContent() {
		return delegate.getInput();
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean isRepeatable() {
		return delegate.isRepeatable();
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);

	}

	public Payload getDelegate() {
		return delegate;
	}

	@Override
	public void release() {
		delegate.release();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public MutableContentMetadata getContentMetadata() {
		return delegate.getContentMetadata();
	}

	@Override
	public void setContentMetadata(MutableContentMetadata in) {
		delegate.setContentMetadata(in);
	}

	@Override
	public void setSensitive(boolean isSensitive) {
		this.isSensitive = isSensitive;
	}

	@Override
	public boolean isSensitive() {
		return this.isSensitive;
	}
}
