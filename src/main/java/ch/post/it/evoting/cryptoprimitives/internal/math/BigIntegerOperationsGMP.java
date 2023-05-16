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

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.squareup.jnagmp.Gmp;
import com.verificatum.vmgj.FpowmTab;
import com.verificatum.vmgj.VMG;

import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;

/**
 * Optimized BigIntegerOperations using GMP.
 *
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsGMP implements BigIntegerOperations {
	private static final HashService hashService = HashService.getInstance();
	private final Cache<String, FpowmTab> fixBaseCache = CacheBuilder.newBuilder()
			.expireAfterAccess(30, TimeUnit.DAYS)
			.removalListener((RemovalListener<String, FpowmTab>) removalNotification -> {
				if (removalNotification.getValue() != null) {
					removalNotification.getValue().free();
				}
			})
			.build();
	private final BigIntegerOperations bigIntegerOperationsJava = new BigIntegerOperationsJava();

	@Override
	public boolean isFixBaseSupported() {
		return VMG.checkLoaded();
	}

	@Override
	public void generateCache(final BigInteger base, final BigInteger modulus) {
		if (!VMG.checkLoaded()) {
			throw VMG.LOAD_ERROR;
		}
		final String key = deriveCacheKey(base, modulus);

		try {
			fixBaseCache.get(key, () -> new FpowmTab(base, modulus, modulus.bitLength() - 1));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private static String deriveCacheKey(final BigInteger base, final BigInteger modulus) {
		Preconditions.checkArgument(modulus.signum() >= 0);
		byte[] bytes = hashService.recursiveHash(
				HashableString.from(Boolean.toString(base.signum() >= 0)),
				HashableBigInteger.from(base.abs()),
				HashableBigInteger.from(modulus));
		return HexFormat.of().formatHex(bytes);
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
		checkArgument(exponent.compareTo(BigInteger.ZERO) >= 0 || base.gcd(modulus).equals(BigInteger.ONE),
				"When the exponent is negative, base and modulus must be relatively prime");
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(modulus.testBit(0), "The modulus must be odd");

		//-1, 0 or 1 as the value of this BigInteger is negative, zero or positive.
		int exponentSignum = exponent.signum();

		BigInteger basis = exponentSignum >= 0 ? base : modInvert(base, modulus);
		BigInteger exp = exponentSignum >= 0 ? exponent : exponent.negate();

		String key = deriveCacheKey(basis, modulus);

		FpowmTab fpowmTab = fixBaseCache.getIfPresent(key);
		if (fpowmTab != null) {
			return fpowmTab.fpowm(exp);
		} else if (VMG.checkLoaded()) {
			// VMG is faster than plain GMP, prefer this if available.
			return VMG.powm(basis, exp, modulus);
		} else {
			return Gmp.modPowSecure(basis, exp, modulus);
		}
	}

	@Override
	public BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		checkNotNull(n);
		checkNotNull(modulus);
		checkArgument(modulus.compareTo(BigInteger.ONE) > 0, MODULUS_CHECK_MESSAGE);
		checkArgument(n.gcd(modulus).equals(BigInteger.ONE), "The number to be inverted must be relatively prime to the modulus.");

		return Gmp.modInverse(n, modulus);
	}

	@Override
	public int getJacobi(final BigInteger a, final BigInteger n) {
		checkNotNull(a);
		checkNotNull(n);
		checkArgument(a.compareTo(BigInteger.ZERO) > 0, "a must be positive");

		// The Kronecker symbol includes the Jacobi symbol as a special case.
		return Gmp.kronecker(a, n);
	}
}
