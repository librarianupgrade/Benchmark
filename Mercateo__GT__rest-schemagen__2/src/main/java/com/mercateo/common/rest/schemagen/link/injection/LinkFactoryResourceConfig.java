package com.mercateo.common.rest.schemagen.link.injection;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import com.mercateo.common.rest.schemagen.JsonSchemaGenerator;
import com.mercateo.common.rest.schemagen.RestJsonSchemaGeneratorFactory;
import com.mercateo.common.rest.schemagen.link.LinkMetaFactory;
import com.mercateo.common.rest.schemagen.plugin.FieldCheckerForSchema;
import com.mercateo.common.rest.schemagen.plugin.MethodCheckerForLink;
import com.mercateo.common.rest.schemagen.plugin.common.FieldCheckerForSchemaFactory;
import com.mercateo.common.rest.schemagen.plugin.common.MethodCheckerForLinkFactory;

public class LinkFactoryResourceConfig {

	public static void configureWithoutPlugins(ResourceConfig config) {
		config.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(RestJsonSchemaGeneratorFactory.class, Singleton.class).to(JsonSchemaGenerator.class)
						.in(Singleton.class);
				bindFactory(LinkMetaFactoryFactory.class).to(LinkMetaFactory.class).in(RequestScoped.class).proxy(true);
				bindFactory(BaseUriFactory.class).to(BaseUri.class).in(RequestScoped.class).proxy(true);
			}
		});

	}

	private static void bindDefaultPlugins(ResourceConfig config) {
		config.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(MethodCheckerForLinkFactory.class).to(MethodCheckerForLink.class).in(RequestScoped.class)
						.proxy(true);
				bindFactory(FieldCheckerForSchemaFactory.class, Singleton.class).to(FieldCheckerForSchema.class)
						.in(Singleton.class);
			}
		});
	}

	public static void configure(ResourceConfig resourceConfig) {
		configureWithoutPlugins(resourceConfig);
		bindDefaultPlugins(resourceConfig);
	}
}
