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

package ch.post.it.evoting.cryptoprimitives.internal.securitylevel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/*
	This class is thread safe.
 */
@SuppressWarnings("java:S101")
public class SHA3_256 implements HashFunction {

	private static final SHA3_256 INSTANCE = new SHA3_256();

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private SHA3_256() {
		// Intentionally left blank.
	}

	public static SHA3_256 getInstance() {
		return INSTANCE;
	}

	@Override
	public ImmutableByteArray hash(final ImmutableByteArray input) {
		checkNotNull(input);
		try {
			final MessageDigest instance = MessageDigest.getInstance("SHA3-256", BouncyCastleProvider.PROVIDER_NAME);
			return new ImmutableByteArray(instance.digest(input.elements()));
		} catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new IllegalStateException("Failed to create the SHA3-256 message digest for the HashService instantiation.");
		}
	}
}
