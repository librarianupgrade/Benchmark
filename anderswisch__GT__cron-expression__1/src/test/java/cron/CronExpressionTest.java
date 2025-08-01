/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Anders Wisch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cron;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cron.DateTimes.*;
import static org.junit.Assert.*;

public class CronExpressionTest {
	public static final CronExpression.Parser withSecondsField = CronExpression.parser().withSecondsField(true);

	private CronExpression expression;

	@Test
	public void testHashCode() {
		assertEquals(CronExpression.daily().hashCode(), CronExpression.parse("@daily").hashCode());
		assertEquals(CronExpression.daily().hashCode(), CronExpression.parse("@midnight").hashCode());
		assertEquals(CronExpression.hourly().hashCode(), CronExpression.parse("@hourly").hashCode());
		assertEquals(CronExpression.monthly().hashCode(), CronExpression.parse("@monthly").hashCode());
		assertEquals(CronExpression.weekly().hashCode(), CronExpression.parse("@weekly").hashCode());
		assertEquals(CronExpression.yearly().hashCode(), CronExpression.parse("@annually").hashCode());
		assertEquals(CronExpression.yearly().hashCode(), CronExpression.parse("@yearly").hashCode());
		assertEquals(CronExpression.parse("0 0 ? * 5#3,2#2").hashCode(),
				CronExpression.parse("0 0 ? * 5#3,2#2").hashCode());
		assertEquals(withSecondsField.parse("0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010").hashCode(),
				withSecondsField.parse("0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010").hashCode());
	}

	@Test
	public void testEquals() {
		assertEquals(CronExpression.daily(), CronExpression.parse("@daily"));
		assertEquals(CronExpression.daily(), CronExpression.parse("@midnight"));
		assertEquals(CronExpression.hourly(), CronExpression.parse("@hourly"));
		assertEquals(CronExpression.monthly(), CronExpression.parse("@monthly"));
		assertEquals(CronExpression.weekly(), CronExpression.parse("@weekly"));
		assertEquals(CronExpression.yearly(), CronExpression.parse("@annually"));
		assertEquals(CronExpression.yearly(), CronExpression.parse("@yearly"));
		assertEquals(CronExpression.parse("0 0 ? * 5#3,2#2"), CronExpression.parse("0 0 ? * 5#3,2#2"));
		assertEquals(withSecondsField.parse("0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010"),
				withSecondsField.parse("0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010"));
	}

	@Test
	public void illegalCharacter() {
		try {
			expression = CronExpression.parse("0 0 4X * *");
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Bad character 'X' at position 5 in string: 0 0 4X * *", e.getMessage());
		}
	}

	@Test
	public void disallowBothDayFields() {
		try {
			expression = CronExpression.parser().allowBothDayFields(false).parse("0 0 1 * 5L");
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Day of month and day of week may not both be specified", e.getMessage());
		}
	}

	@Test
	public void nearestWeekdayWithoutNumber() {
		try {
			expression = CronExpression.parse("0 0 W * *");
		} catch (IllegalArgumentException e) {
			assertEquals("Bad character 'W' in day of month field: W", e.getMessage());
		}
	}

	@Test
	public void nearestWeekdayOfMonth() {
		expression = CronExpression.parse("0 0 5W * *");
		List<DateTime> times = new ArrayList<>();
		DateTime t = DateTimes.startOfYear();
		int year = t.getYear();
		do {
			times.add(nearestWeekday(t.withDayOfMonth(5)));
			t = t.plusMonths(1);
		} while (year == t.getYear());
		assertMatchesAll(times);
	}

