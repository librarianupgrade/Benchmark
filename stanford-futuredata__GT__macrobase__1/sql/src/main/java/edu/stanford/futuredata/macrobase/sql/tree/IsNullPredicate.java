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

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class IsNullPredicate extends ComparisonExpression {

	private final Expression value;

	public IsNullPredicate(Expression value) {
		super(ComparisonExpressionType.EQUAL, value, new NullLiteral());
		requireNonNull(value, "value is null");
		this.value = value;
	}

	public IsNullPredicate(NodeLocation location, Expression value) {
		super(location, ComparisonExpressionType.EQUAL, value, new NullLiteral(location));
		requireNonNull(value, "value is null");
		this.value = value;
	}

	public Expression getValue() {
		return value;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitIsNullPredicate(this, context);
	}

	@Override
	public List<Node> getChildren() {
		return ImmutableList.of(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		IsNullPredicate that = (IsNullPredicate) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
