/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.moshi.adapters;

import com.squareup.moshi.JsonDataException;

import java.util.*;

/**
 * Jackson’s date formatter, pruned to Moshi's needs. Forked from this file:
 * https://github.com/FasterXML/jackson-databind/blob/67ebf7305f492285a8f9f4de31545f5f16fc7c3a/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java
 * <p>
 * Utilities methods for manipulating dates in iso8601 format. This is much much faster and GC
 * friendly than using SimpleDateFormat so highly suitable if you (un)serialize lots of date
 * objects.
 * <p>
 * Supported parse format: [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]
 *
 * @see <a href="http://www.w3.org/TR/NOTE-datetime">this specification</a>
 */
final class Iso8601Utils {
	/**
	 * ID to represent the 'GMT' string
	 */
	static final String GMT_ID = "GMT";

	/**
	 * The GMT timezone, prefetched to avoid more lookups.
	 */
	static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone(GMT_ID);

	/**
	 * Returns {@code date} formatted as yyyy-MM-ddThh:mm:ss.sssZ
	 */
	public static String format(Date date) {
		Calendar calendar = new GregorianCalendar(TIMEZONE_Z, Locale.US);
		calendar.setTime(date);

		// estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
		int capacity = "yyyy-MM-ddThh:mm:ss.sssZ".length();
		StringBuilder formatted = new StringBuilder(capacity);
		padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length());
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length());
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length());
		formatted.append('T');
		padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length());
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length());
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.SECOND), "ss".length());
		formatted.append('.');
		padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length());
		formatted.append('Z');
		return formatted.toString();
	}

	/**
	 * Parse a date from ISO-8601 formatted string. It expects a format
	 * [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh:mm]]
	 *
	 * @param date ISO string to parse in the appropriate format.
	 * @return the parsed date
	 */
	public static Date parse(String date) {
		try {
			int offset = 0;

			// extract year
			int year = parseInt(date, offset, offset += 4);
			if (checkOffset(date, offset, '-')) {
				offset += 1;
			}

			// extract month
			int month = parseInt(date, offset, offset += 2);
			if (checkOffset(date, offset, '-')) {
				offset += 1;
			}

			// extract day
			int day = parseInt(date, offset, offset += 2);
			// default time value
			int hour = 0;
			int minutes = 0;
			int seconds = 0;
			int milliseconds = 0; // always use 0 otherwise returned date will include millis of current time

			// if the value has no time component (and no time zone), we are done
			boolean hasT = checkOffset(date, offset, 'T');

			if (!hasT && (date.length() <= offset)) {
				Calendar calendar = new GregorianCalendar(year, month - 1, day);

				return calendar.getTime();
			}

			if (hasT) {

				// extract hours, minutes, seconds and milliseconds
				hour = parseInt(date, offset += 1, offset += 2);
				if (checkOffset(date, offset, ':')) {
					offset += 1;
				}

				minutes = parseInt(date, offset, offset += 2);
				if (checkOffset(date, offset, ':')) {
					offset += 1;
				}
				// second and milliseconds can be optional
				if (date.length() > offset) {
					char c = date.charAt(offset);
					if (c != 'Z' && c != '+' && c != '-') {
						seconds = parseInt(date, offset, offset += 2);
						if (seconds > 59 && seconds < 63)
							seconds = 59; // truncate up to 3 leap seconds
						// milliseconds can be optional in the format
						if (checkOffset(date, offset, '.')) {
							offset += 1;
							int endOffset = indexOfNonDigit(date, offset + 1); // assume at least one digit
							int parseEndOffset = Math.min(endOffset, offset + 3); // parse up to 3 digits
							int fraction = parseInt(date, offset, parseEndOffset);
							milliseconds = (int) (Math.pow(10, 3 - (parseEndOffset - offset)) * fraction);
							offset = endOffset;
						}
					}
				}
			}

			// extract timezone
			if (date.length() <= offset) {
				throw new IllegalArgumentException("No time zone indicator");
			}

			TimeZone timezone;
			char timezoneIndicator = date.charAt(offset);

			if (timezoneIndicator == 'Z') {
				timezone = TIMEZONE_Z;
			} else if (timezoneIndicator == '+' || timezoneIndicator == '-') {
				String timezoneOffset = date.substring(offset);
				// 18-Jun-2015, tatu: Minor simplification, skip offset of "+0000"/"+00:00"
				if ("+0000".equals(timezoneOffset) || "+00:00".equals(timezoneOffset)) {
					timezone = TIMEZONE_Z;
				} else {
					// 18-Jun-2015, tatu: Looks like offsets only work from GMT, not UTC...
					//    not sure why, but it is what it is.
					String timezoneId = GMT_ID + timezoneOffset;
					timezone = TimeZone.getTimeZone(timezoneId);
					String act = timezone.getID();
					if (!act.equals(timezoneId)) {
						/* 22-Jan-2015, tatu: Looks like canonical version has colons, but we may be given
						 *    one without. If so, don't sweat.
						 *   Yes, very inefficient. Hopefully not hit often.
						 *   If it becomes a perf problem, add 'loose' comparison instead.
						 */
						String cleaned = act.replace(":", "");
						if (!cleaned.equals(timezoneId)) {
							throw new IndexOutOfBoundsException("Mismatching time zone indicator: " + timezoneId
									+ " given, resolves to " + timezone.getID());
						}
					}
				}
			} else {
				throw new IndexOutOfBoundsException("Invalid time zone indicator '" + timezoneIndicator + "'");
			}

			Calendar calendar = new GregorianCalendar(timezone);
			calendar.setLenient(false);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month - 1);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MILLISECOND, milliseconds);

			return calendar.getTime();
			// If we get a ParseException it'll already have the right message/offset.
			// Other exception types can convert here.
		} catch (IndexOutOfBoundsException e) {
			throw new JsonDataException("Not an RFC 3339 date: " + date, e);
		} catch (IllegalArgumentException e) {
			throw new JsonDataException("Not an RFC 3339 date: " + date, e);
		}
	}

	/**
	 * Check if the expected character exist at the given offset in the value.
	 *
	 * @param value    the string to check at the specified offset
	 * @param offset   the offset to look for the expected character
	 * @param expected the expected character
	 * @return true if the expected character exist at the given offset
	 */
	private static boolean checkOffset(String value, int offset, char expected) {
		return (offset < value.length()) && (value.charAt(offset) == expected);
	}

	/**
	 * Parse an integer located between 2 given offsets in a string
	 *
	 * @param value      the string to parse
	 * @param beginIndex the start index for the integer in the string
	 * @param endIndex   the end index for the integer in the string
	 * @return the int
	 * @throws NumberFormatException if the value is not a number
	 */
	private static int parseInt(String value, int beginIndex, int endIndex) throws NumberFormatException {
		if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
			throw new NumberFormatException(value);
		}
		// use same logic as in Integer.parseInt() but less generic we're not supporting negative values
		int i = beginIndex;
		int result = 0;
		int digit;
		if (i < endIndex) {
			digit = Character.digit(value.charAt(i++), 10);
			if (digit < 0) {
				throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
			}
			result = -digit;
		}
		while (i < endIndex) {
			digit = Character.digit(value.charAt(i++), 10);
			if (digit < 0) {
				throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
			}
			result *= 10;
			result -= digit;
		}
		return -result;
	}

	/**
	 * Zero pad a number to a specified length
	 *
	 * @param buffer buffer to use for padding
	 * @param value  the integer value to pad if necessary.
	 * @param length the length of the string we should zero pad
	 */
	private static void padInt(StringBuilder buffer, int value, int length) {
		String strValue = Integer.toString(value);
		for (int i = length - strValue.length(); i > 0; i--) {
			buffer.append('0');
		}
		buffer.append(strValue);
	}

	/**
	 * Returns the index of the first character in the string that is not a digit, starting at
	 * offset.
	 */
	private static int indexOfNonDigit(String string, int offset) {
		for (int i = offset; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c < '0' || c > '9') {
				return i;
			}
		}
		return string.length();
	}
}
