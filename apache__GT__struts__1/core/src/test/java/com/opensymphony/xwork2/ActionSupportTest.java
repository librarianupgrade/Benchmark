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
package com.opensymphony.xwork2;

import com.opensymphony.xwork2.conversion.impl.ConversionData;
import com.opensymphony.xwork2.util.ValueStack;

import java.util.*;

/**
 * Unit test for {@link ActionSupport}.
 *
 * @author Claus Ibsen
 */
public class ActionSupportTest extends XWorkTestCase {

	private ActionSupport as;
	private MyActionSupport mas;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		as = new ActionSupport();
		container.inject(as);

		ActionContext.getContext().withLocale(new Locale("da"));

		mas = new MyActionSupport();
		container.inject(mas);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		as = null;
	}

	public void testNothingDoneOnActionSupport() throws Exception {
		assertFalse(as.hasErrors());

		assertNotNull(as.getActionErrors());
		assertEquals(0, as.getActionErrors().size());
		assertFalse(as.hasActionErrors());

		assertNotNull(as.getActionMessages());
		assertEquals(0, as.getActionMessages().size());
		assertFalse(as.hasActionMessages());

		assertNotNull(as.getFieldErrors());
		assertEquals(0, as.getFieldErrors().size());
		assertFalse(as.hasFieldErrors());

		assertNull(as.getText(null));

		try {
			as.pause(null);
		} catch (Exception e) {
			fail("Should not fail");
		}

		assertEquals(Action.INPUT, as.input());
		assertEquals(Action.SUCCESS, as.execute());
		try {
			as.clone();
			fail("Failure expected for clone()");
		} catch (CloneNotSupportedException e) {
			// success!
		}

		assertNull(as.getText(null, (List<?>) null));
		assertNull(as.getText(null, (String) null));
		assertNull(as.getText(null, (String[]) null));

		assertNull(as.getText(null, null, (List<?>) null));
		assertNull(as.getText(null, null, (String) null));
		assertNull(as.getText(null, null, (String[]) null));

		assertNull(as.getText(null, null, (List<?>) null, null));
		assertNull(as.getText(null, null, (String[]) null, null));

		assertNotNull(as.getLocale());
		assertEquals(ActionContext.getContext().getLocale(), as.getLocale());

		assertNull(as.getTexts()); // can not find a bundle
		assertEquals("not.in.bundle", as.getText("not.in.bundle"));
	}

	public void testActionErrors() {
		assertFalse(as.hasActionErrors());
		assertEquals(0, as.getActionErrors().size());
		as.addActionError("Damm");
		assertEquals(1, as.getActionErrors().size());
		assertEquals("Damm", as.getActionErrors().iterator().next());
		assertTrue(as.hasActionErrors());
		assertTrue(as.hasErrors());

		as.clearErrorsAndMessages();
		assertFalse(as.hasActionErrors());
		assertFalse(as.hasErrors());
	}

	public void testActionMessages() {
		assertFalse(as.hasActionMessages());
		assertEquals(0, as.getActionMessages().size());
		as.addActionMessage("Killroy was here");
		assertEquals(1, as.getActionMessages().size());
		assertEquals("Killroy was here", as.getActionMessages().iterator().next());
		assertTrue(as.hasActionMessages());

		assertFalse(as.hasActionErrors()); // does not count as a error
		assertFalse(as.hasErrors()); // does not count as a error

		as.clearErrorsAndMessages();
		assertFalse(as.hasActionMessages());
		assertFalse(as.hasErrors());
	}

	public void testFieldErrors() {
		assertFalse(as.hasFieldErrors());
		assertEquals(0, as.getFieldErrors().size());
		as.addFieldError("username", "Admin is not allowed as username");
		List<String> errors = as.getFieldErrors().get("username");
		assertEquals(1, errors.size());
		assertEquals("Admin is not allowed as username", errors.get(0));

		assertTrue(as.hasFieldErrors());
		assertTrue(as.hasErrors());

		as.clearErrorsAndMessages();
		assertFalse(as.hasFieldErrors());
		assertFalse(as.hasErrors());
	}

	public void testLocale() {
		Locale defLocale = Locale.getDefault();
		ActionContext.getContext().withLocale(null);

		// will never return null, if no locale is set then default is returned
		assertNotNull(as.getLocale());
		assertEquals(defLocale, as.getLocale());

		ActionContext.getContext().withLocale(Locale.ITALY);
		assertEquals(Locale.ITALY, as.getLocale());

		ActionContext.of(new HashMap<>()).bind();
		assertEquals(defLocale, as.getLocale()); // ActionContext will create a new context, when it was set to null before
	}

	public void testMyActionSupport() throws Exception {
		assertEquals("santa", mas.execute());
		assertNotNull(mas.getTexts());

		assertFalse(mas.hasActionMessages());
		mas.validate();
		assertTrue(mas.hasActionMessages());
	}

	public void testSimpleGetTexts() {
		checkGetTexts(mas);
	}

	public void testSimpleGetTextsWithInjectedTextProvider() {
		ActionContext.getContext().withLocale(new Locale("da"));
		MyActionSupport mas = new MyActionSupport();

		TextProvider textProvider = container.getInstance(TextProvider.class, "system");

		assertNotNull(textProvider);

		container.inject(mas);

		checkGetTexts(mas);
	}

	private void checkGetTexts(MyActionSupport mas) {
		assertEquals("Hello World", mas.getText("hello"));
		assertEquals("not.in.bundle", mas.getText("not.in.bundle"));

		assertEquals("Hello World", mas.getText("hello", "this is default"));
		assertEquals("this is default", mas.getText("not.in.bundle", "this is default"));

		assertEquals("Hello World", mas.getText("hello", (List<?>) null));

		assertEquals("Hello World", mas.getText("hello", (String[]) null));
	}

	public void testGetTextsWithArgs() {
		assertEquals("Hello World", mas.getText("hello", "this is default", "from me")); // no args in bundle
		assertEquals("Hello World from me", mas.getText("hello.0", "this is default", "from me"));
		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", "from me"));
		assertEquals("this is default from me", mas.getText("not.in.bundle", "this is default {0}", "from me"));

		assertEquals("not.in.bundle", mas.getText("not.in.bundle"));
	}

	public void testGetTextsWithListArgs() {
		List<Object> args = new ArrayList<>();
		args.add("Santa");
		args.add("loud");
		assertEquals("Hello World", mas.getText("hello", "this is default", args)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", "this is default", args)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud", mas.getText("hello.1", "this is default", args));

		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", args));
		assertEquals("this is default Santa", mas.getText("not.in.bundle", "this is default {0}", args));
		assertEquals("this is default Santa speaking loud",
				mas.getText("not.in.bundle", "this is default {0} speaking {1}", args));

		assertEquals("Hello World", mas.getText("hello", args)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", args)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud", mas.getText("hello.1", args));

		assertEquals("not.in.bundle", mas.getText("not.in.bundle", args));

		assertEquals("Hello World", mas.getText("hello", "this is default", (List<?>) null));
		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", (List<?>) null));
	}

	public void testGetTextsWithArrayArgs() {
		String[] args = { "Santa", "loud" };
		assertEquals("Hello World", mas.getText("hello", "this is default", args)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", "this is default", args)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud", mas.getText("hello.1", "this is default", args));

		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", args));
		assertEquals("this is default Santa", mas.getText("not.in.bundle", "this is default {0}", args));
		assertEquals("this is default Santa speaking loud",
				mas.getText("not.in.bundle", "this is default {0} speaking {1}", args));

		assertEquals("Hello World", mas.getText("hello", args)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", args)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud", mas.getText("hello.1", args));

		assertEquals("not.in.bundle", mas.getText("not.in.bundle", args));

		assertEquals("Hello World", mas.getText("hello", "this is default", (String[]) null));
		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", (String[]) null));
	}

	public void testGetTextsWithListAndStack() {
		ActionContext.getContext().withLocale(new Locale("da"));
		MyActionSupport mas = container.inject(MyActionSupport.class);

		ValueStack stack = ActionContext.getContext().getValueStack();

		List<Object> args = new ArrayList<>();
		args.add("Santa");
		args.add("loud");
		assertEquals("Hello World", mas.getText("hello", "this is default", args, stack)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", "this is default", args, stack)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud",
				mas.getText("hello.1", "this is default", args, stack));

		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", args, stack));
		assertEquals("this is default Santa", mas.getText("not.in.bundle", "this is default {0}", args, stack));
		assertEquals("this is default Santa speaking loud",
				mas.getText("not.in.bundle", "this is default {0} speaking {1}", args, stack));
	}

	public void testGetTextsWithArrayAndStack() {
		ActionContext.getContext().withLocale(new Locale("da"));
		MyActionSupport mas = container.inject(MyActionSupport.class);

		ValueStack stack = ActionContext.getContext().getValueStack();

		String[] args = { "Santa", "loud" };
		assertEquals("Hello World", mas.getText("hello", "this is default", args, stack)); // no args in bundle
		assertEquals("Hello World Santa", mas.getText("hello.0", "this is default", args, stack)); // only 1 arg in bundle
		assertEquals("Hello World. This is Santa speaking loud",
				mas.getText("hello.1", "this is default", args, stack));

		assertEquals("this is default", mas.getText("not.in.bundle", "this is default", args, stack));
		assertEquals("this is default Santa", mas.getText("not.in.bundle", "this is default {0}", args, stack));
		assertEquals("this is default Santa speaking loud",
				mas.getText("not.in.bundle", "this is default {0} speaking {1}", args, stack));
	}

	public void testGetBundle() {
		ResourceBundle rb = ResourceBundle.getBundle(MyActionSupport.class.getName(), new Locale("da"));
		assertEquals(rb, mas.getTexts(MyActionSupport.class.getName()));
	}

	public void testFormattingSupport() {
		ActionContext.getContext().getValueStack().push(mas);

		mas.setVal(234d);

		String formatted = mas.getFormatted("format.number", "val");

		assertEquals("234,0", formatted);
	}

	public void testFormattingSupportWithConversionError() {
		ActionContext.getContext().getConversionErrors().put("val",
				new ConversionData(new String[] { "4567def" }, Double.class));
		ActionContext.getContext().withLocale(new Locale("da"));
		MyActionSupport mas = new MyActionSupport();
		container.inject(mas);
		ActionContext.getContext().getValueStack().push(mas);

		mas.setVal(234d);

		String formatted = mas.getFormatted("format.number", "val");

		assertEquals("4567def", formatted);
	}

	public static class MyActionSupport extends ActionSupport {

		private Double val;

		@Override
		public String execute() throws Exception {
			return "santa";
		}

		@Override
		public void validate() {
			super.validate(); // to have code coverage
			addActionMessage("validation was called");
		}

		public Double getVal() {
			return val;
		}

		public void setVal(Double val) {
			this.val = val;
		}
	}

}
