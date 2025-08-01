// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

import com.yahoo.bard.webservice.table.LogicalTable;
import com.yahoo.bard.webservice.web.endpoints.DimensionsServlet;
import com.yahoo.bard.webservice.web.endpoints.MetricsServlet;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

/**
 * Class to support custom endpoint of the tables with complete view of each table grains and associated metrics
 * &amp; dimensions.
 */
public class TableFullViewProcessor implements TableMetadataFormatter {

	/**
	 * Method to provide full view of the tables which includes grains, metrics and dimensions.
	 *
	 * @param logicalTables  Set of logical tables
	 * @param uriInfo  Uri information to construct the uri's
	 *
	 * @return List of table details with all the associated meta info
	 */
	@Override
	public List<TableView> formatTables(Set<LogicalTable> logicalTables, UriInfo uriInfo) {

		//Map to keep meta info of the logical table
		Map<String, TableView> tablesMeta = new HashMap<>();
		//Map to keep list of time grain details for the logical table
		Map<String, List<TableGrainView>> grainsData = new HashMap<>();

		for (LogicalTable logicalTable : logicalTables) {
			//An array list to store grain level definition of given logical table
			List<TableGrainView> grains = grainsData.computeIfAbsent(logicalTable.getName(),
					(ignore) -> new ArrayList<>());

			grains.add(formatTableGrain(logicalTable, logicalTable.getGranularity().getName(), uriInfo));

			if (tablesMeta.get(logicalTable.getName()) == null) {
				tablesMeta.put(logicalTable.getName(), formatTable(logicalTable, uriInfo));
			}
		}

		List<TableView> tableViewList = new ArrayList<>();

		Set<Map.Entry<String, TableView>> entrySet = tablesMeta.entrySet();
		for (Map.Entry<String, TableView> entry : entrySet) {
			TableView tableView = entry.getValue();
			tableView.put("timeGrains", grainsData.get(entry.getKey()));
			tableViewList.add(tableView);
		}
		return tableViewList;
	}

	/**
	 * Method to provide metadata of the table.
	 *
	 * @param logicalTable  Logical Table Ex: Network, SpaceId
	 * @param uriInfo  Uri information to construct the uri's
	 *
	 * @return  Meta data details of the given table
	 */
	@Override
	public TableView formatTable(LogicalTable logicalTable, UriInfo uriInfo) {
		TableView resultRow = new TableView();
		resultRow.put("name", logicalTable.getName());
		resultRow.put("longName", logicalTable.getLongName());
		resultRow.put("description", logicalTable.getDescription());
		resultRow.put("category", logicalTable.getCategory());
		return resultRow;
	}

	/**
	 * Method to provide grain level details(with metrics and dimensions) of the given logical table.
	 *
	 * @param logicalTable  Logical Table at the grain level
	 * @param grain  Table grain
	 * @param uriInfo Uri information to construct the uri's
	 *
	 * @return logical table details at grain level with all the associated meta data
	 */
	@Override
	public TableGrainView formatTableGrain(LogicalTable logicalTable, String grain, UriInfo uriInfo) {
		TableGrainView resultRow = new TableGrainView();
		resultRow.put("name", grain);
		resultRow.put("longName", StringUtils.capitalize(grain));
		resultRow.put("description", "The " + logicalTable.getName() + " " + grain + " grain");
		resultRow.put("retention", logicalTable.getRetention().toString());
		resultRow.put("dimensions",
				DimensionsServlet.getDimensionListSummaryView(logicalTable.getDimensions(), uriInfo));
		resultRow.put("metrics",
				MetricsServlet.getLogicalMetricListSummaryView(logicalTable.getLogicalMetrics(), uriInfo));
		return resultRow;
	}
}
