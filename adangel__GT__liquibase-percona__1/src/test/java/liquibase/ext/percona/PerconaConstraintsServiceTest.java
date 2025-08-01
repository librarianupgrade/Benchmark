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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PerconaConstraintsServiceTest {
	@BeforeAll
	public static void init() {
		PerconaConstraintsService.getInstance().disable();
	}

	@Test
	public void testPrefixing() {
		PerconaConstraintsService service = PerconaConstraintsService.getInstance();
		Assertions.assertEquals("_FK_1", service.determineCurrentConstraintName(null, createChange("table", "FK_1")));
		Assertions.assertEquals("__FK_1", service.determineCurrentConstraintName(null, createChange("table", "_FK_1")));
		Assertions.assertEquals("FK_1", service.determineCurrentConstraintName(null, createChange("table", "__FK_1")));
	}

	private static PerconaDropForeignKeyConstraintChange createChange(String baseTableName, String constraintName) {
		PerconaDropForeignKeyConstraintChange change = new PerconaDropForeignKeyConstraintChange();
		change.setBaseTableName(baseTableName);
		change.setConstraintName(constraintName);
		return change;
	}
}
