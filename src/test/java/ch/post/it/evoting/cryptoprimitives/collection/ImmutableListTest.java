/*
 * Copyright 2024 Swiss Post Ltd
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

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.emptyList;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ImmutableListTest {

	@Test
	void testConstructor() {
		assertThrows(NullPointerException.class, () -> ImmutableList.from(null));
		assertThrows(NullPointerException.class, () -> ImmutableList.from(List.of(null)));
	}

	@Test
	void testImmutability() {
		final List<String> mutableInput = Stream.of("a", "b", "c").collect(Collectors.toCollection(ArrayList::new));
		final ImmutableList<String> list = ImmutableList.from(mutableInput);
		mutableInput.set(0, "d");
		assertEquals("a", list.get(0));

		final List<String> unmodifiable = list.asList();
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.set(0, "d"));
		assertEquals("a", unmodifiable.get(0));
	}

	@Test
	void testStream() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals("a", list.stream().findFirst().get());
	}

	@Test
	void size() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals(3, list.size());
	}

	@Test
	void get() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals("a", list.get(0));
	}

	@Test
	void getFirst() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals("a", list.getFirst());
		final NoSuchElementException noSuchElementException = assertThrows(NoSuchElementException.class, () -> emptyList().getFirst());
		assertEquals("The list is empty.", noSuchElementException.getMessage());
	}

	@Test
	void getLast() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals("c", list.getLast());
		final NoSuchElementException noSuchElementException = assertThrows(NoSuchElementException.class, () -> emptyList().getLast());
		assertEquals("The list is empty.", noSuchElementException.getMessage());
	}

	@Test
	void getThrows() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final int index = 3;
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> list.get(index));
		assertEquals(String.format("Index is out of bounds. [index: %s, size: %s]", index, list.size()), illegalArgumentException.getMessage());

		final int anotherIndex = -1;
		illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> list.get(anotherIndex));
		assertEquals(String.format("Index is out of bounds. [index: %s, size: %s]", -1, list.size()), illegalArgumentException.getMessage());
	}

	@Test
	void isEmpty() {
		assertFalse(ImmutableList.of("a", "b", "c").isEmpty());
		assertTrue(ImmutableList.of().isEmpty());
		assertTrue(emptyList().isEmpty());
	}

	@Test
	void contains() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertTrue(list.contains("a"));
		assertFalse(list.contains("d"));
	}

	@Test
	void containsThrows() {
		assertThrows(NullPointerException.class, () -> emptyList().contains(null));
	}

	@Test
	void subList() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals(ImmutableList.of("a", "b"), list.subList(0, 2));
	}

	@Test
	void subListThrows() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final int fromIndex = 0;
		final int toIndex = 4;
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> list.subList(fromIndex, toIndex));
		assertEquals(String.format("Indexes are out of bounds. [fromIndex: %s, toIndex: %s, size: %s]", fromIndex, toIndex, list.size()),
				illegalArgumentException.getMessage());

		final int anotherFromIndex = 2;
		final int anotherToIndex = 1;
		illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> list.subList(anotherFromIndex, anotherToIndex));
		assertEquals(
				String.format("Indexes are out of bounds. [fromIndex: %s, toIndex: %s, size: %s]", anotherFromIndex, anotherToIndex, list.size()),
				illegalArgumentException.getMessage());

		final int negativeFromIndex = -1;
		illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> list.subList(negativeFromIndex, anotherToIndex));
		assertEquals(
				String.format("Indexes are out of bounds. [fromIndex: %s, toIndex: %s, size: %s]", negativeFromIndex, anotherToIndex, list.size()),
				illegalArgumentException.getMessage());
	}

	@Test
	void indexOf() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals(0, list.indexOf("a"));
		assertEquals(1, list.indexOf("b"));
		assertEquals(2, list.indexOf("c"));
	}

	@Test
	void indexOfThrows() {
		assertThrows(NullPointerException.class, () -> emptyList().indexOf(null));
	}

	@Test
	void equals() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertTrue(list.equals(list));
		assertFalse(list.equals(null));
		assertFalse(list.equals("a"));
		assertEquals(list, ImmutableList.of("a", "b", "c"));
		assertNotEquals(list, ImmutableList.of("a", "b"));
		assertNotEquals(list, ImmutableList.of("a", "b", "d"));
		assertEquals(emptyList(), ImmutableList.of());
	}

	@Test
	void ofThrows() {
		assertThrows(NullPointerException.class, () -> ImmutableList.of((String[]) null));
		assertThrows(NullPointerException.class, () -> ImmutableList.of((List<String>) null));
		assertThrows(NullPointerException.class, () -> ImmutableList.of((Stream<String>) null));
		assertThrows(NullPointerException.class, () -> ImmutableList.of(Stream.of(null)));
	}

	@Test
	void collectorTest() {
		final ImmutableList<String> listSequential = Stream.of("a", "b", "c").collect(toImmutableList());
		assertEquals(ImmutableList.of("a", "b", "c"), listSequential);

		final ImmutableList<String> listParallel = Stream.of("a", "b", "c").parallel().collect(toImmutableList());
		assertEquals(ImmutableList.of("a", "b", "c"), listParallel);
	}

	@Test
	void collectorThrows() {
		assertThrows(NullPointerException.class, () -> Stream.of("ignored", null, "ignored").collect(toImmutableList()));
	}

	@Test
	void appendTest() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final ImmutableList<String> appendedList = list.append("d");

		assertEquals(ImmutableList.of("a", "b", "c", "d"), appendedList);
		assertEquals(ImmutableList.of("a", "b", "c"), list);
	}

	@Test
	void appendVarArgsTest() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");

		assertEquals(ImmutableList.of("a", "b", "c", "d", "e"), list.append("d", "e"));
	}

	@Test
	void appendImmutableListTest() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final ImmutableList<String> appendedList = list.append(ImmutableList.of("d", "e"));

		assertEquals(ImmutableList.of("a", "b", "c", "d", "e"), appendedList);
		assertEquals(ImmutableList.of("a", "b", "c"), list);
	}

	@Test
	void appendThrows() {
		final ImmutableList<String> list = emptyList();
		final String[] s = new String[1];
		s[0] = null;

		assertThrows(NullPointerException.class, () -> list.append((String) null));
		assertThrows(NullPointerException.class, () -> list.append((String[]) null));
		assertThrows(NullPointerException.class, () -> list.append(s));
		assertThrows(NullPointerException.class, () -> list.append((ImmutableList<String>) null));
	}

	@Test
	void containsAllTest() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");

		// list
		assertTrue(list.containsAll(ImmutableList.of("a", "b")));
		assertFalse(list.containsAll(ImmutableList.of("a", "d")));

		// set
		assertTrue(list.containsAll(ImmutableSet.of("a", "b")));
		assertFalse(list.containsAll(ImmutableSet.of("a", "d")));
	}

	@Test
	void containsAllThrows() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");

		// list
		assertThrows(NullPointerException.class, () -> list.containsAll((ImmutableList) null));

		// set
		assertThrows(NullPointerException.class, () -> list.containsAll((ImmutableSet) null));
	}

	@Test
	void toArrayTest() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final String[] array = list.toArray(new String[] {});
		assertEquals(3, array.length);
		assertEquals("a", array[0]);
		assertEquals("b", array[1]);
		assertEquals("c", array[2]);
	}

	@Test
	void toArrayThrows() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertThrows(NullPointerException.class, () -> list.toArray(null));
	}

	@Test
	void toImmutableSet() {
		final ImmutableList<String> list = ImmutableList.of("a", "a", "b", "c", "b");
		final ImmutableSet<String> set = list.toImmutableSet();
		assertEquals(3, set.size());
		assertTrue(set.contains("a"));
		assertTrue(set.contains("b"));
		assertTrue(set.contains("c"));
	}

	@Test
	void testHashCode() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals(list.hashCode(), list.hashCode());
		assertEquals(list.hashCode(), ImmutableList.of("a", "b", "c").hashCode());
		assertNotEquals(list.hashCode(), ImmutableList.of("a", "b").hashCode());
		assertNotEquals(list.hashCode(), ImmutableList.of("a", "b", "d").hashCode());
		assertNotEquals(list.hashCode(), ImmutableList.of("a", "b", "c", "d").hashCode());
	}

	@Test
	void testToString() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		assertEquals("ImmutableList{elements=[a, b, c]}", list.toString());
	}

	@Test
	void testForEach() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final List<String> result = new ArrayList<>();
		list.forEach(result::add);
		assertEquals(List.of("a", "b", "c"), result);

		assertThrows(NullPointerException.class, () -> list.forEach(null));
	}

	@Test
	void testIterator() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final List<String> result = new ArrayList<>();
		list.iterator().forEachRemaining(result::add);
		assertEquals(List.of("a", "b", "c"), result);
	}

	@Test
	void testSpliterator() {
		final ImmutableList<String> list = ImmutableList.of("a", "b", "c");
		final List<String> result = new ArrayList<>();
		list.spliterator().forEachRemaining(result::add);
		assertEquals(List.of("a", "b", "c"), result);
	}
}
