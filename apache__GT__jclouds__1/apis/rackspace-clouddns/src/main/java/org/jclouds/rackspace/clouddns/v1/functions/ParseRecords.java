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
package org.jclouds.rackspace.clouddns.v1.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.rackspace.clouddns.v1.functions.ParseRecord.toRecordDetails;

import java.beans.ConstructorProperties;

import javax.inject.Inject;

import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.openstack.v2_0.domain.Link;
import org.jclouds.openstack.v2_0.domain.PaginatedCollection;
import org.jclouds.rackspace.clouddns.v1.domain.RecordDetail;
import org.jclouds.rackspace.clouddns.v1.functions.ParseRecord.RawRecord;

import com.google.common.base.Function;

public class ParseRecords implements Function<HttpResponse, PaginatedCollection<RecordDetail>> {

	private final ParseJson<RawRecords> json;

	@Inject
	ParseRecords(ParseJson<RawRecords> json) {
		this.json = checkNotNull(json, "json");
	}

	@Override
	public PaginatedCollection<RecordDetail> apply(HttpResponse response) {
		RawRecords rawRecords = json.apply(response);
		Iterable<RecordDetail> records = rawRecords.transform(toRecordDetails);

		return new Records(records, rawRecords.getLinks(), rawRecords.getTotalEntries().get());
	}

	private static class RawRecords extends PaginatedCollection<RawRecord> {

		@ConstructorProperties({ "records", "links", "totalEntries" })
		protected RawRecords(Iterable<RawRecord> records, Iterable<Link> links, int totalEntries) {
			super(records, links, totalEntries);
		}
	}

	private static class Records extends PaginatedCollection<RecordDetail> {

		protected Records(Iterable<RecordDetail> records, Iterable<Link> links, int totalEntries) {
			super(records, links, totalEntries);
		}
	}
}
