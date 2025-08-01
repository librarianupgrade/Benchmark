/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Plugins
 *
 * Software for microscopy image analysis
 * %%
 * Copyright (C) 2011 - 2022 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.ij.foci.model;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestLogging;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;

@SuppressWarnings({ "javadoc" })
class FindFociStateMachineTest {
	private static Logger logger;

	private static class UpdateablePropertyChangeEvent extends PropertyChangeEvent {
		private static final long serialVersionUID = 1L;

		String name;

		public UpdateablePropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
			super(source, propertyName, oldValue, newValue);
		}

		@Override
		public String getPropertyName() {
			return name;
		}
	}

	@BeforeAll
	public static void beforeAll() {
		logger = Logger.getLogger(FindFociStateMachineTest.class.getName());
	}

	@AfterAll
	public static void afterAll() {
		logger = null;
	}

	/**
	 * Performs multiple state changes and outputs the time.
	 */
	@SeededTest
	void timeStateTransitions(RandomSeed seed) {
		final Level level = TestLevel.TEST_INFO;
		final int steps = logger.isLoggable(level) ? 1000000 : 100;

		final FindFociStateMachine sm = new FindFociStateMachine();
		final String[] propertyNames = sm.getObservedProperties().toArray(new String[0]);
		final UniformRandomProvider rand = RngFactory.create(seed.get());
		final Integer oldValue = Integer.valueOf(0);
		final Integer newValue = Integer.valueOf(1);

		final String[] randomNames = new String[propertyNames.length * 10];
		for (int j = 0, x = 0; j < 10; j++) {
			for (int i = 0; i < propertyNames.length; i++) {
				randomNames[x++] = propertyNames[rand.nextInt(propertyNames.length)];
			}
		}

		final UpdateablePropertyChangeEvent event = new UpdateablePropertyChangeEvent(this, "", oldValue, newValue);

		final long start = System.nanoTime();
		int c = 0;
		OUTER: for (;;) {
			for (int j = 0, x = 0; j < 10; j++) {
				for (int i = 0; i < propertyNames.length; i++) {
					event.name = randomNames[x++];
					sm.propertyChange(event);
					// sm.propertyChange(new PropertyChangeEvent(this, randomNames[x++], oldValue, newValue));
					if (++c == steps) {
						break OUTER;
					}
				}
				sm.setState(FindFociState.COMPLETE);
			}
		}

		logger.log(TestLogging.getRecord(level, "%d steps : %s", steps,
				TextUtils.nanosToString(System.nanoTime() - start)));
	}
}
