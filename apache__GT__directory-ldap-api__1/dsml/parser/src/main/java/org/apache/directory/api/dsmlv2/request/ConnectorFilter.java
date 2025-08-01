/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    https://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.api.dsmlv2.request;

import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.asn1.DecoderException;

/**
 * This Filter abstract class is used to store a set of filters used by
 * OR/AND/NOT filters.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ConnectorFilter extends Filter {
	/** The set of filters used by And/Or filters */
	protected List<Filter> filterSet;

	/**
	 * Add a new Filter to the list.
	 * 
	 * @param filter The filter to add
	 * @throws DecoderException If the added filter is invalid
	 */
	public void addFilter(Filter filter) throws DecoderException {

		if (filterSet == null) {
			filterSet = new ArrayList<>();
		}

		filterSet.add(filter);
	}

	/**
	 * Get the list of filters stored in the composite filter
	 * 
	 * @return And array of filters
	 */
	public List<Filter> getFilterSet() {
		return filterSet;
	}

	/**
	 * Return a string compliant with RFC 2254 representing a composite filter,
	 * one of AND, OR and NOT
	 * 
	 * @return The composite filter string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if ((filterSet != null) && !filterSet.isEmpty()) {
			for (Filter filter : filterSet) {
				sb.append('(').append(filter).append(')');
			}
		}

		return sb.toString();
	}
}
