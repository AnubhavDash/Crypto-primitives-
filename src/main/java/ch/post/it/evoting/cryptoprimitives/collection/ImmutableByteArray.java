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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

import ch.post.it.evoting.cryptoprimitives.hashing.HashableByteArray;

/**
 * An immutable byte array wrapping a non-null array of bytes.
 *
 * <p>Instances of this class are immutable.</p>
 */
public record ImmutableByteArray(byte[] elements) implements HashableByteArray {
	public static final ImmutableByteArray EMPTY = new ImmutableByteArray(new byte[] {});

	/**
	 * Creates a new immutable byte array from the specified bytes.
	 *
	 * @param elements the bytes to wrap.
	 * @throws NullPointerException if the specified bytes are null.
	 */
	public ImmutableByteArray {
		elements = checkNotNull(elements).clone();
	}

	/**
	 * @return a clone of the byte array wrapped by this instance.
	 */
	public byte[] elements() {
		return elements.clone();
	}

	/**
	 * @param index the index of the byte to return.
	 * @return the byte at the specified index.
	 */
	public byte get(final int index) {
		final int length = length();
		checkArgument(index >= 0 && index < length, "Index is out of bounds. [index: %s, length: %s]", index, length);
		return elements[index];
	}

	/**
	 * @return the length of the byte array.
	 */
	public int length() {
		return elements.length;
	}

	/**
	 * @return true if the byte array is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ImmutableByteArray that = (ImmutableByteArray) o;
		return Arrays.equals(elements, that.elements);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(elements);
	}

	@Override
	public String toString() {
		return String.format("ImmutableByteArray[elements=%s]", Arrays.toString(elements));
	}

	@Override
	public ImmutableByteArray toHashableForm() {
		return this;
	}

	/**
	 * Creates a new immutable byte array from the specified bytes.
	 *
	 * @param elements the bytes to wrap.
	 * @return a new immutable byte array wrapping the specified bytes.
	 * @throws NullPointerException if the specified bytes are null.
	 */
	public static ImmutableByteArray of(final byte... elements) {
		checkNotNull(elements);

		return new ImmutableByteArray(elements);
	}

	/**
	 * Concatenates the specified immutable byte arrays into a new immutable byte array.
	 *
	 * @param arrays the immutable byte arrays to concatenate. Must be non-null.
	 * @return a new immutable byte array containing the concatenation of the specified immutable byte arrays.
	 * @throws NullPointerException if the specified arrays are null.
	 */
	public static ImmutableByteArray concat(final ImmutableByteArray... arrays) {
		checkNotNull(arrays);

		final byte[][] byteArrays = Arrays.stream(arrays)
				.map(Preconditions::checkNotNull)
				.map(ImmutableByteArray::elements)
				.toArray(byte[][]::new);

		return new ImmutableByteArray(Bytes.concat(byteArrays));
	}

	/**
	 * Copies the specified range of the given immutable byte array into a new immutable byte array.
	 *
	 * @param immutableByteArray the original immutable byte array. Must be non-null.
	 * @param from               the initial index of the range to be copied, inclusive.
	 * @param to                 the final index of the range to be copied, exclusive.
	 * @return a new immutable byte array containing the specified range of the original immutable byte array.
	 * @throws NullPointerException     if the original immutable byte array is null.
	 * @throws IllegalArgumentException if the indexes are invalid. The range is valid if 0 &le; from &le; to &le;
	 *                                  <code>immutableByteArray.length</code>.
	 */
	public static ImmutableByteArray copyOfRange(final ImmutableByteArray immutableByteArray, final int from, final int to) {
		checkNotNull(immutableByteArray);
		final int length = immutableByteArray.length();
		checkArgument(0 <= from && from <= to && to <= length, "Indexes are invalid. [from: %s, to: %s, originalLength: %s]", from, to, length);

		return new ImmutableByteArray(Arrays.copyOfRange(immutableByteArray.elements(), from, to));
	}

}
