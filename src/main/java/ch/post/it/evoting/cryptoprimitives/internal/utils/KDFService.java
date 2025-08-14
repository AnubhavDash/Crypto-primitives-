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

package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.function.Supplier;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.utils.Conversions;
import ch.post.it.evoting.cryptoprimitives.utils.KeyDerivation;

/**
 * Key derivation function (KeyDerivation) service.
 */
public class KDFService implements KeyDerivation {
	private static final KDFService instance = new KDFService(SecurityLevelConfig.getSystemSecurityLevel().getKDFHashFunction());
	private final Supplier<Digest> hashSupplier;

	@VisibleForTesting
	KDFService(final Supplier<Digest> hashSupplier) {
		this.hashSupplier = checkNotNull(hashSupplier);
	}

	public static KDFService getInstance() {
		return instance;
	}

	/**
	 * See {@link KeyDerivation#KDF}
	 */
	@SuppressWarnings({ "java:S117", "java:S100" })
	public ImmutableByteArray KDF(final ImmutableByteArray pseudoRandomKey, final ImmutableList<String> contextInformation,
			final int requiredByteLength) {
		final int L = this.hashSupplier.get().getDigestSize();
		final ImmutableByteArray PRK = checkNotNull(pseudoRandomKey);
		final int N = PRK.length();
		final ImmutableList<String> info_vector = checkNotNull(contextInformation);
		final int n = requiredByteLength;

		checkArgument(L > 0, "Requested KeyDerivation byte length is smaller or equal to 0.");
		checkArgument(n > 0, "Requested byte length must be greater than 0. ");

		// Require.
		checkArgument(N >= L, "The pseudo random key length must be greater than the hash function output length.");
		checkArgument(n <= 255 * L, "The required byte length must me smaller than 255 times the hash function output length.");
		info_vector.forEach(info_i -> checkArgument(stringToByteArray(info_i).length() <= 255,
				"The required length of each additional context information must be smaller or equal to 255."));

		// Operation.
		final ImmutableByteArray info = ImmutableByteArray.concat(
				info_vector.stream()
						.map(Conversions::stringToByteArray)
						.map(info_i_bytes -> ImmutableByteArray.concat(ImmutableByteArray.of((byte) info_i_bytes.length()), info_i_bytes))
						.toArray(ImmutableByteArray[]::new)
		);

		return HKDFExpand(PRK, info, n);
	}

	//HKDF-Expand as specified in RFC5869 section 2.3
	//Delegates the implementation to BouncyCastle's implementation
	@SuppressWarnings({ "java:S117", "java:S100" })
	private ImmutableByteArray HKDFExpand(final ImmutableByteArray PRK, final ImmutableByteArray info, final int L) {
		final HKDFBytesGenerator hkdf = new HKDFBytesGenerator(this.hashSupplier.get());
		final HKDFParameters parameters = HKDFParameters.skipExtractParameters(PRK.elements(), info.elements());
		hkdf.init(parameters);

		final byte[] OKM = new byte[L];
		hkdf.generateBytes(OKM, 0, L);

		return new ImmutableByteArray(OKM);
	}

	/**
	 * See {@link KeyDerivation#KDFToZq(ImmutableByteArray, ImmutableList, BigInteger)}
	 */
	@SuppressWarnings({ "java:S117", "java:S100" })
	public ZqElement KDFToZq(final ImmutableByteArray pseudoRandomKey, final ImmutableList<String> contextInformation,
			final BigInteger exclusiveUpperBound) {
		checkNotNull(exclusiveUpperBound);

		final int lambda = SecurityLevelConfig.getSystemSecurityLevel().getSecurityStrength();

		final int L = this.hashSupplier.get().getDigestSize();
		final ImmutableByteArray PRK = checkNotNull(pseudoRandomKey);
		final int N = PRK.length();
		final ImmutableList<String> info = checkNotNull(contextInformation);
		final BigInteger q = exclusiveUpperBound;

		// Require.
		checkArgument(N >= L, "The pseudo random key length must be greater than the hash function output length.");
		checkArgument(ByteArrays.byteLength(q) >= L,
				"The byte length of the exclusive upper bound must be greater than the hash function output length.");
		checkArgument(lambda % 4 == 0, "The algorithm assumes that lambda is a multiple of 4");

		// Operation.
		final int n = ByteArrays.byteLength(q) + lambda / 4;
		final ImmutableByteArray h = KDF(PRK, info, n);
		final BigInteger u = byteArrayToInteger(h).mod(q);

		return ZqElement.create(u, new ZqGroup(q));
	}
}
