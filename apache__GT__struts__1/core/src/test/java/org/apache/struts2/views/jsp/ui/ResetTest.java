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
package org.apache.struts2.views.jsp.ui;

import org.apache.struts2.TestAction;
import org.apache.struts2.views.jsp.AbstractUITagTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Reset Component Test.
 *
 */
public class ResetTest extends AbstractUITagTest {

	public void testDefaultValues() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setTitle("mytitle");
		tag.setSrc("/images/test.png");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-2.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testDefaultValues_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setTitle("mytitle");
		tag.setSrc("/images/test.png");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-2.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimple() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setTabindex("1");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-1.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimple_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setTabindex("1");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-1.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testButtonSimple() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setTabindex("1");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-3.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testButtonSimple_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setTabindex("1");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-3.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testButtonWithLabel() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-4.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testButtonWithLabel_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-4.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageSimple() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setDisabled("true");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-5.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageSimple_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setValue("%{foo}");
		tag.setDisabled("true");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-5.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageWithSrc() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setValue("%{foo}");
		tag.setSrc("some.gif");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageWithSrc_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setValue("%{foo}");
		tag.setSrc("some.gif");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageWithExpressionSrc() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setValue("%{foo}");
		tag.setSrc("%{getText(\"some.image.from.properties\")}");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testImageWithExpressionSrc_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setValue("%{foo}");
		tag.setSrc("%{getText(\"some.image.from.properties\")}");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingActionAndMethod() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction("manager");
		tag.setMethod("update");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-7.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingActionAndMethod_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction("manager");
		tag.setMethod("update");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-7.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingActionOnly() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction("manager");
		tag.setMethod(null); // no method

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-8.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingActionOnly_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction("manager");
		tag.setMethod(null); // no method

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-8.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingMethodOnly() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction(null); // no action
		tag.setMethod("update");

		tag.doStartTag();
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-9.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleThemeImageUsingMethodOnly_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		ResetTag tag = new ResetTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setTheme("simple");
		tag.setType("button");
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setAction(null); // no action
		tag.setMethod("update");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(TextFieldTag.class.getResource("Reset-9.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		ResetTag freshTag = new ResetTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	/**
	 * Initialize a map of {@link org.apache.struts2.views.jsp.AbstractUITagTest.PropertyHolder} for generic tag
	 * property testing. Will be used when calling {@link #verifyGenericProperties(AbstractUITag,
	 * String, String[])} as properties to verify.<br> This implementation extends testdata from AbstractUITag.
	 *
	 * @return A Map of PropertyHolders values bound to {@link org.apache.struts2.views.jsp.AbstractUITagTest.PropertyHolder#getName()}
	 *         as key.
	 */
	@Override
	protected Map initializedGenericTagTestProperties() {
		Map result = new HashMap();
		new PropertyHolder("title", "someTitle").addToMap(result);
		new PropertyHolder("cssClass", "cssClass1", "class=\"cssClass1\"").addToMap(result);
		new PropertyHolder("cssStyle", "cssStyle1", "style=\"cssStyle1\"").addToMap(result);
		new PropertyHolder("name", "someName").addToMap(result);
		new PropertyHolder("value", "someValue").addToMap(result);
		return result;
	}

	public void testGenericSimple() throws Exception {
		ResetTag tag = new ResetTag();
		verifyGenericProperties(tag, "simple", null);
	}

	public void testGenericXhtml() throws Exception {
		ResetTag tag = new ResetTag();
		verifyGenericProperties(tag, "xhtml", null);
	}
}
