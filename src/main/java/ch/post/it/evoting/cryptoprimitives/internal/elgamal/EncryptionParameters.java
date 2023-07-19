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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import com.google.common.primitives.Bytes;

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
	private static final BigInteger TWO = BigInteger.valueOf(2);
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger SIX = BigInteger.valueOf(6);

	private final SecurityLevelInternal lambda;
	private final SecureRandom secureRandom;

	/**
	 * Constructs an instance with a {@link SecurityLevelInternal}.
	 */
	public EncryptionParameters() {
		this.lambda = SecurityLevelConfig.getSystemSecurityLevel();
		this.secureRandom = new SecureRandom();
	}

	/**
	 * Picks verifiable encryption parameters used for the election. The election name is used as the seed.
	 *
	 * @param seed        the election name. Must be non-null.
	 * @param smallPrimes a list of small primes. Must be non-null and not empty.
	 * @return a {@link GqGroup} containing the verifiable encryption parameters p, q and g.
	 */
	@SuppressWarnings("java:S117")
	public GqGroup getEncryptionParameters(final String seed, final List<Integer> smallPrimes) {
		checkNotNull(seed);
		checkNotNull(smallPrimes);
		smallPrimes.forEach(prime -> checkArgument(PrimesInternal.isSmallPrime(prime), "The given number is not a prime. [Number: %s]", prime));

		final int certaintyLevel = lambda.getSecurityLevelBits();
		final ArrayList<BigInteger> sp = smallPrimes.stream().map(BigInteger::valueOf)
				.collect(Collectors.toCollection(ArrayList::new));
		final int l = smallPrimes.size();
		final int pBitLength = lambda.getPBitLength();

		final byte[] q_b_hat = shake128(stringToByteArray(seed), pBitLength / 8);
		final byte[] q_b = Bytes.concat(new byte[] { 0x02 }, q_b_hat);
		final BigInteger q_prime = byteArrayToInteger(q_b).shiftRight(3);
		BigInteger q = q_prime.subtract(q_prime.mod(SIX)).add(FIVE);
		final ArrayList<BigInteger> r = new ArrayList<>(l);
		for (int i = 0; i < l; i++) {
			r.add(i, q.mod(sp.get(i)));
		}
		final BigInteger jump = SIX;
		BigInteger delta = ZERO;
		do {
			delta = delta.add(jump);
			int i = 0;
			while (i < l) {
				if ((r.get(i).add(delta).mod(sp.get(i)).equals(ZERO)) || (TWO.multiply(r.get(i).add(delta)).add(ONE).mod(sp.get(i)).equals(ZERO))) {
					delta = delta.add(jump);
					i = 0;
				} else {
					i = i + 1;
				}
			}
		} while (!(q.add(delta).isProbablePrime(certaintyLevel)) || !(TWO.multiply(q.add(delta)).add(ONE).isProbablePrime(certaintyLevel)));
		q = q.add(delta);
		final BigInteger p = TWO.multiply(q).add(ONE);

		final BigInteger g;
		if (isTwoGroupMember(p)) {
			g = TWO;
		} else {
			g = THREE;
		}

		if (!millerRabin(q, 64) || !millerRabin(p, 64)) {
			throw new IllegalStateException("p and q must both pass the Miller-Rabin test");
		}

		return new GqGroup(p, q, g);
	}

	private byte[] shake128(final byte[] message, final int outputLength) {
		final byte[] result = new byte[outputLength];
		final SHAKEDigest shakeDigest = new SHAKEDigest(128);

		shakeDigest.update(message, 0, message.length);
		shakeDigest.doFinal(result, 0, outputLength);

		return result;
	}

	private boolean millerRabin(final BigInteger n, final int rounds) {
		final BigInteger nMinusOne = n.subtract(ONE);
		final int s = nMinusOne.getLowestSetBit();
		final BigInteger d = nMinusOne.shiftRight(s);
		return IntStream.range(0, rounds).parallel().allMatch(i -> {
			BigInteger a;
			do {
				a = new BigInteger(n.bitLength(), secureRandom);
			} while (a.compareTo(ONE) <= 0 || a.compareTo(nMinusOne) >=0);

			int j = 0;
			BigInteger x = a.modPow(d, n);
			while (!((j == 0 && x.equals(ONE)) || x.equals(nMinusOne))) {
				if (j > 0 && x.equals(ONE) || ++j == s) {
					return false;
				}
				x = x.modPow(TWO, n);
			}
			return true;
		});
	}

	/**
	 * Checks if the value two is a member of the GqGroup defined by p.
	 */
	private boolean isTwoGroupMember(final BigInteger p) {
		return BigIntegerOperationsService.getLegendre(TWO, p) == 1;
	}

}
