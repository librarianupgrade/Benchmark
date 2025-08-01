// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.druid.model.aggregation;

/**
 * Aggregation for the sum of doubles.
 */
public class DoubleSumAggregation extends Aggregation {

	/**
	 * Constructor.
	 *
	 * @param name  Name of the aggregation
	 * @param fieldName  Name of the column that this aggregation is aggregating over
	 */
	public DoubleSumAggregation(String name, String fieldName) {
		super(name, fieldName);
	}

	@Override
	public String getType() {
		return "doubleSum";
	}

	@Override
	public DoubleSumAggregation withName(String name) {
		return new DoubleSumAggregation(name, getFieldName());
	}

	@Override
	public Aggregation withFieldName(String fieldName) {
		return new DoubleSumAggregation(getName(), fieldName);
	}
}
