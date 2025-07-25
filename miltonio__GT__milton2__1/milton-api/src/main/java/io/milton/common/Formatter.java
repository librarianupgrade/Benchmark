/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

	private static final Logger log = LoggerFactory.getLogger(Formatter.class);

	public static final String CHECKBOX_SUFFIX = "_checkbox";

	public static ThreadLocal<DateFormat> tlSdfUkShort = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));
	public static ThreadLocal<DateFormat> tlSdfUkLong = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("dd MMMM yyyy"));
	public static final ThreadLocal<DateFormat> sdfDateOnly = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));
	public static final ThreadLocal<DateFormat> sdfDateAndTime = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy HH:mm"));

	/**
	 * Null safe method, returns empty string if the value is null
	 *
	 * @param object to be a String.
	 * @return Returns a string representation of the object.
	 */
	public String toString(Object object) {
		if (object == null) {
			return "";
		} else {
			return object.toString();
		}
	}

	/**
	 * Converts object to boolean if possible
	 * @param object to convert.
	 * @return boolean represenation of object.
	 */
	public Boolean toBool(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Boolean) {
			return (Boolean) object;
		} else if (object instanceof Integer) {
			Integer i = (Integer) object;
			return i == 0;
		} else if (object instanceof String) {
			String s = (String) object;
			s = s.toLowerCase();
			s = s.trim();
			if (!s.isEmpty()) {
				return s.equals("true") || s.equals("yes");
			} else {
				return null;
			}
		} else {
			throw new RuntimeException("Unsupported boolean type: " + object.getClass());
		}

	}

	/**
	 * Converts object to decimal if possible.
	 * @param object to convert.
	 * @param places how many decimal places.
	 * @return decimal value of the object.
	 */
	public BigDecimal toDecimal(Object object, int places) {
		if (object == null) {
			return BigDecimal.ZERO;
		} else if (object instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) object;
			return bd.setScale(places, RoundingMode.HALF_UP);
		} else if (object instanceof Double) {
			Double d = (Double) object;
			return BigDecimal.valueOf(d).setScale(places, RoundingMode.HALF_UP);
		} else if (object instanceof Integer) {
			Integer i = (Integer) object;
			return BigDecimal.valueOf(i.longValue()).setScale(places, RoundingMode.HALF_UP);
		} else if (object instanceof Float) {
			Float f = (Float) object;
			return BigDecimal.valueOf(f.doubleValue()).setScale(places, RoundingMode.HALF_UP);
		} else if (object instanceof String) {
			String s = (String) object;
			s = s.trim();
			if (s.isEmpty()) {
				return BigDecimal.ZERO;
			} else {
				try {
					return new BigDecimal(s).setScale(places, RoundingMode.HALF_UP);
				} catch (NumberFormatException numberFormatException) {
					throw new RuntimeException("Non-numeric data: " + s);
				}
			}
		} else {
			throw new RuntimeException("Unsupported value type, should be numeric: " + object.getClass());
		}
	}

	/**
	 * Converts object to double if possible.
	 * @param object to convert.
	 * @return double value of the object.
	 */
	public Double toDouble(Object object) {
		if (object == null) {
			return 0d;
		} else if (object instanceof String) {
			String s = (String) object;
			s = s.trim();
			if (s.isEmpty()) {
				return 0d;
			} else {
				try {
					return Double.valueOf(s);
				} catch (NumberFormatException numberFormatException) {
					throw new RuntimeException("Non-numeric data: " + s);
				}
			}
		} else if (object instanceof Double) {
			return (Double) object;
		} else if (object instanceof Integer) {
			Integer i = (Integer) object;
			return (double) i;
		} else if (object instanceof Float) {
			Float f = (Float) object;
			return f.doubleValue();
		} else if (object instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) object;
			return bd.doubleValue();
		} else {
			throw new RuntimeException("Unsupported value type, should be numeric: " + object.getClass());
		}
	}

	public Long toLong(Object oLimit) {
		return toLong(oLimit, false);
	}

	/**
	 * Converts object to Long if possible.
	 * @param object to convert.
	 * @param withNulls if true 0L will be null.
	 * @return Long value of the object.
	 */
	public Long toLong(Object object, boolean withNulls) {
		Long limit;
		if (object == null) {
			limit = withNulls ? null : 0L;
		} else if (object instanceof Long) {
			limit = (Long) object;
		} else if (object instanceof Integer) {
			int i = (Integer) object;
			limit = (long) i;
		} else if (object instanceof Double) {
			Double d = (Double) object;
			return d.longValue();
		} else if (object instanceof Float) {
			Float d = (Float) object;
			return d.longValue();
		} else if (object instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) object;
			return bd.longValue();
		} else if (object instanceof Boolean) {
			Boolean bb = (Boolean) object;
			return Boolean.TRUE.equals(bb) ? 1L : 0L;
		} else if (object instanceof String) {
			String s = (String) object;
			if (s.isEmpty()) {
				limit = withNulls ? null : 0L;
			} else {
				if (s.equals("true") || s.equals("false")) {
					Boolean b = Boolean.parseBoolean(s);
					return toLong(b);
				} else {
					if (s.contains(".")) {
						Double d = toDouble(s);
						limit = d.longValue();
					} else {
						limit = Long.parseLong(s);
					}
				}
			}
		} else {
			throw new RuntimeException("unsupported class: " + object.getClass());
		}
		return limit;
	}

	/**
	 * Returns year if object is instance of date, 0 otherwise.
	 * @param object to get year from.
	 * @return year or 0 if not a date.
	 */
	public int getYear(Object object) {
		if (!(object instanceof Date)) {
			return 0;
		}
		Date dt = (Date) object;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * Returns month if object is instance of date, 0 otherwise.
	 * @param object to get month from.
	 * @return month or 0 if not a date.
	 */
	public int getMonth(Object object) {
		if (!(object instanceof Date)) {
			return 0;
		}
		Date dt = (Date) object;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		return cal.get(Calendar.MONTH) + 1;
	}

	/**
	 * Returns day of month if object is instance of date, 0 otherwise.
	 * @param object to get day of month from.
	 * @return day of month or 0 if not a date.
	 */
	public int getDayOfMonth(Object object) {
		if (!(object instanceof Date)) {
			return 0;
		}
		Date dt = (Date) object;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		return cal.get(Calendar.DAY_OF_MONTH) + 1;
	}

	public String formatMinsAsDuration(Object o) {
		return formatMinsAsDuration(o, true);
	}

	/**
	 * Given a value which can be parsed to a Long, return it formatted as a
	 * human readable duration such as 12:30 (12 mins, 30 seconds) or 12 mins, 3
	 * hrs 20
	 *
	 * @param object which can be parsed as Long.
	 * @param numeric true if numeric representation divided by :.
	 * @return duration.
	 */
	public String formatMinsAsDuration(Object object, boolean numeric) {
		Long l = toLong(object);
		if (l == null) {
			return "";
		} else {
			if (l == 0) {
				return "";
			}
			long hours = l / 60;
			long mins = l % 60;
			if (numeric) {
				return hours + ":" + pad(mins, 2);
			} else {
				if (hours == 0) {
					return mins + "mins";
				} else if (hours == 1) {
					return hours + "hr " + mins;
				} else {
					return hours + "hrs " + mins;
				}
			}
		}
	}

	public String pad2(long l) {
		return pad(l, 2);
	}

	public String pad(long l, int length) {
		return padWith("0", l, length);
	}

	public String padWith(String padChar, long l, int length) {
		return _pad(padChar, l + "", length);
	}

	private String _pad(String padChar, String val, int length) {
		if (val.length() >= length) {
			return val;
		}
		return _pad(padChar, padChar + val, length);
	}

	/**
	 * Format as a percentage, including a percentage symbol and where
	 * blank/null values result in a blank output
	 *
	 * @param num - the numerator
	 * @param div - the divisor
	 * @return
	 */
	public String toPercent(Object num, Object div) {
		return toPercent(num, div, true, true);
	}

	/**
	 * @param num
	 * @param div
	 * @param appendSymbol - if true the percentage symbol is appended if a
	 *                     non-blank value
	 * @param withBlanks   - if true, blank numerators or divisors result in a
	 *                     blank value. Otherwise return zero.
	 * @return
	 */
	public String toPercent(Object num, Object div, boolean appendSymbol, boolean withBlanks) {
		Long lNum = toLong(num, true);
		Long lDiv = toLong(div, true);
		if (lDiv == null || lDiv == 0 || lNum == null) {
			if (withBlanks) {
				return "";
			} else {
				return "0" + (appendSymbol ? "%" : "");
			}
		} else {
			long perc = lNum * 100 / lDiv;
			return perc + (appendSymbol ? "%" : "");
		}
	}

	/**
	 * Removes the file extension if present
	 * <p>
	 * Eg file1.swf -> file1
	 * <p>
	 * file1 -> file1
	 *
	 * @param s
	 * @return
	 */
	public String stripExt(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		return FileUtils.stripExtension(s);
	}

	/**
	 * True if val1 is greater then val2
	 * <p>
	 * will do string conversions
	 *
	 * @param val1
	 * @param val2
	 * @return
	 */
	public boolean gt(Object val1, Object val2) {
		if (val1 == null) {
			return false;
		}
		if (val2 == null) {
			return true;
		}
		Double d1 = toDouble(val1);
		Double d2 = toDouble(val2);
		return d1 > d2;
	}

	public boolean lt(Object val1, Object val2) {
		if (val1 == null) {
			return false;
		}
		if (val2 == null) {
			return true;
		}
		Double d1 = toDouble(val1);
		Double d2 = toDouble(val2);
		return d1 < d2;
	}

	public boolean eq(Object val1, Object val2) {
		if (val1 == null) {
			return (val2 == null);
		}
		if (val2 == null) {
			return false;
		}
		Double d1 = toDouble(val1);
		Double d2 = toDouble(val2);
		return d1 == d2;
	}

	/**
	 * Decode percentage encoded paths. Eg a%20b -> a b
	 *
	 * @param s
	 * @return
	 */
	public String percentDecode(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		return Utils.decodePath(s);
	}

	public String percentEncode(String s) {
		if (s == null) {
			return null;
		}
		return Utils.percentEncode(s);
	}

	public Date toDate(Object oVal) {
		if (oVal == null) {
			return null;
		} else if (oVal instanceof Date) {
			return (Date) oVal;
		} else {
			if (oVal instanceof String) {
				String s = (String) oVal;
				return parseDate(s);
			} else {
				return null;
			}
		}
	}

	public java.sql.Date toSqlDate(Object oVal) {
		Date dt = toDate(oVal);
		if (dt == null) {
			return null;
		} else {
			return new java.sql.Date(dt.getTime());
		}
	}

	public java.sql.Timestamp toSqlTimestamp(Object oVal) {
		Date dt = toDate(oVal);
		if (dt == null) {
			return null;
		} else {
			return new java.sql.Timestamp(dt.getTime());
		}
	}

	public String toPlain(String html) {
		if (html == null) {
			return null;
		}
		html = replaceTag("br", html, "", "\n");
		html = replaceTag("p", html, "", "\n");
		html = replaceTag("b", html, "", "");
		html = replaceTag("i", html, "", "");
		html = replaceTag("h1", html, "", "");
		html = replaceTag("h2", html, "", "");
		html = replaceTag("h3", html, "", "");
		return html;
	}

	private String replaceTag(String tag, String html, String replaceWithOpening, String replaceWithClosing) {
		html = html.replace("<" + tag + "/>", replaceWithClosing); // self closing
		html = html.replace("<" + tag + ">", replaceWithOpening); // opening tag
		html = html.replace("</" + tag + ">", replaceWithClosing); // closing tag
		return html;
	}

	public String getMonthName(int i) {
		switch (i) {
		case 0:
			return "January";
		case 1:
			return "February";
		case 2:
			return "March";
		case 3:
			return "April";
		case 4:
			return "May";
		case 5:
			return "June";
		case 6:
			return "July";
		case 7:
			return "August";
		case 8:
			return "September";
		case 9:
			return "October";
		case 10:
			return "November";
		case 11:
			return "December";
		default:
			return "Unknown month " + i;
		}
	}

	public String ifEqual(String ifEqual, String ifNoteEqual, Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null ? ifEqual : ifNoteEqual;
		} else {
			return o1.equals(o2) ? ifEqual : ifNoteEqual;
		}
	}

	/**
	 * This just permits simple templating syntax for basic conditional values
	 * <p>
	 * Eg: <li><a class="$formatter.ifTrue($item.active, 'navActive', '')"
	 * href="$item.href">$item.text</a></li>
	 *
	 * @param bb
	 * @param o1
	 * @param o2
	 * @return
	 */
	public Object ifTrue(Object bb, Object o1, Object o2) {
		Boolean b = toBool(bb);
		if (b == null) {
			b = Boolean.FALSE;
		}
		return b ? o1 : o2;
	}

	private Date parseDate(String s) {
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		try {
			Date dt;
			if (s.contains(":")) {
				dt = sdf(true).parse(s);
			} else {
				dt = sdf(false).parse(s);
			}
			return dt;
		} catch (ParseException ex) {
			log.warn("couldnt parse date", ex);
			return null;
			//            throw new RuntimeException(ex);
		}
	}

	public DateFormat sdf(boolean hasTime) {
		if (hasTime) {
			return sdfDateAndTime.get();
		} else {
			return sdfDateOnly.get();
		}
	}

	public BigDecimal toBigDecimal(Object o, int decimals) {
		if (o instanceof Integer) {
			Integer ii = (Integer) o;
			return new BigDecimal(ii);
		} else if (o instanceof Double) {
			Double dd = (Double) o;
			return BigDecimal.valueOf(dd).setScale(decimals, RoundingMode.HALF_UP);
		} else if (o instanceof Float) {
			Float ff = (Float) o;
			return BigDecimal.valueOf(ff);
		} else if (o instanceof String) {
			Double dd = toDouble(o);
			return toBigDecimal(dd, decimals);
		} else {
			log.warn("unhandled type: {}", o.getClass());
			return null;
		}
	}

	public String checkbox(String name, Object oChecked) {
		return checkbox(null, name, oChecked, "true");
	}

	public String checkbox(String id, String name, Object oChecked) {
		return checkbox(id, name, oChecked, "true");
	}

	public String checkbox(String id, String name, Object oChecked, String value) {
		Boolean checked = toBool(oChecked);
		if (checked == null) {
			checked = Boolean.FALSE;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<input type='hidden' value='' name='").append(name).append(CHECKBOX_SUFFIX).append("'/>");
		sb.append("<input type=\"checkbox\"");
		sb.append(" name=\"").append(name).append("\" ");
		if (checked) {
			sb.append("checked=\"true\"");
		}
		appendValue(sb, value);
		if (id != null) {
			sb.append(" id=\"").append(id).append("\"");
		}
		sb.append(" />");
		return sb.toString();
	}

	public String radio(String id, String name, Object oChecked, String value) {
		Boolean checked = toBool(oChecked);
		if (checked == null) {
			checked = Boolean.FALSE;
		}
		StringBuilder sb = new StringBuilder("<input type=\"radio\"");
		sb.append(" name=\"").append(name).append("\"");
		if (checked) {
			sb.append(" checked=\"true\"");
		}
		appendValue(sb, value);
		if (id != null) {
			sb.append(" id=\"").append(id).append("\"");
		}
		sb.append(" />");
		return sb.toString();
	}

	/**
	 * Generate an option element
	 *
	 * @return
	 */
	public String option(Object value, String text, Object currentValue) {
		StringBuilder sb = new StringBuilder("<option");
		appendValue(sb, value);
		if (currentValue != null && currentValue.equals(value)) {
			sb.append("selected=\"true\"");
		}
		sb.append(">");
		sb.append(text).append("</option>");
		return sb.toString();

	}

	private void appendValue(StringBuilder sb, Object value) {
		sb.append(" value=");
		sb.append("\"");
		if (value != null) {
			sb.append(value);
		}
		sb.append("\"");
	}

	public String toCsv(Iterable list) {
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			for (Object o : list) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(o.toString());
			}
		}
		return sb.toString();
	}

	public String toCsv(String[] list) {
		return toCsv(Arrays.asList(list));
	}

	/**
	 * Return a date which has the given number of days added (or subtracted if
	 * negative) to the given date
	 *
	 * @param now
	 * @param days
	 * @return
	 */
	public Date addDays(Date now, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

}
