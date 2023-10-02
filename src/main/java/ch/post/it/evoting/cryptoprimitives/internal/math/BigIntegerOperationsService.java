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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.verificatum.vmgj.VMG;

/**
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsService {

	private static final Logger LOG = LoggerFactory.getLogger(BigIntegerOperationsService.class);
	private static final BigIntegerOperations bigIntegerOperations;
	private static final SecureRandom secureRandom;

	static {
		if (VMG.checkLoaded()) {
			LOG.info("Verificatum Multiplicative Groups Library for Java (VMGJ)  is installed and ready to use");
			bigIntegerOperations = new BigIntegerOperationsVMGJ();
		} else {
			LOG.warn("Verificatum Multiplicative Groups Library for Java (VMGJ)  is not installed, some native code optimizations are not available, "
					+ "integer operations will now take longer. Verify that the libraries GMP, GMPMEE and VMGJ are installed and referenced in the java.library.path");
			bigIntegerOperations = new BigIntegerOperationsJava();
		}
		secureRandom = new SecureRandom();
	}

	private BigIntegerOperationsService() {
		throw new UnsupportedOperationException("BigIntegerOperationsService is not supposed to be instantiated");
	}

	public static BigInteger modMultiply(final BigInteger n1, final BigInteger n2, final BigInteger modulus) {
		return bigIntegerOperations.modMultiply(n1, n2, modulus);
	}

	public static BigInteger modExponentiate(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		return bigIntegerOperations.modExponentiate(base, exponent, modulus);
	}

	public static BigInteger multiModExp(final List<BigInteger> bases, final List<BigInteger> exponents, final BigInteger modulus) {
		return bigIntegerOperations.multiModExp(bases, exponents, modulus);
	}

	public static BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		return bigIntegerOperations.modInvert(n, modulus);
	}

	public static int getLegendre(final BigInteger a, final BigInteger p) {
		return bigIntegerOperations.getLegendre(a, p);
	}

	public static void generateCache(final BigInteger basis, final BigInteger modulus) {
		if (bigIntegerOperations.isFixedBaseExponentiationSupported()) {
			bigIntegerOperations.generateCache(basis, modulus);
		}
	}

	/**
	 * Runs the Miller-Rabin probabilistic primality test.
	 *
	 * @param candidate n, an odd integer greater than 3 to be tested. Must be non-null.
	 * @param rounds t, the number of rounds to be done. Must be strictly positive.
	 * @return {@code true} if the candidate is probably prime, {@code false} if the candidate is definitely composite.
	 */
	public static boolean millerRabin(final BigInteger candidate, final int rounds) {
		checkNotNull(candidate);
		checkArgument(candidate.compareTo(TWO) > 0, "n must be at least three.");
		checkArgument(candidate.mod(TWO).equals(ONE), "n must be odd.");
		checkArgument(rounds > 0, "The number of rounds must be strictly positive.");

		// For n = 3, we cannot choose a random integer a, 2 <= a <= n - 2
		if (candidate.equals(BigInteger.valueOf(3))) {
			return true;
		}

		final BigInteger n = candidate;
		final int t = rounds;

		// Write n - 1 = 2^s * r such that r is odd
		final BigInteger n_minus_one = n.subtract(ONE);
		final int s = n_minus_one.getLowestSetBit();
		final BigInteger r = n_minus_one.shiftRight(s);
		return IntStream.range(0, t).parallel().allMatch(i -> {
			// Choose a random integer a, 2 <= a <= n - 2
			BigInteger a;
			do {
				a = new BigInteger(n.bitLength(), secureRandom);
			} while (a.compareTo(ONE) <= 0 || a.compareTo(n_minus_one) >= 0);

			BigInteger y = bigIntegerOperations.modExponentiate(a, r, n);
			if (!y.equals(ONE) && !y.equals(n_minus_one)) {
				int j = 1;
				while (j <= s - 1 && !y.equals(n_minus_one)) {
					y = bigIntegerOperations.modExponentiate(y, TWO, n);
					if (y.equals(ONE)) {
						return false;
					}
					j = j + 1;
				}
				if (!y.equals(n_minus_one)) {
					return false;
				}
			}
			return true;
		});
	}
}
