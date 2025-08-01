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
package org.jclouds.ovf.xml;

import static org.jclouds.util.SaxUtils.currentOrNull;
import static org.jclouds.util.SaxUtils.equalsOrSuffix;

import javax.inject.Provider;

import org.jclouds.http.functions.ParseSax;
import org.jclouds.ovf.Section;
import org.jclouds.ovf.Section.Builder;

public class SectionHandler<T extends Section<T>, B extends Section.Builder<T>> extends ParseSax.HandlerWithResult<T> {
	@SuppressWarnings("unchecked")
	public static SectionHandler create() {
		return new SectionHandler(new Provider<Section.Builder>() {

			@Override
			public Builder get() {
				return new Section.Builder();
			}

		});
	}

	protected final Provider<? extends Section.Builder<T>> builderProvider;
	protected StringBuilder currentText = new StringBuilder();
	protected B builder;

	public SectionHandler(Provider<B> builderProvider) {
		this.builderProvider = builderProvider;
		this.builder = builderProvider.get();
	}

	@SuppressWarnings("unchecked")
	public T getResult() {
		try {
			return (T) builder.build();
		} finally {
			builder = (B) builderProvider.get();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (equalsOrSuffix(qName, "Info")) {
			builder.info(currentOrNull(currentText));
		}
		currentText.setLength(0);
	}

	public void characters(char ch[], int start, int length) {
		currentText.append(ch, start, length);
	}
}