	@Test
	public void nearestFriday() {
		DateTime t = now().withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.SATURDAY);
		expression = CronExpression.parse("0 0 " + t.getDayOfMonth() + "W * *");
		assertMatches(t.minusDays(1));
	}

	@Test
	public void nearestMonday() {
		DateTime t = now().withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.SUNDAY);
		expression = CronExpression.parse("0 0 " + t.getDayOfMonth() + "W * *");
		assertMatches(t.plusDays(1));
	}

	@Test
	public void nonMatchingNth() {
		expression = CronExpression.parse("0 0 ? * 2#2");
		List<DateTime> times = new ArrayList<>();
		DateTime t = new DateTime().withDayOfYear(1).withTimeAtStartOfDay();
		int year = t.getYear();
		while (t.getYear() == year) {
			times.add(nthOfMonth(t, DateTimeConstants.TUESDAY, 1));
			t = t.plusMonths(1);
		}
		for (DateTime time : times)
			assertFalse(expression.matches(time));
	}

	@Test
	public void multipleNth() {
		expression = CronExpression.parse("0 0 ? * 5#3,2#2");
		List<DateTime> times = new ArrayList<>();
		DateTime t = new DateTime().withDayOfYear(1).withTimeAtStartOfDay();
		int year = t.getYear();
		while (t.getYear() == year) {
			times.add(nthOfMonth(t, DateTimeConstants.FRIDAY, 3));
			times.add(nthOfMonth(t, DateTimeConstants.TUESDAY, 2));
			t = t.plusMonths(1);
		}
		assertMatchesAll(times);
	}

	@Test
	public void thirdFriday() {
		String string = "0 0 ? * 5#3";
		List<DateTime> times = new ArrayList<>();
		DateTime t = new DateTime().withDayOfYear(1).withTimeAtStartOfDay();
		int year = t.getYear();
		while (t.getYear() == year) {
			times.add(nthOfMonth(t, DateTimeConstants.FRIDAY, 3));
			t = t.plusMonths(1);
		}
		expression = CronExpression.parse(string);
		assertMatchesAll(times);
	}

	@Test
	public void reboot() {
		expression = CronExpression.parse("@reboot");
		DateTime now = now();
		assertTrue(expression.matches(now));
		assertFalse(expression.matches(now));
	}

	@Test
	public void minuteFullRangeExplicit() {
		expression = CronExpression.parse("0-59 * * * * *");
		DateTime time = startOfHour();
		int hour = time.getHourOfDay();
		do {
			assertMatches(time);
			time = time.plusMinutes(1);
		} while (time.getHourOfDay() == hour);
	}

	@Test
	public void minuteRestrictedRange() {
		expression = CronExpression.parse("10-20 * * * * *");
		int first = 10, last = 20;
		DateTime time = startOfHour();
		int hour = time.getHourOfDay();
		do {
			int minute = time.getMinuteOfHour();
			assertEquals(first <= minute && minute <= last, expression.matches(time));
			time = time.plusMinutes(1);
		} while (time.getHourOfDay() == hour);
	}

	@Test
	public void minuteFullRangeMod() {
		expression = CronExpression.parse("*/5 * * * * *");
		DateTime time = startOfHour();
		int hour = time.getHourOfDay();
		do {
			int minute = time.getMinuteOfHour();
			assertEquals(minute % 5 == 0, expression.matches(time));
			time = time.plusMinutes(1);
		} while (time.getHourOfDay() == hour);
	}

	@Test
	public void minuteRestrictedRangeMod() {
		expression = CronExpression.parse("10-20/5 * * * * *");
		int first = 10, last = 20;
		DateTime time = startOfHour();
		int hour = time.getHourOfDay();
		do {
			int minute = time.getMinuteOfHour();
			assertEquals(first <= minute && minute <= last && minute % 5 == 0, expression.matches(time));
			time = time.plusMinutes(1);
		} while (time.getHourOfDay() == hour);
	}

	@Test
	public void yearly() {
		expression = CronExpression.yearly();
		assertYearly();
	}

	@Test
	public void monthly() {
		expression = CronExpression.monthly();
		assertMonthly();
	}

	@Test
	public void weekly() {
		expression = CronExpression.weekly();
		assertWeekly();
	}

	@Test
	public void daily() {
		expression = CronExpression.daily();
		assertDaily();
	}

	@Test
	public void hourly() {
		expression = CronExpression.hourly();
		assertHourly();
	}

	@Test
	public void yearlyKeyword() {
		expression = CronExpression.parse("@yearly");
		assertYearly();
	}

	@Test
	public void annualKeyword() {
		expression = CronExpression.parse("@annually");
		assertYearly();
	}

	@Test
	public void monthlyKeyword() {
		expression = CronExpression.parse("@monthly");
		assertMonthly();
	}

	@Test
	public void weeklyKeyword() {
		expression = CronExpression.parse("@weekly");
		assertWeekly();
	}

	@Test
	public void dailyKeyword() {
		expression = CronExpression.parse("@daily");
		assertDaily();
	}

	@Test
	public void hourlyKeyword() {
		expression = CronExpression.parse("@hourly");
		assertHourly();
	}

	@Test
	public void invalid() {
		assertFalse(CronExpression.isValid(null));
		assertFalse(CronExpression.isValid(""));
		assertFalse(CronExpression.isValid("a"));
		assertFalse(CronExpression.isValid("0 0 1 * X"));
		assertFalse(CronExpression.isValid("0 0 1 * 1X"));
	}

	@Test
	public void invalidDueToSecondsField() {
		assertTrue(CronExpression.isValid("0 0 1 * 1"));
		assertFalse(CronExpression.parser().allowBothDayFields(false).isValid("0 0 1 * 1"));
	}

	private void assertWeekly() {
		for (int week = 1; week <= 52; week++) {
			assertMatches(midnight().withWeekOfWeekyear(week).withDayOfWeek(1).minusDays(1));
		}
	}

	private void assertDaily() {
		for (int day = 1; day <= 365; day++) {
			assertMatches(midnight().withDayOfYear(day));
		}
	}

	private void assertMonthly() {
		for (int month = DateTimeConstants.JANUARY; month <= DateTimeConstants.DECEMBER; month++) {
			assertMatches(midnight().withMonthOfYear(month).withDayOfMonth(1));
		}
	}

	private void assertHourly() {
		for (int day = 1; day <= 365; day++) {
			for (int hour = 0; hour <= 23; hour++) {
				try {
					assertMatches(midnight().withDayOfYear(day).withHourOfDay(hour));
				} catch (IllegalFieldValueException ignore) {
					// Ignore exceptions caused by daylight savings time transition
				}
			}
		}
	}

	private void assertYearly() {
		assertMatches(midnight().withDayOfYear(1));
	}

	private static final String formatString = "m H d M E yyyy";
	private static final DateTimeFormatter jodaFormat = DateTimeFormat.forPattern(formatString);

	private void assertMatchesAll(List<DateTime> times) {
		for (DateTime time : times)
			assertMatches(time);
	}

	private void assertMatches(DateTime time) {
		assertTrue(jodaFormat.print(time).toUpperCase() + " doesn't match expression: " + expression,
				expression.matches(time));
	}
}
