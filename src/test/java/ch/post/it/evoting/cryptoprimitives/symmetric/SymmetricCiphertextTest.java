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
package ch.post.it.evoting.cryptoprimitives.symmetric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

class SymmetricCiphertextTest {

	@Test
	void checkConstructionImmutability() {
		final byte[] sourceCiphertextBytes = new byte[] { 1, 2, 3 };
		final byte[] sourceNonceBytes = new byte[] { 4, 5, 6 };
		final ImmutableByteArray sourceCiphertext = new ImmutableByteArray(sourceCiphertextBytes);
		final ImmutableByteArray sourceNonce = new ImmutableByteArray(sourceNonceBytes);
		final SymmetricCiphertext symmetricCiphertext = new SymmetricCiphertext(sourceCiphertext, sourceNonce);

		// Mute source arrays
		sourceCiphertextBytes[0] = 7;
		sourceNonceBytes[0] = 8;

		// SymmetricCiphertext inner values must be not equal to source
		assertNotEquals(sourceCiphertextBytes[0], symmetricCiphertext.ciphertext().get(0));
		assertEquals(1, symmetricCiphertext.ciphertext().get(0));
		assertNotEquals(sourceNonceBytes[0], symmetricCiphertext.nonce().get(0));
		assertEquals(4, symmetricCiphertext.nonce().get(0));
	}

	@Test
	void checkGettersImmutability() {
		final SymmetricCiphertext symmetricCiphertext = new SymmetricCiphertext(
				ImmutableByteArray.of((byte) 1, (byte) 2, (byte) 3),
				ImmutableByteArray.of((byte) 4, (byte) 5, (byte) 6));
		final ImmutableByteArray ciphertext = symmetricCiphertext.ciphertext();
		final ImmutableByteArray nonce = symmetricCiphertext.nonce();
		final byte[] ciphertextBytes = ciphertext.elements();
		final byte[] nonceBytes = nonce.elements();

		// Mute arrays from getter
		ciphertextBytes[0] = 7;
		nonceBytes[0] = 8;

		// SymmetricCiphertext inner values must be not equal to muted
		assertNotEquals(ciphertextBytes[0], symmetricCiphertext.ciphertext().get(0));
		assertEquals(1, symmetricCiphertext.ciphertext().get(0));
		assertNotEquals(nonceBytes[0], symmetricCiphertext.nonce().get(0));
		assertEquals(4, symmetricCiphertext.nonce().get(0));
	}
}