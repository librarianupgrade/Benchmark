// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.data.config.metric.makers;

import com.yahoo.bard.webservice.data.metric.LogicalMetric;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.data.metric.TemplateDruidQuery;
import com.yahoo.bard.webservice.data.metric.mappers.NoOpResultSetMapper;
import com.yahoo.bard.webservice.druid.model.aggregation.Aggregation;
import com.yahoo.bard.webservice.druid.model.aggregation.DoubleMinAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.PostAggregation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Metric maker to get the min of double metrics.
 */
public class DoubleMinMaker extends RawAggregationMetricMaker {

	private static final int DEPENDENT_METRICS_REQUIRED = 1;

	/**
	 * Constructor.
	 *
	 * @param metrics  A mapping of metric names to the corresponding LogicalMetrics
	 */
	public DoubleMinMaker(MetricDictionary metrics) {
		super(metrics);
	}

	@Override
	protected LogicalMetric makeInner(String metricName, List<String> dependentMetrics) {

		String dependentMetric = dependentMetrics.get(0);

		DoubleMinAggregation agg = new DoubleMinAggregation(metricName, dependentMetric);

		Set<Aggregation> aggs = Collections.singleton(agg);
		Set<PostAggregation> postAggs = Collections.emptySet();

		TemplateDruidQuery query = new TemplateDruidQuery(aggs, postAggs);

		return new LogicalMetric(query, new NoOpResultSetMapper(), metricName);
	}

	@Override
	protected int getDependentMetricsRequired() {
		return DEPENDENT_METRICS_REQUIRED;
	}
}
