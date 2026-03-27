/*
 * Copyright 2025 Swiss Post Ltd
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

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

public interface BigIntegerOperations {

	String MODULUS_CHECK_MESSAGE = "The modulus must be greater than 1";

	/**
	 * @return true if fixed base exponentiation is supported, false otherwise.
	 */
	default boolean isFixedBaseExponentiationSupported() {
		return false;
	}

	/**
	 * Generates a precomputed cache of values to minimize the number of multiplications required during modular exponentiation. This precomputation
	 * substantially speeds up computation when a fixed element of a group is repeatedly raised to many different powers.
	 *
	 * @param base    the base
	 * @param modulus the modulus
	 */
	default void generateCache(final BigInteger base, final BigInteger modulus) {
		throw new UnsupportedOperationException("This implementation does not support fixed base optimizations");
	}

	/**
	 * Multiplies two {@link BigInteger}s and take the modulus.
	 *
	 * @param n1      the multiplier
	 * @param n2      the multiplicand
	 * @param modulus the modulus &gt; 1
	 * @return the product n1 &times; n2 mod modulus
	 */
	BigInteger modMultiply(BigInteger n1, BigInteger n2, BigInteger modulus);

	/**
	 * Exponentiates a {@link BigInteger} by another and take the modulus. If the exponent is negative, base and modulus must be relatively prime.
	 *
	 * @param base     the base
	 * @param exponent the exponent
	 * @param modulus  the modulus &gt; 1 and odd
	 * @return the power base<sup>exponent</sup> mod modulus
	 */
	BigInteger modExponentiate(BigInteger base, BigInteger exponent, BigInteger modulus);

	/**
	 * Exponentiates the elements of a list of {@link BigInteger}s by the elements of a second list and multiply the resulting terms. If an exponent
	 * is negative, then the corresponding base must be relatively prime to the modulus. This operations needs both lists to be of equal size.
	 *
	 * @param bases     the list of base values
	 * @param exponents the list of exponent values
	 * @param modulus   the modulus &gt; 1
	 * @return the product of the powers b[0]^e[0] * b[1]^e[1] * ... * b[n-1]^e[n-1] mod modulus
	 */
	BigInteger multiModExp(final ImmutableList<BigInteger> bases, final ImmutableList<BigInteger> exponents, final BigInteger modulus);

	/**
	 * Inverts an element with respect to a modulus.
	 *
	 * @param n       the number to be inverted
	 * @param modulus the modulus &gt; 1
	 * @return n<sup>-1</sup> mod modulus
	 */
	BigInteger modInvert(BigInteger n, BigInteger modulus);

	/**
	 * Calculates the Legendre symbol (a|p).
	 * <p>
	 * The Legendre symbol allows us determining group membership efficiently. An integer a is a quadratic residue modulo p if and only if (a|p) = 1.
	 * The caller of this method needs to ensure that p is an odd prime number.
	 * </p>
	 *
	 * @param a positive integer
	 * @param p odd prime.
	 * @return (a | p) Possible values -1,0,1
	 * @throws NullPointerException     if any of the arguments is null
	 * @throws IllegalArgumentException if p is not an odd integer strictly greater than 2.
	 */
	int getLegendre(BigInteger a, BigInteger p);
}
