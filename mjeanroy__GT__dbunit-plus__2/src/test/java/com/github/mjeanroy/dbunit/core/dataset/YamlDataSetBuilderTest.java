/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2019 Mickael Jeanroy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mjeanroy.dbunit.core.dataset;

import com.github.mjeanroy.dbunit.core.resources.Resource;
import com.github.mjeanroy.dbunit.tests.builders.ResourceMockBuilder;
import com.github.mjeanroy.dbunit.yaml.YamlParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class YamlDataSetBuilderTest {

	@Test
	void it_should_create_default_data_set_with_file() throws Exception {
		final Resource resource = createResource();
		final YamlDataSet dataSet = new YamlDataSetBuilder(resource).build();

		assertThat(dataSet.getResource()).isSameAs(resource);
		assertThat(dataSet.isCaseSensitiveTableNames()).isFalse();
	}

	@Test
	void it_should_create_custom_data_set() throws Exception {
		final Resource resource = createResource();
		final YamlParser parser = mock(YamlParser.class);
		final YamlDataSet dataSet = new YamlDataSetBuilder().setYamlFile(resource).setCaseSensitiveTableNames(true)
				.setParser(parser).build();

		assertThat(dataSet).isNotNull();
		assertThat(dataSet.getResource()).isSameAs(resource);
		assertThat(dataSet.isCaseSensitiveTableNames()).isTrue();
		verify(parser).parse(resource);
	}

	private static Resource createResource() {
		return new ResourceMockBuilder().fromClasspath("/dataset/yaml/users.yml").build();
	}
}
