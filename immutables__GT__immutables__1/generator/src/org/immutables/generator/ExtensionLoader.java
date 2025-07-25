/*
   Copyright 2016 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.generator;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public final class ExtensionLoader {
	private ExtensionLoader() {
	}

	private static final Splitter RESOURCE_SPLITTER = Splitter.on("\n").omitEmptyStrings().trimResults();

	public static Supplier<ImmutableSet<String>> findExtensions(final String resource) {
		// Provide lazy-once supplier
		return Suppliers.memoize(new Supplier<ImmutableSet<String>>() {
			@Override
			public ImmutableSet<String> get() {
				List<String> extensions = Lists.newArrayList();

				// best effort to read it from compilation classpath
				try {
					String lines = getClasspathResourceText(EnvironmentState.processing().getFiler(), resource);
					extensions.addAll(RESOURCE_SPLITTER.splitToList(lines));
				} catch (RuntimeException | IOException cannotReadCompilationClasspath) {
					// we ignore this as we did or best effort
					// and there are no plans to halt whole compilation
				}

				ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
				try {
					Enumeration<URL> resources = classLoader.getResources(resource);
					while (resources.hasMoreElements()) {
						URL nextElement = resources.nextElement();
						String lines = getClasspathResourceText(nextElement);
						extensions.addAll(RESOURCE_SPLITTER.splitToList(lines));
					}
				} catch (RuntimeException | IOException cannotReadAnnotationProcessingClasspath) {
					// we ignore this as we did or best effort
					// and there are no plans to halt whole compilation
				}
				return ImmutableSet.copyOf(extensions);
			}
		});
	}

	private static String getClasspathResourceText(Filer filer, String resourceName) throws IOException {
		FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
		try (Reader r = resource.openReader(true)) {
			return CharStreams.toString(r);
		}
	}

	private static String getClasspathResourceText(URL requestURL) throws IOException {
		URLConnection connection = requestURL.openConnection();
		connection.setUseCaches(false);
		try (Reader r = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
			return CharStreams.toString(r);
		}
	}
}
