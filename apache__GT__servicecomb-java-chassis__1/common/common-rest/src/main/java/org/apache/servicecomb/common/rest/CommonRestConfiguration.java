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
package org.apache.servicecomb.common.rest;

import java.util.List;

import org.apache.servicecomb.common.rest.codec.query.QueryCodec;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecCsv;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecMulti;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecPipes;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecSsv;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecs;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecsUtils;
import org.apache.servicecomb.common.rest.filter.inner.RestServerCodecFilter;
import org.apache.servicecomb.common.rest.filter.inner.WebSocketServerCodecFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonRestConfiguration {
	@Bean
	public QueryCodecCsv scbQueryCodecCsv() {
		return new QueryCodecCsv();
	}

	@Bean
	public QueryCodecSsv scbQueryCodecSsv() {
		return new QueryCodecSsv();
	}

	@Bean
	public QueryCodecPipes scbQueryCodecPipes() {
		return new QueryCodecPipes();
	}

	@Bean
	public QueryCodecMulti scbQueryCodecMulti() {
		return new QueryCodecMulti();
	}

	@Bean
	public QueryCodecsUtils scbQueryCodecsUtils(QueryCodecs queryCodecs) {
		return new QueryCodecsUtils(queryCodecs);
	}

	@Bean
	public RestServerCodecFilter scbRestServerCodecFilter() {
		return new RestServerCodecFilter();
	}

	@Bean
	public WebSocketServerCodecFilter scbWebSocketServerCodecFilter() {
		return new WebSocketServerCodecFilter();
	}

	@Bean
	public QueryCodecs scbQueryCodecs(List<QueryCodec> orderedCodecs) {
		return new QueryCodecs(orderedCodecs);
	}

	@Bean
	public RestEngineSchemaListener scbRestEngineSchemaListener() {
		return new RestEngineSchemaListener();
	}
}
