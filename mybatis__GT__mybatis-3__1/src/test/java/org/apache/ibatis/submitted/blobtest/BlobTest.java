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
package org.apache.ibatis.submitted.blobtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlobTest {
	private static SqlSessionFactory sqlSessionFactory;

	@BeforeClass
	public static void initDatabase() throws Exception {
		Connection conn = null;

		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:blobtest", "sa", "");

			Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/blobtest/CreateDB.sql");

			ScriptRunner runner = new ScriptRunner(conn);
			runner.setLogWriter(null);
			runner.setErrorLogWriter(null);
			runner.runScript(reader);
			conn.commit();
			reader.close();

			reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/blobtest/MapperConfig.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	/*
	 * This test demonstrates the use of type aliases for primitive types
	 * in constructor based result maps
	 */
	public void testInsertBlobThenSelectAll() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			BlobMapper blobMapper = sqlSession.getMapper(BlobMapper.class);

			byte[] myblob = new byte[] { 1, 2, 3, 4, 5 };
			BlobRecord blobRecord = new BlobRecord(1, myblob);
			int rows = blobMapper.insert(blobRecord);
			assertEquals(1, rows);

			// NPE here due to unresolved type handler
			List<BlobRecord> results = blobMapper.selectAll();

			assertEquals(1, results.size());
			BlobRecord result = results.get(0);
			assertEquals(blobRecord.getId(), result.getId());
			assertTrue(blobsAreEqual(blobRecord.getBlob(), result.getBlob()));
		} finally {
			sqlSession.close();
		}
	}

	@Test
	/*
	 * This test demonstrates the use of type aliases for primitive types
	 * in constructor based result maps
	 */
	public void testInsertBlobObjectsThenSelectAll() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			BlobMapper blobMapper = sqlSession.getMapper(BlobMapper.class);

			Byte[] myblob = new Byte[] { 1, 2, 3, 4, 5 };
			BlobRecord blobRecord = new BlobRecord(1, myblob);
			int rows = blobMapper.insert(blobRecord);
			assertEquals(1, rows);

			// NPE here due to unresolved type handler
			List<BlobRecord> results = blobMapper.selectAllWithBlobObjects();

			assertEquals(1, results.size());
			BlobRecord result = results.get(0);
			assertEquals(blobRecord.getId(), result.getId());
			assertTrue(blobsAreEqual(blobRecord.getBlob(), result.getBlob()));
		} finally {
			sqlSession.close();
		}
	}

	public static boolean blobsAreEqual(byte[] blob1, byte[] blob2) {
		if (blob1 == null) {
			return blob2 == null;
		}

		if (blob2 == null) {
			return blob1 == null;
		}

		boolean rc = blob1.length == blob2.length;

		if (rc) {
			for (int i = 0; i < blob1.length; i++) {
				if (blob1[i] != blob2[i]) {
					rc = false;
					break;
				}
			}
		}

		return rc;
	}
}
