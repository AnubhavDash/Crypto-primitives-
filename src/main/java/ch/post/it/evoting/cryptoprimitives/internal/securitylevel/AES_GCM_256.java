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

package ch.post.it.evoting.cryptoprimitives.internal.securitylevel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * This class is thread safe.
 */
@SuppressWarnings("java:S101")
public class AES_GCM_256 implements AEAD {

	private static final AES_GCM_256 INSTANCE = new AES_GCM_256();

	private static final String AES = "AES";
	private static final int AES_GCM_TAG_BYTE_LENGTH = 16;
	private static final String ALGORITHM_NAME = "AES_256/GCM/NoPadding";

	private AES_GCM_256() {
		// Intentionally left blank.
	}

	public static AES_GCM_256 getInstance() {
		return INSTANCE;
	}

	@Override
	public ImmutableByteArray authenticatedEncryption(final ImmutableByteArray secretKey, final ImmutableByteArray nonce,
			final ImmutableByteArray plaintext, final ImmutableByteArray associatedData) {
		checkNotNull(secretKey);
		checkNotNull(nonce);
		checkNotNull(plaintext);
		checkNotNull(associatedData);
		checkArgument(nonce.length() == getNonceLengthBytes(), String.format("Invalid nonce length, expected %s", getNonceLengthBytes()));

		final Cipher cipher = getCipher(secretKey, nonce, Cipher.ENCRYPT_MODE);
		cipher.updateAAD(associatedData.elements());

		try {
			return new ImmutableByteArray(cipher.doFinal(plaintext.elements()));
		} catch (final BadPaddingException e) {
			throw new IllegalStateException("We should never get this exception since it is only thrown in decryption mode.");
		} catch (final IllegalBlockSizeException e) {
			throw new IllegalStateException("We should never get this exception since our algorithm is not a block cipher.");
		}
	}

	@Override
	public ImmutableByteArray authenticatedDecryption(final ImmutableByteArray secretKey, final ImmutableByteArray nonce,
			final ImmutableByteArray associatedData, final ImmutableByteArray ciphertext) {
		checkNotNull(secretKey);
		checkNotNull(nonce);
		checkNotNull(associatedData);
		checkNotNull(ciphertext);
		checkArgument(nonce.length() == getNonceLengthBytes(), String.format("Invalid nonce length, expected %s", getNonceLengthBytes()));

		final Cipher cipher = getCipher(secretKey, nonce, Cipher.DECRYPT_MODE);
		cipher.updateAAD(associatedData.elements());

		try {
			return new ImmutableByteArray(cipher.doFinal(ciphertext.elements()));
		} catch (final BadPaddingException e) {
			throw new IllegalStateException("We should never get this exception since no padding is needed for the configured algorithm.", e);
		} catch (final IllegalBlockSizeException e) {
			throw new IllegalStateException("We should never get this exception since our algorithm is not a block cipher.", e);
		}
	}

	@Override
	public int getNonceLengthBytes() {
		return 12;
	}

	private Cipher getCipher(final ImmutableByteArray encryptionKey, final ImmutableByteArray nonce, final int opmode) {
		// Get Cipher Instance
		final Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM_NAME);
		} catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new IllegalStateException("Requested cryptographic algorithm or padding in the algorithm is not available in the environment.", e);
		}

		// Create the encryptionKey
		final Key key = new SecretKeySpec(encryptionKey.elements(), AES);

		// Create the algorithm used for the authentication
		final AlgorithmParameterSpec params = new GCMParameterSpec(AES_GCM_TAG_BYTE_LENGTH * 8, nonce.elements());

		// Initialize Cipher for the authentication
		try {
			cipher.init(opmode, key, params);
		} catch (final InvalidKeyException e) {
			throw new IllegalArgumentException("Error with the given encryptionKey during Cipher initialization", e);
		} catch (final InvalidAlgorithmParameterException e) {
			throw new IllegalStateException("Configured algorithm parameters are invalid or inappropriate.", e);
		}

		return cipher;
	}
}
