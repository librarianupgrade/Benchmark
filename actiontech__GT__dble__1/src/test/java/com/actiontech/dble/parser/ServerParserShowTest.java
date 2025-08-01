/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.parser;

import com.actiontech.dble.server.parser.ServerParseShow;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by szf on 2017/4/20.
 */
public class ServerParserShowTest {

	@Test
	public void showTableTypeTest() {

		String[] testSql = { "SHOW FULL TABLES", "SHOW FULL TABLES FROM TESTDB",
				"SHOW FULL TABLES FROM TESTDB LIKE 'XX%'", "SHOW FULL TABLES IN TESTDB",
				"SHOW FULL TABLES IN TESTDB LIKE 'XX%'", "SHOW FULL TABLES IN TESTDB where Table_type != 'VIEW'",
				"show full tables", "show full tables from testdb", "show full tables from testdb like 'xx%'",
				"show full tables in testdb", "show full tables in testdb like 'xx%'",
				"show full tables from testdb_like               ", "show full tables  from in testdb like 'xx%'",
				"show full tables testdb like 'xx%'", "show full tables  intestdb like 'xx%'",
				"show full tables  fromtestdb like 'xx%'", "show full tables from from testdb like 'xx%'",
				"show full tables from in testdb like 'xx%'", "show full tables in in testdb like 'xx%'",
				"show full tables from like 'xx%'", "show full tables from testdb  'xx%'",
				"show full tables from testdb like like 'xx%'", "show full tables from testdb like               ",
				"show fulltables from testdb like  'x'            ", "showfulltables from testdb like  'x'            ",
				"showfull tables from testdb like  'x'            ", "showfull tables from testdblike  'x'            ",
				"showfull tables from testdb like'x'            ", "show full tablesfrom testdb like  'x'            ",
				"SHOW TABLES", "SHOW  TABLES FROM TESTDB", "SHOW  TABLES FROM TESTDB LIKE 'XX%'",
				"SHOW  TABLES IN TESTDB", "SHOW  TABLES IN TESTDB LIKE 'XX%'", "show  tables",
				"show  tables from testdb", "show  tables from testdb like 'xx%'", "show  tables in testdb",
				"show  tables in testdb like 'xx%'", "show  tables from testdb_like               ",
				"show  tables  from in testdb like 'xx%'", "show  tables testdb like 'xx%'",
				"show  tables  intestdb like 'xx%'", "show  tables  fromtestdb like 'xx%'",
				"show  tables from from testdb like 'xx%'", "show  tables from in testdb like 'xx%'",
				"show  tables in in testdb like 'xx%'", "show  tables from like 'xx%'",
				"show  tables from testdb  'xx%'", "show  tables from testdb like like 'xx%'",
				"show  tables from testdb like               ", "show tables fromtestdb like  'x'            ",
				"showtables from testdb like  'x'            ", "showtables from testdb like  'x'            ",
				"showtables from testdblike  'x'            ", "showtables from testdb like'x'            ",
				"show full tablesfrom testdb like  'x'            " };

		int[] testResult = { ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER };

		for (int i = 0; i < testSql.length; i++) {
			int reslut = ServerParseShow.showTableType(testSql[i]);
			Assert.assertEquals(testResult[i], reslut);
		}

	}

	@Test
	public void fullTableCheckTest() {
		String[] testSql = { "SHOW FULL TABLES", "SHOW FULL TABLES FROM TESTDB",
				"SHOW FULL TABLES FROM TESTDB LIKE 'XX%'", "SHOW FULL TABLES IN TESTDB",
				"SHOW FULL TABLES IN TESTDB LIKE 'XX%'", "show full tables", "show full tables from testdb",
				"show full tables from testdb like 'xx%'", "show full tables in testdb",
				"show full tables in testdb like 'xx%'", "show full tables from testdb_like               ",
				"show full tables  from in testdb like 'xx%'", "show full tables testdb like 'xx%'",
				"show full tables  intestdb like 'xx%'", "show full tables  fromtestdb like 'xx%'",
				"show full tables from from testdb like 'xx%'", "show full tables from in testdb like 'xx%'",
				"show full tables in in testdb like 'xx%'", "show full tables from like 'xx%'",
				"show full tables from testdb  'xx%'", "show full tables from testdb like like 'xx%'",
				"show full tables from testdb like               ", "show fulltables from testdb like  'x'            ",
				"showfulltables from testdb like  'x'            ", "showfull tables from testdb like  'x'            ",
				"showfull tables from testdblike  'x'            ", "showfull tables from testdb like'x'            ",
				"show full tablesfrom testdb like  'x'            ", "SHOW TABLES", "SHOW  TABLES FROM TESTDB",
				"SHOW  TABLES FROM TESTDB LIKE 'XX%'", "SHOW  TABLES IN TESTDB", "SHOW  TABLES IN TESTDB LIKE 'XX%'",
				"show  tables", "show  tables from testdb", "show  tables from testdb like 'xx%'",
				"show  tables in testdb", "show  tables in testdb like 'xx%'",
				"show  tables from testdb_like               ", "show  tables  from in testdb like 'xx%'",
				"show  tables testdb like 'xx%'", "show  tables  intestdb like 'xx%'",
				"show  tables  fromtestdb like 'xx%'", "show  tables from from testdb like 'xx%'",
				"show  tables from in testdb like 'xx%'", "show  tables in in testdb like 'xx%'",
				"show  tables from like 'xx%'", "show  tables from testdb  'xx%'",
				"show  tables from testdb like like 'xx%'", "show  tables from testdb like               ",
				"show tables fromtestdb like  'x'            ", "showtables from testdb like  'x'            ",
				"showtables from testdb like  'x'            ", "showtables from testdblike  'x'            ",
				"showtables from testdb like'x'            ", "show full tablesfrom testdb like  'x'            " };

		int[] testResult = { ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES, ServerParseShow.TABLES,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER, ServerParseShow.OTHER,
				ServerParseShow.OTHER };

		for (int i = 0; i < testSql.length; i++) {
			int reslut = ServerParseShow.showTableType(testSql[i]);
			Assert.assertEquals(testResult[i], reslut);
		}
	}
}
