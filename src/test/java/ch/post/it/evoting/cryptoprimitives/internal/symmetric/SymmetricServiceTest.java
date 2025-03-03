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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import javax.crypto.KeyGenerator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base16Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricCiphertext;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

@DisplayName("SymmetricService calling")
class SymmetricServiceTest extends TestGroupSetup {

	private static final int AES_KEY_SIZE = 256;
	private static final int DIFFERENT_AES_KEY_SIZE = 128;
	private static final int NONCE_LENGTH = 12;
	private static final int DIFFERENT_NONCE_LENGTH = 96;
	private static final int ASSOCIATED_LENGTH = 4;
	private static final int PLAINTEXT_LENGTH = 96;

	private static ImmutableByteArray encryptionKey;
	private static ImmutableByteArray nonce;
	private static String plainText;
	private static SymmetricService symmetricEncryptionService;
	private static ImmutableList<String> associatedData;

	@BeforeAll
	static void setUpAll() throws NoSuchAlgorithmException {
		symmetricEncryptionService = new SymmetricService(randomService);

		final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(AES_KEY_SIZE);

		// Generate encryptionKey
		encryptionKey = new ImmutableByteArray(keyGenerator.generateKey().getEncoded());
	}

	@BeforeEach
	void setUp() {
		associatedData = ImmutableList.of(
				randomService.genRandomString(ASSOCIATED_LENGTH, Base16Alphabet.getInstance()),
				randomService.genRandomString(ASSOCIATED_LENGTH, Base64Alphabet.getInstance()));
		plainText = randomService.genRandomString(PLAINTEXT_LENGTH, Base64Alphabet.getInstance());
		nonce = randomService.randomBytes(NONCE_LENGTH);
	}

