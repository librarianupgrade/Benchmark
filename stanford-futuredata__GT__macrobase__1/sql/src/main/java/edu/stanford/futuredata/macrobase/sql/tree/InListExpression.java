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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InListExpression extends Expression {

	private final List<Expression> values;

	public InListExpression(List<Expression> values) {
		this(Optional.empty(), values);
	}

	public InListExpression(NodeLocation location, List<Expression> values) {
		this(Optional.of(location), values);
	}

	private InListExpression(Optional<NodeLocation> location, List<Expression> values) {
		super(location);
		this.values = values;
	}

	public List<Expression> getValues() {
		return values;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitInListExpression(this, context);
	}

	@Override
	public List<? extends Node> getChildren() {
		return values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		InListExpression that = (InListExpression) o;
		return Objects.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
