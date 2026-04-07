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
package ch.post.it.evoting.cryptoprimitives.internal.hashing;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray.concat;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.hashing.Hash;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.HashFunction;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.XOF;
import ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

/**
 * Recursive hash service using a default SHA3-256 message digest.
 *
 * <p>This class is thread safe.</p>
 */
public class HashService implements Hash {

	public static final int HASH_LENGTH_BYTES = 32;
	private static final HashService INSTANCE = new HashService(SecurityLevelConfig.getSystemSecurityLevel().getRecursiveHashHashFunction(),
			SecurityLevelConfig.getSystemSecurityLevel().getRecursiveHashToZqXOF());

	private static final ImmutableByteArray BYTE_ARRAY_PREFIX = ImmutableByteArray.of((byte) 0x00);
	private static final ImmutableByteArray BIG_INTEGER_PREFIX = ImmutableByteArray.of((byte) 0x01);
	private static final ImmutableByteArray STRING_PREFIX = ImmutableByteArray.of((byte) 0x02);
	private static final ImmutableByteArray ARRAY_PREFIX = ImmutableByteArray.of((byte) 0x03);

	private static final String NO_VALUES = "Cannot hash no values.";
	private final HashFunction hashFunction;
	private final XOF xof;

	@VisibleForTesting
	HashService(final HashFunction hashFunction, final XOF xof) {
		this.hashFunction = hashFunction;
		this.xof = xof;
	}

	public static HashService getInstance() {
		return INSTANCE;
	}

	/**
	 * See {@link Hash#recursiveHash}
	 */
	@Override
	public ImmutableByteArray recursiveHash(final Hashable... values) {
		final ImmutableList<Hashable> v = Arrays.stream(checkNotNull(values))
				.map(Preconditions::checkNotNull)
				.collect(toImmutableList());
		checkArgument(!v.isEmpty(), NO_VALUES);

		if (v.size() > 1) {
			return recursiveHash(HashableList.from(v));
		} else {
			final Hashable value = v.get(0);

			switch (value) {
			case final ImmutableByteArray w -> {
				return hashFunction.hash(concat(BYTE_ARRAY_PREFIX, w));
			}
			case final HashableBigInteger hashableBigInteger -> {
				final BigInteger w = hashableBigInteger.toHashableForm();
				checkArgument(w.signum() >= 0);
				return hashFunction.hash(concat(BIG_INTEGER_PREFIX, integerToByteArray(w)));
			}
			case final HashableString hashableString -> {
				final String w = hashableString.toHashableForm();
				return hashFunction.hash(concat(STRING_PREFIX, stringToByteArray(w)));
			}
			case final HashableList hashableList -> {
				final ImmutableList<? extends Hashable> w = hashableList.toHashableForm();
				return hashFunction.hash(concat(
						Stream.concat(
								Stream.of(ARRAY_PREFIX),
								w.stream().parallel().map(this::recursiveHash)
						).toArray(ImmutableByteArray[]::new)
				));
			}
			default -> throw new IllegalArgumentException(String.format("Object of type %s cannot be hashed.", value.getClass()));
			}
		}
	}

	/**
	 * See {@link Hash#hashAndSquare}
	 */
	@Override
	@SuppressWarnings("java:S117")
	public GqElement hashAndSquare(final BigInteger x, final GqGroup group) {
		checkNotNull(x);
		checkNotNull(group);

		checkArgument(this.getHashLength() * Byte.SIZE < group.getQ().bitLength(),
				"The hash length must be smaller than the bit length of the GqGroup's q.");

		final BigInteger q = group.getQ();

		final BigInteger x_h = recursiveHashToZq(q, HashableString.from("HashAndSquare"), HashableBigInteger.from(x)).getValue().add(BigInteger.ONE);

		return GqElement.GqElementFactory.fromSquareRoot(x_h, group);
	}

