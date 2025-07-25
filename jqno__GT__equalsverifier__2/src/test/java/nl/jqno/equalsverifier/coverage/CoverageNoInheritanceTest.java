/*
 * Copyright 2013 Jan Ouwens
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
package nl.jqno.equalsverifier.coverage;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.testhelpers.types.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CoverageNoInheritanceTest {
	private final Class<?> type;

	public CoverageNoInheritanceTest(Class<?> type) {
		this.type = type;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { EclipseGetClassPoint.class }, { EclipseInstanceOfPoint.class },
				{ HandwrittenGetClassPoint.class }, { HandwrittenInstanceOfPoint.class },
				{ IntelliJGetClassPoint.class }, { IntelliJInstanceOfPoint.class }, { LombokInstanceOfPoint.class },
				{ NetBeansGetClassPoint.class } });
	}

	@Test
	public void testCoverage() {
		EqualsVerifier.forClass(type).verify();
	}

	@Test
	public void callTheConstructor() throws Exception {
		Constructor<?> constructor = type.getConstructor(int.class, int.class, Color.class);
		constructor.newInstance(0, 0, Color.INDIGO);
	}
}
