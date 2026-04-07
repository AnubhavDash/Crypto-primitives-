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

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * Authenticated Encryption with Associated Data
 */
public interface AEAD {

	/**
	 * Authenticated encryption
	 *
	 * @param secretKey      the secret key with which to encrypt
	 * @param nonce          a nonce, length defined by the specific algorithm
	 * @param plaintext      the plaintext to encrypt. May be empty.
	 * @param associatedData data to be authenticated but not encrypted. May be empty.
	 * @return the ciphertext.
	 */
	ImmutableByteArray authenticatedEncryption(final ImmutableByteArray secretKey, final ImmutableByteArray nonce, final ImmutableByteArray plaintext,
			final ImmutableByteArray associatedData);

	/**
	 * Authenticated decryption
	 *
	 * @param secretKey      the secret key with which to decrypt
	 * @param nonce          a nonce, length defined by the specific algorithm
	 * @param associatedData authenticated but not encrypted data
	 * @param ciphertext     the ciphertext to decrypt.
	 * @return the plaintext
	 */
	ImmutableByteArray authenticatedDecryption(final ImmutableByteArray secretKey, final ImmutableByteArray nonce,
			final ImmutableByteArray associatedData, final ImmutableByteArray ciphertext);

	/**
	 * Gets the byte length of the nonce for this algorithm
	 */
	int getNonceLengthBytes();
}
