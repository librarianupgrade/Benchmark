/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.model.Equals;
import org.zalando.baigan.service.ConditionsProcessor;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class TestEtcdConfigService {

	private final static ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule());

	private Configuration<Boolean> configuration;

	private String buffer = null;

	@Before
	public void init() throws JsonMappingException, JsonGenerationException, IOException {

		final Condition<Boolean> condition = new Condition<Boolean>("appdomain", new Equals("1"), true);

		final Set<Condition<Boolean>> conditions = ImmutableSet.of(condition);
		configuration = new Configuration("express.feature.toggle", "Feature toggle", conditions, Boolean.FALSE);

		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, configuration);
		buffer = writer.toString();
	}

	private void testConfiguration(final Configuration<Boolean> configuration) {

		final ConditionsProcessor conditionsProcessor = new ConditionsProcessor();

		assertTrue(conditionsProcessor.process(configuration, ImmutableMap.of("appdomain", "1")));

		assertFalse(conditionsProcessor.process(configuration, ImmutableMap.of("appdomain", "2")));
	}

	@Test
	public void testBooleanConfiguration() throws Exception {
		testConfiguration(configuration);
	}

	@Test
	public void testDeserialize() throws Exception {
		final Configuration deserializedConfiguration = mapper.readValue(buffer, Configuration.class);
		testConfiguration(deserializedConfiguration);
	}

}
