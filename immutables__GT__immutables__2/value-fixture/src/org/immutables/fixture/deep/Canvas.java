/*
   Copyright 2016 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.fixture.deep;

import java.util.Optional;
import java.util.List;
import org.immutables.value.Value;

@Value.Style(deepImmutablesDetection = true, depluralize = true)
public interface Canvas {

	@Value.Modifiable
	@Value.Immutable
	public interface Color {
		@Value.Parameter
		double red();

		@Value.Parameter
		double green();

		@Value.Parameter
		double blue();
	}

	@Value.Modifiable
	@Value.Immutable
	public interface Line {
		List<Point> points();

		Color color();

		Optional<Color> shadow();
	}

	@Value.Modifiable
	@Value.Immutable
	public interface Point {
		@Value.Parameter
		int x();

		@Value.Parameter
		int y();
	}

	@SuppressWarnings("CheckReturnValue")
	default void use() {
		ImmutableLine line = ImmutableLine.builder().color(0.9, 0.7, 0.4).addPoint(1, 2).addPoint(2, 3).addPoint(4, 5)
				.build();

		ImmutableColor color = line.color();

		ImmutableLine.builder().addAllPoints(line.points()).color(color).shadow(0.2, 0.2, 0.2).build();
	}
}
