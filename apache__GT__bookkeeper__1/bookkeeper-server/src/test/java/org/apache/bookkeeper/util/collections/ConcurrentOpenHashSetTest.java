/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.bookkeeper.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

/**
 * Test the concurrent open HashSet class.
 */
public class ConcurrentOpenHashSetTest {

	@Test
	public void testConstructor() {
		try {
			ConcurrentOpenHashSet.<String>newBuilder().expectedItems(0).build();
			fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			ConcurrentOpenHashSet.<String>newBuilder().expectedItems(14).concurrencyLevel(0).build();
			fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			ConcurrentOpenHashSet.<String>newBuilder().expectedItems(4).concurrencyLevel(8).build();
			fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}

	@Test
	public void simpleInsertions() {
		ConcurrentOpenHashSet<String> set = ConcurrentOpenHashSet.<String>newBuilder().expectedItems(16).build();

		assertTrue(set.isEmpty());
		assertTrue(set.add("1"));
		assertFalse(set.isEmpty());

		assertTrue(set.add("2"));
		assertTrue(set.add("3"));

		assertEquals(set.size(), 3);

		assertTrue(set.contains("1"));
		assertEquals(set.size(), 3);

		assertTrue(set.remove("1"));
		assertEquals(set.size(), 2);
		assertFalse(set.contains("1"));
		assertFalse(set.contains("5"));
		assertEquals(set.size(), 2);

		assertTrue(set.add("1"));
		assertEquals(set.size(), 3);
		assertFalse(set.add("1"));
		assertEquals(set.size(), 3);
	}

	@Test
	public void testClear() {
		ConcurrentOpenHashSet<String> map = ConcurrentOpenHashSet.<String>newBuilder().expectedItems(2)
				.concurrencyLevel(1).autoShrink(true).mapIdleFactor(0.25f).build();
		assertTrue(map.capacity() == 4);

		assertTrue(map.add("k1"));
		assertTrue(map.add("k2"));
		assertTrue(map.add("k3"));

		assertTrue(map.capacity() == 8);
		map.clear();
		assertTrue(map.capacity() == 4);
	}

	@Test
	public void testExpandAndShrink() {
		ConcurrentOpenHashSet<String> map = ConcurrentOpenHashSet.<String>newBuilder().expectedItems(2)
				.concurrencyLevel(1).autoShrink(true).mapIdleFactor(0.25f).build();
		assertTrue(map.capacity() == 4);

		assertTrue(map.add("k1"));
		assertTrue(map.add("k2"));
		assertTrue(map.add("k3"));

		// expand hashmap
		assertTrue(map.capacity() == 8);

		assertTrue(map.remove("k1"));
		// not shrink
		assertTrue(map.capacity() == 8);
		assertTrue(map.remove("k2"));
		// shrink hashmap
		assertTrue(map.capacity() == 4);

		// expand hashmap
		assertTrue(map.add("k4"));
		assertTrue(map.add("k5"));
		assertTrue(map.capacity() == 8);

		//verify that the map does not keep shrinking at every remove() operation
		assertTrue(map.add("k6"));
		assertTrue(map.remove("k6"));
		assertTrue(map.capacity() == 8);
	}

	@Test
	public void testExpandShrinkAndClear() {
		ConcurrentOpenHashSet<String> map = ConcurrentOpenHashSet.<String>newBuilder().expectedItems(2)
				.concurrencyLevel(1).autoShrink(true).mapIdleFactor(0.25f).build();
		final long initCapacity = map.capacity();
		assertTrue(map.capacity() == 4);

		assertTrue(map.add("k1"));
		assertTrue(map.add("k2"));
		assertTrue(map.add("k3"));

		// expand hashmap
		assertTrue(map.capacity() == 8);

		assertTrue(map.remove("k1"));
		// not shrink
		assertTrue(map.capacity() == 8);
		assertTrue(map.remove("k2"));
		// shrink hashmap
		assertTrue(map.capacity() == 4);

		assertTrue(map.remove("k3"));
		// Will not shrink the hashmap again because shrink capacity is less than initCapacity
		// current capacity is equal than the initial capacity
		assertTrue(map.capacity() == initCapacity);
		map.clear();
		// after clear, because current capacity is equal than the initial capacity, so not shrinkToInitCapacity
		assertTrue(map.capacity() == initCapacity);
	}

	@Test
	public void testReduceUnnecessaryExpansions() {
		ConcurrentOpenHashSet<String> set = ConcurrentOpenHashSet.<String>newBuilder().expectedItems(2)
				.concurrencyLevel(1).build();

		assertTrue(set.add("1"));
		assertTrue(set.add("2"));
		assertTrue(set.add("3"));
		assertTrue(set.add("4"));

		assertTrue(set.remove("1"));
		assertTrue(set.remove("2"));
		assertTrue(set.remove("3"));
		assertTrue(set.remove("4"));
		assertEquals(0, set.getUsedBucketCount());
	}

	@Test
	public void testRemove() {
		ConcurrentOpenHashSet<String> set = ConcurrentOpenHashSet.<String>newBuilder().build();

		assertTrue(set.isEmpty());
		assertTrue(set.add("1"));
		assertFalse(set.isEmpty());

		assertFalse(set.remove("0"));
		assertFalse(set.isEmpty());
		assertTrue(set.remove("1"));
		assertTrue(set.isEmpty());
	}

	@Test
	public void testRehashing() {
		int n = 16;
		ConcurrentOpenHashSet<Integer> set = ConcurrentOpenHashSet.<Integer>newBuilder().expectedItems(n / 2)
				.concurrencyLevel(1).build();
		assertEquals(set.capacity(), n);
		assertEquals(set.size(), 0);

		for (int i = 0; i < n; i++) {
			set.add(i);
		}

		assertEquals(set.capacity(), 2 * n);
		assertEquals(set.size(), n);
	}

	@Test
	public void testRehashingWithDeletes() {
		int n = 16;
		ConcurrentOpenHashSet<Integer> set = ConcurrentOpenHashSet.<Integer>newBuilder().expectedItems(n / 2)
				.concurrencyLevel(1).build();
		assertEquals(set.capacity(), n);
		assertEquals(set.size(), 0);

		for (int i = 0; i < n / 2; i++) {
			set.add(i);
		}

		for (int i = 0; i < n / 2; i++) {
			set.remove(i);
		}

		for (int i = n; i < (2 * n); i++) {
			set.add(i);
		}

		assertEquals(set.capacity(), 2 * n);
		assertEquals(set.size(), n);
	}

	@Test
	public void concurrentInsertions() throws Throwable {
		ConcurrentOpenHashSet<Long> set = ConcurrentOpenHashSet.<Long>newBuilder().build();
		ExecutorService executor = Executors.newCachedThreadPool();

		final int nThreads = 16;
		final int n = 100_000;

		List<Future<?>> futures = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {
			final int threadIdx = i;

			futures.add(executor.submit(() -> {
				Random random = new Random();

				for (int j = 0; j < n; j++) {
					long key = random.nextLong();
					// Ensure keys are unique
					key -= key % (threadIdx + 1);

					set.add(key);
				}
			}));
		}

		for (Future<?> future : futures) {
			future.get();
		}

		assertEquals(set.size(), n * nThreads);

		executor.shutdown();
	}

	@Test
	public void concurrentInsertionsAndReads() throws Throwable {
		ConcurrentOpenHashSet<Long> map = ConcurrentOpenHashSet.<Long>newBuilder().build();
		ExecutorService executor = Executors.newCachedThreadPool();

		final int nThreads = 16;
		final int n = 100_000;

		List<Future<?>> futures = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {
			final int threadIdx = i;

			futures.add(executor.submit(() -> {
				Random random = new Random();

				for (int j = 0; j < n; j++) {
					long key = random.nextLong();
					// Ensure keys are unique
					key -= key % (threadIdx + 1);

					map.add(key);
				}
			}));
		}

		for (Future<?> future : futures) {
			future.get();
		}

		assertEquals(map.size(), n * nThreads);

		executor.shutdown();
	}

	@Test
	public void testIteration() {
		ConcurrentOpenHashSet<Long> set = ConcurrentOpenHashSet.<Long>newBuilder().build();

		assertEquals(set.values(), Collections.emptyList());

		set.add(0L);

		assertEquals(set.values(), Lists.newArrayList(0L));

		set.remove(0L);

		assertEquals(set.values(), Collections.emptyList());

		set.add(0L);
		set.add(1L);
		set.add(2L);

		List<Long> values = set.values();
		Collections.sort(values);
		assertEquals(values, Lists.newArrayList(0L, 1L, 2L));

		set.clear();
		assertTrue(set.isEmpty());
	}

	@Test
	public void testHashConflictWithDeletion() {
		final int buckets = 16;
		ConcurrentOpenHashSet<Long> set = ConcurrentOpenHashSet.<Long>newBuilder().expectedItems(buckets)
				.concurrencyLevel(1).build();

		// Pick 2 keys that fall into the same bucket
		long key1 = 1;
		long key2 = 27;

		int bucket1 = ConcurrentOpenHashSet.signSafeMod(ConcurrentOpenHashSet.hash(key1), buckets);
		int bucket2 = ConcurrentOpenHashSet.signSafeMod(ConcurrentOpenHashSet.hash(key2), buckets);
		assertEquals(bucket1, bucket2);

		assertTrue(set.add(key1));
		assertTrue(set.add(key2));
		assertEquals(set.size(), 2);

		assertTrue(set.remove(key1));
		assertEquals(set.size(), 1);

		assertTrue(set.add(key1));
		assertEquals(set.size(), 2);

		assertTrue(set.remove(key1));
		assertEquals(set.size(), 1);

		assertFalse(set.add(key2));
		assertTrue(set.contains(key2));

		assertEquals(set.size(), 1);
		assertTrue(set.remove(key2));
		assertTrue(set.isEmpty());
	}

	@Test
	public void testEqualsObjects() {
		class T {
			int value;

			T(int value) {
				this.value = value;
			}

			@Override
			public int hashCode() {
				return Integer.hashCode(value);
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof T) {
					return value == ((T) obj).value;
				}

				return false;
			}
		}

		ConcurrentOpenHashSet<T> set = ConcurrentOpenHashSet.<T>newBuilder().build();

		T t1 = new T(1);
		T t1B = new T(1);
		T t2 = new T(2);

		assertEquals(t1, t1B);
		assertFalse(t1.equals(t2));
		assertFalse(t1B.equals(t2));

		set.add(t1);
		assertTrue(set.contains(t1));
		assertTrue(set.contains(t1B));
		assertFalse(set.contains(t2));

		assertTrue(set.remove(t1B));
		assertFalse(set.contains(t1));
		assertFalse(set.contains(t1B));
	}

}
