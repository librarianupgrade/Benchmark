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
package org.jclouds.s3.config;

import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.aws.config.AWSHttpApiModule;
import org.jclouds.aws.handlers.AWSClientErrorRetryHandler;
import org.jclouds.aws.handlers.AWSServerErrorRetryHandler;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.date.DateService;
import org.jclouds.date.TimeStamp;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRetryHandler;
import org.jclouds.http.annotation.ClientError;
import org.jclouds.http.annotation.Redirection;
import org.jclouds.http.annotation.ServerError;
import org.jclouds.location.Region;
import org.jclouds.location.functions.RegionToEndpointOrProviderIfNull;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.RequestSigner;
import org.jclouds.s3.Bucket;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.blobstore.functions.BucketsToStorageMetadata;
import org.jclouds.s3.domain.BucketMetadata;
import org.jclouds.s3.filters.RequestAuthorizeSignature;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV2;
import org.jclouds.s3.functions.GetRegionForBucket;
import org.jclouds.s3.handlers.ParseS3ErrorFromXmlContent;
import org.jclouds.s3.handlers.S3RedirectionRetryHandler;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

/**
 * Configures the S3 connection, including logging and http transport.
 */
@ConfiguresHttpApi
public class S3HttpApiModule<S extends S3Client> extends AWSHttpApiModule<S> {

	@SuppressWarnings("unchecked")
	public S3HttpApiModule() {
		this(Class.class.cast(S3Client.class));
	}

	protected S3HttpApiModule(Class<S> syncClientType) {
		super(syncClientType);
	}

	@Provides
	@Bucket
	@Singleton
	protected final CacheLoader<String, Optional<String>> provideBucketToRegion(
			@Region Supplier<Set<String>> regionSupplier, final S3Client client) {
		return bucketToRegion(regionSupplier, client);
	}

	protected CacheLoader<String, Optional<String>> bucketToRegion(@Region Supplier<Set<String>> regionSupplier,
			final S3Client client) {
		Set<String> regions = regionSupplier.get();
		if (regions.isEmpty()) {
			return new CacheLoader<String, Optional<String>>() {

				@Override
				public Optional<String> load(String bucket) {
					return Optional.absent();
				}

				@Override
				public String toString() {
					return "noRegions()";
				}
			};
		} else if (regions.size() == 1) {
			final String onlyRegion = Iterables.getOnlyElement(regions);
			return new CacheLoader<String, Optional<String>>() {
				Optional<String> onlyRegionOption = Optional.of(onlyRegion);

				@Override
				public Optional<String> load(String bucket) {
					return onlyRegionOption;
				}

				@Override
				public String toString() {
					return "onlyRegion(" + onlyRegion + ")";
				}
			};
		} else {
			return new CacheLoader<String, Optional<String>>() {
				@Override
				public Optional<String> load(String bucket) {
					try {
						return Optional.fromNullable(client.getBucketLocation(bucket));
					} catch (ContainerNotFoundException e) {
						return Optional.absent();
					}
				}

				@Override
				public String toString() {
					return "bucketToRegion()";
				}
			};
		}
	}

	@Provides
	@Bucket
	@Singleton
	protected final LoadingCache<String, Optional<String>> bucketToRegion(
			@Bucket CacheLoader<String, Optional<String>> loader) {
		return CacheBuilder.newBuilder().build(loader);
	}

	@Provides
	@Bucket
	@Singleton
	protected final Supplier<String> provideDefaultRegionForBucket(@Region Supplier<String> defaultRegion) {
		return defaultRegionForBucket(defaultRegion);
	}

	protected Supplier<String> defaultRegionForBucket(@Region Supplier<String> defaultRegion) {
		return defaultRegion;
	}

	@Provides
	@Singleton
	@Bucket
	protected final Supplier<URI> provideBucketURI(@Bucket Supplier<String> defaultRegion,
			RegionToEndpointOrProviderIfNull regionToEndpoint) {
		return Suppliers.compose(regionToEndpoint, defaultRegion);
	}

	@Override
	protected void configure() {
		super.configure();
		install(new S3ObjectModule());
		install(new S3ParserModule());
		bindRequestSigner();
		bind(new TypeLiteral<Function<String, Optional<String>>>() {
		}).annotatedWith(Bucket.class).to(GetRegionForBucket.class);
		bind(new TypeLiteral<Function<Set<BucketMetadata>, PageSet<? extends StorageMetadata>>>() {
		}).to(BucketsToStorageMetadata.class);
	}

	@Override
	protected void bindErrorHandlers() {
		bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(ParseS3ErrorFromXmlContent.class);
		bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(ParseS3ErrorFromXmlContent.class);
		bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(ParseS3ErrorFromXmlContent.class);
	}

	protected void bindRequestSigner() {
		bind(RequestAuthorizeSignature.class).to(RequestAuthorizeSignatureV2.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	protected final RequestSigner provideRequestSigner(RequestAuthorizeSignature in) {
		if (in instanceof RequestSigner) {
			return (RequestSigner) in;
		}
		return new RequestSigner() {
			@Override
			public String createStringToSign(HttpRequest input) {
				return null;
			}

			@Override
			public String sign(String toSign) {
				return null;
			}
		};
	}

	@Override
	protected void bindRetryHandlers() {
		bind(HttpRetryHandler.class).annotatedWith(Redirection.class).to(S3RedirectionRetryHandler.class);
		bind(HttpRetryHandler.class).annotatedWith(ClientError.class).to(AWSClientErrorRetryHandler.class);
		bind(HttpRetryHandler.class).annotatedWith(ServerError.class).to(AWSServerErrorRetryHandler.class);
	}

	@Provides
	@TimeStamp
	protected final String guiceProvideTimeStamp(@TimeStamp Supplier<String> cache) {
		return provideTimeStamp(cache);
	}

	protected String provideTimeStamp(@TimeStamp Supplier<String> cache) {
		return cache.get();
	}

	/**
	* borrowing concurrency code to ensure that caching takes place properly
	*/
	@Provides
	@TimeStamp
	@Singleton
	protected final Supplier<String> provideTimeStampCache(@Named(Constants.PROPERTY_SESSION_INTERVAL) long seconds,
			final DateService dateService) {
		return Suppliers.memoizeWithExpiration(new Supplier<String>() {
			@Override
			public String get() {
				return dateService.rfc822DateFormat();
			}
		}, seconds, TimeUnit.SECONDS);
	}

	@Provides
	@TimeStamp
	protected Date provideTimeStampDate(@TimeStamp Supplier<Date> cache) {
		return cache.get();
	}

	/**
	* borrowing concurrency code to ensure that caching takes place properly
	*/
	@Provides
	@TimeStamp
	@Singleton
	protected final Supplier<Date> guiceProvideTimeStampCacheDate(
			@Named(Constants.PROPERTY_SESSION_INTERVAL) long seconds, @TimeStamp final Supplier<String> timestamp,
			final DateService dateService) {
		return provideTimeStampCacheDate(seconds, timestamp, dateService);
	}

	protected Supplier<Date> provideTimeStampCacheDate(@Named(Constants.PROPERTY_SESSION_INTERVAL) long seconds,
			@TimeStamp final Supplier<String> timestamp, final DateService dateService) {
		return Suppliers.memoizeWithExpiration(new Supplier<Date>() {
			public Date get() {
				return dateService.rfc822DateParse(timestamp.get());
			}
		}, seconds, TimeUnit.SECONDS);
	}
}
