package org.nnsoft.guice.rocoto.mixed;

/*
 *    Copyright 2009-2012 The 99 Software Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.bindProperties;
import static org.junit.Assert.assertTrue;
import static org.nnsoft.guice.rocoto.Rocoto.expandVariables;

import java.io.File;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
import org.nnsoft.guice.rocoto.converters.FileConverter;

import com.google.inject.AbstractModule;

public final class ConfigurationConverterTestCase {

	@Inject
	@Named("test.suites")
	private File testSuites;

	@Before
	public void setUp() {
		createInjector(expandVariables(new AbstractModule() {

			@Override
			protected void configure() {
				Properties properties = new Properties();
				properties.put("test.suites", "${user.dir}/src/test/resources/testng.xml");

				bindProperties(binder(), properties);
			}

		}, new ConfigurationModule() {

			@Override
			protected void bindConfigurations() {
				bindSystemProperties();
			}

		}), new FileConverter()).injectMembers(this);
	}

	public void setTestSuites(File testSuites) {
		this.testSuites = testSuites;
	}

	@Test
	public void verifyFileExists() {
		assertTrue(testSuites.exists());
	}

}
