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
import java.util.Optional;

public class LogicalBinaryExpression extends Expression {

	public enum Type {
		AND, OR;

		public Type flip() {
			switch (this) {
			case AND:
				return LogicalBinaryExpression.Type.OR;
			case OR:
				return LogicalBinaryExpression.Type.AND;
			default:
				throw new IllegalArgumentException("Unsupported logical expression type: " + this);
			}
		}
	}

	private final Type type;
	private final Expression left;
	private final Expression right;

	public LogicalBinaryExpression(Type type, Expression left, Expression right) {
		this(Optional.empty(), type, left, right);
	}

	public LogicalBinaryExpression(NodeLocation location, Type type, Expression left, Expression right) {
		this(Optional.of(location), type, left, right);
	}

	private LogicalBinaryExpression(Optional<NodeLocation> location, Type type, Expression left, Expression right) {
		super(location);
		requireNonNull(type, "type is null");
		requireNonNull(left, "left is null");
		requireNonNull(right, "right is null");

		this.type = type;
		this.left = left;
		this.right = right;
	}

	public Type getType() {
		return type;
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitLogicalBinaryExpression(this, context);
	}

	@Override
	public List<Node> getChildren() {
		return ImmutableList.of(left, right);
	}

	public static LogicalBinaryExpression and(Expression left, Expression right) {
		return new LogicalBinaryExpression(Optional.empty(), Type.AND, left, right);
	}

	public static LogicalBinaryExpression or(Expression left, Expression right) {
		return new LogicalBinaryExpression(Optional.empty(), Type.OR, left, right);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LogicalBinaryExpression that = (LogicalBinaryExpression) o;
		return type == that.type && Objects.equals(left, that.left) && Objects.equals(right, that.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, left, right);
	}
}
