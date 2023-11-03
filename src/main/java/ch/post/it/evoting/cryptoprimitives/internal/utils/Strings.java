/*
 * Copyright 2023 Post CH Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Strings {

	/**
	 * Pads a string to the desired length by adding the given character to the left of the string.
	 *
	 * @param string              S, the string to be padded. Must be of size > 0.
	 * @param desiredStringLength l, the desired string length. Must be greater than the string length.
	 * @param paddingCharacter    c, the character to be used for the padding.
	 * @return the string padded to the desired length by adding the padding character the needed number of times on the left-hand side
	 * @throws NullPointerException     if the string is null
	 * @throws IllegalArgumentException if the desired length is smaller than the length of the string to be padded
	 */
	public static String leftPad(final String string, final int desiredStringLength, final char paddingCharacter) {
		checkNotNull(string);
		checkArgument(!string.isEmpty(), "The string to be padded must contain at least one character.");

		final int k = string.length();
		final int l = desiredStringLength;
		checkArgument(k <= l, "The desired string length must not be smaller than the string.");

		// This method is equivalent to the specification
		return com.google.common.base.Strings.padStart(string, desiredStringLength, paddingCharacter);
	}

	public static String truncate(final String string, final int length) {

		final String S = checkNotNull(string);
		final int u = S.length();
		final int l = length;

		checkArgument(u > 0, "The input string must be non-empty. [u: %s]", u);
		checkArgument(l > 0, "The input length must be strictly positive. [l: %s]", l);

		// Operation. This implementation yields the same result as the specification's pseudo-code and we have a corresponding unit test that asserts the equivalence of the two implementations.
		final int m = Math.min(u, l);
		return S.substring(0, m);
	}
}
