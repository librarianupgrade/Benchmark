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
import emul.java.time.chrono.ISOChrono;
import emul.java.time.format.DateTimeParseException;
import emul.java.time.jdk8.DefaultInterfaceDateTimeAccessor;
import emul.java.time.jdk8.Jdk7Methods;
import emul.java.time.jdk8.Jdk8Methods;
import emul.java.time.zone.ZoneRules;

import java.io.Serializable;

import static emul.java.time.LocalTime.SECONDS_PER_DAY;
import static emul.java.time.temporal.ChronoField.EPOCH_DAY;
import static emul.java.time.temporal.ChronoField.OFFSET_SECONDS;

/**
 * A date with an offset from UTC/Greenwich in the ISO-8601 calendar system, such as {@code 2007-12-03+01:00}.
 * <p>
 * {@code OffsetDate} is an immutable date-time object that represents a date, often viewed as
 * year-month-day-offset. This object can also access other date fields such as day-of-year, day-of-week and
 * week-of-year.
 * <p>
 * This class does not store or represent a time. For example, the value "2nd October 2007 +02:00" can be
 * stored in an {@code OffsetDate}.
 * 
 * <h4>Implementation notes</h4> This class is immutable and thread-safe.
 */
public final class OffsetDate extends DefaultInterfaceDateTimeAccessor
		implements DateTime, DateTime.WithAdjuster, Comparable<OffsetDate>, Serializable {

	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = -4382054179074397774L;

	/**
	 * The local date.
	 */
	private final LocalDate date;

	/**
	 * The offset from UTC/Greenwich.
	 */
	private final ZoneOffset offset;

	// -----------------------------------------------------------------------
	/**
	 * Obtains the current date from the system clock in the default time-zone.
	 * <p>
	 * This will query the {@link Clock#systemDefaultZone() system clock} in the default time-zone to obtain the
	 * current date. The offset will be calculated from the time-zone in the clock.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current date using the system clock, not null
	 */
	public static OffsetDate now() {

		return now(Clock.systemDefaultZone());
	}

	/**
	 * Obtains the current date from the system clock in the specified time-zone.
	 * <p>
	 * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current date. Specifying the
	 * time-zone avoids dependence on the default time-zone. The offset will be calculated from the specified
	 * time-zone.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current date using the system clock, not null
	 */
	public static OffsetDate now(ZoneId zone) {

		return now(Clock.system(zone));
	}

	/**
	 * Obtains the current date from the specified clock.
	 * <p>
	 * This will query the specified clock to obtain the current date - today. The offset will be calculated
	 * from the time-zone in the clock.
	 * <p>
	 * Using this method allows the use of an alternate clock for testing. The alternate clock may be introduced
	 * using {@link Clock dependency injection}.
	 * 
	 * @param clock the clock to use, not null
	 * @return the current date, not null
	 */
	public static OffsetDate now(Clock clock) {

		Jdk7Methods.Objects_requireNonNull(clock, "clock");
		final Instant now = clock.instant(); // called once
		return ofInstant(now, clock.getZone().getRules().getOffset(now));
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code OffsetDate} from a year, month and day.
	 * <p>
	 * The day must be valid for the year and month, otherwise an exception will be thrown.
	 * 
	 * @param year the year to represent, from MIN_YEAR to MAX_YEAR
	 * @param month the month-of-year to represent, not null
	 * @param dayOfMonth the day-of-month to represent, from 1 to 31
	 * @param offset the zone offset, not null
	 * @return the offset date, not null
	 * @throws DateTimeException if the value of any field is out of range
	 * @throws DateTimeException if the day-of-month is invalid for the month-year
	 */
	public static OffsetDate of(int year, Month month, int dayOfMonth, ZoneOffset offset) {

		LocalDate date = LocalDate.of(year, month, dayOfMonth);
		return new OffsetDate(date, offset);
	}

	/**
	 * Obtains an instance of {@code OffsetDate} from a year, month and day.
	 * <p>
	 * The day must be valid for the year and month, otherwise an exception will be thrown.
	 * 
	 * @param year the year to represent, from MIN_YEAR to MAX_YEAR
	 * @param month the month-of-year to represent, from 1 (January) to 12 (December)
	 * @param dayOfMonth the day-of-month to represent, from 1 to 31
	 * @param offset the zone offset, not null
	 * @return the offset date, not null
	 * @throws DateTimeException if the value of any field is out of range
	 * @throws DateTimeException if the day-of-month is invalid for the month-year
	 */
	public static OffsetDate of(int year, int month, int dayOfMonth, ZoneOffset offset) {

		LocalDate date = LocalDate.of(year, month, dayOfMonth);
		return new OffsetDate(date, offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code OffsetDate} from a local date and an offset.
	 * 
	 * @param date the local date, not null
	 * @param offset the zone offset, not null
	 * @return the offset date, not null
	 */
	public static OffsetDate of(LocalDate date, ZoneOffset offset) {

		return new OffsetDate(date, offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code OffsetDate} from an {@code Instant} and zone ID.
	 * <p>
	 * This creates an offset date with the same instant as midnight at the start of day of the instant
	 * specified. Finding the offset from UTC/Greenwich is simple as there is only one valid offset for each
	 * instant.
	 * 
	 * @param instant the instant to create the time from, not null
	 * @param zone the time-zone, which may be an offset, not null
	 * @return the offset time, not null
	 */
	public static OffsetDate ofInstant(Instant instant, ZoneId zone) {

		Jdk7Methods.Objects_requireNonNull(instant, "instant");
		Jdk7Methods.Objects_requireNonNull(zone, "zone");
		ZoneRules rules = zone.getRules();
		ZoneOffset offset = rules.getOffset(instant);
		long epochSec = instant.getEpochSecond() + offset.getTotalSeconds(); // overflow caught later
		long epochDay = Jdk8Methods.floorDiv(epochSec, SECONDS_PER_DAY);
		LocalDate date = LocalDate.ofEpochDay(epochDay);
		return new OffsetDate(date, offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code OffsetDate} from a date-time object.
	 * <p>
	 * A {@code DateTimeAccessor} represents some form of date and time information. This factory converts the
	 * arbitrary date-time object to an instance of {@code OffsetDate}.
	 * 
	 * @param dateTime the date-time object to convert, not null
	 * @return the offset date, not null
	 * @throws DateTimeException if unable to convert to an {@code OffsetDate}
	 */
	public static OffsetDate from(DateTimeAccessor dateTime) {

		if (dateTime instanceof OffsetDate) {
			return (OffsetDate) dateTime;
		}
		LocalDate date = LocalDate.from(dateTime);
		ZoneOffset offset = ZoneOffset.from(dateTime);
		return new OffsetDate(date, offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code OffsetDate} from a text string such as {@code 2007-12-03+01:00}.
	 * <p>
	 * The string must represent a valid date and is parsed using
	 * {@link format.DateTimeFormatters#isoOffsetDate()}.
	 * 
	 * @param text the text to parse such as "2007-12-03+01:00", not null
	 * @return the parsed offset date, not null
	 * @throws DateTimeParseException if the text cannot be parsed
	 */
	public static OffsetDate parse(CharSequence text) {

		int length = text.length();
		// "yyyy-MM-ddZ".length() == 11, "yyyy-MM-ddXXXXX".length() == 15
		Throwable cause = null;
		try {
			if ((length >= 11) && (length <= 20)) {
				int zoneStartIndex = 10;
				while (!OffsetTime.isZoneStartCharacter(text.charAt(zoneStartIndex))) {
					zoneStartIndex++;
					if (zoneStartIndex >= length) {
						zoneStartIndex = -1;
						break;
					}
				}
				if (zoneStartIndex > 0) {
					LocalDate localDate = LocalDate.parse(text.subSequence(0, zoneStartIndex));
					ZoneOffset zoneOffset = ZoneOffset.of(text.subSequence(zoneStartIndex, length).toString());
					return new OffsetDate(localDate, zoneOffset);
				}
			}
		} catch (RuntimeException e) {
			cause = e;
		}
		throw new DateTimeParseException("Expected format yyyy-MM-ddXXXXX", text, 0, cause);
	}

	// -----------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param date the local date, not null
	 * @param offset the zone offset, not null
	 */
	private OffsetDate(LocalDate date, ZoneOffset offset) {

		this.date = Jdk7Methods.Objects_requireNonNull(date, "date");
		this.offset = Jdk7Methods.Objects_requireNonNull(offset, "offset");
	}

	/**
	 * Returns a new date based on this one, returning {@code this} where possible.
	 * 
	 * @param date the date to create with, not null
	 * @param offset the zone offset to create with, not null
	 */
	private OffsetDate with(LocalDate date, ZoneOffset offset) {

		if (this.date == date && this.offset.equals(offset)) {
			return this;
		}
		return new OffsetDate(date, offset);
	}

	// -----------------------------------------------------------------------
	@Override
	public boolean isSupported(DateTimeField field) {

		if (field instanceof ChronoField) {
			return ((ChronoField) field).isDateField() || field == OFFSET_SECONDS;
		}
		return field != null && field.doIsSupported(this);
	}

	@Override
	public DateTimeValueRange range(DateTimeField field) {

		if (field instanceof ChronoField) {
			if (field == OFFSET_SECONDS) {
				return field.range();
			}
			return this.date.range(field);
		}
		return field.doRange(this);
	}

	@Override
	public long getLong(DateTimeField field) {

		if (field instanceof ChronoField) {
			if (field == OFFSET_SECONDS) {
				return getOffset().getTotalSeconds();
			}
			return this.date.getLong(field);
		}
		return field.doGet(this);
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the zone offset, such as '+01:00'.
	 * <p>
	 * This is the offset of the local date from UTC/Greenwich.
	 * 
	 * @return the zone offset, not null
	 */
	public ZoneOffset getOffset() {

		return this.offset;
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified offset.
	 * <p>
	 * This method returns an object with the same {@code LocalDate} and the specified {@code ZoneOffset}. No
	 * calculation is needed or performed. For example, if this time represents {@code 2007-12-03+02:00} and the
	 * offset specified is {@code +03:00}, then this method will return {@code 2007-12-03+03:00}.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param offset the zone offset to change to, not null
	 * @return an {@code OffsetDate} based on this date with the requested offset, not null
	 */
	public OffsetDate withOffset(ZoneOffset offset) {

		Jdk7Methods.Objects_requireNonNull(offset, "offset");
		return with(this.date, offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the {@code LocalDate} part of this date-time.
	 * <p>
	 * This returns a {@code LocalDate} with the same year, month and day as this date-time.
	 * 
	 * @return the date part of this date-time, not null
	 */
	public LocalDate getDate() {

		return this.date;
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the year field.
	 * <p>
	 * This method returns the primitive {@code int} value for the year.
	 * <p>
	 * The year returned by this method is proleptic as per {@code get(YEAR)}. To obtain the year-of-era, use
	 * {@code get(YEAR_OF_ERA}.
	 * 
	 * @return the year, from MIN_YEAR to MAX_YEAR
	 */
	public int getYear() {

		return this.date.getYear();
	}

	/**
	 * Gets the month-of-year field from 1 to 12.
	 * <p>
	 * This method returns the month as an {@code int} from 1 to 12. Application code is frequently clearer if
	 * the enum {@link Month} is used by calling {@link #getMonth()}.
	 * 
	 * @return the month-of-year, from 1 to 12
	 * @see #getMonth()
	 */
	public int getMonthValue() {

		return this.date.getMonthValue();
	}

	/**
	 * Gets the month-of-year field using the {@code Month} enum.
	 * <p>
	 * This method returns the enum {@link Month} for the month. This avoids confusion as to what {@code int}
	 * values mean. If you need access to the primitive {@code int} value then the enum provides the
	 * {@link Month#getValue() int value}.
	 * 
	 * @return the month-of-year, not null
	 * @see #getMonthValue()
	 */
	public Month getMonth() {

		return this.date.getMonth();
	}

	/**
	 * Gets the day-of-month field.
	 * <p>
	 * This method returns the primitive {@code int} value for the day-of-month.
	 * 
	 * @return the day-of-month, from 1 to 31
	 */
	public int getDayOfMonth() {

		return this.date.getDayOfMonth();
	}

	/**
	 * Gets the day-of-year field.
	 * <p>
	 * This method returns the primitive {@code int} value for the day-of-year.
	 * 
	 * @return the day-of-year, from 1 to 365, or 366 in a leap year
	 */
	public int getDayOfYear() {

		return this.date.getDayOfYear();
	}

	/**
	 * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
	 * <p>
	 * This method returns the enum {@link DayOfWeek} for the day-of-week. This avoids confusion as to what
	 * {@code int} values mean. If you need access to the primitive {@code int} value then the enum provides the
	 * {@link DayOfWeek#getValue() int value}.
	 * <p>
	 * Additional information can be obtained from the {@code DayOfWeek}. This includes textual names of the
	 * values.
	 * 
	 * @return the day-of-week, not null
	 */
	public DayOfWeek getDayOfWeek() {

		return this.date.getDayOfWeek();
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns an adjusted date based on this date.
	 * <p>
	 * This adjusts the date according to the rules of the specified adjuster. A simple adjuster might simply
	 * set the one of the fields, such as the year field. A more complex adjuster might set the date to the last
	 * day of the month. A selection of common adjustments is provided in {@link DateTimeAdjusters}. These
	 * include finding the "last day of the month" and "next Wednesday". The adjuster is responsible for
	 * handling special cases, such as the varying lengths of month and leap years.
	 * <p>
	 * In addition, all principal classes implement the {@link WithAdjuster} interface, including this one. For
	 * example, {@link Month} implements the adjuster interface. As such, this code will compile and run:
	 * 
	 * <pre>
	 *  date.with(Month.JULY);
	 * </pre>
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return an {@code OffsetDate} based on this date with the adjustment made, not null
	 * @throws DateTimeException if the adjustment cannot be made
	 */
	@Override
	public OffsetDate with(WithAdjuster adjuster) {

		if (adjuster instanceof LocalDate) {
			return with((LocalDate) adjuster, this.offset);
		} else if (adjuster instanceof ZoneOffset) {
			return with(this.date, (ZoneOffset) adjuster);
		} else if (adjuster instanceof OffsetDate) {
			return (OffsetDate) adjuster;
		}
		return (OffsetDate) adjuster.doWithAdjustment(this);
	}

	/**
	 * Returns a copy of this date with the specified field altered.
	 * <p>
	 * This method returns a new date based on this date with a new value for the specified field. This can be
	 * used to change any field, for example to set the year, month of day-of-month. The offset is not part of
	 * the calculation and will be unchanged in the result.
	 * <p>
	 * In some cases, changing the specified field can cause the resulting date to become invalid, such as
	 * changing the month from January to February would make the day-of-month 31 invalid. In cases like this,
	 * the field is responsible for resolving the date. Typically it will choose the previous valid date, which
	 * would be the last valid day of February in this example.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param field the field to set in the result, not null
	 * @param newValue the new value of the field in the result
	 * @return an {@code OffsetDate} based on this date with the specified field set, not null
	 * @throws DateTimeException if the value is invalid
	 */
	@Override
	public OffsetDate with(DateTimeField field, long newValue) {

		if (field instanceof ChronoField) {
			if (field == OFFSET_SECONDS) {
				ChronoField f = (ChronoField) field;
				return with(this.date, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
			}
			return with(this.date.with(field, newValue), this.offset);
		}
		return field.doWith(this, newValue);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code OffsetDate} with the year altered. The offset does not affect the
	 * calculation and will be the same in the result. If the day-of-month is invalid for the year, it will be
	 * changed to the last valid day of the month.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param year the year to set in the result, from MIN_YEAR to MAX_YEAR
	 * @return an {@code OffsetDate} based on this date with the requested year, not null
	 * @throws DateTimeException if the year value is invalid
	 */
	public OffsetDate withYear(int year) {

		return with(this.date.withYear(year), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the month-of-year altered. The offset does not affect the
	 * calculation and will be the same in the result. If the day-of-month is invalid for the year, it will be
	 * changed to the last valid day of the month.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param month the month-of-year to set in the result, from 1 (January) to 12 (December)
	 * @return an {@code OffsetDate} based on this date with the requested month, not null
	 * @throws DateTimeException if the month-of-year value is invalid
	 */
	public OffsetDate withMonth(int month) {

		return with(this.date.withMonth(month), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the day-of-month altered. If the resulting date is
	 * invalid, an exception is thrown. The offset does not affect the calculation and will be the same in the
	 * result.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param dayOfMonth the day-of-month to set in the result, from 1 to 28-31
	 * @return an {@code OffsetDate} based on this date with the requested day, not null
	 * @throws DateTimeException if the day-of-month value is invalid
	 * @throws DateTimeException if the day-of-month is invalid for the month-year
	 */
	public OffsetDate withDayOfMonth(int dayOfMonth) {

		return with(this.date.withDayOfMonth(dayOfMonth), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the day-of-year altered. If the resulting date is invalid,
	 * an exception is thrown.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param dayOfYear the day-of-year to set in the result, from 1 to 365-366
	 * @return an {@code OffsetDate} based on this date with the requested day, not null
	 * @throws DateTimeException if the day-of-year value is invalid
	 * @throws DateTimeException if the day-of-year is invalid for the year
	 */
	public OffsetDate withDayOfYear(int dayOfYear) {

		return with(this.date.withDayOfYear(dayOfYear), this.offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this date with the specified period added.
	 * <p>
	 * This method returns a new date based on this date with the specified period added. The adjuster is
	 * typically {@link Period} but may be any other type implementing the
	 * {@link PlusAdjuster} interface. The calculation is delegated to the
	 * specified adjuster, which typically calls back to {@link #plus(long, PeriodUnit)}. The offset is not part
	 * of the calculation and will be unchanged in the result.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return an {@code OffsetDate} based on this date with the addition made, not null
	 * @throws DateTimeException if the addition cannot be made
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	@Override
	public OffsetDate plus(PlusAdjuster adjuster) {

		return (OffsetDate) adjuster.doPlusAdjustment(this);
	}

	/**
	 * Returns a copy of this date with the specified period added.
	 * <p>
	 * This method returns a new date based on this date with the specified period added. This can be used to
	 * add any period that is defined by a unit, for example to add years, months or days. The unit is
	 * responsible for the details of the calculation, including the resolution of any edge cases in the
	 * calculation. The offset is not part of the calculation and will be unchanged in the result.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param amountToAdd the amount of the unit to add to the result, may be negative
	 * @param unit the unit of the period to add, not null
	 * @return an {@code OffsetDate} based on this date with the specified period added, not null
	 * @throws DateTimeException if the unit cannot be added to this type
	 */
	@Override
	public OffsetDate plus(long amountToAdd, PeriodUnit unit) {

		if (unit instanceof ChronoUnit) {
			return with(this.date.plus(amountToAdd, unit), this.offset);
		}
		return unit.doPlus(this, amountToAdd);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in years added.
	 * <p>
	 * This method adds the specified amount to the years field in three steps:
	 * <ol>
	 * <li>Add the input years to the year field</li>
	 * <li>Check if the resulting date would be invalid</li>
	 * <li>Adjust the day-of-month to the last valid day if necessary</li>
	 * </ol>
	 * <p>
	 * For example, 2008-02-29 (leap year) plus one year would result in the invalid date 2009-02-29 (standard
	 * year). Instead of returning an invalid result, the last valid day of the month, 2009-02-28, is selected
	 * instead.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param years the years to add, may be negative
	 * @return an {@code OffsetDate} based on this date with the years added, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate plusYears(long years) {

		return with(this.date.plusYears(years), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in months added.
	 * <p>
	 * This method adds the specified amount to the months field in three steps:
	 * <ol>
	 * <li>Add the input months to the month-of-year field</li>
	 * <li>Check if the resulting date would be invalid</li>
	 * <li>Adjust the day-of-month to the last valid day if necessary</li>
	 * </ol>
	 * <p>
	 * For example, 2007-03-31 plus one month would result in the invalid date 2007-04-31. Instead of returning
	 * an invalid result, the last valid day of the month, 2007-04-30, is selected instead.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param months the months to add, may be negative
	 * @return an {@code OffsetDate} based on this date with the months added, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate plusMonths(long months) {

		return with(this.date.plusMonths(months), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in weeks added.
	 * <p>
	 * This method adds the specified amount in weeks to the days field incrementing the month and year fields
	 * as necessary to ensure the result remains valid. The result is only invalid if the maximum/minimum year
	 * is exceeded.
	 * <p>
	 * For example, 2008-12-31 plus one week would result in 2009-01-07.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param weeks the weeks to add, may be negative
	 * @return an {@code OffsetDate} based on this date with the weeks added, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate plusWeeks(long weeks) {

		return with(this.date.plusWeeks(weeks), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in days added.
	 * <p>
	 * This method adds the specified amount to the days field incrementing the month and year fields as
	 * necessary to ensure the result remains valid. The result is only invalid if the maximum/minimum year is
	 * exceeded.
	 * <p>
	 * For example, 2008-12-31 plus one day would result in 2009-01-01.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param days the days to add, may be negative
	 * @return an {@code OffsetDate} based on this date with the days added, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate plusDays(long days) {

		return with(this.date.plusDays(days), this.offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this date with the specified period subtracted.
	 * <p>
	 * This method returns a new date based on this date with the specified period subtracted. The adjuster is
	 * typically {@link Period} but may be any other type implementing the
	 * {@link MinusAdjuster} interface. The calculation is delegated to the
	 * specified adjuster, which typically calls back to {@link #minus(long, PeriodUnit)}. The offset is not
	 * part of the calculation and will be unchanged in the result.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return an {@code OffsetDate} based on this date with the subtraction made, not null
	 * @throws DateTimeException if the subtraction cannot be made
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	@Override
	public OffsetDate minus(MinusAdjuster adjuster) {

		return (OffsetDate) adjuster.doMinusAdjustment(this);
	}

	/**
	 * Returns a copy of this date with the specified period subtracted.
	 * <p>
	 * This method returns a new date based on this date with the specified period subtracted. This can be used
	 * to subtract any period that is defined by a unit, for example to subtract years, months or days. The unit
	 * is responsible for the details of the calculation, including the resolution of any edge cases in the
	 * calculation. The offset is not part of the calculation and will be unchanged in the result.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param amountToSubtract the amount of the unit to subtract from the result, may be negative
	 * @param unit the unit of the period to subtract, not null
	 * @return an {@code OffsetDate} based on this date with the specified period subtracted, not null
	 * @throws DateTimeException if the unit cannot be added to this type
	 */
	@Override
	public OffsetDate minus(long amountToSubtract, PeriodUnit unit) {

		return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
				: plus(-amountToSubtract, unit));
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in years subtracted.
	 * <p>
	 * This method subtracts the specified amount from the years field in three steps:
	 * <ol>
	 * <li>Subtract the input years to the year field</li>
	 * <li>Check if the resulting date would be invalid</li>
	 * <li>Adjust the day-of-month to the last valid day if necessary</li>
	 * </ol>
	 * <p>
	 * For example, 2008-02-29 (leap year) minus one year would result in the invalid date 2007-02-29 (standard
	 * year). Instead of returning an invalid result, the last valid day of the month, 2007-02-28, is selected
	 * instead.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param years the years to subtract, may be negative
	 * @return an {@code OffsetDate} based on this date with the years subtracted, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate minusYears(long years) {

		return with(this.date.minusYears(years), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in months subtracted.
	 * <p>
	 * This method subtracts the specified amount from the months field in three steps:
	 * <ol>
	 * <li>Subtract the input months to the month-of-year field</li>
	 * <li>Check if the resulting date would be invalid</li>
	 * <li>Adjust the day-of-month to the last valid day if necessary</li>
	 * </ol>
	 * <p>
	 * For example, 2007-03-31 minus one month would result in the invalid date 2007-02-31. Instead of returning
	 * an invalid result, the last valid day of the month, 2007-02-28, is selected instead.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param months the months to subtract, may be negative
	 * @return an {@code OffsetDate} based on this date with the months subtracted, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate minusMonths(long months) {

		return with(this.date.minusMonths(months), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified period in weeks subtracted.
	 * <p>
	 * This method subtracts the specified amount in weeks from the days field decrementing the month and year
	 * fields as necessary to ensure the result remains valid. The result is only invalid if the maximum/minimum
	 * year is exceeded.
	 * <p>
	 * For example, 2009-01-07 minus one week would result in 2008-12-31.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param weeks the weeks to subtract, may be negative
	 * @return an {@code OffsetDate} based on this date with the weeks subtracted, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate minusWeeks(long weeks) {

		return with(this.date.minusWeeks(weeks), this.offset);
	}

	/**
	 * Returns a copy of this {@code OffsetDate} with the specified number of days subtracted.
	 * <p>
	 * This method subtracts the specified amount from the days field decrementing the month and year fields as
	 * necessary to ensure the result remains valid. The result is only invalid if the maximum/minimum year is
	 * exceeded.
	 * <p>
	 * For example, 2009-01-01 minus one day would result in 2008-12-31.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param days the days to subtract, may be negative
	 * @return an {@code OffsetDate} based on this date with the days subtracted, not null
	 * @throws DateTimeException if the result exceeds the supported date range
	 */
	public OffsetDate minusDays(long days) {

		return with(this.date.minusDays(days), this.offset);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns an offset date-time formed from this date at the specified time.
	 * <p>
	 * This merges the two objects - {@code this} and the specified time - to form an instance of
	 * {@code OffsetDateTime}.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param time the time to combine with, not null
	 * @return the offset date-time formed from this date and the specified time, not null
	 */
	public OffsetDateTime atTime(LocalTime time) {

		return OffsetDateTime.of(this.date, time, this.offset);
	}

	// -----------------------------------------------------------------------
	@Override
	public DateTime doWithAdjustment(DateTime dateTime) {

		return dateTime.with(OFFSET_SECONDS, getOffset().getTotalSeconds()).with(EPOCH_DAY, getDate().toEpochDay());
	}

	@Override
	public long periodUntil(DateTime endDateTime, PeriodUnit unit) {

		if (endDateTime instanceof OffsetDate == false) {
			throw new DateTimeException("Unable to calculate period between objects of two different types");
		}
		if (unit instanceof ChronoUnit) {
			OffsetDate end = (OffsetDate) endDateTime;
			long offsetDiff = end.offset.getTotalSeconds() - this.offset.getTotalSeconds();
			LocalDate endLocal = end.date.plusDays(Jdk8Methods.floorDiv(-offsetDiff, SECONDS_PER_DAY));
			return this.date.periodUntil(endLocal, unit);
		}
		return unit.between(this, endDateTime).getAmount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R query(DateTimeAccessor.Query<R> query) {

		if (query == DateTimeAccessor.Query.CHRONO) {
			return (R) ISOChrono.INSTANCE;
		} else if (query == DateTimeAccessor.Query.OFFSET) {
			return (R) getOffset();
		}
		return super.query(query);
	}

	// -----------------------------------------------------------------------
	/**
	 * Converts this date to midnight at the start of day in epoch seconds.
	 * 
	 * @return the epoch seconds value
	 */
	private long toEpochSecond() {

		long epochDay = this.date.toEpochDay();
		long secs = epochDay * SECONDS_PER_DAY;
		return secs - this.offset.getTotalSeconds();
	}

	// -----------------------------------------------------------------------
	/**
	 * Compares this {@code OffsetDate} to another date.
	 * <p>
	 * The comparison is based first on the UTC equivalent instant, then on the local date. It is
	 * "consistent with equals", as defined by {@link Comparable}.
	 * <p>
	 * For example, the following is the comparator order:
	 * <ol>
	 * <li>2008-06-29-11:00</li>
	 * <li>2008-06-29-12:00</li>
	 * <li>2008-06-30+12:00</li>
	 * <li>2008-06-29-13:00</li>
	 * </ol>
	 * Values #2 and #3 represent the same instant on the time-line. When two values represent the same instant,
	 * the local date is compared to distinguish them. This step is needed to make the ordering consistent with
	 * {@code equals()}.
	 * <p>
	 * To compare the underlying local date of two {@code DateTimeAccessor} instances, use
	 * {@link ChronoField#EPOCH_DAY} as a comparator.
	 * 
	 * @param other the other date to compare to, not null
	 * @return the comparator value, negative if less, positive if greater
	 */
	@Override
	public int compareTo(OffsetDate other) {

		if (this.offset.equals(other.offset)) {
			return this.date.compareTo(other.date);
		}
		// int compare = Long.compare(toEpochSecond(), other.toEpochSecond());
		long x = toEpochSecond();
		long y = other.toEpochSecond();
		int compare = (x < y) ? -1 : ((x == y) ? 0 : 1);
		if (compare == 0) {
			compare = this.date.compareTo(other.date);
		}
		return compare;
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if the instant of midnight at the start of this {@code OffsetDate} is after midnight at the start
	 * of the specified date.
	 * <p>
	 * This method differs from the comparison in {@link #compareTo} in that it only compares the instant of the
	 * date. This is equivalent to using {@code date1.toEpochSecond().isAfter(date2.toEpochSecond())}.
	 * 
	 * @param other the other date to compare to, not null
	 * @return true if this is after the instant of the specified date
	 */
	public boolean isAfter(OffsetDate other) {

		return toEpochSecond() > other.toEpochSecond();
	}

	/**
	 * Checks if the instant of midnight at the start of this {@code OffsetDate} is before midnight at the start
	 * of the specified date.
	 * <p>
	 * This method differs from the comparison in {@link #compareTo} in that it only compares the instant of the
	 * date. This is equivalent to using {@code date1.toEpochSecond().isBefore(date2.toEpochSecond())}.
	 * 
	 * @param other the other date to compare to, not null
	 * @return true if this is before the instant of the specified date
	 */
	public boolean isBefore(OffsetDate other) {

		return toEpochSecond() < other.toEpochSecond();
	}

	/**
	 * Checks if the instant of midnight at the start of this {@code OffsetDate} equals midnight at the start of
	 * the specified date.
	 * <p>
	 * This method differs from the comparison in {@link #compareTo} and {@link #equals} in that it only
	 * compares the instant of the date. This is equivalent to using
	 * {@code date1.toEpochSecond().equals(date2.toEpochSecond())}.
	 * 
	 * @param other the other date to compare to, not null
	 * @return true if the instant equals the instant of the specified date
	 */
	public boolean isEqual(OffsetDate other) {

		return toEpochSecond() == other.toEpochSecond();
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if this date is equal to another date.
	 * <p>
	 * The comparison is based on the local-date and the offset. To compare for the same instant on the
	 * time-line, use {@link #isEqual(OffsetDate)}.
	 * <p>
	 * Only objects of type {@code OffsetDate} are compared, other types return false. To compare the underlying
	 * local date of two {@code DateTimeAccessor} instances, use {@link ChronoField#EPOCH_DAY} as a comparator.
	 * 
	 * @param obj the object to check, null returns false
	 * @return true if this is equal to the other date
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj instanceof OffsetDate) {
			OffsetDate other = (OffsetDate) obj;
			return this.date.equals(other.date) && this.offset.equals(other.offset);
		}
		return false;
	}

	/**
	 * A hash code for this date.
	 * 
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {

		return this.date.hashCode() ^ this.offset.hashCode();
	}

	// -----------------------------------------------------------------------
	/**
	 * Outputs this date as a {@code String}, such as {@code 2007-12-03+01:00}.
	 * <p>
	 * The output will be in the ISO-8601 format {@code yyyy-MM-ddXXXXX}.
	 * 
	 * @return a string representation of this date, not null
	 */
	@Override
	public String toString() {

		return this.date.toString() + this.offset.toString();
	}

}
