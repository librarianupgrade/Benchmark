/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.parent_childs;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParentChildTest {

	private static SqlSessionFactory sqlSessionFactory;

	@BeforeClass
	public static void setUp() throws Exception {
		// create a SqlSessionFactory
		Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/parent_childs/mybatis-config.xml");
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		reader.close();

		// populate in-memory database
		SqlSession session = sqlSessionFactory.openSession();
		Connection conn = session.getConnection();
		reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/parent_childs/CreateDB.sql");
		ScriptRunner runner = new ScriptRunner(conn);
		runner.setLogWriter(null);
		runner.runScript(reader);
		reader.close();
		session.close();
	}

	@Test
	public void shouldGetAUser() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			Mapper mapper = sqlSession.getMapper(Mapper.class);
			List<Parent> parents = mapper.getParents();
			Assert.assertEquals(2, parents.size());
			Parent firstParent = parents.get(0);
			Assert.assertEquals("Jose", firstParent.getName());
			Assert.assertEquals(2, firstParent.getChilds().size());
			Parent secondParent = parents.get(1);
			Assert.assertEquals("Juan", secondParent.getName());
			Assert.assertEquals(0, secondParent.getChilds().size()); // note an empty list is inyected
		} finally {
			sqlSession.close();
		}
	}

}
