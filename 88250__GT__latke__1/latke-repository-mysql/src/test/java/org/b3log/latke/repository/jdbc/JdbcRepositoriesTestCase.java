/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.repository.jdbc;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.Map;

import org.b3log.latke.Latkes;
import org.b3log.latke.repository.jdbc.util.FieldDefinition;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.testng.annotations.Test;

/**
 * JdbcRepositoriesTestCase.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Jan 7, 2012
 */
public class JdbcRepositoriesTestCase {

	/**
	 * jsonToModel.
	 */
	@Test
	public void jsonToModel() {
		Latkes.initRuntimeEnv();
		final Map<String, List<FieldDefinition>> map = JdbcRepositories.getRepositoriesMap();

		assertNotNull(map);
	}

	/**
	 * jsonToDB.
	 */
	@Test(groups = { "jdbc" })
	public void jsonToDB() {
		Latkes.initRuntimeEnv();
		//JdbcRepositories.initAllTables();
	}
}
