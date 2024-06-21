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

package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * Byte array utilities.
 */
public final class ByteArrays {

	private ByteArrays() {
		// Intentionally left blank.
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.ByteArrays#cutToBitLength}
	 */
	@SuppressWarnings("java:S117")
	public static ImmutableByteArray cutToBitLength(final ImmutableByteArray byteArray, final int requestedLength) {
		// Input.
		final ImmutableByteArray B = checkNotNull(byteArray);
		final int N = B.length();
		final int n = requestedLength;

		checkArgument(N > 0, "The byte array length must be strictly positive.");
		checkArgument(n > 0, "The requested length must be strictly positive.");

		// Require.
		checkArgument(n <= (N * Byte.SIZE), "The requested length must not be greater than the bit length of the byte array.");

		// Operation.
		final int length = (int) Math.ceil(n / (double) Byte.SIZE);
		final int offset = N - length;
		final byte B_prime_zero;
		if (n % Byte.SIZE != 0) {
			B_prime_zero = (byte) (B.get(offset) & (byte) (Math.pow(2, n % Byte.SIZE) - 1));
		} else {
			B_prime_zero = B.get(offset);
		}

		// Output.
		return ImmutableByteArray.concat(
				ImmutableByteArray.from(B_prime_zero),
				ImmutableByteArray.copy(B, offset + 1, length - 1)
		);
	}

	/**
	 * Computes the length of the byte representation of an integer.
	 *
	 * @param x the integer of which to compute the byte length. Must be non-null.
	 * @return the length of the byte representation of the given integer.
	 * @throws NullPointerException if the given x is null.
	 */
	public static int byteLength(final BigInteger x) {
		checkNotNull(x);

		final int n = (int) Math.ceil(x.bitLength() / (double) Byte.SIZE);

		return Math.max(n, 1);
	}
}
