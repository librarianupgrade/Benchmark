/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.components;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.components.date.DateFormatter;
import org.apache.struts2.views.annotations.StrutsTag;
import org.apache.struts2.views.annotations.StrutsTagAttribute;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * <!-- START SNIPPET: javadoc -->
 * <p>
 * Format Date object in different ways.
 * <p>
 * The date tag will allow you to format a Date in a quick and easy way.
 * You can specify a <b>custom format</b> (eg. "dd/MM/yyyy hh:mm"), you can generate
 * <b>easy readable notations</b> (like "in 2 hours, 14 minutes"), or you can just fall back
 * on a <b>predefined format</b> with key 'struts.date.format' in your properties file.
 * </p>
 *
 * <p>
 * If that key is not defined, it will finally fall back to the default DateFormat.MEDIUM
 * formatting.
 * </p>
 *
 * <p>
 * <b>Note</b>: If the requested Date object isn't found on the stack, a blank will be returned.
 * </p>
 *
 * <p>
 * <b>Note</b>: Since Struts 2.6 a new Java 8 API has been used to format the Date, it's based on
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 * which uses a bit different patterns.
 * </p>
 *
 * <p>
 * Configurable attributes are:
 * </p>
 *
 * <ul>
 *    <li>name</li>
 *    <li>nice</li>
 *    <li>format</li>
 * </ul>
 *
 * <p>
 * Following how the date component will work, depending on the value of nice attribute
 * (which by default is false) and the format attribute.
 * </p>
 *
 * <p>
 * <b><u>Condition 1: With nice attribute as true</u></b>
 * </p>
 * <table border="1" summary="">
 *   <tr>
 *      <td>i18n key</td>
 *      <td>default</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.past</td>
 *      <td>{0} ago</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.future</td>
 *      <td>in {0}</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.seconds</td>
 *      <td>an instant</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.minutes</td>
 *      <td>{0,choice,1#one minute|1&lt;{0} minutes}</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.hours</td>
 *      <td>{0,choice,1#one hour|1&lt;{0} hours}{1,choice,0#|1#, one minute|1&lt;, {1} minutes}</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.days</td>
 *      <td>{0,choice,1#one day|1&lt;{0} days}{1,choice,0#|1#, one hour|1&lt;, {1} hours}</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format.years</td>
 *      <td>{0,choice,1#one year|1&lt;{0} years}{1,choice,0#|1#, one day|1&lt;, {1} days}</td>
 *   </tr>
 * </table>
 *
 * <p>
 * <b><u>Condition 2: With nice attribute as false and format attribute is specified eg. dd/MM/yyyyy </u></b>
 * </p>
 *
 * <p>In this case the format attribute will be used.</p>
 *
 * <p>
 * <b><u>Condition 3: With nice attribute as false and no format attribute is specified </u></b>
 * </p>
 * <table border="1" summary="">
 *    <tr>
 *      <td>i18n key</td>
 *      <td>default</td>
 *   </tr>
 *   <tr>
 *      <td>struts.date.format</td>
 *      <td>if one is not found DateFormat.MEDIUM format will be used</td>
 *   </tr>
 * </table>
 * <p>
 * <p>
 * <!-- END SNIPPET: javadoc -->
 *
 * <p><b>Examples</b></p>
 * <pre>
 *  <!-- START SNIPPET: example -->
 *  &lt;s:date name="person.birthday" format="dd/MM/yyyy" /&gt;
 *  &lt;s:date name="person.birthday" format="%{getText('some.i18n.key')}" /&gt;
 *  &lt;s:date name="person.birthday" nice="true" /&gt;
 *  &lt;s:date name="person.birthday" /&gt;
 *  <!-- END SNIPPET: example -->
 * </pre>
 *
 * <code>Date</code>
 */
@StrutsTag(name = "date", tldBodyContent = "empty", tldTagClass = "org.apache.struts2.views.jsp.DateTag", description = "Render a formatted date.")
public class Date extends ContextBean {

	private static final Logger LOG = LogManager.getLogger(Date.class);

	/**
	 * Property name to fall back when no format is specified
	 */
	public static final String DATETAG_PROPERTY = "struts.date.format";
	/**
	 * Property name that defines the past notation (default: {0} ago)
	 */
	public static final String DATETAG_PROPERTY_PAST = "struts.date.format.past";
	private static final String DATETAG_DEFAULT_PAST = "{0} ago";
	/**
	 * Property name that defines the future notation (default: in {0})
	 */
	public static final String DATETAG_PROPERTY_FUTURE = "struts.date.format.future";
	private static final String DATETAG_DEFAULT_FUTURE = "in {0}";
	/**
	 * Property name that defines the seconds notation (default: in instant)
	 */
	public static final String DATETAG_PROPERTY_SECONDS = "struts.date.format.seconds";
	private static final String DATETAG_DEFAULT_SECONDS = "an instant";
	/**
	 * Property name that defines the minutes notation (default: {0,choice,1#one minute|1&lt;{0} minutes})
	 */
	public static final String DATETAG_PROPERTY_MINUTES = "struts.date.format.minutes";
	private static final String DATETAG_DEFAULT_MINUTES = "{0,choice,1#one minute|1<{0} minutes}";
	/**
	 * Property name that defines the hours notation (default: {0,choice,1#one hour|1&lt;{0} hours}{1,choice,0#|1#, one
	 * minute|1&gt;, {1} minutes})
	 */
	public static final String DATETAG_PROPERTY_HOURS = "struts.date.format.hours";
	private static final String DATETAG_DEFAULT_HOURS = "{0,choice,1#one hour|1<{0} hours}{1,choice,0#|1#, one minute|1<, {1} minutes}";
	/**
	 * Property name that defines the days notation (default: {0,choice,1#one day|1&lt;{0} days}{1,choice,0#|1#, one hour|1&lt;,
	 * {1} hours})
	 */
	public static final String DATETAG_PROPERTY_DAYS = "struts.date.format.days";
	private static final String DATETAG_DEFAULT_DAYS = "{0,choice,1#one day|1<{0} days}{1,choice,0#|1#, one hour|1<, {1} hours}";
	/**
	 * Property name that defines the years notation (default: {0,choice,1#one year|1&lt;{0} years}{1,choice,0#|1#, one
	 * day|1&gt;, {1} days})
	 */
	public static final String DATETAG_PROPERTY_YEARS = "struts.date.format.years";
	private static final String DATETAG_DEFAULT_YEARS = "{0,choice,1#one year|1<{0} years}{1,choice,0#|1#, one day|1<, {1} days}";

	private String name;

	private String format;

	private boolean nice;

	private String timezone;

	private DateFormatter dateFormatter;

	public Date(ValueStack stack) {
		super(stack);
	}

	/**
	 * An instance of {@link DateFormatter}
	 */
	@Inject
	public void setDateFormatter(DateFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
	}

	/**
	 * Calculates the difference in time from now to the given date, and outputs it nicely. <br> An example: <br>
	 * Now = 2006/03/12 13:38:00, date = 2006/03/12 15:50:00 will output "in 1 hour, 12 minutes".
	 *
	 * @param tp   text provider
	 * @param date the date
	 * @return the date nicely
	 */
	public String formatTime(TextProvider tp, ZonedDateTime date) {
		ZonedDateTime now = ZonedDateTime.now();
		StringBuilder sb = new StringBuilder();
		List<Object> args = new ArrayList<>();
		long secs = Math.abs(now.toEpochSecond() - date.toEpochSecond());
		long mins = secs / 60;
		long sec = secs % 60;
		int min = (int) mins % 60;
		long hours = mins / 60;
		int hour = (int) hours % 24;
		int days = (int) hours / 24;
		int day = days % 365;
		int years = days / 365;

		if (years > 0) {
			args.add(years);
			args.add(day);
			args.add(sb);
			args.add(null);
			sb.append(tp.getText(DATETAG_PROPERTY_YEARS, DATETAG_DEFAULT_YEARS, args));
		} else if (day > 0) {
			args.add(day);
			args.add(hour);
			args.add(sb);
			args.add(null);
			sb.append(tp.getText(DATETAG_PROPERTY_DAYS, DATETAG_DEFAULT_DAYS, args));
		} else if (hour > 0) {
			args.add(hour);
			args.add(min);
			args.add(sb);
			args.add(null);
			sb.append(tp.getText(DATETAG_PROPERTY_HOURS, DATETAG_DEFAULT_HOURS, args));
		} else if (min > 0) {
			args.add(min);
			args.add(sec);
			args.add(sb);
			args.add(null);
			sb.append(tp.getText(DATETAG_PROPERTY_MINUTES, DATETAG_DEFAULT_MINUTES, args));
		} else {
			args.add(sec);
			args.add(sb);
			args.add(null);
			sb.append(tp.getText(DATETAG_PROPERTY_SECONDS, DATETAG_DEFAULT_SECONDS, args));
		}

		args.clear();
		args.add(sb.toString());
		if (date.isBefore(now)) {
			// looks like this date is passed
			return tp.getText(DATETAG_PROPERTY_PAST, DATETAG_DEFAULT_PAST, args);
		} else {
			return tp.getText(DATETAG_PROPERTY_FUTURE, DATETAG_DEFAULT_FUTURE, args);
		}
	}

	@Override
	public boolean end(Writer writer, String body) {
		TextProvider textProvider = findProviderInStack();

		ZonedDateTime date = null;
		final ZoneId tz = getTimeZone();
		// find the name on the valueStack
		Object dateObject = findValue(name);
		if (dateObject instanceof java.util.Date) {
			date = ((java.util.Date) dateObject).toInstant().atZone(tz);
		} else if (dateObject instanceof Calendar) {
			date = ((Calendar) dateObject).toInstant().atZone(tz);
		} else if (dateObject instanceof Long) {
			date = Instant.ofEpochMilli((long) dateObject).atZone(tz);
		} else if (dateObject instanceof LocalDateTime) {
			date = ((LocalDateTime) dateObject).atZone(tz);
		} else if (dateObject instanceof LocalDate) {
			date = ((LocalDate) dateObject).atStartOfDay(tz);
		} else if (dateObject instanceof Instant) {
			date = ((Instant) dateObject).atZone(tz);
		} else {
			if (devMode) {
				String developerNotification = "";
				if (textProvider != null) {
					developerNotification = textProvider.getText("devmode.notification", "Developer Notification:\n{0}",
							new String[] { "Expression [" + name + "] passed to <s:date/> tag which was evaluated to ["
									+ dateObject + "](" + (dateObject != null ? dateObject.getClass() : "null")
									+ ") isn't supported!" });
				}
				LOG.warn(developerNotification);
			} else {
				LOG.debug("Expression [{}] passed to <s:date/> tag which was evaluated to [{}]({}) isn't supported!",
						name, dateObject, (dateObject != null ? dateObject.getClass() : "null"));
			}
		}

		//try to find the format on the stack
		if (format != null) {
			format = findString(format);
		}
		String msg;
		if (date != null) {
			if (textProvider != null) {
				if (nice) {
					msg = formatTime(textProvider, date);
				} else {
					msg = formatDate(textProvider, date);
				}
				if (msg != null) {
					try {
						if (getVar() == null) {
							writer.write(msg);
						} else {
							putInContext(msg);
						}
					} catch (IOException e) {
						LOG.error("Could not write out Date tag", e);
					}
				}
			}
		}
		return super.end(writer, "");
	}

	private String formatDate(TextProvider textProvider, ZonedDateTime date) {
		String useFormat = format;
		if (useFormat == null) {
			// if the format is not specified, fall back using the defined property DATETAG_PROPERTY
			useFormat = textProvider.getText(DATETAG_PROPERTY);
			if (DATETAG_PROPERTY.equals(useFormat)) {
				// if tp.getText can not find the property then the
				// returned string is the same as input = DATETAG_PROPERTY
				useFormat = null;
			}
		}
		return dateFormatter.format(date, useFormat);
	}

	private ZoneId getTimeZone() {
		ZoneId tz = ZoneId.systemDefault();
		if (timezone != null) {
			timezone = stripExpression(timezone);
			String actualTimezone = (String) getStack().findValue(timezone, String.class);
			if (actualTimezone != null) {
				timezone = actualTimezone;
			}
			tz = ZoneId.of(timezone);
		}
		return tz;
	}

	private TextProvider findProviderInStack() {
		for (Object o : getStack().getRoot()) {
			if (o instanceof TextProvider) {
				return (TextProvider) o;
			}
		}
		return null;
	}

	@StrutsTagAttribute(description = "Date or DateTime format pattern")
	public void setFormat(String format) {
		this.format = format;
	}

	@StrutsTagAttribute(description = "Whether to print out the date nicely", type = "Boolean", defaultValue = "false")
	public void setNice(boolean nice) {
		this.nice = nice;
	}

	@StrutsTagAttribute(description = "The specific timezone in which to format the date")
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	/**
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	@StrutsTagAttribute(description = "The date value to format", required = true)
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the format.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return the nice.
	 */
	public boolean isNice() {
		return nice;
	}

	/**
	 * @return the timezone.
	 */
	public String getTimezone() {
		return timezone;
	}

}
