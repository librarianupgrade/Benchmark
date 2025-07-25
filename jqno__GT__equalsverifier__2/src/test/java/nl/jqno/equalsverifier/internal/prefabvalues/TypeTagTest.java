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
package nl.jqno.equalsverifier.internal.prefabvalues;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TypeTagTest {
	private static final TypeTag SOME_LONG_TYPETAG = new TypeTag(Map.class, new TypeTag(Integer.class),
			new TypeTag(List.class, new TypeTag(String.class)));

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void equalsAndHashCode() {
		EqualsVerifier.forClass(TypeTag.class)
				.withPrefabValues(TypeTag.class, new TypeTag(Integer.class), SOME_LONG_TYPETAG)
				.suppress(Warning.NULL_FIELDS).verify();
	}

	@Test
	public void typeCannotBeNull() {
		thrown.expect(NullPointerException.class);
		new TypeTag(null);
	}

	@Test
	public void getType() {
		assertEquals(Map.class, SOME_LONG_TYPETAG.getType());
	}

	@Test
	public void getGenericTypes() {
		List<TypeTag> expected = Arrays.asList(new TypeTag(Integer.class),
				new TypeTag(List.class, new TypeTag(String.class)));
		assertEquals(expected, SOME_LONG_TYPETAG.getGenericTypes());
	}

	@Test
	public void testToString() {
		assertEquals("String", new TypeTag(String.class).toString());
		assertEquals("List<String>", new TypeTag(List.class, new TypeTag(String.class)).toString());
		assertEquals("Map<Integer, List<String>>", SOME_LONG_TYPETAG.toString());
	}

	@Test
	public void matchParameterizedField() throws Exception {
		Field enclosingField = ContainerContainer.class.getDeclaredField("stringContainer");
		TypeTag enclosingType = TypeTag.of(enclosingField, TypeTag.NULL);

		Field f = Container.class.getDeclaredField("t");
		TypeTag actual = TypeTag.of(f, enclosingType);

		assertEquals(new TypeTag(String.class), actual);
	}

	@Test
	public void matchParameterizedGenericField() throws Exception {
		Field enclosingField = ContainerContainer.class.getDeclaredField("stringContainer");
		TypeTag enclosingType = TypeTag.of(enclosingField, TypeTag.NULL);

		Field f = Container.class.getDeclaredField("ts");
		TypeTag actual = TypeTag.of(f, enclosingType);

		assertEquals(new TypeTag(List.class, new TypeTag(String.class)), actual);
	}

	@Test
	public void matchParameterizedArrayField() throws Exception {
		Field enclosingField = ContainerContainer.class.getDeclaredField("stringContainer");
		TypeTag enclosingType = TypeTag.of(enclosingField, TypeTag.NULL);

		Field f = Container.class.getDeclaredField("tarr");
		TypeTag actual = TypeTag.of(f, enclosingType);

		assertEquals(new TypeTag(String[].class), actual);
	}

	@Test
	public void matchNestedParameterizedGenericField() throws Exception {
		Field enclosingField = ContainerContainer.class.getDeclaredField("stringContainer");
		TypeTag enclosingType = TypeTag.of(enclosingField, TypeTag.NULL);

		Field f = Container.class.getDeclaredField("tss");
		TypeTag actual = TypeTag.of(f, enclosingType);

		assertEquals(new TypeTag(List.class, new TypeTag(List.class, new TypeTag(String.class))), actual);
	}

	@SuppressWarnings("unused")
	static class ContainerContainer {
		Container<String> stringContainer;
	}

	@SuppressWarnings("unused")
	static class Container<T> {
		T t;
		List<T> ts;
		T[] tarr;
		List<List<T>> tss;
	}
}
