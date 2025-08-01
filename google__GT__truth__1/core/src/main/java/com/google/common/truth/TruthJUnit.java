/*
 * Copyright (c) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import com.google.common.annotations.GwtIncompatible;
import org.junit.AssumptionViolatedException;

/**
 * Provides a way to use Truth to perform JUnit "assumptions." An assumption is a check that, if
 * false, aborts (skips) the test. This is especially useful in JUnit theories, parameterized tests,
 * or other combinatorial tests where some subset of the combinations are simply not applicable for
 * testing.
 *
 * <p>For example:
 *
 * <pre>{@code
 * import static com.google.common.truth.Truth.assertThat;
 * import static com.google.common.truth.TruthJUnit.assume;
 *
 * public void @Test testFoosAgainstBars {
 *   assume().that(foo).isNotNull();
 *   assume().that(bar).isNotNull();
 *   assertThat(foo.times(bar)).isEqualTo(blah);
 * }
 * }</pre>
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtIncompatible("JUnit4")
@J2ktIncompatible
public final class TruthJUnit {
	@SuppressWarnings("ConstantCaseForConstants") // Despite the "Builder" name, it's not mutable.
	private static final StandardSubjectBuilder ASSUME = StandardSubjectBuilder.forCustomFailureStrategy(failure -> {
		AssumptionViolatedException assumptionViolated = new AssumptionViolatedException(failure.getMessage(),
				failure.getCause());
		assumptionViolated.setStackTrace(failure.getStackTrace());
		throw assumptionViolated;
	});

	/**
	 * Begins a call chain with the fluent Truth API. If the check made by the chain fails, it will
	 * throw {@link AssumptionViolatedException}.
	 */
	public static final StandardSubjectBuilder assume() {
		return ASSUME;
	}

	private TruthJUnit() {
	}
}
