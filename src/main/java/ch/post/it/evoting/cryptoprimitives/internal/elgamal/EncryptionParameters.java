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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import static ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegerOperationsService.millerRabin;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
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

	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = BigInteger.TWO;
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger SIX = BigInteger.valueOf(6);

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
	@SuppressWarnings("java:S117")
	public GqGroup getEncryptionParameters(final String seed, final List<Integer> smallPrimes) {
		checkNotNull(seed);
		checkNotNull(smallPrimes);
		smallPrimes.forEach(prime -> checkArgument(PrimesInternal.isSmallPrime(prime), "The given number is not a prime. [Number: %s]", prime));

		final int lambda = securityLevel.getSecurityStrength();
		final ArrayList<BigInteger> sp = smallPrimes.stream().map(BigInteger::valueOf)
				.collect(Collectors.toCollection(ArrayList::new));
		final int l = smallPrimes.size();
		final int pBitLength = securityLevel.getPBitLength();

		final ImmutableByteArray q_b_hat = shake256(stringToByteArray(seed), pBitLength / 8);
		final ImmutableByteArray q_b = ImmutableByteArray.concat(ImmutableByteArray.of((byte) 0x02), q_b_hat);
		final BigInteger q_prime = byteArrayToInteger(q_b).shiftRight(3);
		BigInteger q = q_prime.subtract(q_prime.mod(SIX)).add(FIVE);
		final ArrayList<BigInteger> r = new ArrayList<>(l);
		for (int i = 0; i < l; i++) {
			r.add(i, q.mod(sp.get(i)));
		}
		BigInteger delta = ZERO;
		do {
			do {
				delta = delta.add(SIX);
				int i = 0;
				while (i < l) {
					if ((r.get(i).add(delta).mod(sp.get(i)).equals(ZERO)) || (TWO.multiply(r.get(i).add(delta)).add(ONE).mod(sp.get(i))
							.equals(ZERO))) {
						delta = delta.add(SIX);
						i = 0;
					} else {
						i = i + 1;
					}
				}
			} while (!(millerRabin(q.add(delta), 1)) || !(millerRabin(TWO.multiply(q.add(delta)).add(ONE), 1)));
		} while (!(millerRabin(q.add(delta), lambda / 2)) || !(millerRabin(TWO.multiply(q.add(delta)).add(ONE), lambda / 2)));
		q = q.add(delta);
		final BigInteger p = TWO.multiply(q).add(ONE);

		final BigInteger g;
		if (isTwoGroupMember(p)) {
			g = TWO;
		} else {
			g = THREE;
		}

		return new GqGroup(p, q, g);
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

}
