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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;

class ImmutableByteArrayTest {

	private static final byte[] input = { 1, 2, 3 };

	@Test
	void testImmutability() {
		final byte[] mutableInput = { 1, 2, 3 };
		final ImmutableByteArray array = new ImmutableByteArray(mutableInput);
		mutableInput[0] = 4;
		assertArrayEquals(input, array.elements());

		final byte[] elements = array.elements();
		elements[0] = 4;
		assertArrayEquals(input, array.elements());
	}

	@Test
	void testElements() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertArrayEquals(input, array.elements());
	}

	@Test
	void testGet() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertEquals(1, array.get(0));
		assertEquals(2, array.get(1));
		assertEquals(3, array.get(2));
	}

	@Test
	void testLength() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertEquals(input.length, array.length());
	}

	@Test
	void testIsEmpty() {
		final ImmutableByteArray array = new ImmutableByteArray(new byte[] {});
		assertTrue(array.isEmpty());
		assertTrue(ImmutableByteArray.EMPTY.isEmpty());
	}

	@Test
	void testEquals() {
		final ImmutableByteArray array1 = new ImmutableByteArray(input);
		final ImmutableByteArray array2 = new ImmutableByteArray(input);
		assertEquals(array1, array2);
	}

	@Test
	void testOf() {
		final byte inputByte = 1;
		final ImmutableByteArray array = ImmutableByteArray.of(inputByte);
		assertArrayEquals(new byte[] { inputByte }, array.elements());
	}

	@Test
	void testConcat() {
		final byte[] input2 = { 4, 5, 6 };
		final ImmutableByteArray array1 = new ImmutableByteArray(input);
		final ImmutableByteArray array2 = new ImmutableByteArray(input2);
		final ImmutableByteArray result = ImmutableByteArray.concat(array1, array2);
		assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, result.elements());
	}

	@Test
	void testCopyOfRange() {
		final byte[] biggerInput = { 1, 2, 3, 4, 5, 6 };
		final ImmutableByteArray array = new ImmutableByteArray(biggerInput);
		final ImmutableByteArray result = ImmutableByteArray.copyOfRange(array, 2, 5);
		assertArrayEquals(new byte[] { 3, 4, 5 }, result.elements());
	}

	@Test
	void testToHashableForm() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		final Hashable hashable = array.toHashableForm();
		assertEquals(array, hashable);
	}

	@Test
	void testHashCode() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertEquals(array.hashCode(), array.hashCode());
	}

	@Test
	void testToString() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertEquals("ImmutableByteArray[elements=[1, 2, 3]]", array.toString());
	}

	@Test
	void testEqualsSameObject() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertEquals(array, array);
	}

	@Test
	void testConstructorThrows() {
		assertThrows(NullPointerException.class, () -> new ImmutableByteArray(null));
	}

	@Test
	void testGetThrows() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		int index = -1;
		final IllegalArgumentException illegalArgumentException1 = assertThrows(IllegalArgumentException.class, () -> array.get(-1));
		assertEquals(String.format("Index is out of bounds. [index: %s, length: %s]", index, input.length), illegalArgumentException1.getMessage());
		index = 3;
		final IllegalArgumentException illegalArgumentException2 = assertThrows(IllegalArgumentException.class, () -> array.get(3));
		assertEquals(String.format("Index is out of bounds. [index: %s, length: %s]", index, input.length), illegalArgumentException2.getMessage());
	}

	@Test
	void testOfThrows() {
		assertThrows(NullPointerException.class, () -> ImmutableByteArray.of(null));
	}

	@Test
	void testConcatThrows() {
		assertThrows(NullPointerException.class, () -> ImmutableByteArray.concat((ImmutableByteArray[]) null));

		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertThrows(NullPointerException.class, () -> ImmutableByteArray.concat(array, null));
	}

	@Test
	void testCopyOfRangeThrows() {
		final ImmutableByteArray array = new ImmutableByteArray(input);
		assertThrows(NullPointerException.class, () -> ImmutableByteArray.copyOfRange(null, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> ImmutableByteArray.copyOfRange(array, -1, 2));
		assertThrows(IllegalArgumentException.class, () -> ImmutableByteArray.copyOfRange(array, 2, 1));
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> ImmutableByteArray.copyOfRange(array, 0, 4));
		assertEquals("Indexes are invalid. [from: 0, to: 4, originalLength: 3]", illegalArgumentException.getMessage());
	}

}