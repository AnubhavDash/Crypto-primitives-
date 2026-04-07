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
package ch.post.it.evoting.cryptoprimitives.utils;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;

public interface Strings {

	/**
	 * Implements the Truncate algorithm.
	 * <p>
	 * If the given string is longer than the given length, the string is truncated to the desired length, otherwise the string is kept in its whole
	 * length.
	 * </p>
	 *
	 * @param string S, the string to be truncated. Must be non-null.
	 * @param length l, the desired maximum length for the truncated string. Must be positive.
	 * @return S<sup>'</sup>, the truncated string.
	 * @throws NullPointerException     if the input string is null.
	 * @throws IllegalArgumentException if the input length is not positive.
	 */
	static String truncate(final String string, final int length) {
		return ch.post.it.evoting.cryptoprimitives.internal.utils.Strings.truncate(string, length);
	}

	/**
	 * Implements the GetMergedString algorithm.
	 * <p>
	 * Merges a list of strings of equal length into a single string by combining them character-wise. For each position, the algorithm sums the ranks
	 * of the characters from all input strings and uses the result modulo the alphabet size to determine the output character.
	 * </p>
	 *
	 * @param strings  s, the list of strings to be merged. All strings must be in the given alphabet and have the same size. Must be non-null.
	 * @param alphabet A, the alphabet in which to merge the strings. Must be non-null.
	 * @return S, the merged string
	 * @throws NullPointerException     if {@code strings} or {@code alphabet} is null.
	 * @throws IllegalArgumentException if:
	 * <ul>
	 *     <li>the list of strings is empty;</li>
	 *     <li>the strings in the list have different sizes;</li>
	 *     <li>any of the strings contains characters not in the alphabet.</li>
	 * </ul>
	 */
	static String getMergedString(final ImmutableList<String> strings, final Alphabet alphabet) {
		return ch.post.it.evoting.cryptoprimitives.internal.utils.Strings.getMergedString(strings, alphabet);
	}
}
