package liquibase.ext.percona;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = PerconaModifyDataTypeChange.NAME, description = "Modify data type", priority = PerconaModifyDataTypeChange.PRIORITY, appliesTo = "column")
public class PerconaModifyDataTypeChange extends ModifyDataTypeChange implements PerconaChange {
	public static final String NAME = "modifyDataType";
	public static final int PRIORITY = ChangeMetaData.PRIORITY_DEFAULT + 50;

	@Override
	public SqlStatement[] generateStatements(Database database) {
		return PerconaChangeUtil.generateStatements(this, database, super.generateStatements(database));
	}

	@Override
	public String generateAlterStatement(Database database) {
		StringBuilder alter = new StringBuilder();

		alter.append("MODIFY ");
		String columnName = database.escapeColumnName(getCatalogName(), getSchemaName(), getTableName(),
				getColumnName());
		alter.append(columnName);

		alter.append(' ');
		if (getNewDataType() != null) {
			String dataType = String.valueOf(DataTypeFactory.getInstance().fromDescription(getNewDataType(), database)
					.toDatabaseDataType(database));
			alter.append(dataType);
		}

		return alter.toString();
	}

	@Override
	public String getTargetTableName() {
		return getTableName();
	}

	@Override
	public String getTargetDatabaseName() {
		return getCatalogName();
	}

	//CPD-OFF - common PerconaChange implementation
	private Boolean usePercona;

	private String perconaOptions;

	@Override
	public String getChangeName() {
		return NAME;
	}

	@Override
	@DatabaseChangeProperty(requiredForDatabase = {})
	public Boolean getUsePercona() {
		return usePercona;
	}

	@Override
	public void setUsePercona(Boolean usePercona) {
		this.usePercona = usePercona;
	}

	@Override
	@DatabaseChangeProperty(requiredForDatabase = {})
	public String getPerconaOptions() {
		return perconaOptions;
	}

	@Override
	public void setPerconaOptions(String perconaOptions) {
		this.perconaOptions = perconaOptions;
	}

	@Override
	public Set<String> getSerializableFields() {
		Set<String> fields = new HashSet<>(super.getSerializableFields());
		fields.remove("usePercona");
		fields.remove("perconaOptions");
		return Collections.unmodifiableSet(fields);
	}
	//CPD-ON
}
