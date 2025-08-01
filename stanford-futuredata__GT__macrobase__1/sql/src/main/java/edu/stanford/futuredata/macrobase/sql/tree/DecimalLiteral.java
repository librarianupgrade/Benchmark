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

import java.util.Objects;
import java.util.Optional;

public class DecimalLiteral extends Literal {

	private final double value;

	public DecimalLiteral(String value) {
		this(Optional.empty(), value);
	}

	public DecimalLiteral(NodeLocation location, String value) {
		this(Optional.of(location), value);
	}

	public DecimalLiteral(Optional<NodeLocation> location, String value) {
		super(location);
		requireNonNull(value, "value is null");
		this.value = Double.parseDouble(value);
	}

	public double getValue() {
		return value;
	}

	@Override
	public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
		return visitor.visitDecimalLiteral(this, context);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DecimalLiteral that = (DecimalLiteral) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
