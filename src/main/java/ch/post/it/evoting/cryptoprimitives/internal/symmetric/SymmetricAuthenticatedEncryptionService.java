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
package ch.post.it.evoting.cryptoprimitives.internal.symmetric;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray.concat;
import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.AEAD;
import ch.post.it.evoting.cryptoprimitives.symmetric.Symmetric;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricCiphertext;

@SuppressWarnings({ "java:S116", "java:S117" })
public class SymmetricAuthenticatedEncryptionService {

	private final RandomService randomService;
	private final AEAD aead;

	SymmetricAuthenticatedEncryptionService(final RandomService randomService, final AEAD aead) {
		this.randomService = checkNotNull(randomService);
		this.aead = checkNotNull(aead);
	}

	/**
	 * @see Symmetric#genCiphertextSymmetric(ImmutableByteArray, ImmutableByteArray, ImmutableList)
	 */
	SymmetricCiphertext genCiphertextSymmetric(final ImmutableByteArray encryptionKey, final ImmutableByteArray plaintext,
			final ImmutableList<String> associatedData) {
		// Input.
		final ImmutableByteArray K = checkNotNull(encryptionKey);
		final ImmutableByteArray P = checkNotNull(plaintext);
		final ImmutableList<ImmutableByteArray> associated_bytes = checkNotNull(associatedData).stream()
				.map(associated_i -> {
					checkNotNull(associated_i);
					final ImmutableByteArray associated_i_bytes = stringToByteArray(associated_i);
					checkArgument(associated_i_bytes.length() <= 255, "The required length of each associated data must be smaller or equal to 255.");
					return associated_i_bytes;
				})
				.collect(toImmutableList());

		// Operation.
		final ImmutableByteArray nonce = randomService.randomBytes(aead.getNonceLengthBytes());
		final ImmutableByteArray associated = concat(associated_bytes.stream()
				.map(associated_i_bytes -> concat(ImmutableByteArray.of((byte) associated_i_bytes.length()), associated_i_bytes))
				.toArray(ImmutableByteArray[]::new)
		);
		final ImmutableByteArray C = aead.authenticatedEncryption(K, nonce, P, associated);

		// Compute C.
		return new SymmetricCiphertext(C, nonce);
	}

	/**
	 * @see Symmetric#getPlaintextSymmetric(ImmutableByteArray, ImmutableByteArray, ImmutableByteArray, ImmutableList)
	 */
	ImmutableByteArray getPlaintextSymmetric(final ImmutableByteArray encryptionKey, final ImmutableByteArray ciphertext,
			final ImmutableByteArray nonce, final ImmutableList<String> associatedData) {
		// Input.
		final ImmutableByteArray K = checkNotNull(encryptionKey);
		final ImmutableByteArray C = checkNotNull(ciphertext);
		checkNotNull(nonce);
		final ImmutableList<ImmutableByteArray> associated_bytes = checkNotNull(associatedData).stream()
				.map(associated_i -> {
					checkNotNull(associated_i);
					final ImmutableByteArray associated_i_bytes = stringToByteArray(associated_i);
					checkArgument(associated_i_bytes.length() <= 255, "The required length of each associated data must be smaller or equal to 255.");
					return associated_i_bytes;
				})
				.collect(toImmutableList());

		// Operation.
		final ImmutableByteArray associated = concat(associated_bytes.stream()
				.map(associated_i_bytes -> concat(ImmutableByteArray.of((byte) associated_i_bytes.length()), associated_i_bytes))
				.toArray(ImmutableByteArray[]::new)
		);

		// Compute P.
		return aead.authenticatedDecryption(K, nonce, associated, C);
	}

}
