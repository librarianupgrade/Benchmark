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
package edu.stanford.futuredata.macrobase.sql.tree;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Table extends QueryBody {

	private final QualifiedName name;

	public Table(QualifiedName name) {
		this(Optional.empty(), name);
	}

	public Table(NodeLocation location, QualifiedName name) {
		this(Optional.of(location), name);
	}

	private Table(Optional<NodeLocation> location, QualifiedName name) {
		super(location);
		this.name = name;
	}

	public QualifiedName getName() {
		return name;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitTable(this, context);
	}

	@Override
	public Select getSelect() {
		return SELECT_ALL;
	}

	@Override
	public Optional<Expression> getWhere() {
		return Optional.empty();
	}

	@Override
	public Optional<OrderBy> getOrderBy() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getLimit() {
		return Optional.empty();
	}

	@Override
	public Optional<ExportClause> getExportExpr() {
		return Optional.empty();
	}

	@Override
	public List<Node> getChildren() {
		return ImmutableList.of();
	}

	@Override
	public String toString() {
		return toStringHelper(this).addValue(name).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Table table = (Table) o;
		return Objects.equals(name, table.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
