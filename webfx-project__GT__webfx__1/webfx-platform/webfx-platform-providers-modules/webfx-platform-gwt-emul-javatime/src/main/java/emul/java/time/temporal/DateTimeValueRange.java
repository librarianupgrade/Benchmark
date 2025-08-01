/*
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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
package emul.java.time.temporal;

import emul.java.time.DateTimeException;

import java.io.Serializable;

/**
 * The range of valid values for a date-time field.
 * <p>
 * All {@link DateTimeField} instances have a valid range of values. For example, the ISO day-of-month runs
 * from 1 to somewhere between 28 and 31. This class captures that valid range.
 * <p>
 * Instances of this class are not tied to a specific rule
 * 
 * <h4>Implementation notes</h4> This class is immutable and thread-safe.
 */
public final class DateTimeValueRange implements Serializable {

	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = -7317881728594519368L;

	/**
	 * The smallest minimum value.
	 */
	private final long minSmallest;

	/**
	 * The largest minimum value.
	 */
	private final long minLargest;

	/**
	 * The smallest maximum value.
	 */
	private final long maxSmallest;

	/**
	 * The largest maximum value.
	 */
	private final long maxLargest;

	/**
	 * Obtains a fixed value range.
	 * <p>
	 * This factory obtains a range where the minimum and maximum values are fixed. For example, the ISO
	 * month-of-year always runs from 1 to 12.
	 * 
	 * @param min the minimum value
	 * @param max the maximum value
	 */
	public static DateTimeValueRange of(long min, long max) {

		if (min > max) {
			throw new IllegalArgumentException("Minimum value must be less than maximum value");
		}
		return new DateTimeValueRange(min, min, max, max);
	}

	/**
	 * Obtains a variable value range.
	 * <p>
	 * This factory obtains a range where the minimum value is fixed and the maximum value may vary. For
	 * example, the ISO day-of-month always starts at 1, but ends between 28 and 31.
	 * 
	 * @param min the minimum value
	 * @param maxSmallest the smallest maximum value
	 * @param maxLargest the largest maximum value
	 */
	public static DateTimeValueRange of(long min, long maxSmallest, long maxLargest) {

		return of(min, min, maxSmallest, maxLargest);
	}

