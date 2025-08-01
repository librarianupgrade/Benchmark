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
package com.opensymphony.xwork2.conversion.impl;

import com.opensymphony.xwork2.*;
import com.opensymphony.xwork2.ognl.OgnlValueStack;
import com.opensymphony.xwork2.test.ModelDrivenAction2;
import com.opensymphony.xwork2.test.User;
import com.opensymphony.xwork2.util.Bar;
import com.opensymphony.xwork2.util.Cat;
import com.opensymphony.xwork2.util.Foo;
import com.opensymphony.xwork2.util.FurColor;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;
import ognl.OgnlRuntime;
import ognl.TypeConverter;
import org.apache.struts2.components.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;

import java.util.Date;
import java.util.Set;

/**
 * @author $Author$
 * @version $Revision$
 */
public class XWorkConverterTest extends XWorkTestCase {

	Map<String, Object> context;
	XWorkConverter converter;
	OgnlValueStack stack;

	//    public void testConversionToSetKeepsOriginalSetAndReplacesContents() {
	//        ValueStack stack = ValueStackFactory.getFactory().createValueStack();
	//
	//        Map stackContext = stack.getContext();
	//        stackContext.put(InstantiatingNullHandler.CREATE_NULL_OBJECTS, Boolean.TRUE);
	//        stackContext.put(XWorkMethodAccessor.DENY_METHOD_EXECUTION, Boolean.TRUE);
	//        stackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);
	//
	//        String[] param = new String[] {"abc", "def", "ghi"};
	//        List paramList = Arrays.asList(param);
	//
	//        List originalList = new ArrayList();
	//        originalList.add("jkl");
	//        originalList.add("mno");
	//
	//        User user = new User();
	//        user.setList(originalList);
	//        stack.push(user);
	//
	//        stack.setValue("list", param);
	//
	//        List userList = user.getList();
	//        assertEquals(3,userList.size());
	//        assertEquals(paramList,userList);
	//        assertSame(originalList,userList);
	//    }

	public void testArrayToNumberConversion() {
		String[] value = new String[] { "12345" };
		assertEquals(12345, converter.convertValue(context, null, null, null, value, Integer.class));
		assertEquals(12345L, converter.convertValue(context, null, null, null, value, Long.class));
		value[0] = "123.45";
		assertEquals(123.45f, converter.convertValue(context, null, null, null, value, Float.class));
		assertEquals(123.45, converter.convertValue(context, null, null, null, value, Double.class));
		value[0] = "1234567890123456789012345678901234567890";
		assertEquals(new BigInteger(value[0]),
				converter.convertValue(context, null, null, null, value, BigInteger.class));
		value[0] = "1234567890123456789.012345678901234567890";
		assertEquals(new BigDecimal(value[0]),
				converter.convertValue(context, null, null, null, value, BigDecimal.class));
	}

	public void testDateConversion() throws ParseException {
		java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
		assertEquals(sqlDate, converter.convertValue(context, null, null, null, sqlDate, Date.class));

		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		Date date = format.parse("01/10/2001 00:00:00");

		SimpleDateFormat formatt = new SimpleDateFormat("hh:mm:ss");
		java.sql.Time datet = new java.sql.Time(formatt.parse("10:11:12").getTime());

		String dateStr = (String) converter.convertValue(context, null, null, null, date, String.class);
		String datetStr = (String) converter.convertValue(context, null, null, null, datet, String.class);

		Date date2 = (Date) converter.convertValue(context, null, null, null, dateStr, Date.class);
		assertEquals(date, date2);
		java.sql.Date date3 = (java.sql.Date) converter.convertValue(context, null, null, null, dateStr,
				java.sql.Date.class);
		assertEquals(date, date3);
		java.sql.Timestamp ts = (java.sql.Timestamp) converter.convertValue(context, null, null, null, dateStr,
				java.sql.Timestamp.class);
		assertEquals(date, ts);
		java.sql.Time time1 = (java.sql.Time) converter.convertValue(context, null, null, null, datetStr,
				java.sql.Time.class);
		assertEquals(datet, time1);

		Date dateWithTime = format.parse("01/10/2001 01:02:03");
		Date dateRfc3339 = (Date) converter.convertValue(context, null, null, null, "2001-01-10T01:02:03", Date.class);
		assertEquals(dateWithTime, dateRfc3339);

		Date dateRfc3339DateOnly = (Date) converter.convertValue(context, null, null, null, "2001-01-10", Date.class);
		assertEquals(date, dateRfc3339DateOnly);
	}

