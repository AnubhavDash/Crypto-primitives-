/*
 * Copyright 2026 Swiss Post Ltd
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * This class is thread safe.
 */
@SuppressWarnings({"java:S6548"})
public class SHAKE256 implements XOF {

	private static final SHAKE256 INSTANCE = new SHAKE256();

	private SHAKE256() {
		// Intentionally left blank.
	}

	public static SHAKE256 getInstance() {
		return INSTANCE;
	}

	@Override
	public ImmutableByteArray xof(final Integer outputLength, final ImmutableByteArray message) {
		checkArgument(outputLength > 0, "The output length must be strictly positive.");
		checkNotNull(message);

		final byte[] result = new byte[outputLength];
		final SHAKEDigest shakeDigest = new SHAKEDigest(256);

		shakeDigest.update(message.elements(), 0, message.length());
		shakeDigest.doFinal(result, 0, outputLength);

		return new ImmutableByteArray(result);
	}

	@Override
	public int getMinimumOutputLengthBits() {
		return 512;
	}
}
