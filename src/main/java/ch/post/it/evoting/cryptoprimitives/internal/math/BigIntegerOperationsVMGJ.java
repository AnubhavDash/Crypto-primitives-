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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.verificatum.vmgj.FpowmTab;
import com.verificatum.vmgj.VMG;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * Optimized BigIntegerOperations using Verificatum Multiplicative Groups Library for Java (VMGJ) .
 *
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsVMGJ implements BigIntegerOperations {

	private final Cache<CacheKey, FpowmTab> fixedBaseCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.DAYS)
			.removalListener((RemovalListener<CacheKey, FpowmTab>) removalNotification -> {
				if (removalNotification.getValue() != null) {
					removalNotification.getValue().free();
				}
			})
			.build();
	private final BigIntegerOperations bigIntegerOperationsJava = new BigIntegerOperationsJava();

	@Override
	public boolean isFixedBaseExponentiationSupported() {
		return VMG.checkLoaded();
	}

	@Override
	public void generateCache(final BigInteger base, final BigInteger modulus) {
		if (!VMG.checkLoaded()) {
			throw VMG.LOAD_ERROR;
		}
		final CacheKey key = deriveCacheKey(base, modulus);

		try {
			fixedBaseCache.get(key, () -> new FpowmTab(base, modulus, modulus.bitLength() - 1));
		} catch (final ExecutionException e) {
			throw new IllegalStateException("Could not create precomputed table for the given basis and modulus.", e);
		}
	}

	private static CacheKey deriveCacheKey(final BigInteger base, final BigInteger modulus) {
		checkArgument(modulus.signum() >= 0);
		return new CacheKey(base.signum(), base.abs(), modulus);
	}

	@Override
	public BigInteger modMultiply(final BigInteger n1, final BigInteger n2, final BigInteger modulus) {
		return bigIntegerOperationsJava.modMultiply(n1, n2, modulus);
	}

	@Override
	public BigInteger modExponentiate(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		checkNotNull(base);
		checkNotNull(exponent);
		checkNotNull(modulus);
		checkArgument(exponent.signum() >= 0 || base.gcd(modulus).equals(BigInteger.ONE),
				"When the exponent is negative, base and modulus must be relatively prime");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");

		//-1, 0 or 1 as the value of this BigInteger is negative, zero or positive.
		final int exponentSignum = exponent.signum();

		final BigInteger basis = exponentSignum >= 0 ? base : modInvert(base, modulus);
		final BigInteger exp = exponentSignum >= 0 ? exponent : exponent.negate();

		final CacheKey key = deriveCacheKey(basis, modulus);

		final FpowmTab fpowmTab = fixedBaseCache.getIfPresent(key);
		if (fpowmTab != null) {
			return fpowmTab.fpowm(exp);
		} else {
			return VMG.powm(basis, exp, modulus);
		}
	}

	@Override
	public BigInteger multiModExp(final ImmutableList<BigInteger> bases, final ImmutableList<BigInteger> exponents, final BigInteger modulus) {
		final BigInteger[] basesArray = checkNotNull(bases).stream().toArray(BigInteger[]::new);
		checkArgument(basesArray.length != 0, "Bases must be non empty.");

		final int exponentsSize = exponents.size();
		final BigInteger[] exponentsArray = checkNotNull(exponents).stream()
				.filter(exponent -> checkNotNull(exponent).signum() >= 0)
				.toArray(BigInteger[]::new);
		checkArgument(exponentsSize == exponentsArray.length, "Exponents must be positive");

		// The next check assures also that exponentsArray is not empty
		checkArgument(basesArray.length == exponentsArray.length, "Bases and exponents must have the same size");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");

		return VMG.spowm(basesArray, exponentsArray, modulus);
	}

	@Override
	public BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		checkNotNull(n);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		// For performance reasons, we omit an explicit check that n and the modulus are relatively prime. GMP throws a division by zero error if the
		// two operands are not relatively prime.

		return VMG.powm(n, BigInteger.ONE.negate(), modulus);
	}

	@Override
	public int getLegendre(final BigInteger a, final BigInteger p) {
		checkNotNull(a);
		checkNotNull(p);
		checkArgument(p.compareTo(BigInteger.TWO) > 0 && p.mod(BigInteger.TWO).equals(BigInteger.ONE),
				"p must be an odd integer greater than 2");

		return VMG.legendre(a, p);
	}

	private record CacheKey(int baseSignum, BigInteger absoluteBase, BigInteger modulus) {
		public CacheKey {
			checkArgument(baseSignum >= -1 && baseSignum <= 1, "baseSignum must be in range [-1, 1]");
			checkNotNull(absoluteBase);
			checkNotNull(modulus);
		}
	}
}
