/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.xml_external_ref;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

public class NonFullyQualifiedNamespaceTest {
	@Test
	public void testCrossReferenceXmlConfig() throws Exception {
		Reader configReader = Resources.getResourceAsReader(
				"org/apache/ibatis/submitted/xml_external_ref/NonFullyQualifiedNamespaceConfig.xml");
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
		configReader.close();

		Configuration configuration = sqlSessionFactory.getConfiguration();

		MappedStatement selectPerson = configuration.getMappedStatement("person namespace.select");
		assertEquals("org/apache/ibatis/submitted/xml_external_ref/NonFullyQualifiedNamespacePersonMapper.xml",
				selectPerson.getResource());

		Connection conn = configuration.getEnvironment().getDataSource().getConnection();
		initDb(conn);

		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			Person person = (Person) sqlSession.selectOne("person namespace.select", 1);
			assertEquals((Integer) 1, person.getId());
			assertEquals(2, person.getPets().size());
			assertEquals((Integer) 2, person.getPets().get(1).getId());

			Pet pet = (Pet) sqlSession.selectOne("person namespace.selectPet", 1);
			assertEquals(Integer.valueOf(1), pet.getId());

			Pet pet2 = (Pet) sqlSession.selectOne("pet namespace.select", 3);
			assertEquals((Integer) 3, pet2.getId());
			assertEquals((Integer) 2, pet2.getOwner().getId());
		} finally {
			sqlSession.close();
		}
	}

	private static void initDb(Connection conn) throws IOException, SQLException {
		try {
			Reader scriptReader = Resources
					.getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/CreateDB.sql");
			ScriptRunner runner = new ScriptRunner(conn);
			runner.setLogWriter(null);
			runner.setErrorLogWriter(null);
			runner.runScript(scriptReader);
			conn.commit();
			scriptReader.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
