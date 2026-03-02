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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import static ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegerOperationsService.millerRabin;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegerOperationsService;
import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * Provides functionality to create verifiable encryption parameters as a {@link GqGroup}.
 *
 * <p> This class is immutable and thread safe. </p>
 */
public final class EncryptionParameters {

	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = BigInteger.TWO;
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger SIX = BigInteger.valueOf(6);
	private static final int OMEGA = 50_000; // Number of candidate offsets tested per chunk

	private final SecurityLevelInternal securityLevel;

	/**
	 * Constructs an instance with a {@link SecurityLevelInternal}.
	 */
	public EncryptionParameters() {
		this.securityLevel = SecurityLevelConfig.getSystemSecurityLevel();
	}

	/**
	 * Generates verifiable encryption parameters used for the election.
	 * <p>
	 * Executions with the same seed, yield the same encryption parameters.
	 * </p>
	 *
	 * @param seed        the name of the election event. Must be non-null.
	 * @param smallPrimes a list of small primes. Must be non-null.
	 * @return a {@link GqGroup} containing the verifiable encryption parameters p, q and g.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the numbers in small primes list is not a prime.
	 */
	@SuppressWarnings({ "java:S117", "java:S3776" })
	public GqGroup getEncryptionParameters(final String seed, final ImmutableList<Integer> smallPrimes) {
		checkNotNull(seed);
		checkNotNull(smallPrimes);
		smallPrimes.forEach(prime -> checkArgument(PrimesInternal.isSmallPrime(prime), "The given number is not a prime. [Number: %s]", prime));

		final int[] sp = smallPrimes.stream()
				.mapToInt(Integer::intValue)
				.toArray();

		final int lambda = securityLevel.getSecurityStrength();
		// getPBitLength ensures |p| mod 8 = 0.
		final int pBitLength = securityLevel.getPBitLength();

		// Operation.
		final ImmutableByteArray q_b_hat = shake256(stringToByteArray(seed), pBitLength / Byte.SIZE);
		final ImmutableByteArray q_b = ImmutableByteArray.concat(ImmutableByteArray.of((byte) 0x02), q_b_hat);
		final BigInteger q_prime = byteArrayToInteger(q_b).shiftRight(3);
		final BigInteger q = q_prime.subtract(q_prime.mod(SIX)).add(FIVE);

		final int[] r = Arrays.stream(sp)
				.map(sp_i -> q.mod(BigInteger.valueOf(sp_i)).intValue())
				.toArray();

		long kappa = 0;

		while (true) {
			final long mu = kappa * OMEGA;

			final ImmutableList<BigInteger> C = IntStream.range(0, OMEGA)
					.parallel()
					.mapToObj(k -> {
						final long delta = 6 * (mu + k + 1);
						if (!passesSmallPrimeSieve(r, delta, sp)) {
							return null;
						}

						final BigInteger q_cand = q.add(BigInteger.valueOf(delta));
						if (!millerRabin(q_cand, 1)) {
							return null;
						}

						if (!millerRabin(TWO.multiply(q_cand).add(ONE), 1)) {
							return null;
						}

						return q_cand;
					})
					.filter(Objects::nonNull)
					// Ensure ascending order of the candidates.
					.sorted(BigInteger::compareTo)
					.collect(ImmutableList.toImmutableList());

			for (final BigInteger q_cand : C) {
				final BigInteger p_cand = TWO.multiply(q_cand).add(ONE);

				if (millerRabin(q_cand, lambda / 2) && millerRabin(p_cand, lambda / 2)) {
					final BigInteger g = isTwoGroupMember(p_cand) ? TWO : THREE;

					return new GqGroup(p_cand, q_cand, g);
				}
			}
			kappa++;
		}
	}

	private ImmutableByteArray shake256(final ImmutableByteArray message, final int outputLength) {
		final byte[] result = new byte[outputLength];
		final SHAKEDigest shakeDigest = new SHAKEDigest(256);

		shakeDigest.update(message.elements(), 0, message.length());
		shakeDigest.doFinal(result, 0, outputLength);

		return new ImmutableByteArray(result);
	}

	/**
	 * Checks if the value two is a member of the GqGroup defined by p.
	 */
	private boolean isTwoGroupMember(final BigInteger p) {
		return BigIntegerOperationsService.getLegendre(TWO, p) == 1;
	}

	/**
	 * Word-sized sieve for rejecting composite safe-prime candidates.
	 * <p>
	 * This method tests whether a candidate safe-prime offset {@code delta} passes a small-prime divisibility sieve. For each small prime
	 * {@code sp[i]}, it checks that neither the candidate {@code qCandidate = qBase + delta} nor the associated value
	 * {@code pCandidate = 2 · qCandidate + 1} is divisible by {@code sp[i]}.
	 * </p>
	 * <p>
	 * The test is performed using word-sized modular arithmetic. For each small prime {@code sp[i]}, the offset {@code delta} is reduced modulo
	 * {@code sp[i]} and added to the corresponding precomputed residue {@code r[i]}, where {@code r[i]} denotes the residue of the fixed base value
	 * modulo {@code sp[i]}. This allows efficient rejection of composite candidates before applying probabilistic primality tests.
	 * </p>
	 * <p>
	 * <strong>Performance considerations:</strong> This method is performance-critical
	 * and intentionally avoids any input validation or defensive checks. It is a private helper invoked in a controlled context, and all inputs are
	 * assumed to satisfy the required preconditions by contract.
	 * </p>
	 *
	 * @param r     the precomputed residues of the fixed base value modulo the small primes; assumed to be aligned with {@code sp}.
	 * @param delta the non-negative offset applied to the base value.
	 * @param sp    the small primes used for trial division.
	 * @return {@code true} if both {@code qCandidate} and {@code pCandidate} are not divisible by any of the given small primes; {@code false}
	 * otherwise.
	 */
	private static boolean passesSmallPrimeSieve(final int[] r, final long delta, final int[] sp) {
		final int l = sp.length;
		for (int i = 0; i < l; i++) {
			final int r_prime = (r[i] + (int) (delta % sp[i])) % sp[i];
			if (r_prime == 0) {
				return false;
			}
			if ((2 * r_prime + 1) % sp[i] == 0) {
				return false;
			}
		}
		return true;
	}
}
