/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

class ReportTransformer implements Runnable {

	private final Writer out;
	private final Transformer transformer;
	private final Reader in;

	public ReportTransformer(final Writer out, final Reader style, final Reader in)
			throws TransformerConfigurationException {
		this.out = out;
		transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(style));
		this.in = in;
	}

	public ReportTransformer(final Writer out, final InputStream style, final Reader in)
			throws TransformerConfigurationException {
		this.out = out;
		transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(style));
		this.in = in;
	}

	public void run() {
		try {
			transform();
		} catch (TransformerException e) {
			throw new ReportFailedRuntimeException(e.getMessage(), e);
		}
	}

	public void transform() throws TransformerException {
		transformer.transform(new StreamSource(in), new StreamResult(out));
	}

}
