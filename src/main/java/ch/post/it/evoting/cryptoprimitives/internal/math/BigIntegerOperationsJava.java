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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;

/**
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsJava implements BigIntegerOperations {

	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = BigInteger.TWO;

	@Override
	public BigInteger modMultiply(final BigInteger n1, final BigInteger n2, final BigInteger modulus) {
		checkNotNull(n1);
		checkNotNull(n2);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(ONE) > 0, MODULUS_CHECK_MESSAGE);

		return n1.multiply(n2).mod(modulus);
	}

	@Override
	public BigInteger modExponentiate(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		checkNotNull(base);
		checkNotNull(exponent);
		checkNotNull(modulus);
		checkArgument(exponent.signum() >= 0 || base.gcd(modulus).equals(ONE),
				"When the exponent is negative, base and modulus must be relatively prime");
		checkArgument(modulus.compareTo(ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");
		return base.modPow(exponent, modulus);
	}

	@Override
	public BigInteger multiModExp(final List<BigInteger> bases, final List<BigInteger> exponents, final BigInteger modulus) {
		final List<BigInteger> basesCopy = checkNotNull(bases).stream()
				.map(Preconditions::checkNotNull)
				.toList();
		checkArgument(!basesCopy.isEmpty(), "Bases must be non empty.");

		final int exponentsSize = exponents.size();
		final List<BigInteger> exponentsCopy = checkNotNull(exponents).stream()
				.filter(exponent -> checkNotNull(exponent).signum() >= 0)
				.toList();
		checkArgument(exponentsSize == exponentsCopy.size(), "Exponents must be positive");

		// The next check assures also that exponentsCopy is not empty
		checkArgument(basesCopy.size() == exponentsCopy.size(), "Bases and exponents must have the same size");
		checkArgument(modulus.compareTo(ONE) > 0, MODULUS_CHECK_MESSAGE);

		final int numElements = basesCopy.size();

		return IntStream.range(0, numElements)
				.parallel()
				.mapToObj(i -> modExponentiate(basesCopy.get(i), exponentsCopy.get(i), modulus))
				.reduce(ONE, (a, b) -> modMultiply(a, b, modulus));
	}

	@Override
	public BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		checkNotNull(n);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(n.gcd(modulus).equals(ONE), "The number to be inverted must be relatively prime to the modulus.");

		return n.modInverse(modulus);
	}

	@Override
	public int getLegendre(final BigInteger a, final BigInteger p) {
		checkNotNull(a);
		checkNotNull(p);
		checkArgument(p.compareTo(TWO) > 0 && p.mod(TWO).equals(ONE),
				"p must be an odd integer greater than 2");

		final BigInteger three = BigInteger.valueOf(3);
		final BigInteger four = BigInteger.valueOf(4);
		final BigInteger five = BigInteger.valueOf(5);
		final BigInteger eight = BigInteger.valueOf(8);

		// Operation
		BigInteger b = a.mod(p);
		BigInteger q = p;
		int t = 1;
		BigInteger r;

		while (!b.equals(ZERO)) {
			while (b.mod(TWO).equals(ZERO)) {
				b = b.divide(TWO);
				r = q.mod(eight);
				if (r.equals(three) || r.equals(five)) {
					t = -t;
				}
			}

			r = q;
			q = b;
			b = r;
			if (b.mod(four).equals(three) && q.mod(four).equals(three)) {
				t = -t;
			}
			b = b.mod(q);
		}
		if (q.equals(ONE)) {
			return t;
		}

		return 0;
	}
}
