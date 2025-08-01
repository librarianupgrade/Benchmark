/*
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package emul.java.time;

import emul.java.time.temporal.*;
import emul.java.time.chrono.Chrono;
import emul.java.time.chrono.ISOChrono;
import emul.java.time.format.DateTimeParseException;
import emul.java.time.jdk8.DefaultInterfaceDateTimeAccessor;
import emul.java.time.jdk8.Jdk7Methods;

import java.io.Serializable;

/**
 * A month-day in the ISO-8601 calendar system, such as {@code --12-03}.
 * <p>
 * {@code MonthDay} is an immutable date-time object that represents the combination of a year and month. Any
 * field that can be derived from a month and day, such as quarter-of-year, can be obtained.
 * <p>
 * This class does not store or represent a year, time or time-zone. For example, the value "December 3rd" can
 * be stored in a {@code MonthDay}.
 * <p>
 * Since a {@code MonthDay} does not possess a year, the leap day of February 29th is considered valid.
 * <p>
 * This class implements {@link DateTimeAccessor} rather than {@link DateTime}. This is because it is not
 * possible to define whether February 29th is valid or not without external information, preventing the
 * implementation of plus/minus. Related to this, {@code MonthDay} only provides access to query and set the
 * fields {@code MONTH_OF_YEAR} and {@code DAY_OF_MONTH}.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today in most of the world. It is
 * equivalent to the proleptic Gregorian calendar system, in which todays's rules for leap years are applied
 * for all time. For most applications written today, the ISO-8601 rules are entirely suitable. Any
 * application that uses historical dates should consider using {@code HistoricDate}.
 * 
 * <h4>Implementation notes</h4> This class is immutable and thread-safe.
 */
