/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.examples;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author xiaojing
 */
@Configuration
public class DatabaseConfiguration {

	//  druid don't support GraalVM now because of there is CGlib proxy
	//	@Bean
	//	@Primary
	//	@ConfigurationProperties("spring.datasource")
	//	public DataSource storageDataSource() {
	//		return new DruidDataSource();
	//	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update("delete from account_tbl where user_id = 'U100001'");
		jdbcTemplate.update("insert into account_tbl(user_id, money) values ('U100001', 10000)");

		return jdbcTemplate;
	}

}
