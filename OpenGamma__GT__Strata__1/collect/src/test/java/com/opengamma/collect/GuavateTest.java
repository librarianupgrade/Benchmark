/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.collect;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

/**
 * Test.
 */
@Test
public class GuavateTest {

	//-------------------------------------------------------------------------
	public void test_stream_Iterable() {
		Iterable<String> iterable = Arrays.asList("a", "b", "c");
		List<String> test = Guavate.stream(iterable).collect(Collectors.toList());
		assertEquals(test, ImmutableList.of("a", "b", "c"));
	}

	//-------------------------------------------------------------------------
	public void test_toImmutableList() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableList<String> test = list.stream().filter(s -> s.length() == 1).collect(Guavate.toImmutableList());
		assertEquals(test, ImmutableList.of("a", "b", "c", "a"));
	}

	public void test_toImmutableSet() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableSet<String> test = list.stream().filter(s -> s.length() == 1).collect(Guavate.toImmutableSet());
		assertEquals(test, ImmutableSet.of("a", "b", "c"));
	}

	public void test_toImmutableSortedSet() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableSortedSet<String> test = list.stream().filter(s -> s.length() == 1)
				.collect(Guavate.toImmutableSortedSet());
		assertEquals(test, ImmutableSortedSet.of("a", "b", "c"));
	}

	public void test_toImmutableSortedSet_comparator() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableSortedSet<String> test = list.stream().filter(s -> s.length() == 1)
				.collect(Guavate.toImmutableSortedSet(Ordering.natural().reverse()));
		assertEquals(test, ImmutableSortedSet.reverseOrder().add("a").add("b").add("c").build());
	}

	public void test_toImmutableMultiset() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableMultiset<String> test = list.stream().filter(s -> s.length() == 1)
				.collect(Guavate.toImmutableMultiset());
		assertEquals(test, ImmutableMultiset.of("a", "a", "b", "c"));
	}

	//-------------------------------------------------------------------------
	public void test_toImmutableMap_key() {
		List<String> list = Arrays.asList("a", "ab", "bob");
		ImmutableMap<Integer, String> test = list.stream().collect(Guavate.toImmutableMap(s -> s.length()));
		assertEquals(test, ImmutableMap.builder().put(1, "a").put(2, "ab").put(3, "bob").build());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_toImmutableMap_key_duplicateKeys() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		list.stream().collect(Guavate.toImmutableMap(s -> s.length()));
	}

	public void test_toImmutableMap_keyValue() {
		List<String> list = Arrays.asList("a", "ab", "bob");
		ImmutableMap<Integer, String> test = list.stream()
				.collect(Guavate.toImmutableMap(s -> s.length(), s -> "!" + s));
		assertEquals(test, ImmutableMap.builder().put(1, "!a").put(2, "!ab").put(3, "!bob").build());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_toImmutableMap_keyValue_duplicateKeys() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		list.stream().collect(Guavate.toImmutableMap(s -> s.length(), s -> "!" + s));
	}

	//-------------------------------------------------------------------------
	public void test_toImmutableListMultimap_key() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableListMultimap<Integer, String> test = list.stream()
				.collect(Guavate.toImmutableListMultimap(s -> s.length()));
		ImmutableListMultimap<Object, Object> expected = ImmutableListMultimap.builder().put(1, "a").put(2, "ab")
				.put(1, "b").put(2, "bb").put(1, "c").put(1, "a").build();
		assertEquals(test, expected);
	}

	public void test_toImmutableListMultimap_keyValue() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableListMultimap<Integer, String> test = list.stream()
				.collect(Guavate.toImmutableListMultimap(s -> s.length(), s -> "!" + s));
		ImmutableListMultimap<Object, Object> expected = ImmutableListMultimap.builder().put(1, "!a").put(2, "!ab")
				.put(1, "!b").put(2, "!bb").put(1, "!c").put(1, "!a").build();
		assertEquals(test, expected);
	}

	//-------------------------------------------------------------------------
	public void test_toImmutableSetMultimap_key() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableSetMultimap<Integer, String> test = list.stream()
				.collect(Guavate.toImmutableSetMultimap(s -> s.length()));
		ImmutableSetMultimap<Object, Object> expected = ImmutableSetMultimap.builder().put(1, "a").put(2, "ab")
				.put(1, "b").put(2, "bb").put(1, "c").put(1, "a").build();
		assertEquals(test, expected);
	}

	public void test_toImmutableSetMultimap_keyValue() {
		List<String> list = Arrays.asList("a", "ab", "b", "bb", "c", "a");
		ImmutableSetMultimap<Integer, String> test = list.stream()
				.collect(Guavate.toImmutableSetMultimap(s -> s.length(), s -> "!" + s));
		ImmutableSetMultimap<Object, Object> expected = ImmutableSetMultimap.builder().put(1, "!a").put(2, "!ab")
				.put(1, "!b").put(2, "!bb").put(1, "!c").build();
		assertEquals(test, expected);
	}

	//-------------------------------------------------------------------------
	public void coverage() {
		TestHelper.coverPrivateConstructor(Guavate.class);
	}

}
