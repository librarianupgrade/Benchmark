/*
 * Copyright 2019 Immutables Authors and Contributors
 * Copyright 2016-2018 Apache Software Foundation (ASF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.immutables.criteria.elasticsearch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testing correct parsing of JSON (elasticsearch) response.
 * <p>Some parts of this class have been copied from <a href="https://calcite.apache.org/">Apache Calcite</a> project.
 */
public class JsonTest {

	private ObjectMapper mapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
			.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

	@Test
	public void aggEmpty() throws Exception {
		String json = "{}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		assertNotNull(a);
		assertThat(a.asList().size(), is(0));
		assertThat(a.asMap().size(), is(0));
	}

	@Test
	public void aggSingle1() throws Exception {
		String json = "{agg1: {value: '111'}}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		assertNotNull(a);
		assertEquals(1, a.asList().size());
		assertEquals(1, a.asMap().size());
		assertEquals("agg1", a.asList().get(0).getName());
		assertEquals("agg1", a.asMap().keySet().iterator().next());
		assertEquals("111", ((Json.MultiValue) a.asList().get(0)).value());

		List<Map<String, Object>> rows = new ArrayList<>();
		Json.visitValueNodes(a, rows::add);
		assertThat(rows.size(), is(1));
		assertThat(rows.get(0).get("agg1"), is("111"));
	}

	@Test
	public void aggMultiValues() throws Exception {
		String json = "{ agg1: {min: 0, max: 2, avg: 2.33}}";
		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		assertNotNull(a);
		assertEquals(1, a.asList().size());
		assertEquals(1, a.asMap().size());
		assertEquals("agg1", a.asList().get(0).getName());

		Map<String, Object> values = ((Json.MultiValue) a.get("agg1")).values();
		assertThat(values.keySet(), hasItems("min", "max", "avg"));
	}

	@Test
	public void aggSingle2() throws Exception {
		String json = "{ agg1: {value: 'foo'}, agg2: {value: 42}}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		assertNotNull(a);
		assertEquals(2, a.asList().size());
		assertEquals(2, a.asMap().size());
		assertThat(a.asMap().keySet(), hasItems("agg1", "agg2"));
	}

	@Test
	public void aggBuckets1() throws Exception {
		String json = "{ groupby: {buckets: [{key:'k1', doc_count:0, myagg:{value: 1.1}},"
				+ " {key:'k2', myagg:{value: 2.2}}] }}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);

		assertThat(a.asMap().keySet(), hasItem("groupby"));
		assertThat(a.get("groupby"), instanceOf(Json.MultiBucketsAggregation.class));
		Json.MultiBucketsAggregation multi = a.get("groupby");
		assertThat(multi.buckets().size(), is(2));
		assertThat(multi.getName(), is("groupby"));
		assertThat(multi.buckets().get(0).key(), is("k1"));
		assertThat(multi.buckets().get(0).keyAsString(), is("k1"));
		assertThat(multi.buckets().get(1).key(), is("k2"));
		assertThat(multi.buckets().get(1).keyAsString(), is("k2"));
	}

	@Test
	public void aggManyAggregations() throws Exception {
		String json = "{groupby:{buckets:[" + "{key:'k1', a1:{value:1}, a2:{value:2}},"
				+ "{key:'k2', a1:{value:3}, a2:{value:4}}" + "]}}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		Json.MultiBucketsAggregation multi = a.get("groupby");

		assertThat(multi.buckets().get(0).getAggregations().asMap().size(), is(2));
		assertThat(multi.buckets().get(0).getName(), is("groupby"));
		assertThat(multi.buckets().get(0).key(), is("k1"));
		assertThat(multi.buckets().get(0).getAggregations().asMap().keySet(), hasItems("a1", "a2"));
		assertThat(multi.buckets().get(1).getAggregations().asMap().size(), is(2));
		assertThat(multi.buckets().get(1).getName(), is("groupby"));
		assertThat(multi.buckets().get(1).key(), is("k2"));
		assertThat(multi.buckets().get(1).getAggregations().asMap().keySet(), hasItems("a1", "a2"));
		List<Map<String, Object>> rows = new ArrayList<>();
		Json.visitValueNodes(a, rows::add);
		assertThat(rows.size(), is(2));
		assertThat(rows.get(0).get("groupby"), is("k1"));
		assertThat(rows.get(0).get("a1"), is(1));
		assertThat(rows.get(0).get("a2"), is(2));
	}

	@Test
	public void aggMultiBuckets() throws Exception {
		String json = "{col1: {buckets: [" + "{col2: {doc_count:1, buckets:[{key:'k3', max:{value:41}}]}, key:'k1'},"
				+ "{col2: {buckets:[{key:'k4', max:{value:42}}], doc_count:1}, key:'k2'}" + "]}}";

		Json.Aggregations a = mapper.readValue(json, Json.Aggregations.class);
		assertNotNull(a);

		assertThat(a.asMap().keySet(), hasItem("col1"));
		assertThat(a.get("col1"), instanceOf(Json.MultiBucketsAggregation.class));
		Json.MultiBucketsAggregation m = a.get("col1");
		assertThat(m.getName(), is("col1"));
		assertThat(m.buckets().size(), is(2));
		assertThat(m.buckets().get(0).key(), is("k1"));
		assertThat(m.buckets().get(0).getName(), is("col1"));
		assertThat(m.buckets().get(0).getAggregations().asMap().keySet(), hasItem("col2"));
		assertThat(m.buckets().get(1).key(), is("k2"));
		List<Map<String, Object>> rows = new ArrayList<>();
		Json.visitValueNodes(a, rows::add);
		assertThat(rows.size(), is(2));

		assertThat(rows.get(0).keySet(), hasItems("col1", "col2", "max"));
		assertThat(rows.get(0).get("col1"), is("k1"));
		assertThat(rows.get(0).get("col2"), is("k3"));
		assertThat(rows.get(0).get("max"), is(41));

		assertThat(rows.get(1).keySet(), hasItems("col1", "col2", "max"));
		assertThat(rows.get(1).get("col1"), is("k2"));
		assertThat(rows.get(1).get("col2"), is("k4"));
		assertThat(rows.get(1).get("max"), is(42));
	}
}
