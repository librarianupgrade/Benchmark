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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class RadioTest extends AbstractUITagTest {

	public void testMapWithBooleanAsKey() throws Exception {
		TestAction testAction = (TestAction) action;

		HashMap map = new LinkedHashMap();
		map.put(Boolean.TRUE, "male");
		map.put(Boolean.FALSE, "female");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{true}");
		tag.setList("map");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-3.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testMapWithBooleanAsKey_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;

		HashMap map = new LinkedHashMap();
		map.put(Boolean.TRUE, "male");
		map.put(Boolean.FALSE, "female");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{true}");
		tag.setList("map");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-3.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testMapChecked() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		Map<String, String> map = new LinkedHashMap<>();
		map.put("1", "One");
		map.put("2", "Two");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("\"1\"");
		tag.setList("map");
		tag.setListKey("key");
		tag.setListValue("value");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-2.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testMapChecked_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		Map<String, String> map = new LinkedHashMap<>();
		map.put("1", "One");
		map.put("2", "Two");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("\"1\"");
		tag.setList("map");
		tag.setListKey("key");
		tag.setListValue("value");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-2.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testMapCheckedNull() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		HashMap map = new HashMap();
		map.put("1", "One");
		map.put("2", "Two");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{map['3']}");
		tag.setList("#@java.util.TreeMap@{\"1\":\"One\", \"2\":\"Two\", \"\":\"N/A\"}");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-4.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testMapCheckedNull_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");

		HashMap map = new HashMap();
		map.put("1", "One");
		map.put("2", "Two");
		testAction.setMap(map);

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("%{map['3']}");
		tag.setList("#@java.util.TreeMap@{\"1\":\"One\", \"2\":\"Two\", \"\":\"N/A\"}");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-4.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
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
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-1.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimple_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-1.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleWithStringMap() throws Exception {
		final Map<String, String> myMap = new TreeMap<String, String>();
		myMap.put("name", "Std.");
		stack.push(new HashMap() {
			{
				put("myMap", myMap);
			}
		});

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setName("myMap['name']");
		tag.setList("#@java.util.TreeMap@{\"Opt.\":\"Opt.\", \"Std.\":\"Std.\", \"\":\"N/A\"}");
		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleWithStringMap_clearTagStateSet() throws Exception {
		final Map<String, String> myMap = new TreeMap<String, String>();
		myMap.put("name", "Std.");
		stack.push(new HashMap() {
			{
				put("myMap", myMap);
			}
		});

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setName("myMap['name']");
		tag.setList("#@java.util.TreeMap@{\"Opt.\":\"Opt.\", \"Std.\":\"Std.\", \"\":\"N/A\"}");
		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-6.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleWithLabelSeparator() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");
		tag.setLabelSeparator("--");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-5.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testSimpleWithLabelSeparator_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");
		tag.setLabelSeparator("--");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-5.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testGenericSimple() throws Exception {
		RadioTag tag = new RadioTag();
		prepareTagGeneric(tag);
		verifyGenericProperties(tag, "simple", new String[] { "id", "value" });
	}

	public void testGenericXhtml() throws Exception {
		RadioTag tag = new RadioTag();
		prepareTagGeneric(tag);
		verifyGenericProperties(tag, "xhtml", new String[] { "id", "value" });
	}

	public void testDynamicAttributes() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");
		tag.setDynamicAttribute(null, "dojo", "checked: %{top[0]}");

		tag.doStartTag();
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-7.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testDynamicAttributes_clearTagStateSet() throws Exception {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });

		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setPageContext(pageContext);
		tag.setLabel("mylabel");
		tag.setName("myname");
		tag.setValue("");
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");
		tag.setDynamicAttribute(null, "dojo", "checked: %{top[0]}");

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(RadioTag.class.getResource("Radio-7.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testNotExistingListValueKey() throws Exception {
		RadioTag tag = new RadioTag();
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setList("#{'a':'aaa', 'b':'bbb', 'c':'ccc'}");
		tag.setListValueKey("notExistingProperty");

		tag.setPageContext(pageContext);

		tag.doStartTag();
		tag.doEndTag();

		verify(SelectTag.class.getResource("Radio-8.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPageContext(pageContext);
		assertFalse(
				"Tag state after doEndTag() under default tag clear state is equal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	public void testNotExistingListValueKey_clearTagStateSet() throws Exception {
		RadioTag tag = new RadioTag();
		tag.setPerformClearTagStateForTagPoolingServers(true); // Explicitly request tag state clearing.
		tag.setName("myname");
		tag.setLabel("mylabel");
		tag.setList("#{'a':'aaa', 'b':'bbb', 'c':'ccc'}");
		tag.setListValueKey("notExistingProperty");

		tag.setPageContext(pageContext);

		tag.doStartTag();
		setComponentTagClearTagState(tag, true); // Ensure component tag state clearing is set true (to match tag).
		tag.doEndTag();

		verify(SelectTag.class.getResource("Radio-8.txt"));

		// Basic sanity check of clearTagStateForTagPoolingServers() behaviour for Struts Tags after doEndTag().
		RadioTag freshTag = new RadioTag();
		freshTag.setPerformClearTagStateForTagPoolingServers(true);
		freshTag.setPageContext(pageContext);
		assertTrue(
				"Tag state after doEndTag() and explicit tag state clearing is inequal to new Tag with pageContext/parent set.  "
						+ "May indicate that clearTagStateForTagPoolingServers() calls are not working properly.",
				strutsBodyTagsAreReflectionEqual(tag, freshTag));
	}

	private void prepareTagGeneric(RadioTag tag) {
		TestAction testAction = (TestAction) action;
		testAction.setFoo("bar");
		testAction.setList(new String[][] { { "hello", "world" }, { "foo", "bar" } });
		tag.setList("list");
		tag.setListKey("top[0]");
		tag.setListValue("top[1]");
	}

}
