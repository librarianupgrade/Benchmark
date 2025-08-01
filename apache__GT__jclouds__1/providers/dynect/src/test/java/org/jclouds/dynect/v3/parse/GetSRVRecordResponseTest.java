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
package org.jclouds.dynect.v3.parse;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import org.jclouds.dynect.v3.domain.Record;
import org.jclouds.dynect.v3.domain.rdata.SRVData;
import org.jclouds.dynect.v3.internal.BaseDynECTParseTest;
import org.jclouds.rest.annotations.SelectJson;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class GetSRVRecordResponseTest extends BaseDynECTParseTest<Record<SRVData>> {

	@Override
	public String resource() {
		return "/get_record_srv.json";
	}

	@Override
	@SelectJson("data")
	@Consumes(MediaType.APPLICATION_JSON)
	public Record<SRVData> expected() {
		return Record.<SRVData>builder().zone("adrianc.zone.dynecttest.jclouds.org").fqdn("_http._tcp.www.jclouds.org.")
				.type("SRV").id(50976579l).ttl(3600)
				.rdata(SRVData.builder().priority(0).weight(2).port(80).target("www.jclouds.org.").build()).build();
	}
}