	public void testDateConversionWithDefault() throws ParseException {
		Map<String, String> lookupMap = new HashMap<>();
		TextProvider tp = new StubTextProvider(lookupMap);
		StubValueStack valueStack = new StubValueStack();
		valueStack.push(tp);
		context.put(ActionContext.VALUE_STACK, valueStack);

		String dateToFormat = "2017---06--15";
		Object unparseableDate = converter.convertValue(context, null, null, null, dateToFormat, Date.class);
		assertEquals(unparseableDate, com.opensymphony.xwork2.conversion.TypeConverter.NO_CONVERSION_POSSIBLE);

		lookupMap.put(org.apache.struts2.components.Date.DATETAG_PROPERTY, "yyyy---MM--dd");

		SimpleDateFormat format = new SimpleDateFormat("yyyy---MM--dd");
		Date expectedDate = format.parse(dateToFormat);
		Object parseableDate = converter.convertValue(context, null, null, null, dateToFormat, Date.class);
		assertEquals(expectedDate, parseableDate);

		Object standardDate = converter.convertValue(context, null, null, null, "2017-06-15", Date.class);
		assertEquals(expectedDate, standardDate);

	}

	public void testFieldErrorMessageAddedForComplexProperty() {
		SimpleAction action = new SimpleAction();
		action.setBean(new TestBean());

		stack.push(action);

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);
		ognlStackContext.put(XWorkConverter.CONVERSION_PROPERTY_FULLNAME, "bean.birth");

		String[] value = new String[] { "invalid date" };
		assertEquals("Conversion should have failed.", OgnlRuntime.NoConversionPossible,
				converter.convertValue(ognlStackContext, action.getBean(), null, "birth", value, Date.class));
		stack.pop();

