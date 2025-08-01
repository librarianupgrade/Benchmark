// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.metadata;

import static com.yahoo.bard.webservice.web.ErrorMessageFormat.DRUID_METADATA_READ_ERROR;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import com.yahoo.bard.webservice.application.Loader;
import com.yahoo.bard.webservice.config.SystemConfig;
import com.yahoo.bard.webservice.config.SystemConfigProvider;
import com.yahoo.bard.webservice.druid.client.DruidWebService;
import com.yahoo.bard.webservice.druid.client.FailureCallback;
import com.yahoo.bard.webservice.druid.client.HttpErrorCallback;
import com.yahoo.bard.webservice.druid.client.SuccessCallback;
import com.yahoo.bard.webservice.table.PhysicalTable;
import com.yahoo.bard.webservice.table.PhysicalTableDictionary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

/**
 * Datasource metadata loader sends requests to the druid datasource metadata endpoint ('datasources') and returns the
 * lists of available data segments for each datasource. It then builds Datasource Metadata objects which pivot this
 * data into columns of intervals and then updates the Physical Table instances in the physical table dictionary.
 *
 * Note that this uses the segmentMetadata query that touches the coordinator.
 */
@Singleton
public class DataSourceMetadataLoader extends Loader<Boolean> {

	private static final SystemConfig SYSTEM_CONFIG = SystemConfigProvider.getInstance();

	public static final String DATASOURCE_METADATA_QUERY_FORMAT = "/datasources/%s?full";
	public static final String DRUID_SEG_LOADER_TIMER_DURATION_KEY = SYSTEM_CONFIG
			.getPackageVariableName("druid_seg_loader_timer_duration");
	public static final String DRUID_SEG_LOADER_TIMER_DELAY_KEY = SYSTEM_CONFIG
			.getPackageVariableName("druid_seg_loader_timer_delay");

	private static final Logger LOG = LoggerFactory.getLogger(DataSourceMetadataLoader.class);

	private final DruidWebService druidWebService;
	private final PhysicalTableDictionary physicalTableDictionary;
	private final DataSourceMetadataService metadataService;
	private final AtomicReference<DateTime> lastRunTimestamp;
	private final ObjectMapper mapper;
	private final FailureCallback failureCallback;

	/**
	 * Datasource metadata loader fetches data from the druid endpoint and updates the dimensions on that table.
	 *
	 * @param physicalTableDictionary  The physical tables to update
	 * @param metadataService  The service that will store the metadata loaded by this loader
	 * @param druidWebService  The druid webservice to query
	 * @param mapper  Object mapper to parse druid metadata
	 */
	public DataSourceMetadataLoader(PhysicalTableDictionary physicalTableDictionary,
			DataSourceMetadataService metadataService, DruidWebService druidWebService, ObjectMapper mapper) {
		super(DataSourceMetadataLoader.class.getSimpleName(),
				SYSTEM_CONFIG.getLongProperty(DRUID_SEG_LOADER_TIMER_DELAY_KEY, 0), SYSTEM_CONFIG
						.getLongProperty(DRUID_SEG_LOADER_TIMER_DURATION_KEY, TimeUnit.MILLISECONDS.toMillis(60000)));

		this.physicalTableDictionary = physicalTableDictionary;
		this.metadataService = metadataService;
		this.druidWebService = druidWebService;
		this.mapper = mapper;
		this.failureCallback = getFailureCallback();

		lastRunTimestamp = new AtomicReference<>();
	}

	@Override
	public void run() {
		physicalTableDictionary.values().stream()
				.peek(table -> LOG.trace("Querying metadata for datasource: {}", table))
				.forEach(this::queryDataSourceMetadata);
		lastRunTimestamp.set(DateTime.now());
	}

	/**
	 * Queries the data mart for updated datasource metadata and then updates the physical table.
	 *
	 * @param table  The physical table to be updated.
	 */
	protected void queryDataSourceMetadata(PhysicalTable table) {
		String resourcePath = String.format(DATASOURCE_METADATA_QUERY_FORMAT, table.getName());

		// Success callback will update datasource metadata on success
		SuccessCallback success = buildDataSourceMetadataSuccessCallback(table);
		HttpErrorCallback errorCallback = getErrorCallback(table);
		druidWebService.getJsonObject(success, errorCallback, failureCallback, resourcePath);
	}

