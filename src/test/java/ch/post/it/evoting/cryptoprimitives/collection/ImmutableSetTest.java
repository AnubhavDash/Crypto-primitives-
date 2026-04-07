/*
 * Copyright 2026 Swiss Post Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.post.it.evoting.cryptoprimitives.collection;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableSet.toImmutableSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ImmutableSetTest {

	@Test
	void testImmutability() {
		final Set<String> mutable = new HashSet<>();
		mutable.add("a");
		mutable.add("b");
		mutable.add("c");

		final ImmutableSet<String> set = ImmutableSet.from(mutable);
		final Set<String> unmodifiable = set.asSet();
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.add("d"));
		assertTrue(set.contains("a"));
		assertFalse(set.asSet().contains("d"));

		mutable.add("d");
		assertTrue(mutable.contains("d"));
		assertFalse(set.contains("d"));
	}

	@Test
	void testFrom() {
		final Set<String> mutable = new HashSet<>();
		mutable.add("a");
		mutable.add("b");
		mutable.add("c");

		final ImmutableSet<String> set = ImmutableSet.from(mutable);
		assertEquals(mutable.size(), set.size());
		assertTrue(set.contains("a"));
		assertTrue(set.contains("b"));
		assertTrue(set.contains("c"));

		assertThrows(NullPointerException.class, () -> ImmutableSet.from(null));

		final Set<String> setWithNullElement = new HashSet<>();
		setWithNullElement.add(null);
		assertThrows(NullPointerException.class, () -> ImmutableSet.from(setWithNullElement));
	}

	@Test
	void testOf() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals(3, set.size());
		assertTrue(set.contains("a"));
		assertTrue(set.contains("b"));
		assertTrue(set.contains("c"));

		assertThrows(NullPointerException.class, () -> ImmutableSet.of((String[]) null));
		assertThrows(NullPointerException.class, () -> ImmutableSet.of("a", null, "c"));
	}

	@Test
	void testEmptySet() {
		final ImmutableSet<String> set = ImmutableSet.emptySet();
		assertEquals(0, set.size());
	}

	@Test
	void testToImmutableSet() {
		final ImmutableList<String> list = ImmutableList.of("a", "a", "b", "c", "b");
		final ImmutableSet<String> immutableSetSequential = list.stream().collect(toImmutableSet());
		assertEquals(3, immutableSetSequential.size());
		assertTrue(immutableSetSequential.contains("a"));
		assertTrue(immutableSetSequential.contains("b"));
		assertTrue(immutableSetSequential.contains("c"));

		final ImmutableSet<String> immutableSetParallel = list.stream().parallel().collect(toImmutableSet());
		assertEquals(3, immutableSetParallel.size());
		assertTrue(immutableSetParallel.contains("a"));
		assertTrue(immutableSetParallel.contains("b"));
		assertTrue(immutableSetParallel.contains("c"));

		final Stream<String> streamWithNull = Stream.of("a", null);
		final Collector<String, ?, ImmutableSet<String>> toImmutableSet = toImmutableSet();
		assertThrows(NullPointerException.class, () -> streamWithNull.collect(toImmutableSet));
	}

	@Test
	void testSize() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals(3, set.size());
	}

	@Test
	void testContains() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertTrue(set.contains("a"));
		assertFalse(set.contains("d"));

		assertThrows(NullPointerException.class, () -> set.contains(null));
	}

	@Test
	void testContainsAll() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertTrue(set.containsAll(ImmutableSet.of("a", "b")));
		assertFalse(set.containsAll(ImmutableSet.of("a", "d")));

		assertThrows(NullPointerException.class, () -> set.containsAll(null));
	}

	@Test
	void testIsEmpty() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertFalse(set.isEmpty());

		final ImmutableSet<String> emptySet = ImmutableSet.emptySet();
		assertTrue(emptySet.isEmpty());
	}

	@Test
	void testStream() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals(3, set.stream().count());
	}

	@Test
	void testElements() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		final Set<String> unmodifiable = set.asSet();
		assertEquals(3, unmodifiable.size());

		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.add("d"));
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.remove("a"));
	}

	@Test
	void testForEach() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		final Set<String> elements = new HashSet<>();
		set.forEach(elements::add);
		assertEquals(3, elements.size());
		assertTrue(elements.contains("a"));
		assertTrue(elements.contains("b"));
		assertTrue(elements.contains("c"));

		assertThrows(NullPointerException.class, () -> set.forEach(null));
	}

	@Test
	void testIterator() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		final Set<String> elements = new HashSet<>();
		set.iterator().forEachRemaining(elements::add);
		assertEquals(3, elements.size());
		assertTrue(elements.contains("a"));
		assertTrue(elements.contains("b"));
		assertTrue(elements.contains("c"));
	}

	@Test
	void testSpliterator() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		final Set<String> elements = new HashSet<>();
		set.spliterator().forEachRemaining(elements::add);
		assertEquals(3, elements.size());
		assertTrue(elements.contains("a"));
		assertTrue(elements.contains("b"));
		assertTrue(elements.contains("c"));
	}

	@Test
	void testToString() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals("ImmutableSet{elements=[a, b, c]}", set.toString());
	}

	@Test
	void testEquals() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals(set, set);
		assertEquals(set, ImmutableSet.of("a", "c", "b"));
		assertNotEquals(set, ImmutableSet.of("a", "b"));
		assertNotEquals(set, ImmutableSet.of("a", "b", "d"));
		assertNotEquals(set, ImmutableSet.of("a", "b", "c", "d"));
		assertNotEquals(set, ImmutableList.of("a", "b", "c"));
		assertNotEquals(null, set);
	}

	@Test
	void testHashCode() {
		final ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
		assertEquals(ImmutableSet.of("a", "c", "b").hashCode(), set.hashCode());
		assertNotEquals(ImmutableSet.of("a", "b").hashCode(), set.hashCode());
		assertNotEquals(ImmutableSet.of("a", "b", "d").hashCode(), set.hashCode());
		assertNotEquals(ImmutableSet.of("a", "b", "c", "d").hashCode(), set.hashCode());
	}
}
