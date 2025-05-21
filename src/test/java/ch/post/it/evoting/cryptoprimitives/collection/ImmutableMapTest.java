/*
 * Copyright 2025 Swiss Post Ltd
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

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.Entry.comparingByKey;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.emptyMap;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.entry;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.from;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.of;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap.toImmutableMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ImmutableMapTest {

	@Test
	void testImmutability() {
		final Map<String, Integer> mutableInput = new HashMap<>();
		mutableInput.put("one", 1);
		mutableInput.put("two", 2);

		final ImmutableMap<String, Integer> immutableMap = from(mutableInput);
		mutableInput.put("three", 3);
		assertEquals(ImmutableSet.of(entry("one", 1), entry("two", 2)), immutableMap.entrySet());

		final Map<String, Integer> map = immutableMap.asMap();
		assertThrows(UnsupportedOperationException.class, () -> map.put("three", 3));
	}

	@Nested
	class testFrom {
		final Map<String, Integer> map = new HashMap<>();
		final Map<String, Integer> mapWithNullKey = new HashMap<>();
		final Map<String, Integer> mapWithNullValue = new HashMap<>();

		@BeforeEach
		void setUp() {
			map.put("one", 1);
			mapWithNullKey.put(null, 1);
			mapWithNullValue.put("one", null);
		}

		@Test
		void testFromWithMap() {
			assertDoesNotThrow(() -> from(map));
			assertThrows(NullPointerException.class, () -> from(mapWithNullKey));
			assertThrows(NullPointerException.class, () -> from(mapWithNullValue));
			assertThrows(NullPointerException.class, () -> from(null));
		}

		@Test
		void testFromWithMapAndMapFactory() {
			assertDoesNotThrow(() -> from(map, HashMap::new));
			assertThrows(NullPointerException.class, () -> from(map, null));
			assertThrows(NullPointerException.class, () -> from(mapWithNullKey, HashMap::new));
			assertThrows(NullPointerException.class, () -> from(mapWithNullValue, HashMap::new));
			assertThrows(NullPointerException.class, () -> from(null, HashMap::new));
		}

	}

	@Nested
	class testOf {

		@Test
		void testOfWithZeroKeyPair() {
			assertDoesNotThrow(() -> of());
		}

		@Test
		void testOfWithOneKeyPair() {
			assertDoesNotThrow(() -> of("one", 1));
			assertThrows(NullPointerException.class, () -> of(null, 1));
			assertThrows(NullPointerException.class, () -> of("one", null));
		}

		@Test
		void testOfWithTwoKeyPairs() {
			assertDoesNotThrow(() -> of("one", 1, "two", 2));
			assertThrows(NullPointerException.class, () -> of(null, 1, "two", 2));
			assertThrows(NullPointerException.class, () -> of("one", 1, "two", null));
		}

		@Test
		void testOfWithThreeKeyPairs() {
			assertDoesNotThrow(() -> of("one", 1, "two", 2, "three", 3));
			assertThrows(NullPointerException.class, () -> of(null, 1, "two", 2, "three", 3));
			assertThrows(NullPointerException.class, () -> of("one", 1, "two", null, "three", 3));
		}

		@Test
		void testOfWithFourKeyPairs() {
			assertDoesNotThrow(() -> of("one", 1, "two", 2, "three", 3, "four", 4));
			assertThrows(NullPointerException.class, () -> of(null, 1, "two", 2, "three", 3, "four", 4));
			assertThrows(NullPointerException.class, () -> of("one", 1, "two", null, "three", 3, "four", 4));
		}

		@Test
		@SuppressWarnings("java:S5778")
			// manually checked.
		void testOfWithFiveKeyPairs() {
			assertDoesNotThrow(() -> of(
					entry("one", 1),
					entry("two", 2),
					entry("three", 3),
					entry("four", 4),
					entry("five", 5))
			);
			assertThrows(NullPointerException.class, () -> of(
					entry("one", 1),
					entry("two", 2),
					entry("three", 3),
					entry("four", 4),
					null)
			);
		}
	}

	@Test
	void testEmptyMap() {
		assertEquals(0, emptyMap().size());
	}

	@Nested
	@SuppressWarnings("java:S5778")
			// manually checked.
	class testToImmutableMap {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);

		final ImmutableList<ImmutableMap.Entry<String, Integer>> entries = ImmutableList.of(
				entry("one", 1),
				entry("two", 2)
		);

		final Supplier<Stream<ImmutableMap.Entry<String, Integer>>> entriesWithNullElement = () -> Stream.of(
				entry("one", 1),
				entry("two", 2),
				null);

		final Supplier<Stream<ImmutableMap.Entry<String, Integer>>> entriesWithNullKey = () -> Stream.of(
				entry("one", 1),
				entry(null, 2));

		final Supplier<Stream<ImmutableMap.Entry<String, Integer>>> entriesWithNullValue = () -> Stream.of(
				entry("one", 1),
				entry("two", null));

		final Supplier<Stream<ImmutableMap.Entry<String, Integer>>> entriesWithDuplicatedKey = () -> Stream.of(
				entry("one", 1),
				entry("one", 2));

		@Test
		void testToImmutableMapNoArgs() {
			assertDoesNotThrow(() -> entries.stream().collect(toImmutableMap()));
			assertThrows(NullPointerException.class, () -> entriesWithNullElement.get().collect(toImmutableMap()));
			assertThrows(NullPointerException.class, () -> entriesWithNullKey.get().collect(toImmutableMap()));
			assertThrows(NullPointerException.class, () -> entriesWithNullValue.get().collect(toImmutableMap()));
			assertThrows(IllegalStateException.class, () -> entriesWithDuplicatedKey.get().collect(toImmutableMap()));
		}

		@Test
		void testToImmutableMapWithMapFactory() {
			final ImmutableMap<String, Integer> collectedWithMapFactory = entries.stream().parallel().collect(toImmutableMap(ConcurrentHashMap::new));
			assertEquals(immutable, collectedWithMapFactory);
			assertThrows(NullPointerException.class, () -> entries.stream().collect(toImmutableMap(null)));
			assertThrows(NullPointerException.class, () -> entriesWithNullElement.get().collect(toImmutableMap(ConcurrentHashMap::new)));
			assertThrows(NullPointerException.class, () -> entriesWithNullKey.get().collect(toImmutableMap(ConcurrentHashMap::new)));
			assertThrows(NullPointerException.class, () -> entriesWithNullValue.get().collect(toImmutableMap(ConcurrentHashMap::new)));
			assertThrows(IllegalStateException.class,
					() -> entriesWithDuplicatedKey.get().parallel().collect(toImmutableMap(ConcurrentHashMap::new)));
		}

		@Test
		void testToImmutableMapWithKeyMapperAndValueMapper() {
			final ImmutableMap<String, Integer> collectedWithKeyValueMapper = entries.stream().parallel()
					.collect(toImmutableMap(ImmutableMap.Entry::key, ImmutableMap.Entry::value));
			assertEquals(immutable, collectedWithKeyValueMapper);
			assertThrows(NullPointerException.class, () -> entries.stream().collect(toImmutableMap(null, ImmutableMap.Entry::value)));
			assertThrows(NullPointerException.class, () -> entries.stream().collect(toImmutableMap(ImmutableMap.Entry::key, null)));
			assertThrows(NullPointerException.class,
					() -> entriesWithNullElement.get().collect(toImmutableMap(ImmutableMap.Entry::key, ImmutableMap.Entry::value)));
			assertThrows(NullPointerException.class,
					() -> entriesWithNullKey.get().collect(toImmutableMap(ImmutableMap.Entry::key, ImmutableMap.Entry::value)));
			assertThrows(NullPointerException.class,
					() -> entriesWithNullValue.get().collect(toImmutableMap(ImmutableMap.Entry::key, ImmutableMap.Entry::value)));
			assertThrows(IllegalStateException.class,
					() -> entriesWithDuplicatedKey.get().parallel().collect(toImmutableMap(ImmutableMap.Entry::key, ImmutableMap.Entry::value)));
		}

	}

	@Test
	void testGet() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		assertEquals(1, immutable.get("one"));
		assertEquals(2, immutable.get("two"));
		assertThrows(NullPointerException.class, () -> immutable.get(null));
	}

	@Test
	void testValues() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final ImmutableList<Integer> values = immutable.values();
		assertEquals(ImmutableList.of(1, 2), values);
	}

	@Test
	void testContainsKey() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		assertTrue(immutable.containsKey("one"));
		assertFalse(immutable.containsKey("three"));
		assertThrows(NullPointerException.class, () -> immutable.containsKey(null));
	}

	@Test
	void testIsEmpty() {
		assertTrue(emptyMap().isEmpty());
		assertFalse(of("one", 1).isEmpty());
	}

	@Test
	void testKeySet() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final ImmutableSet<String> keySet = immutable.keySet();
		assertEquals(ImmutableSet.of("one", "two"), keySet);
	}

	@Test
	void testEntrySet() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final ImmutableSet<ImmutableMap.Entry<String, Integer>> entrySet = immutable.entrySet();
		assertEquals(ImmutableSet.of(entry("one", 1), entry("two", 2)), entrySet);
	}

	@Test
	void testSize() {
		assertEquals(0, emptyMap().size());
		assertEquals(1, of("one", 1).size());
		assertEquals(2, of("one", 1, "two", 2).size());
	}

	@Test
	void testForEach() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		immutable.forEach((key, value) -> {
			if ("one".equals(key)) {
				assertEquals(1, value);
			} else if ("two".equals(key)) {
				assertEquals(2, value);
			} else {
				throw new AssertionError("Unexpected key: " + key);
			}
		});
	}

	@Test
	void testAsMap() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final Map<String, Integer> map = immutable.asMap();
		assertEquals(2, map.size());
		assertEquals(1, map.get("one"));
		assertEquals(2, map.get("two"));
	}

	@Test
	void testToString() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		assertEquals("ImmutableMap{elements={one=1, two=2}}", immutable.toString());
	}

	@Test
	void testEquals() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final ImmutableMap<String, Integer> other = of("one", 1, "two", 2);
		assertEquals(immutable, other);
		assertEquals(immutable, immutable);
		assertNotEquals(null, immutable);
		assertNotEquals("one", immutable);
	}

	@Test
	void testHashCode() {
		final ImmutableMap<String, Integer> immutable = of("one", 1, "two", 2);
		final ImmutableMap<String, Integer> other = of("one", 1, "two", 2);
		assertEquals(immutable.hashCode(), other.hashCode());
	}

	@Nested
	class testEntry {

		@Test
		void testEntryByMethod() {
			final ImmutableMap.Entry<String, Integer> entry = entry("one", 1);
			assertEquals("one", entry.key());
			assertEquals(1, entry.value());
			assertThrows(NullPointerException.class, () -> entry(null, 1));
			assertThrows(NullPointerException.class, () -> entry("one", null));
		}

		@Test
		void testEntryByConstructor() {
			final ImmutableMap.Entry<String, Integer> entry = new ImmutableMap.Entry<>("one", 1);
			assertEquals("one", entry.key());
			assertEquals(1, entry.value());
			assertThrows(NullPointerException.class, () -> new ImmutableMap.Entry<>(null, 1));
			assertThrows(NullPointerException.class, () -> new ImmutableMap.Entry<>("one", null));
		}

	}

	@Test
	void testComparingByKey() {
		final ImmutableList<ImmutableMap.Entry<String, Integer>> entries = of("two", 2, "three", 3, "one", 1).entrySet().stream()
				.collect(toImmutableList());
		final ImmutableList<ImmutableMap.Entry<String, Integer>> sorted = entries.stream().sorted(comparingByKey()).collect(toImmutableList());
		final ImmutableList<ImmutableMap.Entry<String, Integer>> expected = ImmutableList.of(entry("one", 1), entry("three", 3), entry("two", 2));
		assertEquals(expected, sorted);
	}

}
