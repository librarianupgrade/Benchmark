package liquibase.ext.percona;

/*
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class PerconaDropForeignKeyConstraintChangeTest
		extends AbstractPerconaChangeTest<PerconaDropForeignKeyConstraintChange> {

	public PerconaDropForeignKeyConstraintChangeTest() {
		super(PerconaDropForeignKeyConstraintChange.class);
	}

	@Override
	protected void setupChange(PerconaDropForeignKeyConstraintChange change) {
		change.setBaseTableName("address");
		change.setConstraintName("fk_address_person");

		setTargetTableName("address");
		alterText = "DROP FOREIGN KEY _fk_address_person";
	}

	@Test
	public void testWithoutPercona() {
		PTOnlineSchemaChangeStatement.available = false;
		SqlStatement[] statements = generateStatements();
		Assertions.assertEquals(1, statements.length);
		Assertions.assertEquals(DropForeignKeyConstraintStatement.class, statements[0].getClass());
	}

	@Test
	public void testWithoutPerconaAndFail() {
		System.setProperty(Configuration.FAIL_IF_NO_PT, "true");
		PTOnlineSchemaChangeStatement.available = false;

		Assertions.assertThrows(RuntimeException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				generateStatements();
			}
		});
	}

	@Test
	public void testReal() {
		assertPerconaChange(alterText);
	}

	@Test
	public void testUpdateSQL() {
		enableLogging();

		SqlStatement[] statements = generateStatements();
		Assertions.assertEquals(3, statements.length);
		Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
		Assertions.assertEquals("pt-online-schema-change " + "--alter-foreign-keys-method=auto "
				+ "--nocheck-unique-key-change " + "--alter=\"" + alterText + "\" " + "--password=*** --execute "
				+ "h=localhost,P=3306,u=user,D=testdb,t=address", ((CommentStatement) statements[0]).getText());
		Assertions.assertEquals(CommentStatement.class, statements[1].getClass());
		Assertions.assertEquals(DropForeignKeyConstraintStatement.class, statements[2].getClass());
	}

	@Test
	public void testUpdateSQLNoAlterSqlDryMode() {
		enableLogging();
		System.setProperty(Configuration.NO_ALTER_SQL_DRY_MODE, "true");

		SqlStatement[] statements = generateStatements();
		Assertions.assertEquals(1, statements.length);
		Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
		Assertions.assertEquals("pt-online-schema-change " + "--alter-foreign-keys-method=auto "
				+ "--nocheck-unique-key-change " + "--alter=\"" + alterText + "\" " + "--password=*** --execute "
				+ "h=localhost,P=3306,u=user,D=testdb,t=address", ((CommentStatement) statements[0]).getText());
	}

	@Test
	public void testSkipDropForeignKeyConstraintChange() {
		System.setProperty(Configuration.SKIP_CHANGES, "dropForeignKeyConstraint");
		SqlStatement[] statements = generateStatements();
		Assertions.assertEquals(1, statements.length);
		Assertions.assertEquals(DropForeignKeyConstraintStatement.class, statements[0].getClass());
	}
}