		Map<String, ConversionData> conversionErrors = stack.getActionContext().getConversionErrors();
		assertNotNull(conversionErrors);
		assertEquals(1, conversionErrors.size());
		assertEquals(value, conversionErrors.get("bean.birth").getValue());
	}

	public void testFieldErrorMessageAddedWhenConversionFails() {
		SimpleAction action = new SimpleAction();
		action.setDate(null);

		stack.push(action);

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		String[] value = new String[] { "invalid date" };
		assertEquals("Conversion should have failed.", OgnlRuntime.NoConversionPossible,
				converter.convertValue(ognlStackContext, action, null, "date", value, Date.class));
		stack.pop();

		Map<String, ConversionData> conversionErrors = ActionContext.of(ognlStackContext).getConversionErrors();
		assertNotNull(conversionErrors);
		assertEquals(1, conversionErrors.size());
		assertNotNull(conversionErrors.get("date"));
		assertEquals(value, conversionErrors.get("date").getValue());
	}

	public void testFieldErrorMessageAddedWhenConversionFailsOnModelDriven() {
		ModelDrivenAction action = new ModelDrivenAction();
		stack.push(action);
		stack.push(action.getModel());

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		String[] value = new String[] { "invalid date" };
		assertEquals("Conversion should have failed.", OgnlRuntime.NoConversionPossible,
				converter.convertValue(ognlStackContext, action, null, "birth", value, Date.class));
		stack.pop();
		stack.pop();

		Map<String, ConversionData> conversionErrors = ActionContext.of(ognlStackContext).getConversionErrors();
		assertNotNull(conversionErrors);
		assertEquals(1, conversionErrors.size());
		assertNotNull(conversionErrors.get("birth"));
		assertEquals(value, conversionErrors.get("birth").getValue());
	}

	public void testDateStrictConversion() throws Exception {
		// see XW-341
		String dateStr = "13/01/2005"; // us date format is used in context
		Object res = converter.convertValue(context, null, null, null, dateStr, Date.class);
		assertEquals(res, OgnlRuntime.NoConversionPossible);

		dateStr = "02/30/2005"; // us date format is used in context
		res = converter.convertValue(context, null, null, null, dateStr, Date.class);
		assertEquals(res, OgnlRuntime.NoConversionPossible);

		// and test a date that is passable
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		dateStr = "12/31/2005"; // us date format
		res = converter.convertValue(context, null, null, null, dateStr, Date.class);
		Date date = format.parse(dateStr);
		assertNotSame(res, OgnlRuntime.NoConversionPossible);
		assertEquals(date, res);
	}

	public void testFindConversionErrorMessage() {
		ModelDrivenAction action = new ModelDrivenAction();
		container.inject(action);

		stack.push(action);
		stack.push(action.getModel());

		String message = XWorkConverter.getConversionErrorMessage("birth", Integer.class, stack);
		assertNotNull(message);
		assertEquals("Invalid date for birth.", message);

		message = XWorkConverter.getConversionErrorMessage("foo", Integer.class, stack);
		assertNotNull(message);
		assertEquals("Invalid field value for field \"foo\".", message);
	}

	public void testFindConversionMappingForInterface() {
		ModelDrivenAction2 action = new ModelDrivenAction2();
		stack.push(action);
		stack.push(action.getModel());

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		String value = "asdf:123";
		Object o = converter.convertValue(ognlStackContext, action.getModel(), null, "barObj", value, Bar.class);
		assertNotNull(o);
		assertTrue(o instanceof Bar);

		Bar b = (Bar) o;
		assertEquals(value, b.getTitle() + ":" + b.getSomethingElse());
	}

	public void testDefaultFieldConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("baz", int.class, stack);
		assertNotNull(message);
		assertEquals("Invalid field value for field \"baz\".", message);
	}

	public void testCustomFieldConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("foo", int.class, stack);
		assertNotNull(message);
		assertEquals("Custom error message for foo.", message);
	}

	public void testCustomPrimitiveConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("percentage", double.class, stack);
		assertNotNull(message);
		assertEquals("Custom error message for double.", message);
	}

	public void testCustomClassConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("date", Date.class, stack);
		assertNotNull(message);
		assertEquals("Custom error message for java.util.Date.", message);
	}

	public void testDefaultIndexedConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("beanList[0].name", String.class, stack);
		assertNotNull(message);
		assertEquals("Invalid field value for field \"beanList[0].name\".", message);
	}

	public void testCustomIndexedFieldConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("beanList[0].count", int.class, stack);
		assertNotNull(message);
		assertEquals("Custom error message for beanList.count.", message);
	}

	public void testCustomIndexedClassConversionErrorMessage() {
		SimpleAction action = new SimpleAction();
		container.inject(action);

		stack.push(action);

		String message = XWorkConverter.getConversionErrorMessage("beanList[0].birth", Date.class, stack);
		assertNotNull(message);
		assertEquals("Custom error message for java.util.Date.", message);
	}

	public void testLocalizedDateConversion() {
		Date date = new Date(System.currentTimeMillis());
		Locale locale = Locale.GERMANY;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		String dateString = df.format(date);
		context = ActionContext.of(context).withLocale(locale).getContextMap();

		assertEquals(dateString, converter.convertValue(context, null, null, null, date, String.class));
	}

	public void testStringToIntConversions() {
		SimpleAction action = new SimpleAction();
		action.setBean(new TestBean());

		stack.push(action);

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		assertEquals("Conversion should have failed.", OgnlRuntime.NoConversionPossible,
				converter.convertValue(ognlStackContext, action.getBean(), null, "count", "111.1", int.class));
		stack.pop();

		Map<String, ConversionData> conversionErrors = stack.getActionContext().getConversionErrors();
		assertNotNull(conversionErrors);
		assertEquals(1, conversionErrors.size());
	}

	public void testStringArrayToCollection() {
		List<String> list = new ArrayList<>();
		list.add("foo");
		list.add("bar");
		list.add("baz");
		assertEquals(list, converter.convertValue(context, null, null, null, new String[] { "foo", "bar", "baz" },
				Collection.class));
	}

	public void testStringArrayToList() {
		List<String> list = new ArrayList<>();
		list.add("foo");
		list.add("bar");
		list.add("baz");
		assertEquals(list,
				converter.convertValue(context, null, null, null, new String[] { "foo", "bar", "baz" }, List.class));
	}

	public void testStringArrayToPrimitiveWrappers() {
		Long[] longs = (Long[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				Long[].class);
		assertNotNull(longs);
		assertArrayEquals(new Long[] { 123L, 456L }, longs);

		Integer[] ints = (Integer[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				Integer[].class);
		assertNotNull(ints);
		assertArrayEquals(new Integer[] { 123, 456 }, ints);

		Double[] doubles = (Double[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				Double[].class);
		assertNotNull(doubles);
		assertArrayEquals(new Double[] { 123D, 456D }, doubles);

		Float[] floats = (Float[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				Float[].class);
		assertNotNull(floats);
		assertArrayEquals(new Float[] { 123F, 456F }, floats);

		Boolean[] booleans = (Boolean[]) converter.convertValue(context, null, null, null,
				new String[] { "true", "false" }, Boolean[].class);
		assertNotNull(booleans);
		assertArrayEquals(new Boolean[] { Boolean.TRUE, Boolean.FALSE }, booleans);
	}

	public void testStringArrayToPrimitives() {
		long[] longs = (long[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				long[].class);
		assertNotNull(longs);
		assertArrayEquals(new long[] { 123, 456 }, longs);

		int[] ints = (int[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				int[].class);
		assertNotNull(ints);
		assertArrayEquals(new int[] { 123, 456 }, ints);

		double[] doubles = (double[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				double[].class);
		assertNotNull(doubles);
		assertArrayEquals(new double[] { 123, 456 }, doubles, 0.0);

		float[] floats = (float[]) converter.convertValue(context, null, null, null, new String[] { "123", "456" },
				float[].class);
		assertNotNull(floats);
		assertArrayEquals(new float[] { 123, 456 }, floats, 0.0f);

		boolean[] booleans = (boolean[]) converter.convertValue(context, null, null, null,
				new String[] { "true", "false" }, boolean[].class);
		assertNotNull(booleans);
		assertArrayEquals(new boolean[] { true, false }, booleans);
	}

	public void testStringArrayToSet() {
		Set<String> list = new HashSet<>();
		list.add("foo");
		list.add("bar");
		list.add("baz");
		assertEquals(list, converter.convertValue(context, null, null, null,
				new String[] { "foo", "bar", "bar", "baz" }, Set.class));
	}

	public void testStringToCollectionConversion() {
		Map<String, Object> stackContext = stack.getContext();
		stackContext.put(ReflectionContextState.CREATE_NULL_OBJECTS, Boolean.TRUE);
		stackContext.put(ReflectionContextState.DENY_METHOD_EXECUTION, Boolean.TRUE);
		stackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		User user = new User();
		stack.push(user);

		stack.setValue("list", "asdf");
		assertNotNull(user.getList());
		assertEquals(1, user.getList().size());
		assertEquals(String.class, user.getList().get(0).getClass());
		assertEquals("asdf", user.getList().get(0));
	}

	public void testStringToCustomTypeUsingCustomConverter() {
		// the converter needs to be registered as the Bar.class converter 
		// it won't be detected from the Foo-conversion.properties
		// because the Foo-conversion.properties file is only used when converting a property of Foo
		converter.registerConverter(Bar.class.getName(), new FooBarConverter());

		Bar bar = (Bar) converter.convertValue(null, null, null, null, "blah:123", Bar.class);
		assertNotNull("conversion failed", bar);
		assertEquals(123, bar.getSomethingElse());
		assertEquals("blah", bar.getTitle());
	}

	public void testStringToCustomTypeUsingCustomConverterFromProperties() throws Exception {

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new ClassLoader(cl) {
				@Override
				public Enumeration<URL> getResources(String name) throws IOException {
					if ("struts-conversion.properties".equals(name)) {
						return new Enumeration<URL>() {
							boolean done = false;

							public boolean hasMoreElements() {
								return !done;
							}

							public URL nextElement() {
								if (done) {
									throw new RuntimeException("Conversion configuration loading "
											+ "failed because it asked the enumeration for the next URL "
											+ "too many times");
								}

								done = true;
								return getClass().getResource(
										"/com/opensymphony/xwork2/conversion/impl/test-struts-conversion.properties");
							}
						};
					} else {
						return super.getResources(name);
					}
				}
			});
			setUp();
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		Bar bar = (Bar) converter.convertValue(null, null, null, null, "blah:123", Bar.class);
		assertNotNull("conversion failed", bar);
		assertEquals(123, bar.getSomethingElse());
		assertEquals("blah", bar.getTitle());
	}

	public void testStringToPrimitiveWrappers() {
		assertEquals(123L, converter.convertValue(context, null, null, null, "123", Long.class));
		assertEquals(123, converter.convertValue(context, null, null, null, "123", Integer.class));
		assertEquals(123.5, converter.convertValue(context, null, null, null, "123.5", Double.class));
		assertEquals(123.5f, converter.convertValue(context, null, null, null, "123.5", float.class));
		assertEquals(false, converter.convertValue(context, null, null, null, "false", Boolean.class));
		assertEquals(true, converter.convertValue(context, null, null, null, "true", Boolean.class));
	}

	public void testStringToPrimitives() {
		assertEquals(123L, converter.convertValue(context, null, null, null, "123", long.class));
		assertEquals(123.5, converter.convertValue(context, null, null, null, "123.5", double.class));
		assertEquals(123.5f, converter.convertValue(context, null, null, null, "123.5", float.class));
		assertEquals(false, converter.convertValue(context, null, null, null, "false", boolean.class));
		assertEquals(true, converter.convertValue(context, null, null, null, "true", boolean.class));
		assertEquals(new BigDecimal("123.5"),
				converter.convertValue(context, null, null, null, "123.5", BigDecimal.class));
		assertEquals(new BigInteger("123"), converter.convertValue(context, null, null, null, "123", BigInteger.class));
	}

	public void testOverflows() {
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Double.MAX_VALUE + "1", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Double.MIN_VALUE + "-1", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Double.MAX_VALUE + "1", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Double.MIN_VALUE + "-1", Double.class));

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Float.MAX_VALUE + "1", float.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Float.MIN_VALUE + "-1", float.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Float.MAX_VALUE + "1", Float.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Float.MIN_VALUE + "-1", Float.class));

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Integer.MAX_VALUE + "1", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Integer.MIN_VALUE + "-1", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Integer.MAX_VALUE + "1", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Integer.MIN_VALUE + "-1", Integer.class));

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Byte.MAX_VALUE + "1", byte.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Byte.MIN_VALUE + "-1", byte.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Byte.MAX_VALUE + "1", Byte.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Byte.MIN_VALUE + "-1", Byte.class));

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Short.MAX_VALUE + "1", short.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Short.MIN_VALUE + "-1", short.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Short.MAX_VALUE + "1", Short.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Short.MIN_VALUE + "-1", Short.class));

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Long.MAX_VALUE + "1", long.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Long.MIN_VALUE + "-1", long.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Long.MAX_VALUE + "1", Long.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, Long.MIN_VALUE + "-1", Long.class));
	}

	public void testStringToInt() {
		assertEquals(123, converter.convertValue(context, null, null, null, "123", int.class));
		context = ActionContext.of(context).withLocale(Locale.US).getContextMap();
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123.12", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,23", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", int.class));
		context = ActionContext.of(context).withLocale(Locale.GERMANY).getContextMap();
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123.12", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,23", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234", int.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", int.class));
	}

	public void testStringToInteger() {
		assertEquals(123, converter.convertValue(context, null, null, null, "123", Integer.class));
		context = ActionContext.of(context).withLocale(Locale.US).getContextMap();
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123.12", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", Integer.class));
		assertEquals(1234, converter.convertValue(context, null, null, null, "1,234", Integer.class));
		// WRONG: locale separator is wrongly placed
		assertEquals(123, converter.convertValue(context, null, null, null, "1,23", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", Integer.class));

		context = ActionContext.of(context).withLocale(Locale.GERMANY).getContextMap();
		// WRONG: locale separator is wrongly placed
		assertEquals(12312, converter.convertValue(context, null, null, null, "123.12", Integer.class));
		assertEquals(1234, converter.convertValue(context, null, null, null, "1.234", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,23", Integer.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", Integer.class));
	}

	public void testStringToPrimitiveDouble() {
		assertEquals(123d, converter.convertValue(context, null, null, null, "123", double.class));
		context = ActionContext.of(context).withLocale(Locale.US).getContextMap();
		assertEquals(123.12, converter.convertValue(context, null, null, null, "123.12", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", double.class));
		assertEquals(1234d, converter.convertValue(context, null, null, null, "1,234", double.class));
		assertEquals(1234.12, converter.convertValue(context, null, null, null, "1,234.12", double.class));
		assertEquals(123d, converter.convertValue(context, null, null, null, "1,23", double.class));
		assertEquals(1.234, converter.convertValue(context, null, null, null, "1.234", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", double.class));

		context = ActionContext.of(context).withLocale(Locale.GERMANY).getContextMap();
		assertEquals(12312d, converter.convertValue(context, null, null, null, "123.12", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", double.class));
		assertEquals(1.234, converter.convertValue(context, null, null, null, "1,234", double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", double.class));
		assertEquals(1.23, converter.convertValue(context, null, null, null, "1,23", double.class));
		assertEquals(1234d, converter.convertValue(context, null, null, null, "1.234", double.class));
		assertEquals(1234.12, converter.convertValue(context, null, null, null, "1.234,12", double.class));
	}

	public void testStringToDouble() {
		assertEquals(123d, converter.convertValue(context, null, null, null, "123", Double.class));
		context = ActionContext.of(context).withLocale(Locale.US).getContextMap();
		assertEquals(123.12, converter.convertValue(context, null, null, null, "123.12", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", Double.class));
		assertEquals(1234d, converter.convertValue(context, null, null, null, "1,234", Double.class));
		assertEquals(1234.12, converter.convertValue(context, null, null, null, "1,234.12", Double.class));
		// WRONG: locale separator is wrongly placed 
		assertEquals(123d, converter.convertValue(context, null, null, null, "1,23", Double.class));
		assertEquals(1.234, converter.convertValue(context, null, null, null, "1.234", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1.234,12", Double.class));

		context = ActionContext.of(context).withLocale(Locale.GERMANY).getContextMap();
		// WRONG: locale separator is wrongly placed
		assertEquals(12312d, converter.convertValue(context, null, null, null, "123.12", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "123aa", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "aa123", Double.class));
		assertEquals(1.234, converter.convertValue(context, null, null, null, "1,234", Double.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "1,234.12", Double.class));
		assertEquals(1.23, converter.convertValue(context, null, null, null, "1,23", Double.class));
		assertEquals(1234d, converter.convertValue(context, null, null, null, "1.234", Double.class));
		assertEquals(1234.12, converter.convertValue(context, null, null, null, "1.234,12", Double.class));

	}

	public void testStringToEnum() {
		assertEquals(FurColor.BLACK, converter.convertValue(context, null, null, null, "BLACK", FurColor.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "black", FurColor.class));
		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, null, null, null, "red", FurColor.class));
	}

	// Testing for null result on non-primitive Number types supplied as empty String or 
	public void testNotPrimitiveDefaultsToNull() {
		assertNull(converter.convertValue(context, null, null, null, null, Double.class));
		assertNull(converter.convertValue(context, null, null, null, "", Double.class));

		assertNull(converter.convertValue(context, null, null, null, null, Integer.class));
		assertNull(converter.convertValue(context, null, null, null, "", Integer.class));

		assertNull(converter.convertValue(context, null, null, null, null, Float.class));
		assertNull(converter.convertValue(context, null, null, null, "", Float.class));

		assertNull(converter.convertValue(context, null, null, null, null, Character.class));
		assertNull(converter.convertValue(context, null, null, null, "", Character.class));

		assertNull(converter.convertValue(context, null, null, null, null, Long.class));
		assertNull(converter.convertValue(context, null, null, null, "", Long.class));

		assertNull(converter.convertValue(context, null, null, null, null, Short.class));
		assertNull(converter.convertValue(context, null, null, null, "", Short.class));

	}

	public void testConvertChar() {
		assertEquals('A', converter.convertValue(context, "A", char.class));
		assertEquals('Z', converter.convertValue(context, "Z", char.class));
		assertEquals('A', converter.convertValue(context, "A", Character.class));
		assertEquals('Z', converter.convertValue(context, "Z", Character.class));

		assertEquals('A', converter.convertValue(context, 'A', char.class));
		assertEquals('Z', converter.convertValue(context, 'Z', char.class));
		assertEquals('A', converter.convertValue(context, 'A', Character.class));
		assertEquals('Z', converter.convertValue(context, 'Z', Character.class));

		assertEquals('D', converter.convertValue(context, "DEF", char.class));
		assertEquals('X', converter.convertValue(context, "XYZ", Character.class));
		assertEquals(' ', converter.convertValue(context, " ", Character.class));
		assertEquals(' ', converter.convertValue(context, "   ", char.class));

		assertNull(converter.convertValue(context, "", char.class));
	}

	public void testConvertClass() {
		Class<?> clazz = (Class<?>) converter.convertValue(context, "java.util.Date", Class.class);
		assertEquals(Date.class.getName(), clazz.getName());

		Class<?> clazz2 = (Class<?>) converter.convertValue(context, "com.opensymphony.xwork2.util.Bar", Class.class);
		assertEquals(Bar.class.getName(), clazz2.getName());

		assertEquals(OgnlRuntime.NoConversionPossible,
				converter.convertValue(context, "com.opensymphony.xwork2.util.IDoNotExist", Class.class));

		assertEquals(OgnlRuntime.NoConversionPossible, converter.convertValue(context, new Bar(), Class.class)); // only supports string values
	}

	public void testConvertBoolean() {
		assertEquals(Boolean.TRUE, converter.convertValue(context, "true", Boolean.class));
		assertEquals(Boolean.FALSE, converter.convertValue(context, "false", Boolean.class));

		assertEquals(Boolean.TRUE, converter.convertValue(context, Boolean.TRUE, Boolean.class));
		assertEquals(Boolean.FALSE, converter.convertValue(context, Boolean.FALSE, Boolean.class));

		assertNull(converter.convertValue(context, null, Boolean.class));
		assertEquals(Boolean.TRUE, converter.convertValue(context, new Bar(), Boolean.class)); // Ognl converter will default to true
	}

	public void testConvertPrimitiveArraysToString() {
		assertEquals("2, 3, 1", converter.convertValue(context, new int[] { 2, 3, 1 }, String.class));
		assertEquals("100, 200, 300", converter.convertValue(context, new long[] { 100, 200, 300 }, String.class));
		assertEquals("1.5, 2.5, 3.5", converter.convertValue(context, new double[] { 1.5, 2.5, 3.5 }, String.class));
		assertEquals("true, false, true",
				converter.convertValue(context, new boolean[] { true, false, true }, String.class));
	}

	public void testConvertSameCollectionToCollection() {
		Collection<String> names = new ArrayList<>();
		names.add("XWork");
		names.add("Struts");

		Collection<String> col = (Collection<String>) converter.convertValue(context, names, Collection.class);
		assertSame(names, col);
	}

	public void testConvertSqlTimestamp() {
		assertNotNull(converter.convertValue(context, new Timestamp(new Date().getTime()), String.class));
		assertNotNull(converter.convertValue(null, new Timestamp(new Date().getTime()), String.class));
	}

	public void testValueStackWithTypeParameter() {
		stack.push(new Foo1());
		Bar1 bar = (Bar1) stack.findValue("bar", Bar1.class);
		assertNotNull(bar);
	}

	public void testNestedConverters() {
		Cat cat = new Cat();
		cat.setFoo(new Foo());
		stack.push(cat);
		stack.setValue("foo.number", "123");
		assertEquals(321, cat.getFoo().getNumber());
	}

	public void testCollectionConversion() {
		// given
		String[] col1 = new String[] { "1", "2", "ble", "3" };

		// when
		Object converted = converter.convertValue(context, new ListAction(), null, "ints", col1, List.class);

		// then
		assertEquals(converted, Arrays.asList(1, 2, 3));
	}

	public static class Foo1 {
		public Bar1 getBar() {
			return new Bar1Impl();
		}
	}

	public interface Bar1 {
	}

	public static class Bar1Impl implements Bar1 {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		converter = container.getInstance(XWorkConverter.class);

		ActionContext ac = ActionContext.getContext().withLocale(Locale.US);
		context = ac.getContextMap();
		stack = (OgnlValueStack) ac.getValueStack();
	}

}

class ListAction {

	private List<Integer> ints = new ArrayList<>();

	public List<Integer> getInts() {
		return ints;
	}

	public void setInts(List<Integer> ints) {
		this.ints = ints;
	}

}