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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import ch.post.it.evoting.cryptoprimitives.hashing.HashableByteArray;

/**
 * An immutable byte array wrapping a non-null array of bytes.
 *
 * <p>Instances of this class are immutable. </p>
 */
public record ImmutableByteArray(byte[] elements) implements HashableByteArray {

	public ImmutableByteArray {
		elements = checkNotNull(elements).clone();
	}

	public byte[] elements() {
		return elements.clone();
	}

	public int length() {
		return elements.length;
	}

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
	public byte[] toHashableForm() {
		return elements();
	}
}

