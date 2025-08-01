/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @since 2.0
 */
public class CombinedResources extends ResourceBundle {

	// locale.getLanguage()
	// locale.getCountry()
	// locale.getVariant()

	private final String resourceName;
	private volatile boolean inited;
	private final Properties properties = new Properties();

	/**
	 * Constructs a new instance.
	 *
	 * @param resourceName A resource name.
	 */
	public CombinedResources(final String resourceName) {
		this.resourceName = resourceName;
		init();
	}

	@Override
	public Enumeration<String> getKeys() {
		return new Enumeration<String>() {
			@Override
			public boolean hasMoreElements() {
				return properties.keys().hasMoreElements();
			}

			@Override
			public String nextElement() {
				// We know that our properties will only ever contain Strings
				return (String) properties.keys().nextElement();
			}

		};
	}

	/**
	 * Gets the resource name.
	 *
	 * @return the resource name.
	 */
	public String getResourceName() {
		return resourceName;
	}

	@Override
	protected Object handleGetObject(final String key) {
		return properties.get(key);
	}

	protected void init() {
		if (inited) {
			return;
		}

		loadResources(getResourceName());
		loadResources(Locale.getDefault());
		loadResources(getLocale());
		inited = true;
	}

	protected void loadResources(final Locale locale) {
		if (locale == null) {
			return;
		}
		final String[] parts = { locale.getLanguage(), locale.getCountry(), locale.getVariant() };
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			sb.append(getResourceName());
			for (int j = 0; j < i; j++) {
				sb.append('_').append(parts[j]);
			}
			if (!parts[i].isEmpty()) {
				sb.append('_').append(parts[i]);
				loadResources(sb.toString());
			}
			sb.setLength(0);
		}
	}

	protected void loadResources(String resourceName) {
		ClassLoader loader = getClass().getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		if (loader != null) {
			resourceName = resourceName.replace('.', '/') + ".properties";
			try {
				final Enumeration<URL> resources = loader.getResources(resourceName);
				while (resources.hasMoreElements()) {
					final URL resource = resources.nextElement();
					try (InputStream inputStream = resource.openConnection().getInputStream()) {
						properties.load(inputStream);
					} catch (final IOException ignored) {
						// Ignore
					}
				}
			} catch (final IOException ignored) {
				// Ignore
			}
		}
	}
}
