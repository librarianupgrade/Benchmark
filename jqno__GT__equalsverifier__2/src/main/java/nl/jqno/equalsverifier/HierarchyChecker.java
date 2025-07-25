/*
 * Copyright 2009-2011, 2013, 2015-2016 Jan Ouwens
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
package nl.jqno.equalsverifier;

import nl.jqno.equalsverifier.internal.ClassAccessor;
import nl.jqno.equalsverifier.internal.Formatter;
import nl.jqno.equalsverifier.internal.ObjectAccessor;
import nl.jqno.equalsverifier.internal.exceptions.ReflectionException;
import nl.jqno.equalsverifier.internal.prefabvalues.TypeTag;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static nl.jqno.equalsverifier.internal.Assert.*;

class HierarchyChecker<T> implements Checker {
	private final Configuration<T> config;
	private final Class<T> type;
	private final TypeTag typeTag;
	private final ClassAccessor<T> classAccessor;
	private final Class<? extends T> redefinedSubclass;
	private final boolean typeIsFinal;
	private final CachedHashCodeInitializer<T> cachedHashCodeInitializer;

	public HierarchyChecker(Configuration<T> config) {
		this.config = config;

		if (config.getWarningsToSuppress().contains(Warning.STRICT_INHERITANCE)
				&& config.getRedefinedSubclass() != null) {
			fail(Formatter.of("withRedefinedSubclass and weakInheritanceCheck are mutually exclusive."));
		}

		this.type = config.getType();
		this.typeTag = config.getTypeTag();
		this.classAccessor = config.createClassAccessor();
		this.redefinedSubclass = config.getRedefinedSubclass();
		this.typeIsFinal = Modifier.isFinal(type.getModifiers());
		this.cachedHashCodeInitializer = config.getCachedHashCodeInitializer();
	}

	@Override
	public void check() {
		checkSuperclass();
		checkSubclass();

		checkRedefinedSubclass();
		if (!config.getWarningsToSuppress().contains(Warning.STRICT_INHERITANCE)) {
			checkFinalEqualsMethod();
		}
	}

	private void checkSuperclass() {
		ClassAccessor<? super T> superAccessor = classAccessor.getSuperAccessor();
		if (superAccessor.isEqualsInheritedFromObject()) {
			return;
		}

		if (config.hasRedefinedSuperclass() || config.isUsingGetClass()) {
			T reference = classAccessor.getRedObject(typeTag);
			Object equalSuper = getEqualSuper(reference);

			Formatter formatter = Formatter.of(
					"Redefined superclass:\n  %%\nshould not equal superclass instance\n  %%\nbut it does.", reference,
					equalSuper);
			assertFalse(formatter, reference.equals(equalSuper) || equalSuper.equals(reference));
		} else {
			checkSuperProperties(classAccessor.getRedAccessor(typeTag));
			checkSuperProperties(classAccessor.getDefaultValuesAccessor(typeTag));
		}
	}

	private void checkSuperProperties(ObjectAccessor<T> referenceAccessor) {
		T reference = referenceAccessor.get();
		Object equalSuper = getEqualSuper(reference);

		T shallow = referenceAccessor.copy();
		ObjectAccessor.of(shallow).shallowScramble(config.getPrefabValues(), typeTag);

		Formatter symmetryFormatter = Formatter.of("Symmetry:\n  %%\ndoes not equal superclass instance\n  %%",
				reference, equalSuper);
		assertTrue(symmetryFormatter, reference.equals(equalSuper) && equalSuper.equals(reference));

		Formatter transitivityFormatter = Formatter.of(
				"Transitivity:\n  %%\nand\n  %%\nboth equal superclass instance\n  %%\nwhich implies they equal each other.",
				reference, shallow, equalSuper);
		assertTrue(transitivityFormatter,
				reference.equals(shallow) || reference.equals(equalSuper) != equalSuper.equals(shallow));

		int referenceHashCode = cachedHashCodeInitializer.getInitializedHashCode(reference);
		int equalSuperHashCode = cachedHashCodeInitializer.getInitializedHashCode(equalSuper);
		Formatter superclassFormatter = Formatter.of(
				"Superclass: hashCode for\n  %% (%%)\nshould be equal to hashCode for superclass instance\n  %% (%%)",
				reference, referenceHashCode, equalSuper, equalSuperHashCode);
		assertTrue(superclassFormatter, referenceHashCode == equalSuperHashCode);
	}

	private Object getEqualSuper(T reference) {
		return ObjectAccessor.of(reference, type.getSuperclass()).copy();
	}

	private void checkSubclass() {
		if (typeIsFinal) {
			return;
		}

		ObjectAccessor<T> referenceAccessor = classAccessor.getRedAccessor(typeTag);
		T reference = referenceAccessor.get();
		T equalSub = referenceAccessor.copyIntoAnonymousSubclass();

		if (config.isUsingGetClass()) {
			Formatter formatter = Formatter
					.of("Subclass: object is equal to an instance of a trivial subclass with equal fields:"
							+ "\n  %%\nThis should not happen when using getClass().", reference);
			assertFalse(formatter, reference.equals(equalSub));
		} else {
			Formatter formatter = Formatter.of(
					"Subclass: object is not equal to an instance of a trivial subclass with equal fields:\n  %%\nConsider making the class final.",
					reference);
			assertTrue(formatter, reference.equals(equalSub));
		}
	}

	private void checkRedefinedSubclass() {
		if (typeIsFinal || redefinedSubclass == null) {
			return;
		}

		if (methodIsFinal("equals", Object.class)) {
			fail(Formatter.of("Subclass: %% has a final equals method.\nNo need to supply a redefined subclass.",
					type.getSimpleName()));
		}

		ObjectAccessor<T> referenceAccessor = classAccessor.getRedAccessor(typeTag);
		T reference = referenceAccessor.get();
		T redefinedSub = referenceAccessor.copyIntoSubclass(redefinedSubclass);
		assertFalse(Formatter.of("Subclass:\n  %%\nequals subclass instance\n  %%", reference, redefinedSub),
				reference.equals(redefinedSub));
	}

	private void checkFinalEqualsMethod() {
		if (typeIsFinal || redefinedSubclass != null) {
			return;
		}

		boolean equalsIsFinal = methodIsFinal("equals", Object.class);
		boolean hashCodeIsFinal = methodIsFinal("hashCode");

		if (config.isUsingGetClass()) {
			assertEquals(Formatter.of("Finality: equals and hashCode must both be final or both be non-final."),
					equalsIsFinal, hashCodeIsFinal);
		} else {
			Formatter equalsFormatter = Formatter.of("Subclass: equals is not final."
					+ "\nSupply an instance of a redefined subclass using withRedefinedSubclass if equals cannot be final.");
			assertTrue(equalsFormatter, equalsIsFinal);

			Formatter hashCodeFormatter = Formatter.of("Subclass: hashCode is not final."
					+ "\nSupply an instance of a redefined subclass using withRedefinedSubclass if hashCode cannot be final.");
			assertTrue(hashCodeFormatter, hashCodeIsFinal);
		}
	}

	private boolean methodIsFinal(String methodName, Class<?>... parameterTypes) {
		try {
			Method method = type.getMethod(methodName, parameterTypes);
			return Modifier.isFinal(method.getModifiers());
		} catch (SecurityException | NoSuchMethodException e) {
			throw new ReflectionException("Should never occur: cannot find " + type.getName() + "." + methodName);
		}

	}
}
