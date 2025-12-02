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
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.verificatum.vmgj.FpowmTab;
import com.verificatum.vmgj.VMG;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * Optimized BigIntegerOperations using Verificatum Multiplicative Groups Library for Java (VMGJ) .
 * The methods which are not optimized yet will use the java implementation by inheritance.
 *
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsVMGJ extends BigIntegerOperationsJava {

	private static final int DESIRED_PARALLELISM =
			Math.max(1, Integer.getInteger("vmgj.multi.parallel",
					Runtime.getRuntime().availableProcessors()));
	private static final int MULTI_MOD_EXP_MIN_CHUNK_SIZE = Math.max(1, Integer.getInteger("vmgj.multiModExp.min.chunk.size", 32));

	private final Cache<CacheKey, FpowmTab> fixedBaseCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.DAYS)
			.removalListener((RemovalListener<CacheKey, FpowmTab>) removalNotification -> {
				if (removalNotification.getValue() != null) {
					removalNotification.getValue().free();
				}
			})
			.build();

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

	@VisibleForTesting
	static CacheKey deriveCacheKey(final BigInteger base, final BigInteger modulus) {
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		final BigInteger b = base.mod(modulus);
		return new CacheKey(b, modulus);
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
		checkNotNull(bases);
		checkNotNull(exponents);
		checkNotNull(modulus);

		final int n = bases.size();
		checkArgument(n > 0 && n == exponents.size(), "Bases and exponents must have same non-zero size");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");

		// if the list has a single element, plain exponentiation (using powm) is faster
		if (n == 1) {
			final BigInteger e0 = exponents.getFirst();
			checkArgument(e0.signum() >= 0, "Exponents must be non negative");
			return VMG.powm(bases.getFirst(), e0, modulus);
		}

		final BigInteger[] b = new BigInteger[n];
		final BigInteger[] e = new BigInteger[n];
		for (int i = 0; i < n; i++) {
			final BigInteger ei = exponents.get(i);
			checkArgument(ei.signum() >= 0, "Exponents must be non negative");
			b[i] = bases.get(i);
			e[i] = ei;
		}

		final int targetChunkSize = (n / DESIRED_PARALLELISM) + 1;
		final int K = Math.max(MULTI_MOD_EXP_MIN_CHUNK_SIZE, targetChunkSize);

		// If the list is smaller than MULTI_MOD_EXP_MIN_CHUNK_SIZE + 2, we omit chunking.
		if (n <= K + 2) {
			return VMG.spowm(b, e, modulus);
		}

		// We avoid cases where the last chunk is smaller than 2 elements.
		int chunks = (n + K - 1) / K;
		final int remainder = n - (chunks - 1) * K;
		if (chunks > 1 && remainder > 0 && remainder <= 2) {
			chunks -= 1;
		}

		final int finalChunks = chunks;
		final int lastChunkSize = (n - (finalChunks - 1) * K);

		return IntStream.range(0, chunks).parallel()
				.mapToObj(ci -> {
					final int from = ci * K;
					final int to = (ci == finalChunks - 1) ? (from + lastChunkSize) : (from + K);
					return VMG.spowm(Arrays.copyOfRange(b, from, to),
							Arrays.copyOfRange(e, from, to),
							modulus);
				})
				.reduce(BigInteger.ONE, (x, y) -> modMultiply(x, y, modulus));
	}

	@Override
	public BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		checkNotNull(n);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		// For performance reasons, we omit an explicit check that n and the modulus are relatively prime.
		// modInvert is only called in the context of Gq element inversion, so n and the modulus are always relatively prime.

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

	record CacheKey(BigInteger base, BigInteger modulus) {
		public CacheKey {
			checkNotNull(base);
			checkNotNull(modulus);
		}
	}
}