public final class MonthDay extends DefaultInterfaceDateTimeAccessor
		implements DateTimeAccessor, DateTime.WithAdjuster, Comparable<MonthDay>, Serializable {

	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = -939150713474957432L;

	/**
	 * The month-of-year, not null.
	 */
	private final int month;

	/**
	 * The day-of-month.
	 */
	private final int day;

	// -----------------------------------------------------------------------
	/**
	 * Obtains the current month-day from the system clock in the default time-zone.
	 * <p>
	 * This will query the {@link Clock#systemDefaultZone() system clock} in the default time-zone to obtain the
	 * current month-day.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current month-day using the system clock and default time-zone, not null
	 */
	public static MonthDay now() {

		return now(Clock.systemDefaultZone());
	}

	/**
	 * Obtains the current month-day from the system clock in the specified time-zone.
	 * <p>
	 * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current month-day. Specifying
	 * the time-zone avoids dependence on the default time-zone.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current month-day using the system clock, not null
	 */
	public static MonthDay now(ZoneId zone) {

		return now(Clock.system(zone));
	}

	/**
	 * Obtains the current month-day from the specified clock.
	 * <p>
	 * This will query the specified clock to obtain the current month-day. Using this method allows the use of
	 * an alternate clock for testing. The alternate clock may be introduced using {@link Clock dependency
	 * injection}.
	 * 
	 * @param clock the clock to use, not null
	 * @return the current month-day, not null
	 */
	public static MonthDay now(Clock clock) {

		final LocalDate now = LocalDate.now(clock); // called once
		return of(now.getMonth(), now.getDayOfMonth());
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code MonthDay}.
	 * <p>
	 * The day-of-month must be valid for the month within a leap year. Hence, for February, day 29 is valid.
	 * <p>
	 * For example, passing in April and day 31 will throw an exception, as there can never be April 31st in any
	 * year. By contrast, passing in February 29th is permitted, as that month-day can sometimes be valid.
	 * 
	 * @param month the month-of-year to represent, not null
	 * @param dayOfMonth the day-of-month to represent, from 1 to 31
	 * @return the month-day, not null
	 * @throws DateTimeException if the value of any field is out of range
	 * @throws DateTimeException if the day-of-month is invalid for the month
	 */
	public static MonthDay of(Month month, int dayOfMonth) {

		Jdk7Methods.Objects_requireNonNull(month, "month");
		ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
		if (dayOfMonth > month.maxLength()) {
			throw new DateTimeException("Illegal value for DayOfMonth field, value " + dayOfMonth
					+ " is not valid for month " + month.name());
		}
		return new MonthDay(month.getValue(), dayOfMonth);
	}

	/**
	 * Obtains an instance of {@code MonthDay}.
	 * <p>
	 * The day-of-month must be valid for the month within a leap year. Hence, for month 2 (February), day 29 is
	 * valid.
	 * <p>
	 * For example, passing in month 4 (April) and day 31 will throw an exception, as there can never be April
	 * 31st in any year. By contrast, passing in February 29th is permitted, as that month-day can sometimes be
	 * valid.
	 * 
	 * @param month the month-of-year to represent, from 1 (January) to 12 (December)
	 * @param dayOfMonth the day-of-month to represent, from 1 to 31
	 * @return the month-day, not null
	 * @throws DateTimeException if the value of any field is out of range
	 * @throws DateTimeException if the day-of-month is invalid for the month
	 */
	public static MonthDay of(int month, int dayOfMonth) {

		return of(Month.of(month), dayOfMonth);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code MonthDay} from a date-time object.
	 * <p>
	 * A {@code DateTimeAccessor} represents some form of date and time information. This factory converts the
	 * arbitrary date-time object to an instance of {@code MonthDay}.
	 * <p>
	 * The conversion extracts the {@link ChronoField#MONTH_OF_YEAR month-of-year} and
	 * {@link ChronoField#DAY_OF_MONTH day-of-month} fields. The extraction is only permitted if the date-time
	 * has an ISO chronology.
	 * 
	 * @param dateTime the date-time object to convert, not null
	 * @return the month-day, not null
	 * @throws DateTimeException if unable to convert to a {@code MonthDay}
	 */
	public static MonthDay from(DateTimeAccessor dateTime) {

		if (dateTime instanceof MonthDay) {
			return (MonthDay) dateTime;
		}
		if (ISOChrono.INSTANCE.equals(Chrono.from(dateTime)) == false) {
			dateTime = LocalDate.from(dateTime);
		}
		return of(dateTime.get(ChronoField.MONTH_OF_YEAR), dateTime.get(ChronoField.DAY_OF_MONTH));
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code MonthDay} from a text string such as {@code --12-03}.
	 * <p>
	 * The string must represent a valid month-day. The format is {@code --MM-dd}.
	 * 
	 * @param text the text to parse such as "--12-03", not null
	 * @return the parsed month-day, not null
	 * @throws DateTimeParseException if the text cannot be parsed
	 */
	public static MonthDay parse(CharSequence text) {

		int length = text.length();
		int errorIndex = 0;
		Throwable cause = null;
		try {
			// "--MM-dd".length() == 7
			if ((length == 7) && (text.charAt(0) == '-') && (text.charAt(1) == '-') && (text.charAt(4) == '-')) {
				errorIndex = 2;
				String monthString = text.subSequence(2, 4).toString();
				int month = Integer.parseInt(monthString);
				errorIndex = 5;
				String dayString = text.subSequence(5, 7).toString();
				int day = Integer.parseInt(dayString);
				return of(month, day);
			}
		} catch (RuntimeException e) {
			cause = e;
		}
		throw new DateTimeParseException("Expected format --MM-dd", text, errorIndex, cause);
	}

	// -----------------------------------------------------------------------
	/**
	 * Constructor, previously validated.
	 * 
	 * @param month the month-of-year to represent, validated from 1 to 12
	 * @param dayOfMonth the day-of-month to represent, validated from 1 to 29-31
	 */
	private MonthDay(int month, int dayOfMonth) {

		this.month = month;
		this.day = dayOfMonth;
	}

	// -----------------------------------------------------------------------
	@Override
	public boolean isSupported(DateTimeField field) {

		if (field instanceof ChronoField) {
			return field == ChronoField.MONTH_OF_YEAR || field == ChronoField.DAY_OF_MONTH;
		}
		return field != null && field.doIsSupported(this);
	}

	@Override
	public DateTimeValueRange range(DateTimeField field) {

		if (field == ChronoField.MONTH_OF_YEAR) {
			return field.range();
		} else if (field == ChronoField.DAY_OF_MONTH) {
			return DateTimeValueRange.of(1, getMonth().minLength(), getMonth().maxLength());
		}
		return super.range(field);
	}

	@Override
	public long getLong(DateTimeField field) {

		if (field instanceof ChronoField) {
			switch ((ChronoField) field) {
			// alignedDOW and alignedWOM not supported because they cannot be set in with()
			case DAY_OF_MONTH:
				return this.day;
			case MONTH_OF_YEAR:
				return this.month;
			}
			throw new DateTimeException("Unsupported field: " + field.getName());
		}
		return field.doGet(this);
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the month-of-year field using the {@code Month} enum.
	 * <p>
	 * This method returns the enum {@link Month} for the month. This avoids confusion as to what {@code int}
	 * values mean. If you need access to the primitive {@code int} value then the enum provides the
	 * {@link Month#getValue() int value}.
	 * 
	 * @return the month-of-year, not null
	 */
	public Month getMonth() {

		return Month.of(this.month);
	}

	/**
	 * Gets the day-of-month field.
	 * <p>
	 * This method returns the primitive {@code int} value for the day-of-month.
	 * 
	 * @return the day-of-month, from 1 to 31
	 */
	public int getDayOfMonth() {

		return this.day;
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code MonthDay} with the month-of-year altered.
	 * <p>
	 * This returns a month-day with the specified month. If the day-of-month is invalid for the specified
	 * month, the day will be adjusted to the last valid day-of-month.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param month the month-of-year to set in the returned month-day, from 1 (January) to 12 (December)
	 * @return a {@code MonthDay} based on this month-day with the requested month, not null
	 * @throws DateTimeException if the month-of-year value is invalid
	 */
	public MonthDay withMonth(int month) {

		return with(Month.of(month));
	}

	/**
	 * Returns a copy of this {@code MonthDay} with the month-of-year altered.
	 * <p>
	 * This returns a month-day with the specified month. If the day-of-month is invalid for the specified
	 * month, the day will be adjusted to the last valid day-of-month.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param month the month-of-year to set in the returned month-day, not null
	 * @return a {@code MonthDay} based on this month-day with the requested month, not null
	 */
	public MonthDay with(Month month) {

		Jdk7Methods.Objects_requireNonNull(month, "month");
		if (month.getValue() == this.month) {
			return this;
		}
		int day = Math.min(this.day, month.maxLength());
		return new MonthDay(month.getValue(), day);
	}

	/**
	 * Returns a copy of this {@code MonthDay} with the day-of-month altered.
	 * <p>
	 * This returns a month-day with the specified day-of-month. If the day-of-month is invalid for the month,
	 * an exception is thrown
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param dayOfMonth the day-of-month to set in the return month-day, from 1 to 31
	 * @return a {@code MonthDay} based on this month-day with the requested day, not null
	 * @throws DateTimeException if the day-of-month value is invalid
	 * @throws DateTimeException if the day-of-month is invalid for the month
	 */
	public MonthDay withDayOfMonth(int dayOfMonth) {

		if (dayOfMonth == this.day) {
			return this;
		}
		return of(this.month, dayOfMonth);
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if the year is valid for this month-day.
	 * <p>
	 * This method checks whether this month and day and the input year form a valid date. This can only return
	 * false for February 29th.
	 * 
	 * @param year the year to validate, an out of range value returns false
	 * @return true if the year is valid for this month-day
	 * @see Year#isValidMonthDay(MonthDay)
	 */
	public boolean isValidYear(int year) {

		return (this.day == 29 && this.month == 2 && Year.isLeap(year) == false) == false;
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a date formed from this month-day at the specified year.
	 * <p>
	 * This method merges {@code this} and the specified year to form an instance of {@code LocalDate}.
	 * 
	 * <pre>
	 * LocalDate date = monthDay.atYear(year);
	 * </pre>
	 * A month-day of February 29th will be adjusted to February 28th in the resulting date if the year is not a
	 * leap year.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param year the year to use, from MIN_YEAR to MAX_YEAR
	 * @return the local date formed from this month-day and the specified year, not null
	 * @see Year#atMonthDay(MonthDay)
	 */
	public LocalDate atYear(int year) {

		return LocalDate.of(year, this.month, isValidYear(year) ? this.day : 28);
	}

	// -----------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Override
	public <R> R query(Query<R> query) {

		if (query == Query.CHRONO) {
			return (R) ISOChrono.INSTANCE;
		}
		return super.query(query);
	}

	/**
	 * Implementation of the strategy to make an adjustment to the specified date-time object.
	 * <p>
	 * This method is not intended to be called by application code directly. Applications should use the
	 * {@code with(WithAdjuster)} method on the date-time object to make the adjustment passing this as the
	 * argument.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * <h4>Implementation notes</h4>
	 * Adjusts the specified date-time to have the value of this month-day. The date-time object must use the
	 * ISO calendar system. The adjustment is equivalent to using {@link DateTime#with(DateTimeField, long)}
	 * twice passing {@code MONTH_OF_YEAR} and {@code DAY_OF_MONTH} as the fields.
	 * 
	 * @param dateTime the target object to be adjusted, not null
	 * @return the adjusted object, not null
	 */
	@Override
	public DateTime doWithAdjustment(DateTime dateTime) {

		if (Chrono.from(dateTime).equals(ISOChrono.INSTANCE) == false) {
			throw new DateTimeException("Adjustment only supported on ISO date-time");
		}
		dateTime = dateTime.with(ChronoField.MONTH_OF_YEAR, this.month);
		return dateTime.with(ChronoField.DAY_OF_MONTH,
				Math.min(dateTime.range(ChronoField.DAY_OF_MONTH).getMaximum(), this.day));
	}

	// -----------------------------------------------------------------------
	/**
	 * Compares this month-day to another month-day.
	 * <p>
	 * The comparison is based first on value of the month, then on the value of the day. It is
	 * "consistent with equals", as defined by {@link Comparable}.
	 * 
	 * @param other the other month-day to compare to, not null
	 * @return the comparator value, negative if less, positive if greater
	 */
	@Override
	public int compareTo(MonthDay other) {

		int cmp = (this.month - other.month);
		if (cmp == 0) {
			cmp = (this.day - other.day);
		}
		return cmp;
	}

	/**
	 * Is this month-day after the specified month-day.
	 * 
	 * @param other the other month-day to compare to, not null
	 * @return true if this is after the specified month-day
	 */
	public boolean isAfter(MonthDay other) {

		return compareTo(other) > 0;
	}

	/**
	 * Is this month-day before the specified month-day.
	 * 
	 * @param other the other month-day to compare to, not null
	 * @return true if this point is before the specified month-day
	 */
	public boolean isBefore(MonthDay other) {

		return compareTo(other) < 0;
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if this month-day is equal to another month-day.
	 * <p>
	 * The comparison is based on the time-line position of the month-day within a year.
	 * 
	 * @param obj the object to check, null returns false
	 * @return true if this is equal to the other month-day
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj instanceof MonthDay) {
			MonthDay other = (MonthDay) obj;
			return this.month == other.month && this.day == other.day;
		}
		return false;
	}

	/**
	 * A hash code for this month-day.
	 * 
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {

		return (this.month << 6) + this.day;
	}

	// -----------------------------------------------------------------------
	/**
	 * Outputs this month-day as a {@code String}, such as {@code --12-03}.
	 * <p>
	 * The output will be in the format {@code --MM-dd}:
	 * 
	 * @return a string representation of this month-day, not null
	 */
	@Override
	public String toString() {

		return new StringBuilder(10).append("--").append(this.month < 10 ? "0" : "").append(this.month)
				.append(this.day < 10 ? "-0" : "-").append(this.day).toString();
	}

}
