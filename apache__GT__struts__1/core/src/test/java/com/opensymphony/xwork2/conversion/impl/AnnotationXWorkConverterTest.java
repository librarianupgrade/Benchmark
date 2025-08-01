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
import com.opensymphony.xwork2.test.AnnotationUser;
import com.opensymphony.xwork2.test.ModelDrivenAnnotationAction2;
import com.opensymphony.xwork2.util.Bar;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;
import ognl.OgnlRuntime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author $Author$
 * @author Rainer Hermanns
 * @version $Revision$
 */
public class AnnotationXWorkConverterTest extends XWorkTestCase {

	ActionContext ac;
	Map<String, Object> context;
	XWorkConverter converter;

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
	//        AnnotationUser user = new AnnotationUser();
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
		String dateStr = (String) converter.convertValue(context, null, null, null, date, String.class);
		Date date2 = (Date) converter.convertValue(context, null, null, null, dateStr, Date.class);
		assertEquals(date, date2);
	}

	public void testFieldErrorMessageAddedForComplexProperty() {
		SimpleAnnotationAction action = new SimpleAnnotationAction();
		action.setBean(new AnnotatedTestBean());

		ValueStack stack = ActionContext.getContext().getValueStack();
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
		SimpleAnnotationAction action = new SimpleAnnotationAction();
		action.setDate(null);

		ValueStack stack = ActionContext.getContext().getValueStack();
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
		ModelDrivenAnnotationAction action = new ModelDrivenAnnotationAction();
		ValueStack stack = ActionContext.getContext().getValueStack();
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

	public void testFindConversionErrorMessage() {
		ModelDrivenAnnotationAction action = new ModelDrivenAnnotationAction();
		container.inject(action);

		ValueStack stack = ActionContext.getContext().getValueStack();
		stack.push(action);
		stack.push(action.getModel());

		String message = XWorkConverter.getConversionErrorMessage("birth", Integer.class, stack);
		assertNotNull(message);
		assertEquals("Invalid date for birth.", message);

		message = XWorkConverter.getConversionErrorMessage("foo", Integer.class, stack);
		assertNotNull(message);
		assertEquals("Invalid field value for field \"foo\".", message);
	}

	public void testFindConversionMappingForInterfaceAndSuperclass() {
		ModelDrivenAnnotationAction2 action = new ModelDrivenAnnotationAction2();
		ValueStack stack = ActionContext.getContext().getValueStack();
		stack.push(action);
		stack.push(action.getModel());

		Map<String, Object> ognlStackContext = stack.getContext();
		ognlStackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		String value = "asdf:123";
		Object o = converter.convertValue(ognlStackContext, action.getModel(), null, "barObj", value, Bar.class);
		assertNotNull(o);
		assertTrue("class is: " + o.getClass(), o instanceof Bar);

		Bar b = (Bar) o;
		assertEquals(value, b.getTitle() + ":" + b.getSomethingElse());

		String value2 = "qwer:456";
		Object o2 = converter.convertValue(ognlStackContext, action.getModel(), null, "supperBarObj", value2,
				Bar.class);
		assertNotNull(o2);
		assertTrue("class is: " + o.getClass(), o2 instanceof Bar);

		Bar b2 = (Bar) o2;
		assertEquals(value2, b2.getTitle() + ":" + b2.getSomethingElse());
	}

	public void testLocalizedDateConversion() {
		Date date = new Date(System.currentTimeMillis());
		Locale locale = Locale.GERMANY;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		String dateString = df.format(date);
		context = ActionContext.of(context).withLocale(locale).getContextMap();
		assertEquals(dateString, converter.convertValue(context, null, null, null, date, String.class));
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

	// TODO: Fixme... This test does not work with GenericsObjectDeterminer!
	public void testStringToCollectionConversion() {
		ValueStack stack = ActionContext.getContext().getValueStack();
		Map<String, Object> stackContext = stack.getContext();
		stackContext.put(ReflectionContextState.CREATE_NULL_OBJECTS, Boolean.TRUE);
		stackContext.put(ReflectionContextState.DENY_METHOD_EXECUTION, Boolean.TRUE);
		stackContext.put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

		AnnotationUser user = new AnnotationUser();
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
		assertEquals(123, converter.convertValue(context, null, null, null, "123", int.class));
		assertEquals(123.5, converter.convertValue(context, null, null, null, "123.5", double.class));
		assertEquals(123.5f, converter.convertValue(context, null, null, null, "123.5", float.class));
		assertEquals(false, converter.convertValue(context, null, null, null, "false", boolean.class));
		assertEquals(true, converter.convertValue(context, null, null, null, "true", boolean.class));
		assertEquals(new BigDecimal("123.5"),
				converter.convertValue(context, null, null, null, "123.5", BigDecimal.class));
		assertEquals(new BigInteger("123"), converter.convertValue(context, null, null, null, "123", BigInteger.class));
	}

	public void testValueStackWithTypeParameter() {
		ValueStack stack = ActionContext.getContext().getValueStack();
		stack.push(new Foo1());
		Bar1 bar = (Bar1) stack.findValue("bar", Bar1.class);
		assertNotNull(bar);
	}

	public void testGenericProperties() {
		GenericsBean gb = new GenericsBean();
		ValueStack stack = ac.getValueStack();
		stack.push(gb);

		String[] value = new String[] { "123.12", "123.45" };
		stack.setValue("doubles", value);
		assertEquals(2, gb.getDoubles().size());
		assertEquals(Double.class, gb.getDoubles().get(0).getClass());
		assertEquals(123.12, gb.getDoubles().get(0));
		assertEquals(123.45, gb.getDoubles().get(1));
	}

	public void testGenericPropertiesFromField() {
		GenericsBean gb = new GenericsBean();
		ValueStack stack = ac.getValueStack();
		stack.push(gb);

		stack.setValue("genericMap[123.12]", "66");
		stack.setValue("genericMap[456.12]", "42");

		assertEquals(2, gb.getGenericMap().size());
		assertEquals("66", stack.findValue("genericMap.get(123.12).toString()"));
		assertEquals("42", stack.findValue("genericMap.get(456.12).toString()"));
		assertEquals(66, stack.findValue("genericMap.get(123.12)"));
		assertEquals(42, stack.findValue("genericMap.get(456.12)"));
		assertEquals(true, stack.findValue("genericMap.containsValue(66)"));
		assertEquals(true, stack.findValue("genericMap.containsValue(42)"));
		assertEquals(true, stack.findValue("genericMap.containsKey(123.12)"));
		assertEquals(true, stack.findValue("genericMap.containsKey(456.12)"));
	}

	public void testGenericPropertiesFromSetter() {
		GenericsBean gb = new GenericsBean();
		ValueStack stack = ac.getValueStack();
		stack.push(gb);

		stack.setValue("genericMap[123.12]", "66");
		stack.setValue("genericMap[456.12]", "42");

		assertEquals(2, gb.getGenericMap().size());
		assertEquals("66", stack.findValue("genericMap.get(123.12).toString()"));
		assertEquals("42", stack.findValue("genericMap.get(456.12).toString()"));
		assertEquals(66, stack.findValue("genericMap.get(123.12)"));
		assertEquals(42, stack.findValue("genericMap.get(456.12)"));
		assertEquals(true, stack.findValue("genericMap.containsValue(66)"));
		assertEquals(true, stack.findValue("genericMap.containsValue(42)"));
		assertEquals(true, stack.findValue("genericMap.containsKey(123.12)"));
		assertEquals(true, stack.findValue("genericMap.containsKey(456.12)"));
	}

	public void testGenericPropertiesFromGetter() {
		GenericsBean gb = new GenericsBean();
		ValueStack stack = ac.getValueStack();
		stack.push(gb);

		assertEquals(1, gb.getGetterList().size());
		assertEquals("42.42", stack.findValue("getterList.get(0).toString()"));
		assertEquals(42.42, stack.findValue("getterList.get(0)"));
		assertEquals(42.42, gb.getGetterList().get(0));

	}

	// FIXME: Implement nested Generics such as: List of Generics List, Map of Generic keys/values, etc...
	public void no_testGenericPropertiesWithNestedGenerics() {
		GenericsBean gb = new GenericsBean();
		ValueStack stack = ac.getValueStack();
		stack.push(gb);

		stack.setValue("extendedMap[123.12]", new String[] { "1", "2", "3", "4" });
		stack.setValue("extendedMap[456.12]", new String[] { "5", "6", "7", "8", "9" });

		System.out.println("gb.getExtendedMap(): " + gb.getExtendedMap());

		assertEquals(2, gb.getExtendedMap().size());
		System.out.println(stack.findValue("extendedMap"));
		assertEquals(4, stack.findValue("extendedMap.get(123.12).size"));
		assertEquals(5, stack.findValue("extendedMap.get(456.12).size"));

		assertEquals("1", stack.findValue("extendedMap.get(123.12).get(0)"));
		assertEquals("5", stack.findValue("extendedMap.get(456.12).get(0)"));
		assertEquals(Integer.class, stack.findValue("extendedMap.get(123.12).get(0).class"));
		assertEquals(Integer.class, stack.findValue("extendedMap.get(456.12).get(0).class"));

		assertEquals(List.class, stack.findValue("extendedMap.get(123.12).class"));
		assertEquals(List.class, stack.findValue("extendedMap.get(456.12).class"));

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

		ac = ActionContext.getContext().withLocale(Locale.US);
		context = ac.getContextMap();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ActionContext.clear();
	}
}
