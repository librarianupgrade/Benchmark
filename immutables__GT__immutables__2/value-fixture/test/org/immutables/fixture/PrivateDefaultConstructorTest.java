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
package org.immutables.fixture;

import org.junit.jupiter.api.Test;
import static org.immutables.check.Checkers.check;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrivateDefaultConstructorTest {

	@Test
	public void testNominal() throws Exception {
		int modifiers = ImmutablePrivateNoargConstructorNominal.class.getDeclaredConstructor().getModifiers();
		check(Modifier.isPrivate(modifiers));
	}

	@Test
	public void testProtectedNoargConstructor() throws Exception {
		int modifiers = ImmutableProtectedNoargConstructorNominal.class.getDeclaredConstructor().getModifiers();
		check(Modifier.isProtected(modifiers));
	}

	@SuppressWarnings("CheckReturnValue")
	@Test
	public void testOverridePrehash() {
		assertThrows(NoSuchMethodException.class,
				() -> ImmutablePrivateNoargConstructorOverridePrehash.class.getDeclaredMethod("computeHashCode"));
	}

	@Test
	public void testDoesNotOverridePrehashWhenOff() throws Exception {
		ImmutablePrivateNoargConstructorOptionFalseDoNotAffectPrehash.class.getDeclaredMethod("computeHashCode");
	}

	@Test
	public void testOverridenBySingleton() throws Exception {
		ImmutablePrivateNoargConstructorIsOverriddenBySingleton singleton = ImmutablePrivateNoargConstructorIsOverriddenBySingleton
				.of();
		Field test = singleton.getClass().getDeclaredField("test");
		test.setAccessible(true);
		assertEquals(singleton.test(), test.get(singleton));
	}
}