	/**
	 * Callback to parse druid datasource metadata response.
	 * <p>
	 * Typical druid datasource metadata response:
	 * <pre>
	 *  """
	 *  {
	 *      "name": "tableName",
	 *      "properties": { },
	 *      "segments": [
	 *          {
	 *              "dataSource": "tableName",
	 *              "interval": "2015-01-01T00:00:00.000Z/2015-01-02T00:00:00.000Z",
	 *              "version": "2015-01-15T18:08:20.435Z",
	 *              "loadSpec": {
	 *                  "type": "hdfs",
	 *                  "path": "hdfs:/some_hdfs_URL/tableName/.../index.zip"
	 *              },
	 *              "dimensions": "color", "shape",
	 *              "metrics": "height", "width",
	 *              "shardSpec": {
	 *                  "type":"hashed",
	 *                  "partitionNum": 0,
	 *                  "partitions": 2
	 *              },
	 *              "binaryVersion":9,
	 *              "size":1024,
	 *              "identifier":"tableName_2015-01-01T00:00:00.000Z_2015-01-02T00:00:00.000Z_2015-02-15T18:08:20.435Z"
	 *          },
	 *          {
	 *              "dataSource": "tableName",
	 *              "interval": "2015-01-01T00:00:00.000Z/2015-01-02T00:00:00.000Z",
	 *              "version": "2015-02-01T07:02:05.912Z",
	 *              "loadSpec": {
	 *                  "type": "hdfs",
	 *                  "path": "hdfs:/some_hdfs_URL/tableName/.../index.zip"
	 *              },
	 *              "dimensions": "color", "shape",
	 *              "metrics": "height", "width",
	 *              "shardSpec": {
	 *                  "type":"hashed",
	 *                  "partitionNum": 1,
	 *                  "partitions": 2
	 *              },
	 *              "binaryVersion":9,
	 *              "size":512,
	 *              "identifier":"tableName_2015-01-01T00:00:00.000Z_2015-01-02T00:00:00.000Z_2015-02-01T07:02:05.912Z"
	 *          }
	 *      ]
	 *   }"""
	 * </pre>
	 *
	 * @param table  The table to inject into this callback.
	 *
	 * @return The callback itself.
	 */
	protected final SuccessCallback buildDataSourceMetadataSuccessCallback(PhysicalTable table) {
		return new SuccessCallback() {
			@Override
			public void invoke(JsonNode rootNode) {
				try {
					DataSourceMetadata dataSourceMetadata = mapper.treeToValue(rootNode, DataSourceMetadata.class);
					metadataService.update(table, dataSourceMetadata);
				} catch (IOException e) {
					throw new UnsupportedOperationException(DRUID_METADATA_READ_ERROR.format(table.getName()), e);
				}
			}
		};
	}

	/**
	 * Return when this loader ran most recently.
	 *
	 * @return The date and time of the most recent execution of this loader.
	 */
	public DateTime getLastRunTimestamp() {
		return lastRunTimestamp.get();
	}

	/**
	 * Get a default callback for an http error.
	 *
	 * @param table  The PhysicalTable that the error callback will relate to.
	 *
	 * @return A newly created http error callback object.
	 */
	protected HttpErrorCallback getErrorCallback(PhysicalTable table) {
		return new TaskHttpErrorCallback(table);
	}

	/**
	 * Defines the callback for http errors.
	 */
	private final class TaskHttpErrorCallback extends Loader<?>.TaskHttpErrorCallback {
		private final PhysicalTable table;

		/**
		 * Constructor.
		 *
		 * @param table  PhysicalTable that this error callback is tied to
		 */
		TaskHttpErrorCallback(PhysicalTable table) {
			this.table = table;
		}

		@SuppressWarnings("checkstyle:linelength")
		@Override
		public void invoke(int statusCode, String reason, String responseBody) {
			String msg = String.format(
					"%s: HTTP error while trying to load metadata for table: %s - Status: %d, Cause: %s, Response body: %s",
					getName(), table.getName(), statusCode, reason, responseBody);

			if (statusCode == NO_CONTENT.getStatusCode()) {
				LOG.warn(msg);
				metadataService.update(table,
						new DataSourceMetadata(table.getName(), Collections.emptyMap(), Collections.emptyList()));
			} else {
				LOG.error(msg);
			}
		}
	}
}
