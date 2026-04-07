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
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

public interface KeyDerivation {
	/**
	 * Derives a key from a cryptographically strong pseudo-random key. Uses SHA-256 as a hash function.
	 *
	 * @param pseudoRandomKey    a cryptographically strong pseudo-random key, of byte length greater or equal to 32.
	 * @param contextInformation optional additional context information.
	 * @param requiredByteLength the required byte length of the output key, in range 0 (exclusive) to 8160 (inclusive).
	 * @return a cryptographically strong key of length {@code requiredByteLength}
	 * @throws NullPointerException     if any input is null or contains nulls
	 * @throws IllegalArgumentException if any of the preconditions mentioned above are not respected.
	 */
	@SuppressWarnings({ "java:S100" })
	ImmutableByteArray KDF(final ImmutableByteArray pseudoRandomKey, final ImmutableList<String> contextInformation, final int requiredByteLength);

	/**
	 * Generates a value in Zq using the Key Derivation Function based on SHA-256.
	 *
	 * @param pseudoRandomKey     PRK &isin; &#8492;<sup>l</sup>, a cryptographically strong pseudo-random key, of byte length greater or equal to
	 *                            32.
	 * @param contextInformation  info &isin; (A<sub>UCS</sub><sup>*</sup>)<sup>n</sup>, optional additional context information.
	 * @param exclusiveUpperBound q &isin; &#8469;<sup>+</sup>, the requested exclusive upper bound, such that
	 *                            {@code ceil(exclusiveUpperBound / 8) >= 32}.
	 * @return a {@link ZqElement}
	 * @throws NullPointerException     if any input is null or contains nulls
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>the length of the pseudo random key is smaller than the SHA-256 output length</li>
	 *                                      <li>the byte length of the exclusive upper bound is smaller SHA-256 output length</li>
	 *                                  </ul>
	 */
	@SuppressWarnings({ "java:S100" })
	ZqElement KDFToZq(final ImmutableByteArray pseudoRandomKey, final ImmutableList<String> contextInformation, final BigInteger exclusiveUpperBound);
}
