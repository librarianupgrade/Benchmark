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
import emul.java.time.chrono.ChronoLocalDateTime;
import emul.java.time.chrono.ChronoZonedDateTime;
import emul.java.time.format.DateTimeParseException;
import emul.java.time.jdk8.DefaultInterfaceDateTimeAccessor;
import emul.java.time.jdk8.Jdk7Methods;

import java.io.Serializable;

/**
 * A time without time-zone in the ISO-8601 calendar system, such as {@code 10:15:30}.
 * <p>
 * {@code LocalTime} is an immutable date-time object that represents a time, often viewed as
 * hour-minute-second.
 * <p>
 * This class stores all time fields, to a precision of nanoseconds. It does not store or represent a date or
 * time-zone. For example, the value "13:45.30.123456789" can be stored in a {@code LocalTime}.
 * 
 * <h4>Implementation notes</h4> This class is immutable and thread-safe.
 */
public final class LocalTime extends DefaultInterfaceDateTimeAccessor
		implements DateTime, DateTime.WithAdjuster, Comparable<LocalTime>, Serializable {

	private static final int[] NANO_FACTORS = new int[] { 1000000000, 100000000, 10000000, 1000000, 100000, 10000, 1000,
			100, 10, 1 };

	/**
	 * Constant for the local time of midnight, 00:00.
	 */
	public static final LocalTime MIN;

	/**
	 * Constant for the local time just before midnight, 23:59:59.999999999.
	 */
	public static final LocalTime MAX;

	/**
	 * Constant for the local time of midnight, 00:00.
	 */
	public static final LocalTime MIDNIGHT;

	/**
	 * Constant for the local time of noon, 12:00.
	 */
	public static final LocalTime NOON;

	/**
	 * Constants for the local time of each hour.
	 */
	private static final LocalTime[] HOURS = new LocalTime[24];
	static {
		for (int i = 0; i < HOURS.length; i++) {
			HOURS[i] = new LocalTime(i, 0, 0, 0);
		}
		MIDNIGHT = HOURS[0];
		NOON = HOURS[12];
		MIN = HOURS[0];
		MAX = new LocalTime(23, 59, 59, 999999999);
	}

	/**
	 * Hours per minute.
	 */
	static final int HOURS_PER_DAY = 24;

	/**
	 * Minutes per hour.
	 */
	static final int MINUTES_PER_HOUR = 60;

	/**
	 * Minutes per day.
	 */
	static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

	/**
	 * Seconds per minute.
	 */
	static final int SECONDS_PER_MINUTE = 60;

	/**
	 * Seconds per hour.
	 */
	static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	/**
	 * Seconds per day.
	 */
	static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

	/**
	 * Milliseconds per day.
	 */
	static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L;

	/**
	 * Microseconds per day.
	 */
	static final long MICROS_PER_DAY = SECONDS_PER_DAY * 1000000L;

	/**
	 * Nanos per second.
	 */
	static final long NANOS_PER_SECOND = 1000000000L;

	/**
	 * Nanos per minute.
	 */
	static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;

	/**
	 * Nanos per hour.
	 */
	static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR;

	/**
	 * Nanos per day.
	 */
	static final long NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY;

	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = 6414437269572265201L;

	/**
	 * The hour.
	 */
	private final byte hour;

	/**
	 * The minute.
	 */
	private final byte minute;

	/**
	 * The second.
	 */
	private final byte second;

	/**
	 * The nanosecond.
	 */
	private final int nano;

	// -----------------------------------------------------------------------
	/**
	 * Obtains the current time from the system clock in the default time-zone.
	 * <p>
	 * This will query the {@link Clock#systemDefaultZone() system clock} in the default time-zone to obtain the
	 * current time.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current time using the system clock and default time-zone, not null
	 */
	public static LocalTime now() {

		return now(Clock.systemDefaultZone());
	}

	/**
	 * Obtains the current time from the system clock in the specified time-zone.
	 * <p>
	 * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current time. Specifying the
	 * time-zone avoids dependence on the default time-zone.
	 * <p>
	 * Using this method will prevent the ability to use an alternate clock for testing because the clock is
	 * hard-coded.
	 * 
	 * @return the current time using the system clock, not null
	 */
	public static LocalTime now(ZoneId zone) {

		return now(Clock.system(zone));
	}

	/**
	 * Obtains the current time from the specified clock.
	 * <p>
	 * This will query the specified clock to obtain the current time. Using this method allows the use of an
	 * alternate clock for testing. The alternate clock may be introduced using {@link Clock dependency
	 * injection}.
	 * 
	 * @param clock the clock to use, not null
	 * @return the current time, not null
	 */
	public static LocalTime now(Clock clock) {

		Jdk7Methods.Objects_requireNonNull(clock, "clock");
		// inline OffsetTime factory to avoid creating object and InstantProvider checks
		final Instant now = clock.instant(); // called once
		ZoneOffset offset = clock.getZone().getRules().getOffset(now);
		long secsOfDay = now.getEpochSecond() % SECONDS_PER_DAY;
		secsOfDay = (secsOfDay + offset.getTotalSeconds()) % SECONDS_PER_DAY;
		if (secsOfDay < 0) {
			secsOfDay += SECONDS_PER_DAY;
		}
		return LocalTime.ofSecondOfDay(secsOfDay, now.getNano());
	}

	// ------------------------get-----------------------------------------------
	/**
	 * Obtains an instance of {@code LocalTime} from an hour and minute.
	 * <p>
	 * The second and nanosecond fields will be set to zero by this factory method.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param hour the hour-of-day to represent, from 0 to 23
	 * @param minute the minute-of-hour to represent, from 0 to 59
	 * @return the local time, not null
	 * @throws DateTimeException if the value of any field is out of range
	 */
	public static LocalTime of(int hour, int minute) {

		ChronoField.HOUR_OF_DAY.checkValidValue(hour);
		if (minute == 0) {
			return HOURS[hour]; // for performance
		}
		ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
		return new LocalTime(hour, minute, 0, 0);
	}

	/**
	 * Obtains an instance of {@code LocalTime} from an hour, minute and second.
	 * <p>
	 * The nanosecond field will be set to zero by this factory method.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param hour the hour-of-day to represent, from 0 to 23
	 * @param minute the minute-of-hour to represent, from 0 to 59
	 * @param second the second-of-minute to represent, from 0 to 59
	 * @return the local time, not null
	 * @throws DateTimeException if the value of any field is out of range
	 */
	public static LocalTime of(int hour, int minute, int second) {

		ChronoField.HOUR_OF_DAY.checkValidValue(hour);
		if ((minute | second) == 0) {
			return HOURS[hour]; // for performance
		}
		ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
		ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
		return new LocalTime(hour, minute, second, 0);
	}

	/**
	 * Obtains an instance of {@code LocalTime} from an hour, minute, second and nanosecond.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param hour the hour-of-day to represent, from 0 to 23
	 * @param minute the minute-of-hour to represent, from 0 to 59
	 * @param second the second-of-minute to represent, from 0 to 59
	 * @param nanoOfSecond the nano-of-second to represent, from 0 to 999,999,999
	 * @return the local time, not null
	 * @throws DateTimeException if the value of any field is out of range
	 */
	public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {

		ChronoField.HOUR_OF_DAY.checkValidValue(hour);
		ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
		ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
		ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
		return create(hour, minute, second, nanoOfSecond);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code LocalTime} from a second-of-day value.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param secondOfDay the second-of-day, from {@code 0} to {@code 24 * 60 * 60 - 1}
	 * @return the local time, not null
	 * @throws DateTimeException if the second-of-day value is invalid
	 */
	public static LocalTime ofSecondOfDay(long secondOfDay) {

		ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
		int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
		secondOfDay -= hours * SECONDS_PER_HOUR;
		int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
		secondOfDay -= minutes * SECONDS_PER_MINUTE;
		return create(hours, minutes, (int) secondOfDay, 0);
	}

	/**
	 * Obtains an instance of {@code LocalTime} from a second-of-day value, with associated nanos of second.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param secondOfDay the second-of-day, from {@code 0} to {@code 24 * 60 * 60 - 1}
	 * @param nanoOfSecond the nano-of-second, from 0 to 999,999,999
	 * @return the local time, not null
	 * @throws DateTimeException if the either input value is invalid
	 */
	public static LocalTime ofSecondOfDay(long secondOfDay, int nanoOfSecond) {

		ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
		ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
		int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
		secondOfDay -= hours * SECONDS_PER_HOUR;
		int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
		secondOfDay -= minutes * SECONDS_PER_MINUTE;
		return create(hours, minutes, (int) secondOfDay, nanoOfSecond);
	}

	/**
	 * Obtains an instance of {@code LocalTime} from a nanos-of-day value.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param nanoOfDay the nano of day, from {@code 0} to {@code 24 * 60 * 60 * 1,000,000,000 - 1}
	 * @return the local time, not null
	 * @throws DateTimeException if the nanos of day value is invalid
	 */
	public static LocalTime ofNanoOfDay(long nanoOfDay) {

		ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay);
		int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
		nanoOfDay -= hours * NANOS_PER_HOUR;
		int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
		nanoOfDay -= minutes * NANOS_PER_MINUTE;
		int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
		nanoOfDay -= seconds * NANOS_PER_SECOND;
		return create(hours, minutes, seconds, (int) nanoOfDay);
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code LocalTime} from a date-time object.
	 * <p>
	 * A {@code DateTimeAccessor} represents some form of date and time information. This factory converts the
	 * arbitrary date-time object to an instance of {@code LocalTime}.
	 * <p>
	 * The conversion extracts the {@link ChronoField#NANO_OF_DAY nano-of-day} field.
	 * 
	 * @param dateTime the date-time object to convert, not null
	 * @return the local time, not null
	 * @throws DateTimeException if unable to convert to a {@code LocalTime}
	 */
	public static LocalTime from(DateTimeAccessor dateTime) {

		if (dateTime instanceof LocalTime) {
			return (LocalTime) dateTime;
		} else if (dateTime instanceof ChronoLocalDateTime) {
			return ((ChronoLocalDateTime<?>) dateTime).getTime();
		} else if (dateTime instanceof ZonedDateTime) {
			return ((ChronoZonedDateTime<?>) dateTime).getTime();
		}
		// handle builder as a special case
		if (dateTime instanceof DateTimeBuilder) {
			DateTimeBuilder builder = (DateTimeBuilder) dateTime;
			LocalTime time = builder.extract(LocalTime.class);
			if (time != null) {
				return time;
			}
		}
		return ofNanoOfDay(dateTime.getLong(ChronoField.NANO_OF_DAY));
	}

	// -----------------------------------------------------------------------
	/**
	 * Obtains an instance of {@code LocalTime} from a text string such as {@code 10:15}.
	 * <p>
	 * The string must represent a valid time and is parsed using
	 * {@link format.DateTimeFormatters#isoLocalTime()}.
	 * 
	 * @param text the text to parse such as "10:15:30", not null
	 * @return the parsed local time, not null
	 * @throws DateTimeParseException if the text cannot be parsed
	 */
	public static LocalTime parse(CharSequence text) {

		int length = text.length();
		int errorIndex = 0;
		Throwable cause = null;
		try {
			// "HH:mm".length() == 5, "HH:mm:ss".length() == 8, "HH:mm:ss.SSS".length() == 12,
			// "HH:mm:ss.SSSSSS".length() == 15, "HH:mm:ss.SSSSSSSSS".length()==18
			if (((length == 5) || ((length >= 8) && (length <= 18))) && (text.charAt(2) == ':')) {
				String hourString = text.subSequence(0, 2).toString();
				int hour = Integer.parseInt(hourString);
				errorIndex = 3;
				String minuteString = text.subSequence(3, 5).toString();
				int minute = Integer.parseInt(minuteString);
				int second = 0;
				int nano = 0;
				// "HH:mm:ss".length() == 8
				if (length >= 8) {
					if (text.charAt(5) != ':') {
						errorIndex = 5;
						throw new IllegalArgumentException(text.toString());
					}
					errorIndex = 6;
					String secondString = text.subSequence(6, 8).toString();
					second = Integer.parseInt(secondString);
					// "HH:mm:ss.SSS".length() == 12, "HH:mm:ss.SSSSSSSSS".length()==18
					if (length >= 10) {
						if (text.charAt(8) != '.') {
							errorIndex = 8;
							throw new IllegalArgumentException(text.toString());
						}
						errorIndex = 9;
						String nanoString = text.subSequence(9, length).toString();
						nano = Integer.parseInt(nanoString);
						int factor = NANO_FACTORS[nanoString.length()];
						nano = nano * factor;
					}
				}
				return of(hour, minute, second, nano);
			}
		} catch (RuntimeException e) {
			cause = e;
		}
		throw new DateTimeParseException("Expected format HH:mm:ss.SSSSSSSSS", text, errorIndex, cause);
	}

	// -----------------------------------------------------------------------
	/**
	 * Creates a local time from the hour, minute, second and nanosecond fields.
	 * <p>
	 * This factory may return a cached value, but applications must not rely on this.
	 * 
	 * @param hour the hour-of-day to represent, validated from 0 to 23
	 * @param minute the minute-of-hour to represent, validated from 0 to 59
	 * @param second the second-of-minute to represent, validated from 0 to 59
	 * @param nanoOfSecond the nano-of-second to represent, validated from 0 to 999,999,999
	 * @return the local time, not null
	 */
	private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {

		if ((minute | second | nanoOfSecond) == 0) {
			return HOURS[hour];
		}
		return new LocalTime(hour, minute, second, nanoOfSecond);
	}

	/**
	 * Constructor, previously validated.
	 * 
	 * @param hour the hour-of-day to represent, validated from 0 to 23
	 * @param minute the minute-of-hour to represent, validated from 0 to 59
	 * @param second the second-of-minute to represent, validated from 0 to 59
	 * @param nanoOfSecond the nano-of-second to represent, validated from 0 to 999,999,999
	 */
	private LocalTime(int hour, int minute, int second, int nanoOfSecond) {

		this.hour = (byte) hour;
		this.minute = (byte) minute;
		this.second = (byte) second;
		this.nano = nanoOfSecond;
	}

	// -----------------------------------------------------------------------
	@Override
	public boolean isSupported(DateTimeField field) {

		if (field instanceof ChronoField) {
			return ((ChronoField) field).isTimeField();
		}
		return field != null && field.doIsSupported(this);
	}

	@Override
	public DateTimeValueRange range(DateTimeField field) {

		if (field instanceof ChronoField) {
			if (((ChronoField) field).isTimeField()) {
				return field.range();
			}
			throw new DateTimeException("Unsupported field: " + field.getName());
		}
		return field.doRange(this);
	}

	@Override
	public int get(DateTimeField field) {

		if (field instanceof ChronoField) {
			return get0(field);
		}
		return super.get(field);
	}

	@Override
	public long getLong(DateTimeField field) {

		if (field instanceof ChronoField) {
			if (field == ChronoField.NANO_OF_DAY) {
				return toNanoOfDay();
			}
			if (field == ChronoField.MICRO_OF_DAY) {
				return toNanoOfDay() / 1000;
			}
			return get0(field);
		}
		return field.doGet(this);
	}

	private int get0(DateTimeField field) {

		switch ((ChronoField) field) {
		case NANO_OF_SECOND:
			return this.nano;
		case NANO_OF_DAY:
			throw new DateTimeException("Field too large for an int: " + field);
		case MICRO_OF_SECOND:
			return this.nano / 1000;
		case MICRO_OF_DAY:
			throw new DateTimeException("Field too large for an int: " + field);
		case MILLI_OF_SECOND:
			return this.nano / 1000000;
		case MILLI_OF_DAY:
			return (int) (toNanoOfDay() / 1000000);
		case SECOND_OF_MINUTE:
			return this.second;
		case SECOND_OF_DAY:
			return toSecondOfDay();
		case MINUTE_OF_HOUR:
			return this.minute;
		case MINUTE_OF_DAY:
			return this.hour * 60 + this.minute;
		case HOUR_OF_AMPM:
			return this.hour % 12;
		case CLOCK_HOUR_OF_AMPM:
			int ham = this.hour % 12;
			return (ham % 12 == 0 ? 12 : ham);
		case HOUR_OF_DAY:
			return this.hour;
		case CLOCK_HOUR_OF_DAY:
			return (this.hour == 0 ? 24 : this.hour);
		case AMPM_OF_DAY:
			return this.hour / 12;
		}
		throw new DateTimeException("Unsupported field: " + field.getName());
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the hour-of-day field.
	 * 
	 * @return the hour-of-day, from 0 to 23
	 */
	public int getHour() {

		return this.hour;
	}

	/**
	 * Gets the minute-of-hour field.
	 * 
	 * @return the minute-of-hour, from 0 to 59
	 */
	public int getMinute() {

		return this.minute;
	}

	/**
	 * Gets the second-of-minute field.
	 * 
	 * @return the second-of-minute, from 0 to 59
	 */
	public int getSecond() {

		return this.second;
	}

	/**
	 * Gets the nano-of-second field.
	 * 
	 * @return the nano-of-second, from 0 to 999,999,999
	 */
	public int getNano() {

		return this.nano;
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns an adjusted time based on this time.
	 * <p>
	 * This adjusts the time according to the rules of the specified adjuster. A simple adjuster might simply
	 * set the one of the fields, such as the hour field. A more complex adjuster might set the time to the last
	 * hour of the day. The adjuster is responsible for handling special cases, such as the varying lengths of
	 * month and leap years.
	 * <p>
	 * For example, were there to be a class {@code AmPm} implementing the adjuster interface then this method
	 * could be used to change the AM/PM value.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return a {@code LocalTime} based on this time with the adjustment made, not null
	 * @throws DateTimeException if the adjustment cannot be made
	 */
	@Override
	public LocalTime with(WithAdjuster adjuster) {

		if (adjuster instanceof LocalTime) {
			return (LocalTime) adjuster;
		}
		return (LocalTime) adjuster.doWithAdjustment(this);
	}

	/**
	 * Returns a copy of this time with the specified field altered.
	 * <p>
	 * This method returns a new time based on this time with a new value for the specified field. This can be
	 * used to change any field, for example to set the hour-of-day.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param field the field to set in the result, not null
	 * @param newValue the new value of the field in the result
	 * @return a {@code LocalTime} based on this time with the specified field set, not null
	 * @throws DateTimeException if the value is invalid
	 */
	@Override
	public LocalTime with(DateTimeField field, long newValue) {

		if (field instanceof ChronoField) {
			ChronoField f = (ChronoField) field;
			f.checkValidValue(newValue);
			switch (f) {
			case NANO_OF_SECOND:
				return withNano((int) newValue);
			case NANO_OF_DAY:
				return LocalTime.ofNanoOfDay(newValue);
			case MICRO_OF_SECOND:
				return withNano((int) newValue * 1000);
			case MICRO_OF_DAY:
				return plusNanos((newValue - toNanoOfDay() / 1000) * 1000);
			case MILLI_OF_SECOND:
				return withNano((int) newValue * 1000000);
			case MILLI_OF_DAY:
				return plusNanos((newValue - toNanoOfDay() / 1000000) * 1000000);
			case SECOND_OF_MINUTE:
				return withSecond((int) newValue);
			case SECOND_OF_DAY:
				return plusSeconds(newValue - toSecondOfDay());
			case MINUTE_OF_HOUR:
				return withMinute((int) newValue);
			case MINUTE_OF_DAY:
				return plusMinutes(newValue - (this.hour * 60 + this.minute));
			case HOUR_OF_AMPM:
				return plusHours(newValue - (this.hour % 12));
			case CLOCK_HOUR_OF_AMPM:
				return plusHours((newValue == 12 ? 0 : newValue) - (this.hour % 12));
			case HOUR_OF_DAY:
				return withHour((int) newValue);
			case CLOCK_HOUR_OF_DAY:
				return withHour((int) (newValue == 24 ? 0 : newValue));
			case AMPM_OF_DAY:
				return plusHours((newValue - (this.hour / 12)) * 12);
			}
			throw new DateTimeException("Unsupported field: " + field.getName());
		}
		return field.doWith(this, newValue);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code LocalTime} with the hour-of-day value altered.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param hour the hour-of-day to set in the result, from 0 to 23
	 * @return a {@code LocalTime} based on this time with the requested hour, not null
	 * @throws DateTimeException if the hour value is invalid
	 */
	public LocalTime withHour(int hour) {

		if (this.hour == hour) {
			return this;
		}
		ChronoField.HOUR_OF_DAY.checkValidValue(hour);
		return create(hour, this.minute, this.second, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the minute-of-hour value altered.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param minute the minute-of-hour to set in the result, from 0 to 59
	 * @return a {@code LocalTime} based on this time with the requested minute, not null
	 * @throws DateTimeException if the minute value is invalid
	 */
	public LocalTime withMinute(int minute) {

		if (this.minute == minute) {
			return this;
		}
		ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
		return create(this.hour, minute, this.second, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the second-of-minute value altered.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param second the second-of-minute to set in the result, from 0 to 59
	 * @return a {@code LocalTime} based on this time with the requested second, not null
	 * @throws DateTimeException if the second value is invalid
	 */
	public LocalTime withSecond(int second) {

		if (this.second == second) {
			return this;
		}
		ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
		return create(this.hour, this.minute, second, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the nano-of-second value altered.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param nanoOfSecond the nano-of-second to set in the result, from 0 to 999,999,999
	 * @return a {@code LocalTime} based on this time with the requested nanosecond, not null
	 * @throws DateTimeException if the nanos value is invalid
	 */
	public LocalTime withNano(int nanoOfSecond) {

		if (this.nano == nanoOfSecond) {
			return this;
		}
		ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
		return create(this.hour, this.minute, this.second, nanoOfSecond);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code LocalTime} with the time truncated.
	 * <p>
	 * Truncating the time returns a copy of the original time with fields smaller than the specified unit set
	 * to zero. For example, truncating with the {@link ChronoUnit#MINUTES minutes} unit will set the
	 * second-of-minute and nano-of-second field to zero.
	 * <p>
	 * Not all units are accepted. The {@link ChronoUnit#DAYS days} unit and time units with an exact duration
	 * can be used, other units throw an exception.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param unit the unit to truncate to, not null
	 * @return a {@code LocalTime} based on this time with the time truncated, not null
	 * @throws DateTimeException if unable to truncate
	 */
	public LocalTime truncatedTo(PeriodUnit unit) {

		if (unit == ChronoUnit.NANOS) {
			return this;
		} else if (unit == ChronoUnit.DAYS) {
			return MIDNIGHT;
		} else if (unit.isDurationEstimated()) {
			throw new DateTimeException("Unit must not have an estimated duration");
		}
		long nod = toNanoOfDay();
		long dur = unit.getDuration().toNanos();
		if (dur >= NANOS_PER_DAY) {
			throw new DateTimeException("Unit must not be a date unit");
		}
		nod = (nod / dur) * dur;
		return ofNanoOfDay(nod);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this date with the specified period added.
	 * <p>
	 * This method returns a new time based on this time with the specified period added. The adjuster is
	 * typically {@link Period} but may be any other type implementing the
	 * {@link PlusAdjuster} interface. The calculation is delegated to the
	 * specified adjuster, which typically calls back to {@link #plus(long, PeriodUnit)}.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return a {@code LocalTime} based on this time with the addition made, not null
	 * @throws DateTimeException if the addition cannot be made
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	@Override
	public LocalTime plus(PlusAdjuster adjuster) {

		return (LocalTime) adjuster.doPlusAdjustment(this);
	}

	/**
	 * Returns a copy of this time with the specified period added.
	 * <p>
	 * This method returns a new time based on this time with the specified period added. This can be used to
	 * add any period that is defined by a unit, for example to add hours, minutes or seconds. The unit is
	 * responsible for the details of the calculation, including the resolution of any edge cases in the
	 * calculation.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param amountToAdd the amount of the unit to add to the result, may be negative
	 * @param unit the unit of the period to add, not null
	 * @return a {@code LocalTime} based on this time with the specified period added, not null
	 * @throws DateTimeException if the unit cannot be added to this type
	 */
	@Override
	public LocalTime plus(long amountToAdd, PeriodUnit unit) {

		if (unit instanceof ChronoUnit) {
			ChronoUnit f = (ChronoUnit) unit;
			switch (f) {
			case NANOS:
				return plusNanos(amountToAdd);
			case MICROS:
				return plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
			case MILLIS:
				return plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
			case SECONDS:
				return plusSeconds(amountToAdd);
			case MINUTES:
				return plusMinutes(amountToAdd);
			case HOURS:
				return plusHours(amountToAdd);
			case HALF_DAYS:
				return plusHours((amountToAdd % 2) * 12);
			case DAYS:
				return this;
			}
			throw new DateTimeException("Unsupported unit: " + unit.getName());
		}
		return unit.doPlus(this, amountToAdd);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in hours added.
	 * <p>
	 * This adds the specified number of hours to this time, returning a new time. The calculation wraps around
	 * midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param hoursToAdd the hours to add, may be negative
	 * @return a {@code LocalTime} based on this time with the hours added, not null
	 */
	public LocalTime plusHours(long hoursToAdd) {

		if (hoursToAdd == 0) {
			return this;
		}
		int newHour = ((int) (hoursToAdd % HOURS_PER_DAY) + this.hour + HOURS_PER_DAY) % HOURS_PER_DAY;
		return create(newHour, this.minute, this.second, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in minutes added.
	 * <p>
	 * This adds the specified number of minutes to this time, returning a new time. The calculation wraps
	 * around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param minutesToAdd the minutes to add, may be negative
	 * @return a {@code LocalTime} based on this time with the minutes added, not null
	 */
	public LocalTime plusMinutes(long minutesToAdd) {

		if (minutesToAdd == 0) {
			return this;
		}
		int mofd = this.hour * MINUTES_PER_HOUR + this.minute;
		int newMofd = ((int) (minutesToAdd % MINUTES_PER_DAY) + mofd + MINUTES_PER_DAY) % MINUTES_PER_DAY;
		if (mofd == newMofd) {
			return this;
		}
		int newHour = newMofd / MINUTES_PER_HOUR;
		int newMinute = newMofd % MINUTES_PER_HOUR;
		return create(newHour, newMinute, this.second, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in seconds added.
	 * <p>
	 * This adds the specified number of seconds to this time, returning a new time. The calculation wraps
	 * around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param secondstoAdd the seconds to add, may be negative
	 * @return a {@code LocalTime} based on this time with the seconds added, not null
	 */
	public LocalTime plusSeconds(long secondstoAdd) {

		if (secondstoAdd == 0) {
			return this;
		}
		int sofd = this.hour * SECONDS_PER_HOUR + this.minute * SECONDS_PER_MINUTE + this.second;
		int newSofd = ((int) (secondstoAdd % SECONDS_PER_DAY) + sofd + SECONDS_PER_DAY) % SECONDS_PER_DAY;
		if (sofd == newSofd) {
			return this;
		}
		int newHour = newSofd / SECONDS_PER_HOUR;
		int newMinute = (newSofd / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
		int newSecond = newSofd % SECONDS_PER_MINUTE;
		return create(newHour, newMinute, newSecond, this.nano);
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in nanoseconds added.
	 * <p>
	 * This adds the specified number of nanoseconds to this time, returning a new time. The calculation wraps
	 * around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param nanosToAdd the nanos to add, may be negative
	 * @return a {@code LocalTime} based on this time with the nanoseconds added, not null
	 */
	public LocalTime plusNanos(long nanosToAdd) {

		if (nanosToAdd == 0) {
			return this;
		}
		long nofd = toNanoOfDay();
		long newNofd = ((nanosToAdd % NANOS_PER_DAY) + nofd + NANOS_PER_DAY) % NANOS_PER_DAY;
		if (nofd == newNofd) {
			return this;
		}
		int newHour = (int) (newNofd / NANOS_PER_HOUR);
		int newMinute = (int) ((newNofd / NANOS_PER_MINUTE) % MINUTES_PER_HOUR);
		int newSecond = (int) ((newNofd / NANOS_PER_SECOND) % SECONDS_PER_MINUTE);
		int newNano = (int) (newNofd % NANOS_PER_SECOND);
		return create(newHour, newMinute, newSecond, newNano);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this time with the specified period subtracted.
	 * <p>
	 * This method returns a new time based on this time with the specified period subtracted. The adjuster is
	 * typically {@link Period} but may be any other type implementing the
	 * {@link MinusAdjuster} interface. The calculation is delegated to the
	 * specified adjuster, which typically calls back to {@link #minus(long, PeriodUnit)}.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param adjuster the adjuster to use, not null
	 * @return a {@code LocalTime} based on this time with the subtraction made, not null
	 * @throws DateTimeException if the subtraction cannot be made
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	@Override
	public LocalTime minus(MinusAdjuster adjuster) {

		return (LocalTime) adjuster.doMinusAdjustment(this);
	}

	/**
	 * Returns a copy of this time with the specified period subtracted.
	 * <p>
	 * This method returns a new time based on this time with the specified period subtracted. This can be used
	 * to subtract any period that is defined by a unit, for example to subtract hours, minutes or seconds. The
	 * unit is responsible for the details of the calculation, including the resolution of any edge cases in the
	 * calculation.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param amountToSubtract the amount of the unit to subtract from the result, may be negative
	 * @param unit the unit of the period to subtract, not null
	 * @return a {@code LocalTime} based on this time with the specified period subtracted, not null
	 * @throws DateTimeException if the unit cannot be added to this type
	 */
	@Override
	public LocalTime minus(long amountToSubtract, PeriodUnit unit) {

		return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
				: plus(-amountToSubtract, unit));
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in hours subtracted.
	 * <p>
	 * This subtracts the specified number of hours from this time, returning a new time. The calculation wraps
	 * around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param hoursToSubtract the hours to subtract, may be negative
	 * @return a {@code LocalTime} based on this time with the hours subtracted, not null
	 */
	public LocalTime minusHours(long hoursToSubtract) {

		return plusHours(-(hoursToSubtract % HOURS_PER_DAY));
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in minutes subtracted.
	 * <p>
	 * This subtracts the specified number of minutes from this time, returning a new time. The calculation
	 * wraps around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param minutesToSubtract the minutes to subtract, may be negative
	 * @return a {@code LocalTime} based on this time with the minutes subtracted, not null
	 */
	public LocalTime minusMinutes(long minutesToSubtract) {

		return plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY));
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in seconds subtracted.
	 * <p>
	 * This subtracts the specified number of seconds from this time, returning a new time. The calculation
	 * wraps around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param secondsToSubtract the seconds to subtract, may be negative
	 * @return a {@code LocalTime} based on this time with the seconds subtracted, not null
	 */
	public LocalTime minusSeconds(long secondsToSubtract) {

		return plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY));
	}

	/**
	 * Returns a copy of this {@code LocalTime} with the specified period in nanoseconds subtracted.
	 * <p>
	 * This subtracts the specified number of nanoseconds from this time, returning a new time. The calculation
	 * wraps around midnight.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param nanosToSubtract the nanos to subtract, may be negative
	 * @return a {@code LocalTime} based on this time with the nanoseconds subtracted, not null
	 */
	public LocalTime minusNanos(long nanosToSubtract) {

		return plusNanos(-(nanosToSubtract % NANOS_PER_DAY));
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a local date-time formed from this time at the specified date.
	 * <p>
	 * This merges the two objects - {@code this} and the specified date - to form an instance of
	 * {@code LocalDateTime}.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 * 
	 * @param date the date to combine with, not null
	 * @return the local date-time formed from this time and the specified date, not null
	 */
	public LocalDateTime atDate(LocalDate date) {

		return LocalDateTime.of(date, this);
	}

	// -----------------------------------------------------------------------
	@Override
	public DateTime doWithAdjustment(DateTime dateTime) {

		return dateTime.with(ChronoField.NANO_OF_DAY, toNanoOfDay());
	}

	@Override
	public long periodUntil(DateTime endDateTime, PeriodUnit unit) {

		if (endDateTime instanceof LocalTime == false) {
			throw new DateTimeException("Unable to calculate period between objects of two different types");
		}
		LocalTime end = (LocalTime) endDateTime;
		if (unit instanceof ChronoUnit) {
			long nanosUntil = end.toNanoOfDay() - toNanoOfDay(); // no overflow
			switch ((ChronoUnit) unit) {
			case NANOS:
				return nanosUntil;
			case MICROS:
				return nanosUntil / 1000;
			case MILLIS:
				return nanosUntil / 1000000;
			case SECONDS:
				return nanosUntil / NANOS_PER_SECOND;
			case MINUTES:
				return nanosUntil / NANOS_PER_MINUTE;
			case HOURS:
				return nanosUntil / NANOS_PER_HOUR;
			case HALF_DAYS:
				return nanosUntil / (12 * NANOS_PER_HOUR);
			}
			throw new DateTimeException("Unsupported unit: " + unit.getName());
		}
		return unit.between(this, endDateTime).getAmount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R query(DateTimeAccessor.Query<R> query) {

		if (query == DateTimeAccessor.Query.TIME_PRECISION) {
			return (R) ChronoUnit.NANOS;
		}
		return super.query(query);
	}

	// -----------------------------------------------------------------------
	/**
	 * Extracts the time as seconds of day, from {@code 0} to {@code 24 * 60 * 60 - 1}.
	 * 
	 * @return the second-of-day equivalent to this time
	 */
	public int toSecondOfDay() {

		int total = this.hour * SECONDS_PER_HOUR;
		total += this.minute * SECONDS_PER_MINUTE;
		total += this.second;
		return total;
	}

	/**
	 * Extracts the time as nanos of day, from {@code 0} to {@code 24 * 60 * 60 * 1,000,000,000 - 1}.
	 * 
	 * @return the nano of day equivalent to this time
	 */
	public long toNanoOfDay() {

		long total = this.hour * NANOS_PER_HOUR;
		total += this.minute * NANOS_PER_MINUTE;
		total += this.second * NANOS_PER_SECOND;
		total += this.nano;
		return total;
	}

	// -----------------------------------------------------------------------
	/**
	 * Compares this {@code LocalTime} to another time.
	 * <p>
	 * The comparison is based on the time-line position of the local times within a day. It is
	 * "consistent with equals", as defined by {@link Comparable}.
	 * 
	 * @param other the other time to compare to, not null
	 * @return the comparator value, negative if less, positive if greater
	 * @throws NullPointerException if {@code other} is null
	 */
	@Override
	public int compareTo(LocalTime other) {

		int cmp = Jdk7Methods.Integer_compare(this.hour, other.hour);
		if (cmp == 0) {
			cmp = Jdk7Methods.Integer_compare(this.minute, other.minute);
			if (cmp == 0) {
				cmp = Jdk7Methods.Integer_compare(this.second, other.second);
				if (cmp == 0) {
					cmp = Jdk7Methods.Integer_compare(this.nano, other.nano);
				}
			}
		}
		return cmp;
	}

	/**
	 * Checks if this {@code LocalTime} is after the specified time.
	 * <p>
	 * The comparison is based on the time-line position of the time within a day.
	 * 
	 * @param other the other time to compare to, not null
	 * @return true if this is after the specified time
	 * @throws NullPointerException if {@code other} is null
	 */
	public boolean isAfter(LocalTime other) {

		return compareTo(other) > 0;
	}

	/**
	 * Checks if this {@code LocalTime} is before the specified time.
	 * <p>
	 * The comparison is based on the time-line position of the time within a day.
	 * 
	 * @param other the other time to compare to, not null
	 * @return true if this point is before the specified time
	 * @throws NullPointerException if {@code other} is null
	 */
	public boolean isBefore(LocalTime other) {

		return compareTo(other) < 0;
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if this time is equal to another time.
	 * <p>
	 * The comparison is based on the time-line position of the time within a day.
	 * <p>
	 * Only objects of type {@code LocalTime} are compared, other types return false. To compare the date of two
	 * {@code DateTimeAccessor} instances, use {@link ChronoField#NANO_OF_DAY} as a comparator.
	 * 
	 * @param obj the object to check, null returns false
	 * @return true if this is equal to the other time
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj instanceof LocalTime) {
			LocalTime other = (LocalTime) obj;
			return this.hour == other.hour && this.minute == other.minute && this.second == other.second
					&& this.nano == other.nano;
		}
		return false;
	}

	/**
	 * A hash code for this time.
	 * 
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {

		long nod = toNanoOfDay();
		return (int) (nod ^ (nod >>> 32));
	}

	// -----------------------------------------------------------------------
	/**
	 * Outputs this time as a {@code String}, such as {@code 10:15}.
	 * <p>
	 * The output will be one of the following ISO-8601 formats:
	 * <p>
	 * <ul>
	 * <li>{@code HH:mm}</li>
	 * <li>{@code HH:mm:ss}</li>
	 * <li>{@code HH:mm:ss.SSS}</li>
	 * <li>{@code HH:mm:ss.SSSSSS}</li>
	 * <li>{@code HH:mm:ss.SSSSSSSSS}</li>
	 * </ul>
	 * <p>
	 * The format used will be the shortest that outputs the full value of the time where the omitted parts are
	 * implied to be zero.
	 * 
	 * @return a string representation of this time, not null
	 */
	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder(18);
		int hourValue = this.hour;
		int minuteValue = this.minute;
		int secondValue = this.second;
		int nanoValue = this.nano;
		buf.append(hourValue < 10 ? "0" : "").append(hourValue).append(minuteValue < 10 ? ":0" : ":")
				.append(minuteValue);
		if (secondValue > 0 || nanoValue > 0) {
			buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
			if (nanoValue > 0) {
				buf.append('.');
				if (nanoValue % 1000000 == 0) {
					buf.append(Integer.toString((nanoValue / 1000000) + 1000).substring(1));
				} else if (nanoValue % 1000 == 0) {
					buf.append(Integer.toString((nanoValue / 1000) + 1000000).substring(1));
				} else {
					buf.append(Integer.toString((nanoValue) + 1000000000).substring(1));
				}
			}
		}
		return buf.toString();
	}

}
