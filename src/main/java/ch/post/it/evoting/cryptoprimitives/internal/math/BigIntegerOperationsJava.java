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
import static org.bouncycastle.pqc.legacy.math.linearalgebra.IntegerFunctions.jacobi;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsJava implements BigIntegerOperations {

	@Override
	public BigInteger modMultiply(final BigInteger n1, final BigInteger n2, final BigInteger modulus) {
		checkNotNull(n1);
		checkNotNull(n2);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);

		return n1.multiply(n2).mod(modulus);
	}

	@Override
	public BigInteger modExponentiate(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		checkNotNull(base);
		checkNotNull(exponent);
		checkNotNull(modulus);
		checkArgument(exponent.compareTo(BigInteger.ZERO) >= 0 || base.gcd(modulus).equals(BigInteger.ONE),
				"When the exponent is negative, base and modulus must be relatively prime");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");
		return base.modPow(exponent, modulus);
	}

	@Override
	public BigInteger multiModExp(final List<BigInteger> bases, final List<BigInteger> exponents, final BigInteger modulus) {
		checkNotNull(bases);
		checkArgument(bases.stream().allMatch(Objects::nonNull), "Elements must not contain nulls");
		final List<BigInteger> basesCopy = List.copyOf(bases);
		checkArgument(!basesCopy.isEmpty(), "Bases must be non empty.");

		checkNotNull(exponents);
		checkArgument(exponents.stream().allMatch(exponent -> checkNotNull(exponent).signum() >= 0), "Elements must be positive");
		final List<BigInteger> exponentsCopy = List.copyOf(exponents);

		// The next check assures also that exponentsCopy is not empty
		checkArgument(basesCopy.size() == exponentsCopy.size(), "Bases and exponents must have the same size");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);

		final int numElements = basesCopy.size();

		return IntStream.range(0, numElements)
				.parallel()
				.mapToObj(i -> modExponentiate(basesCopy.get(i), exponentsCopy.get(i), modulus))
				.reduce(BigInteger.ONE, (a, b) -> modMultiply(a, b, modulus));
	}

	@Override
	public BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		checkNotNull(n);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(n.gcd(modulus).equals(BigInteger.ONE), "The number to be inverted must be relatively prime to the modulus.");

		return n.modInverse(modulus);
	}

	@Override
	public int getJacobi(final BigInteger a, final BigInteger n) {
		checkNotNull(a);
		checkNotNull(n);
		checkArgument(a.compareTo(BigInteger.ZERO) > 0, "a must be positive");

		return jacobi(a, n);
	}
}
