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

public class TableSubquery extends QueryBody {

	private final Query query;

	public TableSubquery(Query query) {
		this(Optional.empty(), query);
	}

	public TableSubquery(NodeLocation location, Query query) {
		this(Optional.of(location), query);
	}

	private TableSubquery(Optional<NodeLocation> location, Query query) {
		super(location);
		this.query = query;
	}

	public Query getQuery() {
		return query;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitTableSubquery(this, context);
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
		return ImmutableList.of(query);
	}

	@Override
	public String toString() {
		return toStringHelper(this).addValue(query).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TableSubquery tableSubquery = (TableSubquery) o;
		return Objects.equals(query, tableSubquery.query);
	}

	@Override
	public int hashCode() {
		return query.hashCode();
	}
}