	/**
	 * Obtains a fully variable value range.
	 * <p>
	 * This factory obtains a range where both the minimum and maximum value may vary.
	 * 
	 * @param minSmallest the smallest minimum value
	 * @param minLargest the largest minimum value
	 * @param maxSmallest the smallest maximum value
	 * @param maxLargest the largest maximum value
	 */
	public static DateTimeValueRange of(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {

		if (minSmallest > minLargest) {
			throw new IllegalArgumentException("Smallest minimum value must be less than largest minimum value");
		}
		if (maxSmallest > maxLargest) {
			throw new IllegalArgumentException("Smallest maximum value must be less than largest maximum value");
		}
		if (minSmallest > maxLargest) {
			throw new IllegalArgumentException("Minimum value must be less than maximum value");
		}
		return new DateTimeValueRange(minSmallest, minLargest, maxSmallest, maxLargest);
	}

	/**
	 * Restrictive constructor.
	 * 
	 * @param minSmallest the smallest minimum value
	 * @param minLargest the largest minimum value
	 * @param maxSmallest the smallest minimum value
	 * @param maxLargest the largest minimum value
	 */
	private DateTimeValueRange(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {

		this.minSmallest = minSmallest;
		this.minLargest = minLargest;
		this.maxSmallest = maxSmallest;
		this.maxLargest = maxLargest;
	}

	// -----------------------------------------------------------------------
	/**
	 * Is the value range fixed and fully known.
	 * <p>
	 * For example, the ISO day-of-month runs from 1 to between 28 and 31. Since there is uncertainty about the
	 * maximum value, the range is not fixed. However, for the month of January, the range is always 1 to 31,
	 * thus it is fixed.
	 * 
	 * @return true if the set of values is fixed
	 */
	public boolean isFixed() {

		return this.minSmallest == this.minLargest && this.maxSmallest == this.maxLargest;
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the minimum value that the field can take.
	 * <p>
	 * For example, the ISO day-of-month always starts at 1. The minimum is therefore 1.
	 * 
	 * @return the minimum value for this field
	 */
	public long getMinimum() {

		return this.minSmallest;
	}

	/**
	 * Gets the largest possible minimum value that the field can take.
	 * <p>
	 * For example, the ISO day-of-month always starts at 1. The largest minimum is therefore 1.
	 * 
	 * @return the largest possible minimum value for this field
	 */
	public long getLargestMinimum() {

		return this.minLargest;
	}

	/**
	 * Gets the smallest possible maximum value that the field can take.
	 * <p>
	 * For example, the ISO day-of-month runs to between 28 and 31 days. The smallest maximum is therefore 28.
	 * 
	 * @return the smallest possible maximum value for this field
	 */
	public long getSmallestMaximum() {

		return this.maxSmallest;
	}

	/**
	 * Gets the maximum value that the field can take.
	 * <p>
	 * For example, the ISO day-of-month runs to between 28 and 31 days. The maximum is therefore 31.
	 * 
	 * @return the maximum value for this field
	 */
	public long getMaximum() {

		return this.maxLargest;
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks if all values in the range fit in an {@code int}.
	 * <p>
	 * This checks that all valid values are within the bounds of an {@code int}.
	 * <p>
	 * For example, the ISO month-of-year has values from 1 to 12, which fits in an {@code int}. By comparison,
	 * ISO nano-of-day runs from 1 to 86,400,000,000,000 which does not fit in an {@code int}.
	 * <p>
	 * This implementation uses {@link #getMinimum()} and {@link #getMaximum()}.
	 * 
	 * @return true if a valid value always fits in an {@code int}
	 */
	public boolean isIntValue() {

		return getMinimum() >= Integer.MIN_VALUE && getMaximum() <= Integer.MAX_VALUE;
	}

	/**
	 * Checks if the value is within the valid range.
	 * <p>
	 * This checks that the value is within the stored range of values.
	 * 
	 * @param value the value to check
	 * @return true if the value is valid
	 */
	public boolean isValidValue(long value) {

		return (value >= getMinimum() && value <= getMaximum());
	}

	/**
	 * Checks if the value is within the valid range and that all values in the range fit in an {@code int}.
	 * <p>
	 * This method combines {@link #isIntValue()} and {@link #isValidValue(long)}.
	 * 
	 * @param value the value to check
	 * @return true if the value is valid and fits in an {@code int}
	 */
	public boolean isValidIntValue(long value) {

		return isIntValue() && isValidValue(value);
	}

	/**
	 * Checks that the specified value is valid.
	 * <p>
	 * This validates that the value is within the valid range of values. The field is only used to improve the
	 * error message.
	 * 
	 * @param value the value to check
	 * @param field the field being checked, may be null
	 * @return the value that was passed in
	 * @see #isValidValue(long)
	 */
	public long checkValidValue(long value, DateTimeField field) {

		if (isValidValue(value) == false) {
			if (field != null) {
				throw new DateTimeException(
						"Invalid value for " + field.getName() + " (valid values " + this + "): " + value);
			} else {
				throw new DateTimeException("Invalid value (valid values " + this + "): " + value);
			}
		}
		return value;
	}

	/**
	 * Checks that the specified value is valid and fits in an {@code int}.
	 * <p>
	 * This validates that the value is within the valid range of values and that all valid values are within
	 * the bounds of an {@code int}. The field is only used to improve the error message.
	 * 
	 * @param value the value to check
	 * @param field the field being checked, may be null
	 * @return the value that was passed in
	 * @see #isValidIntValue(long)
	 */
	public int checkValidIntValue(long value, DateTimeField field) {

		if (isValidIntValue(value) == false) {
			throw new DateTimeException("Invalid int value for " + field.getName() + ": " + value);
		}
		return (int) value;
	}

	// -----------------------------------------------------------------------
	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (obj instanceof DateTimeValueRange) {
			DateTimeValueRange other = (DateTimeValueRange) obj;
			return this.minSmallest == other.minSmallest && this.minLargest == other.minLargest
					&& this.maxSmallest == other.maxSmallest && this.maxLargest == other.maxLargest;
		}
		return false;
	}

	@Override
	public int hashCode() {

		long hash = this.minSmallest + this.minLargest << 16 + this.minLargest >> 48 + this.maxSmallest << 32
				+ this.maxSmallest >> 32 + this.maxLargest << 48 + this.maxLargest >> 16;
		return (int) (hash ^ (hash >>> 32));
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();
		buf.append(this.minSmallest);
		if (this.minSmallest != this.minLargest) {
			buf.append('/').append(this.minLargest);
		}
		buf.append(" - ").append(this.maxSmallest);
		if (this.maxSmallest != this.maxLargest) {
			buf.append('/').append(this.maxLargest);
		}
		return buf.toString();
	}

}
