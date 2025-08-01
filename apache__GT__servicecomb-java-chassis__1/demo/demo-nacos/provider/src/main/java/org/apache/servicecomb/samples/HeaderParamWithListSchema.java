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

package org.apache.servicecomb.samples;

import java.util.List;

import org.apache.servicecomb.demo.api.IHeaderParamWithListSchema;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "HeaderParamWithListSchema", schemaInterface = IHeaderParamWithListSchema.class)
public class HeaderParamWithListSchema implements IHeaderParamWithListSchema {
	@Override
	public String headerListDefault(List<String> headerList) {
		return headerList == null ? "null" : headerList.size() + ":" + headerList;
	}

	@Override
	public String headerListCSV(List<String> headerList) {
		return headerList == null ? "null" : headerList.size() + ":" + headerList;
	}

	@Override
	public String headerListMULTI(List<String> headerList) {
		return headerList == null ? "null" : headerList.size() + ":" + headerList;
	}

	@Override
	public String headerListSSV(List<String> headerList) {
		return headerList == null ? "null" : headerList.size() + ":" + headerList;
	}

	@Override
	public String headerListPIPES(List<String> headerList) {
		return headerList == null ? "null" : headerList.size() + ":" + headerList;
	}
}
