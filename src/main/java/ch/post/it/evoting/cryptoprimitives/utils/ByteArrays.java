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

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

public interface ByteArrays {

	/**
	 * Cuts the given byte array to the requested bit length
	 *
	 * @param byteArray       the byte array to be cut
	 * @param requestedLength the length in bits to which the array is to be cut. Non-negative and not greater than the byte array's bit length.
	 * @return the byte array cut to the requested length
	 * @throws NullPointerException     if the given byte array is null
	 * @throws IllegalArgumentException if the requested length is not within the required range
	 */
	static ImmutableByteArray cutToBitLength(final ImmutableByteArray byteArray, final int requestedLength) {
		return ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.cutToBitLength(byteArray, requestedLength);
	}

	/**
	 * Computes the length in bytes of the given non-negative integer.
	 *
	 * @param x the integer of which to compute the byte length. Must be non-null and non-negative.
	 * @return the length of the byte representation of the given integer.
	 * @throws NullPointerException     if the given x is null.
	 * @throws IllegalArgumentException if the given x is negative.
	 */
	static int byteLength(final BigInteger x) {
		return ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.byteLength(x);
	}
}
