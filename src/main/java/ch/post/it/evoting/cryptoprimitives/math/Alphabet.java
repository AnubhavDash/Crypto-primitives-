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
package ch.post.it.evoting.cryptoprimitives.math;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * Alphabet abstract class.
 * <p>
 * This abstract class is sealed and permits the following subclasses: {@link Base10Alphabet}, {@link Base16Alphabet}, {@link Base32Alphabet},
 * {@link Base64Alphabet}, {@link UsabilityBase32Alphabet}, {@link LatinAlphabet}.
 */
public abstract sealed class Alphabet
		permits Base10Alphabet, Base16Alphabet, Base32Alphabet, Base64Alphabet, UsabilityBase32Alphabet, LatinAlphabet {

	private final int size;
	private final ImmutableList<String> alphabetInternal;

	Alphabet(final int size, final ImmutableList<String> alphabet) {
		this.size = size;
		this.alphabetInternal = checkNotNull(alphabet);

		checkArgument(this.alphabetInternal.size() == size, "The size of the alphabet must be %s. [actual size: %s]", size,
				this.alphabetInternal.size());

		checkArgument(new HashSet<>(this.alphabetInternal.stream().toList()).size() == this.alphabetInternal.size(),
				"The alphabet must not contain duplicates.");
	}

	/**
	 * @param index i, the index of element to be returned.
	 * @return the i-th element of the alphabet
	 * @throws IllegalArgumentException if the index is out-of-bound.
	 */
	public String get(final int index) {
		checkArgument(index >= 0, "The index cannot be negative. [index: %s]", index);
		checkArgument(index < size, "The index must be strictly smaller than the alphabet size. [index: %s, size: %s]", index, size);

		return this.alphabetInternal.get(index);
	}

	/**
	 * @return the size of the alphabet.
	 */
	public int size() {
		return size;
	}

	/**
	 * @param character c, the character to be checked.
	 * @return true if c is in the alphabet, false otherwise.
	 * @throws NullPointerException if c is null.
	 */
	public boolean contains(final String character) {
		checkNotNull(character);

		return this.alphabetInternal.contains(character);
	}

	/**
	 * @param codePoint cp, the code point of the character to be checked.
	 * @return true if the character corresponding to cp is in the alphabet, false otherwise.
	 */
	public boolean contains(final int codePoint) {
		checkArgument(codePoint >= Character.MIN_CODE_POINT && codePoint <= Character.MAX_CODE_POINT,
				"The provided code point is out-of-range. [codePoint: %s]", codePoint);
		final String character = Character.toString(codePoint);
		return this.alphabetInternal.contains(character);
	}

	/**
	 * @param character c, the character to be checked.
	 * @return the index of c in the alphabet, or -1 if the alphabet does not contain c.
	 * @throws NullPointerException if c is null.
	 */
	public int indexOf(final String character) {
		checkNotNull(character);

		return this.alphabetInternal.indexOf(character);
	}
}