	@Test
	@DisplayName("valid parameters does not throw, basic encryption path with Java AES 256 GCM Encryption Algorithm")
	void basicJavaAES256GCMEncryptionPath() {
		final SymmetricCiphertext authenticationEncrypted = symmetricEncryptionService.genCiphertextSymmetric(
				encryptionKey, new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8)), associatedData);

		final ImmutableByteArray authenticationDecrypted = symmetricEncryptionService.getPlaintextSymmetric(encryptionKey,
				authenticationEncrypted.ciphertext(), authenticationEncrypted.nonce(), associatedData);

		assertEquals(plainText, new String(authenticationDecrypted.elements(), StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("wrong parameters throws illegalArgumentException, basic encryption path with Java AES 256 GCM Encryption Algorithm")
	void wrongEncryptionInvalidNonceLength() {
		// Different nonce between encryption and decryption execute 'Invalid nonce length'!
		final SymmetricCiphertext authenticationEncrypted = symmetricEncryptionService.genCiphertextSymmetric(
				encryptionKey, new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8)), associatedData);

		nonce = randomService.randomBytes(DIFFERENT_NONCE_LENGTH);

		final ImmutableByteArray ciphertext = authenticationEncrypted.ciphertext();
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> symmetricEncryptionService.getPlaintextSymmetric(encryptionKey, ciphertext, nonce, associatedData));

		assertEquals("Invalid nonce length, expected 12", Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("wrong encryption key length throws illegalArgumentException, basic encryption path with Java AES 256 GCM Encryption Algorithm")
	void wrongEncryptionInvalidKeyLength() {

		final ImmutableByteArray differentEncryptionKey = randomService.randomBytes(DIFFERENT_AES_KEY_SIZE / 8);

		final ImmutableByteArray plainTextBytes = new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8));
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> symmetricEncryptionService.genCiphertextSymmetric(differentEncryptionKey, plainTextBytes, associatedData));

		assertEquals("The key must be 32 bytes", Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("call default constructor")
	void defaultConstructor() {
		assertDoesNotThrow(() -> new SymmetricService());
	}

	@Nested
	@DisplayName("genCiphertextSymmetric with")
	class GenCiphertextSymmetric {

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void nullParams() {
			final ImmutableByteArray plainTextBytes = new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8));

			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.genCiphertextSymmetric(null, plainTextBytes,
							associatedData));
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.genCiphertextSymmetric(encryptionKey, null,
							associatedData));
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.genCiphertextSymmetric(encryptionKey, plainTextBytes,
							null));
		}

		static Stream<Arguments> genCiphertextSymmetricProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/symmetric/gen-ciphertext-symmetric.json");

			return parametersList.stream().map(testParameters -> {
				// Inputs.
				final JsonData input = testParameters.getInput();
				final ImmutableByteArray encryptionKey = input.get("encryption_key", ImmutableByteArray.class);
				final ImmutableByteArray plaintext = input.get("plaintext", ImmutableByteArray.class);
				final ImmutableList<String> associatedData = ImmutableList.of(input.get("associated_data", String[].class));

				// Output.
				final JsonData output = testParameters.getOutput();
				final ImmutableByteArray ciphertext = output.get("ciphertext", ImmutableByteArray.class);
				final ImmutableByteArray nonce = output.get("nonce", ImmutableByteArray.class);
				final SymmetricCiphertext symmetricCiphertext = new SymmetricCiphertext(ciphertext, nonce);

				return Arguments.of(encryptionKey, plaintext, associatedData, symmetricCiphertext, testParameters.getDescription());
			});
		}

		@ParameterizedTest()
		@MethodSource("genCiphertextSymmetricProvider")
		@DisplayName("genCiphertextSymmetric returns expected output")
		void testGenCiphertextSymmetricWithRealValues(final ImmutableByteArray encryptionKey, final ImmutableByteArray plaintext,
				final ImmutableList<String> associatedData, final SymmetricCiphertext expectedResult, final String description) {
			// mock RandomService to use the same nonce as in the test file.
			final RandomService mockRandomService = spy(RandomService.class);
			when(mockRandomService.randomBytes(anyInt())).thenReturn(expectedResult.nonce());
			final SymmetricService symmetricService = new SymmetricService(mockRandomService);

			final SymmetricCiphertext actualResult = symmetricService.genCiphertextSymmetric(encryptionKey, plaintext, associatedData);
			assertEquals(expectedResult, actualResult, String.format("assertion failed for: %s", description));
		}

		@Test
		@DisplayName("with an empty encryption key throws illegalArgumentException")
		void emptyEncryptionKeyThrows() {
			final ImmutableByteArray plainTextBytes = new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8));
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> symmetricEncryptionService.genCiphertextSymmetric(ImmutableByteArray.EMPTY, plainTextBytes, associatedData));

			assertEquals("The encryption key must have a length between 1 and 255 bytes. [length: 0]",
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}

		@Test
		@DisplayName("with an encryption key of length greater than 255 throws illegalArgumentException")
		void biggerEncryptionKeyThrows() {
			final int encryptionKeyLength = 256;
			final ImmutableByteArray biggerEncryptionKey = randomService.randomBytes(encryptionKeyLength);
			final ImmutableByteArray plainTextBytes = new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8));
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> symmetricEncryptionService.genCiphertextSymmetric(biggerEncryptionKey, plainTextBytes, associatedData));

			assertEquals(String.format("The encryption key must have a length between 1 and 255 bytes. [length: %s]", encryptionKeyLength),
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}
	}

	@Nested
	@DisplayName("getPlaintextSymmetric with")
	class GetPlaintextSymmetric {

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void nullParams() {
			final SymmetricCiphertext authenticationEncrypted = symmetricEncryptionService.genCiphertextSymmetric(
					encryptionKey, new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8)), associatedData);

			final ImmutableByteArray ciphertext = authenticationEncrypted.ciphertext();
			final ImmutableByteArray authenticationEncryptedNonce = authenticationEncrypted.nonce();
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(null, ciphertext, authenticationEncryptedNonce, associatedData));
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(encryptionKey, null, authenticationEncryptedNonce, associatedData));
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(encryptionKey, ciphertext, null, associatedData));
			assertThrows(NullPointerException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(encryptionKey, ciphertext, authenticationEncryptedNonce, null));
		}

		static Stream<Arguments> getPlaintextSymmetricProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/symmetric/get-plaintext-symmetric.json");

			return parametersList.stream().map(testParameters -> {
				// Inputs.
				final JsonData input = testParameters.getInput();
				final ImmutableByteArray encryptionKey = input.get("encryption_key", ImmutableByteArray.class);
				final ImmutableByteArray ciphertext = input.get("ciphertext", ImmutableByteArray.class);
				final ImmutableByteArray nonce = input.get("nonce", ImmutableByteArray.class);
				final ImmutableList<String> associatedData = ImmutableList.of(input.get("associated_data", String[].class));

				// Output.
				final JsonData output = testParameters.getOutput();
				final ImmutableByteArray plaintext = output.get("plaintext", ImmutableByteArray.class);
				return Arguments.of(encryptionKey, ciphertext, nonce, associatedData, plaintext, testParameters.getDescription());
			});
		}

		@ParameterizedTest()
		@MethodSource("getPlaintextSymmetricProvider")
		@DisplayName("getPlaintextSymmetric returns expected output")
		void testGetPlaintextSymmetricWithRealValues(final ImmutableByteArray encryptionKey, final ImmutableByteArray ciphertext,
				final ImmutableByteArray nonce, final ImmutableList<String> associatedData, final ImmutableByteArray expectedResult,
				final String description) {
			final ImmutableByteArray actualResult = symmetricEncryptionService.getPlaintextSymmetric(encryptionKey, ciphertext, nonce,
					associatedData);
			assertEquals(expectedResult, actualResult, String.format("assertion failed for: %s", description));
		}

		@Test
		@DisplayName("with an empty encryption key throws illegalArgumentException")
		void emptyEncryptionKeyThrows() {

			final SymmetricCiphertext authenticationEncrypted = symmetricEncryptionService.genCiphertextSymmetric(
					encryptionKey, new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8)), associatedData);

			final ImmutableByteArray authenticationEncryptedCiphertext = authenticationEncrypted.ciphertext();
			final ImmutableByteArray authenticationEncryptedNonce = authenticationEncrypted.nonce();

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(ImmutableByteArray.EMPTY, authenticationEncryptedCiphertext,
							authenticationEncryptedNonce, associatedData));

			assertEquals("The encryption key must have a length between 1 and 255 bytes. [length: 0]",
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}

		@Test
		@DisplayName("with an encryption key of length greater than 255 throws illegalArgumentException")
		void biggerEncryptionKeyThrows() {
			final int encryptionKeyLength = 256;
			final ImmutableByteArray biggerEncryptionKey = randomService.randomBytes(encryptionKeyLength);

			final SymmetricCiphertext authenticationEncrypted = symmetricEncryptionService.genCiphertextSymmetric(
					encryptionKey, new ImmutableByteArray(plainText.getBytes(StandardCharsets.UTF_8)), associatedData);

			final ImmutableByteArray authenticationEncryptedCiphertext = authenticationEncrypted.ciphertext();
			final ImmutableByteArray authenticationEncryptedNonce = authenticationEncrypted.nonce();

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> symmetricEncryptionService.getPlaintextSymmetric(biggerEncryptionKey, authenticationEncryptedCiphertext,
							authenticationEncryptedNonce, associatedData));

			assertEquals(String.format("The encryption key must have a length between 1 and 255 bytes. [length: %s]", encryptionKeyLength),
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}
	}
}
