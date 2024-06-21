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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.byteLength;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.cutToBitLength;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
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
	 * @see Random#genRandomInteger(BigInteger)
	 */
	@SuppressWarnings("java:S117")
	public BigInteger genRandomInteger(final BigInteger upperBound) {
		// Input.
		final BigInteger m = checkNotNull(upperBound);
		checkArgument(m.signum() > 0, "The upper bound must be a positive integer greater than 0.");

		// Operation.
		if (m.compareTo(BigInteger.ONE) == 0) {
			return BigInteger.ZERO;
		}
		final BigInteger m_minus_one = m.subtract(BigInteger.ONE);
		final int length = byteLength(m_minus_one);
		final int bitLength = m_minus_one.bitLength();
		BigInteger r;
		do {
			final ImmutableByteArray rBytes = cutToBitLength(randomBytes(length), bitLength);
			r = byteArrayToInteger(rBytes);
		} while (r.compareTo(m) >= 0);

		// Output.
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

		final Set<String> codes = HashSet.newHashSet(n);
		while (codes.size() < n) {
			final String c = genRandomString(l, A_10);

			// The Set#add method is, in this context, equivalent to the if statement in the specification.
			codes.add(c);
		}

		return codes.stream().toList();
	}

	/**
	 * Generates a vector (collection) of random {@link ZqElement}s between 0 (incl.) and {@code upperBound} (excl.).
	 *
	 * @param upperBound q, the exclusive upper bound. Must be non null and strictly positive.
	 * @param length     n, the desired length. Must be strictly positive.
	 * @return {@code List<ZqElement>}
	 */
	public GroupVector<ZqElement, ZqGroup> genRandomVector(final BigInteger upperBound, final int length) {
		final BigInteger q = checkNotNull(upperBound);
		checkArgument(q.signum() > 0, "The upper bound should be greater than zero");
		checkArgument(length > 0, "The length should be greater than zero");

		final int n = length;

		final ZqGroup zqGroup = new ZqGroup(q);

		return Stream.generate(() -> ZqElement.create(genRandomInteger(q), zqGroup))
				.limit(n)
				.collect(toGroupVector());
	}

	/**
	 * Generates an immutable array of {@code byteLength} random bytes.
	 *
	 * @param byteLength The number of bytes to generate.
	 * @return An immutable array of {@code byteLength} random bytes.
	 */
	public ImmutableByteArray randomBytes(final int byteLength) {
		final byte[] randomBytes = new byte[byteLength];
		secureRandom.nextBytes(randomBytes);

		return ImmutableByteArray.from(randomBytes);
	}

	/**
	 * @see Random#genRandomString(int, Alphabet)
	 */
	@SuppressWarnings("java:S117")
	public String genRandomString(final int length, final Alphabet alphabet) {

		checkArgument(length > 0, "The desired length of string must be strictly positive. [length: %s]", length);

		// Input
		final int l = length;
		final Alphabet A = checkNotNull(alphabet);
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
