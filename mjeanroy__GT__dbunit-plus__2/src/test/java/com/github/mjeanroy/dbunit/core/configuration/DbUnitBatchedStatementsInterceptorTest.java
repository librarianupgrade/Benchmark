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

package com.github.mjeanroy.dbunit.core.configuration;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.dbunit.database.DatabaseConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DbUnitBatchedStatementsInterceptorTest {

	private static final String PROPERTY_NAME = "http://www.dbunit.org/features/batchedStatements";

	@Test
	void it_should_activate_property_by_default() {
		final DbUnitBatchedStatementsInterceptor interceptor = new DbUnitBatchedStatementsInterceptor();
		final DatabaseConfig config = new DatabaseConfig();
		interceptor.applyConfiguration(config);

		assertThat(config.getProperty(PROPERTY_NAME)).isEqualTo(true);
	}

	@Test
	void it_should_activate_property() {
		final DbUnitBatchedStatementsInterceptor interceptor = new DbUnitBatchedStatementsInterceptor(true);
		final DatabaseConfig config = new DatabaseConfig();
		interceptor.applyConfiguration(config);

		assertThat(config.getProperty(PROPERTY_NAME)).isEqualTo(true);
	}

	@Test
	void it_should_not_activate_property() {
		final DbUnitBatchedStatementsInterceptor interceptor = new DbUnitBatchedStatementsInterceptor(false);
		final DatabaseConfig config = new DatabaseConfig();
		interceptor.applyConfiguration(config);

		assertThat(config.getProperty(PROPERTY_NAME)).isEqualTo(false);
	}

	@Test
	void it_should_implement_equals_hash_code() {
		EqualsVerifier.forClass(DbUnitBatchedStatementsInterceptor.class).verify();
	}

	@Test
	void it_should_implement_to_string() {
		final DbUnitBatchedStatementsInterceptor interceptor = new DbUnitBatchedStatementsInterceptor(true);
		assertThat(interceptor).hasToString("DbUnitBatchedStatementsInterceptor{"
				+ "property: \"http://www.dbunit.org/features/batchedStatements\", " + "value: true" + "}");
	}
}
