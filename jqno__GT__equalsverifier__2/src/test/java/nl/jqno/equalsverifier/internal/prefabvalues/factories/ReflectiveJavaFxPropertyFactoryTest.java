/*
 * Copyright 2015-2016 Jan Ouwens
 *
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
package nl.jqno.equalsverifier.internal.prefabvalues.factories;

import nl.jqno.equalsverifier.JavaApiPrefabValues;
import nl.jqno.equalsverifier.internal.prefabvalues.PrefabValues;
import nl.jqno.equalsverifier.internal.prefabvalues.Tuple;
import nl.jqno.equalsverifier.internal.prefabvalues.TypeTag;
import nl.jqno.equalsverifier.testhelpers.types.Point;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("rawtypes")
public class ReflectiveJavaFxPropertyFactoryTest {
	private PrefabValues prefabValues;

	@Before
	public void setUp() {
		prefabValues = new PrefabValues();
		JavaApiPrefabValues.addTo(prefabValues);
	}

	@Test
	public void createInstancesWithCorrectSingleGenericParameter() {
		TypeTag tag = new TypeTag(GenericContainer.class, new TypeTag(String.class));
		TypeTag listTag = new TypeTag(List.class, new TypeTag(String.class));

		PrefabValueFactory<GenericContainer> factory = new ReflectiveJavaFxPropertyFactory<>(
				GenericContainer.class.getName(), List.class);
		Tuple<GenericContainer> tuple = factory.createValues(tag, prefabValues, null);

		assertEquals(prefabValues.giveRed(listTag), tuple.getRed().t);
		assertEquals(prefabValues.giveBlack(listTag), tuple.getBlack().t);
		assertEquals(String.class, tuple.getRed().t.get(0).getClass());
	}

	@Test
	public void createInstancesWithCorrectMultipleGenericParameter() {
		TypeTag tag = new TypeTag(GenericMultiContainer.class, new TypeTag(String.class), new TypeTag(Point.class));
		TypeTag mapTag = new TypeTag(Map.class, new TypeTag(String.class), new TypeTag(Point.class));

		PrefabValueFactory<GenericMultiContainer> factory = new ReflectiveJavaFxPropertyFactory<>(
				GenericMultiContainer.class.getName(), Map.class);
		Tuple<GenericMultiContainer> tuple = factory.createValues(tag, prefabValues, null);

		assertEquals(prefabValues.giveRed(mapTag), tuple.getRed().t);
		assertEquals(prefabValues.giveBlack(mapTag), tuple.getBlack().t);

		Map.Entry next = (Map.Entry) tuple.getRed().t.entrySet().iterator().next();
		assertEquals(String.class, next.getKey().getClass());
		assertEquals(Point.class, next.getValue().getClass());
	}

	private static final class GenericContainer<T> {
		private final List<T> t;

		@SuppressWarnings("unused")
		public GenericContainer(List<T> t) {
			this.t = t;
		}
	}

	private static final class GenericMultiContainer<K, V> {
		private final Map<K, V> t;

		@SuppressWarnings("unused")
		public GenericMultiContainer(Map<K, V> t) {
			this.t = t;
		}
	}
}