	/**
	 * See {@link Hash#recursiveHashToZq}
	 */
	@Override
	@SuppressWarnings("java:S117")
	public ZqElement recursiveHashToZq(final BigInteger exclusiveUpperBound, final Hashable... values) {
		checkNotNull(exclusiveUpperBound);
		checkNotNull(values);
		Arrays.stream(values).forEach(Preconditions::checkNotNull);

		final int lambda = SecurityLevelConfig.getSystemSecurityLevel().getSecurityStrength();

		final int k = values.length;
		final BigInteger q = exclusiveUpperBound;
		final Hashable[] v = values;
		checkArgument(k > 0, NO_VALUES);
		checkArgument(q.signum() > 0, "The upper bound must be strictly positive.");
		checkArgument(q.bitLength() >= 512, "The exclusive upper bound must have a bit length of at least 512.");

		final BigInteger h_prime = byteArrayToInteger(recursiveHashOfLength(q.bitLength() + 2 * lambda,
				Streams.concat(Stream.of(HashableBigInteger.from(q)), Stream.of(HashableString.from("RecursiveHash")), Arrays.stream(v))
						.toArray(Hashable[]::new)));
		final BigInteger h = h_prime.mod(q);

		return ZqElement.create(h, new ZqGroup(q));
	}

	/**
	 * Computes the hash of a requested size of multiple (potentially) recursive inputs.
	 *
	 * @param requestedBitLength the requested bit length of the output >= 512.
	 * @param values             the objects to be hashed. Non-empty.
	 * @return a hash of the requested bit length
	 * @throws NullPointerException     if the values are null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>the values contain null elements</li>
	 *                                      <li>the values are empty</li>
	 *                                      <li>the requested bit length is smaller than 512</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	@VisibleForTesting
	ImmutableByteArray recursiveHashOfLength(final int requestedBitLength, final Hashable... values) {
		final ImmutableList<Hashable> v = Arrays.stream(checkNotNull(values))
				.map(Preconditions::checkNotNull)
				.collect(toImmutableList());

		final int k = values.length;
		final int l = requestedBitLength;
		checkArgument(k > 0, NO_VALUES);
		checkArgument(l >= xof.getMinimumOutputLengthBits(), "The requested bit length must be at least %s.", xof.getMinimumOutputLengthBits());

		final int L = Math.ceilDivExact(l, Byte.SIZE);
		if (k > 1) {
			return recursiveHashOfLength(l, HashableList.from(v));
		} else {
			final Hashable value = values[0];

			switch (value) {
			case final ImmutableByteArray w -> {
				final ImmutableByteArray h = concat(BYTE_ARRAY_PREFIX, w);
				return ByteArrays.cutToBitLength(shake256(L, h), l);
			}
			case final HashableBigInteger hashableBigInteger -> {
				final BigInteger w = hashableBigInteger.toHashableForm();
				checkArgument(w.signum() >= 0);
				final ImmutableByteArray h = concat(BIG_INTEGER_PREFIX, integerToByteArray(w));
				return ByteArrays.cutToBitLength(shake256(L, h), l);
			}
			case final HashableString hashableString -> {
				final String w = hashableString.toHashableForm();
				final ImmutableByteArray h = concat(STRING_PREFIX, stringToByteArray(w));
				return ByteArrays.cutToBitLength(shake256(L, h), l);
			}
			case final HashableList hashableList -> {
				final ImmutableList<? extends Hashable> w = hashableList.toHashableForm();
				final ImmutableByteArray h = Stream.concat(
								Stream.of(ARRAY_PREFIX),
								w.stream().parallel().map(w_i -> recursiveHashOfLength(l, w_i)))
						.reduce(ImmutableByteArray.EMPTY, ImmutableByteArray::concat);
				return ByteArrays.cutToBitLength(shake256(L, h), l);
			}
			default -> throw new IllegalArgumentException(String.format("Object of type %s cannot be hashed.", value.getClass()));
			}
		}
	}

	/**
	 * @return this message digest length in bytes.
	 */
	public int getHashLength() {
		return HASH_LENGTH_BYTES;
	}

	private ImmutableByteArray shake256(final int outputLength, final ImmutableByteArray message) {
		final byte[] result = new byte[outputLength];
		final SHAKEDigest shakeDigest = new SHAKEDigest(256);

		shakeDigest.update(message.elements(), 0, message.length());
		shakeDigest.doFinal(result, 0, outputLength);

		return new ImmutableByteArray(result);
	}
}
