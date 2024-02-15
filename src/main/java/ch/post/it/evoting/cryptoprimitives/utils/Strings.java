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
package ch.post.it.evoting.cryptoprimitives.utils;

public interface Strings {

	/**
	 * Implements the Truncate algorithm.
	 * <p>
	 *     If the given string is longer than the given length, the string is truncated to the desired length,
	 *     otherwise the string is kept in its whole length.
	 * </p>
	 *
	 * @param string S, the string to be truncated. Must be non-null and non-empty.
	 * @param length l, the desired maximum length for the truncated string. Must be strictly positive.
	 * @return S<sup>'</sup>, the truncated string.
	 * @throws NullPointerException     if the input string is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>the input string is empty.</li>
	 *                                      <li>the input length is not strictly positive.</li>
	 *                                  </ul>
	 */
	static String truncate(final String string, final int length) {
		return ch.post.it.evoting.cryptoprimitives.internal.utils.Strings.truncate(string, length);
	}
}
