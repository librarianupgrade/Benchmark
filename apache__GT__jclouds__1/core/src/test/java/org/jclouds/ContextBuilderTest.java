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
package org.jclouds;

import static com.google.common.base.Suppliers.ofInstance;
import static org.jclouds.providers.AnonymousProviderMetadata.forApiOnEndpoint;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.domain.Credentials;
import org.jclouds.events.config.EventBusModule;
import org.jclouds.http.IntegrationTestClient;
import org.jclouds.http.config.ConfiguresHttpCommandExecutorService;
import org.jclouds.http.config.JavaUrlHttpCommandExecutorServiceModule;
import org.jclouds.location.Provider;
import org.jclouds.logging.config.LoggingModule;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.logging.jdk.config.JDKLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.annotations.ApiVersion;
import org.jclouds.rest.config.CredentialStoreModule;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * Tests behavior of modules configured in ContextBuilder
 */
@Test(groups = "unit", testName = "ContextBuilderTest")
public class ContextBuilderTest {

	@ConfiguresHttpCommandExecutorService
	static class HttpModule extends AbstractModule {
		protected void configure() {
		}
	}

	private ContextBuilder testContextBuilder() {
		return ContextBuilder.newBuilder(forApiOnEndpoint(IntegrationTestClient.class, "http://localhost"));
	}

	@Test
	public void testVariablesReplaceOnEndpoint() {
		ContextBuilder withVariablesToReplace = testContextBuilder().endpoint("http://${jclouds.identity}.service.com")
				.credentials("foo", "bar");
		URI endpoint = withVariablesToReplace.buildInjector().getInstance(Key.get(new TypeLiteral<Supplier<URI>>() {
		}, Provider.class)).get();
		assertEquals(endpoint, URI.create("http://foo.service.com"));
	}

	@Test
	public void testContextName() {
		ContextBuilder withNoName = testContextBuilder().endpoint("http://${jclouds.identity}.service.com")
				.name("mytest").credentials("foo", "bar");
		Context context = withNoName.build();
		assertEquals(context.getName(), "mytest");
	}

	@Test
	public void testProviderMetadataBoundWithCorrectEndpoint() {
		ContextBuilder withVariablesToReplace = testContextBuilder().endpoint("http://${jclouds.identity}.service.com")
				.credentials("foo", "bar");
		String endpoint = withVariablesToReplace.buildInjector().getInstance(ProviderMetadata.class).getEndpoint();
		assertEquals(endpoint, "http://foo.service.com");
	}

	@Test
	public void testProviderMetadataWithEmptyIsoCodePropertyHasEmptySet() {
		Properties overrides = new Properties();
		overrides.setProperty(Constants.PROPERTY_ISO3166_CODES, "");
		ContextBuilder withVariablesToReplace = testContextBuilder().overrides(overrides).credentials("foo", "bar");
		Set<String> codes = withVariablesToReplace.buildInjector().getInstance(ProviderMetadata.class)
				.getIso3166Codes();
		assertEquals(codes, ImmutableSet.<String>of());
	}

	@Test
	public void testProviderMetadataWithCredentialsSetViaProperty() {
		Properties overrides = new Properties();
		overrides.setProperty(Constants.PROPERTY_IDENTITY, "foo");
		overrides.setProperty(Constants.PROPERTY_CREDENTIAL, "BAR");
		ContextBuilder withCredsInProps = testContextBuilder().overrides(overrides);
		Credentials creds = withCredsInProps.buildInjector()
				.getInstance(Key.get(new TypeLiteral<Supplier<Credentials>>() {
				}, Provider.class)).get();
		assertEquals(creds, new Credentials("foo", "BAR"));
	}

	@Test
	public void testProviderMetadataWithCredentialsSetSupplier() {
		ContextBuilder withCredsSupplier = testContextBuilder()
				.credentialsSupplier(ofInstance(new Credentials("foo", "BAR")));
		Credentials creds = withCredsSupplier.buildInjector()
				.getInstance(Key.get(new TypeLiteral<Supplier<Credentials>>() {
				}, Provider.class)).get();
		assertEquals(creds, new Credentials("foo", "BAR"));
	}

	@Test
	public void testProviderMetadataWithVersionSetViaProperty() {
		Properties overrides = new Properties();
		overrides.setProperty(Constants.PROPERTY_API_VERSION, "1.1");
		ContextBuilder withVersionInProps = testContextBuilder().overrides(overrides);
		String version = withVersionInProps.buildInjector().getInstance(Key.get(String.class, ApiVersion.class));
		assertEquals(version, "1.1");
	}

