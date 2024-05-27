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

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.AEAD;
import ch.post.it.evoting.cryptoprimitives.symmetric.Symmetric;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricCiphertext;
import ch.post.it.evoting.cryptoprimitives.utils.Conversions;

@SuppressWarnings({ "java:S116", "java:S117" })
public class SymmetricAuthenticatedEncryptionService {

	private final RandomService randomService;
	private final AEAD aead;

	SymmetricAuthenticatedEncryptionService(final RandomService randomService, final AEAD aead) {
		this.randomService = checkNotNull(randomService);
		this.aead = checkNotNull(aead);
	}

	/**
	 * @see Symmetric#genCiphertextSymmetric(byte[], byte[], List)
	 */
	SymmetricCiphertext genCiphertextSymmetric(final byte[] encryptionKey, final byte[] plaintext, final List<String> associatedData) {
		checkNotNull(encryptionKey);
		checkNotNull(plaintext);

		final List<byte[]> associated_bytes = checkNotNull(associatedData).stream()
				.map(associated_i -> {
					checkNotNull(associated_i);
					final byte[] associated_i_bytes = stringToByteArray(associated_i);
					checkArgument(associated_i_bytes.length <= 255, "The required length of each associated data must be smaller or equal to 255.");
					return associated_i_bytes;
				}).toList();

		// Context.
		final byte[] K = encryptionKey;
		final byte[] P = plaintext;

		// Operation.
		final byte[] nonce = randomService.randomBytes(aead.getNonceLengthBytes());
		final byte[] associated =
				Bytes.concat(
						associated_bytes.stream()
								.map(associated_i_bytes -> Bytes.concat(new byte[] { (byte) associated_i_bytes.length }, associated_i_bytes))
								.toArray(byte[][]::new)
				);
		final byte[] C = aead.authenticatedEncryption(K, nonce, P, associated);

		// Compute C.
		return new SymmetricCiphertext(C, nonce);
	}

	/**
	 * @see Symmetric#getPlaintextSymmetric(byte[], byte[], byte[], List)
	 */
	byte[] getPlaintextSymmetric(final byte[] encryptionKey, final byte[] ciphertext, final byte[] nonce, final List<String> associatedData) {
		checkNotNull(encryptionKey);
		checkNotNull(ciphertext);
		checkNotNull(nonce);

		final List<byte[]> associated_bytes = checkNotNull(associatedData).stream()
				.map(associated_i -> {
					checkNotNull(associated_i);
					final byte[] associated_i_bytes = stringToByteArray(associated_i);
					checkArgument(associated_i_bytes.length <= 255, "The required length of each associated data must be smaller or equal to 255.");
					return associated_i_bytes;
				}).toList();

		// Context.
		final byte[] K = encryptionKey;
		final byte[] C = ciphertext;

		// Operation.
		final byte[] associated =
				Bytes.concat(
						associated_bytes.stream()
								.map(associated_i_bytes -> Bytes.concat(new byte[] { (byte) associated_i_bytes.length }, associated_i_bytes))
								.toArray(byte[][]::new)
				);

		// Compute P.
		return aead.authenticatedDecryption(K, nonce, associated, C);
	}

}
