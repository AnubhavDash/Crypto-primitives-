/*
 * Copyright 2022 Post CH Ltd
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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

/**
 * This class is thread safe.
 */
public class RandomService implements Random {

	private final SecureRandom secureRandom;

	/**
	 * Constructs a RandomService with a {@link SecureRandom} as its randomness source.
	 */
	public RandomService() {
		this(new SecureRandom());
	}

	@VisibleForTesting
	RandomService(final SecureRandom secureRandom) {
		this.secureRandom = checkNotNull(secureRandom);
	}

	/**
	 * This implementation yields the same result as the specification's pseudocode, and we have a corresponding unit test that asserts the
	 * equivalence of the two implementations.
	 *
	 * @see Random#genRandomInteger(BigInteger)
	 */
	public BigInteger genRandomInteger(final BigInteger upperBound) {
		checkNotNull(upperBound);
		checkArgument(upperBound.compareTo(BigInteger.ZERO) > 0, "The upper bound must be a positive integer greater than 0.");
		final BigInteger m = upperBound;

		final int bitLength = m.bitLength();

		BigInteger r;
		do {
			// This constructor internally masks the excess generated bits.
			r = new BigInteger(bitLength, secureRandom);
		} while (r.compareTo(m) >= 0);

		return r;
	}

	/**
	 * @see Random#genRandomInteger(int)
	 */
	public int genRandomInteger(final int upperBound) {
		checkArgument(upperBound > 0, "The upper bound must be a positive integer greater than 0.");

		return genRandomInteger(BigInteger.valueOf(upperBound)).intValueExact();
	}

	/**
	 * @see Random#genUniqueDecimalStrings(int, int)
	 */
	@SuppressWarnings("java:S117")
	public List<String> genUniqueDecimalStrings(final int desiredCodeLength, final int numberOfUniqueCodes) {
		final int l = desiredCodeLength;
		final int n = numberOfUniqueCodes;
		checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
		checkArgument(n > 0, "The number of unique codes must be strictly positive.");

		checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

		final Alphabet A_10 = Base10Alphabet.getInstance();

		final List<String> codes = new ArrayList<>(n);
		while (codes.size() < n) {
			final String c = genRandomString(l, A_10);

			if (!codes.contains(c)) {
				codes.add(c);
			}
		}

		return codes;
	}

	/**
	 * Generates a vector (collection) of random {@link ZqElement}s between 0 (incl.) and {@code upperBound} (excl.).
	 *
	 * @param upperBound q, the exclusive upper bound. Must be non null and strictly positive.
	 * @param length     n, the desired length. Must be strictly positive.
	 * @return {@code List<ZqElement>}
	 */
	public GroupVector<ZqElement, ZqGroup> genRandomVector(final BigInteger upperBound, final int length) {
		checkNotNull(upperBound);
		checkArgument(upperBound.compareTo(BigInteger.ZERO) > 0, "The upper bound should be greater than zero");
		checkArgument(length > 0, "The length should be greater than zero");

		final BigInteger q = upperBound;
		final int n = length;

		final ZqGroup zqGroup = new ZqGroup(q);

		return Stream.generate(() -> ZqElement.create(genRandomInteger(q), zqGroup))
				.limit(n)
				.collect(toGroupVector());
	}

	/**
	 * Generates an array of {@code byteLength} random bytes.
	 *
	 * @param byteLength The number of bytes to generate.
	 * @return An array of {@code byteLength} random bytes.
	 */
	public byte[] randomBytes(final int byteLength) {
		final byte[] randomBytes = new byte[byteLength];
		secureRandom.nextBytes(randomBytes);

		return randomBytes;
	}

	/**
	 * @see Random#genRandomString(int, Alphabet)
	 */
	@SuppressWarnings("java:S117")
	public String genRandomString(final int length, final Alphabet alphabet) {

		checkArgument(length > 0, "The desired length of string must be strictly positive. [length: %s]", length);
		checkNotNull(alphabet);

		// Input
		final int l = length;
		final Alphabet A = alphabet;
		final int k = A.size();

		// Operation
		return IntStream.range(0, l)
				.mapToObj(i -> {
					final int m = genRandomInteger(k);
					return A.get(m);
				})
				.collect(Collectors.joining());
	}
}