	@Test
	public void testAllPropertiesAreStrings() {
		Properties overrides = new Properties();
		overrides.setProperty("foo", "bar");
		overrides.put("one", 1);
		overrides.put("two", 2.0f);
		overrides.put("true", true);
		overrides.put("object", new Object() {
			@Override
			public String toString() {
				return "object";
			}
		});
		Context withObjectsInProps = testContextBuilder().overrides(overrides).build();
		Properties resolved = withObjectsInProps.getProviderMetadata().getDefaultProperties();
		assertEquals(resolved.getProperty("foo"), "bar");
		assertEquals(resolved.getProperty("one"), "1");
		assertEquals(resolved.getProperty("true"), "true");
		assertEquals(resolved.getProperty("object"), "object");
	}

	@Test
	public void testAddHttpModuleIfNotPresent() {
		List<Module> modules = Lists.newArrayList();
		HttpModule module = new HttpModule();
		modules.add(module);
		ContextBuilder.addHttpModuleIfNeededAndNotPresent(modules);
		assertEquals(modules.size(), 1);
		assertEquals(modules.remove(0), module);
	}

	@Test
	public void testAddLoggingModuleIfNotPresent() {
		List<Module> modules = Lists.newArrayList();
		LoggingModule module = new NullLoggingModule();
		modules.add(module);
		ContextBuilder.addLoggingModuleIfNotPresent(modules);
		assertEquals(modules.size(), 1);
		assertEquals(modules.remove(0), module);
	}

	@Test
	public void testAddEventBusModuleIfNotPresent() {
		List<Module> modules = Lists.newArrayList();
		EventBusModule module = new EventBusModule();
		modules.add(module);
		ContextBuilder.addEventBusIfNotPresent(modules);
		assertEquals(modules.size(), 1);
		assertEquals(modules.remove(0), module);
	}

	@Test
	public void testAddExecutorServiceModuleIfNotPresent() {
		List<Module> modules = Lists.newArrayList();
		ExecutorServiceModule module = new ExecutorServiceModule();
		modules.add(module);
		ContextBuilder.addExecutorServiceIfNotPresent(modules);
		assertEquals(modules.size(), 1);
		assertEquals(modules.remove(0), module);
	}

	@Test
	public void testAddCredentialStoreModuleIfNotPresent() {
		List<Module> modules = Lists.newArrayList();
		CredentialStoreModule module = new CredentialStoreModule();
		modules.add(module);
		ContextBuilder.addCredentialStoreIfNotPresent(modules);
		assertEquals(modules.size(), 1);
		assertEquals(modules.remove(0), module);
	}

	@Test
	public void testAddNone() {
		List<Module> modules = Lists.newArrayList();
		LoggingModule loggingModule = new NullLoggingModule();
		modules.add(loggingModule);
		HttpModule httpModule = new HttpModule();
		modules.add(httpModule);
		ContextBuilder.addHttpModuleIfNeededAndNotPresent(modules);
		ContextBuilder.addLoggingModuleIfNotPresent(modules);
		assertEquals(modules.size(), 2);
		assertEquals(modules.remove(0), loggingModule);
		assertEquals(modules.remove(0), httpModule);
	}

	@Test
	public void testAddBothWhenDefault() {
		List<Module> modules = Lists.newArrayList();
		ContextBuilder.addHttpModuleIfNeededAndNotPresent(modules);
		ContextBuilder.addLoggingModuleIfNotPresent(modules);
		assertEquals(modules.size(), 2);
		assert modules.remove(0) instanceof JavaUrlHttpCommandExecutorServiceModule;
		assert modules.remove(0) instanceof JDKLoggingModule;
	}

	@Test
	public void testAddBothWhenLive() {
		List<Module> modules = Lists.newArrayList();
		ContextBuilder.addHttpModuleIfNeededAndNotPresent(modules);
		ContextBuilder.addLoggingModuleIfNotPresent(modules);
		assertEquals(modules.size(), 2);
		assert modules.remove(0) instanceof JavaUrlHttpCommandExecutorServiceModule;
		assert modules.remove(0) instanceof JDKLoggingModule;
	}

	public void testBuilder() {

		Module module1 = new AbstractModule() {
			protected void configure() {
			}
		};
		Module module2 = new AbstractModule() {
			protected void configure() {
			}
		};
		ContextBuilder builder = testContextBuilder();
		builder.modules(Arrays.asList(module1, module2));

	}
}
