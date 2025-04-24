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
package ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs;

import static ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs.DecryptionProofService.computePhiDecryption;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import ch.post.it.evoting.cryptoprimitives.collection.AuxiliaryInformation;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamal;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.Generators;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;
import ch.post.it.evoting.cryptoprimitives.utils.VerificationResult;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;

class DecryptionProofServiceTest extends TestGroupSetup {

	private static final ElGamal elGamal = new ElGamalService();
	private static final AuxiliaryInformation auxiliaryInformation = AuxiliaryInformation.of("aux", "1");

	private static DecryptionProofService decryptionProofService;

	@BeforeAll
	static void setupAll() {
		final HashService hashService = TestHashService.create(gqGroup.getQ());
		decryptionProofService = new DecryptionProofService(randomService, hashService);
	}

	@Nested
	@DisplayName("Computing a phi decryption...")
	class ComputePhiDecryptionTest {
		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void notNullChecks() {
			final GroupVector<ZqElement, ZqGroup> preImage = GroupVector.empty();
			final GqElement gamma = gqGroupGenerator.genMember();

			assertThrows(NullPointerException.class, () -> computePhiDecryption(preImage, null));
			assertThrows(NullPointerException.class, () -> computePhiDecryption(null, gamma));
		}

		@Test
		@DisplayName("with the pre-image and base having different group orders throws an IllegalArgumentException")
		void checkNotSameOrder() {
			final GqElement gamma = gqGroupGenerator.genMember();
			final int zqGroupVectorSize = 3;
			final GroupVector<ZqElement, ZqGroup> preImage = otherZqGroupGenerator.genRandomZqElementVector(zqGroupVectorSize);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> computePhiDecryption(preImage, gamma));

			assertEquals("The preImage and base should have the same group order.", illegalArgumentException.getMessage());
		}

		@RepeatedTest(10)
		@DisplayName("with valid input arguments returns an image with the correct size")
		void testPhiFunctionSizeAndCalculatingWithoutErrorOnRandomValues() {
			final GqElement gamma = gqGroupGenerator.genMember();
			final int zqGroupVectorSize = 3;
			final GroupVector<ZqElement, ZqGroup> preImage = zqGroupGenerator.genRandomZqElementVector(zqGroupVectorSize);

			final GroupVector<GqElement, GqGroup> phiFunction = computePhiDecryption(preImage, gamma);

			assertEquals(2 * preImage.size(), phiFunction.size());
		}

		@Test
		@DisplayName("with specific values returns the expected result")
		void checkPhiFunctionAgainstHandCalculations() {

			final GqGroup groupP59 = GroupTestData.getGroupP59();
			final GqElement gamma = GqElementFactory.fromValue(BigInteger.valueOf(12), groupP59);

			final ZqGroup zqGroup = ZqGroup.sameOrderAs(groupP59);
			final ZqElement zqElement9 = ZqElement.create(BigInteger.valueOf(9), zqGroup);
			final ZqElement zqElement15 = ZqElement.create(BigInteger.valueOf(15), zqGroup);
			final ZqElement zqElement8 = ZqElement.create(BigInteger.valueOf(8), zqGroup);

			final GroupVector<ZqElement, ZqGroup> preImage = GroupVector.of(zqElement9, zqElement15, zqElement8);

			final GroupVector<GqElement, GqGroup> computePhiFunction = computePhiDecryption(preImage, gamma);

			final GqElement gqElement36 = GqElementFactory.fromValue(BigInteger.valueOf(36), groupP59);
			final GqElement gqElement48 = GqElementFactory.fromValue(BigInteger.valueOf(48), groupP59);
			final GqElement gqElement12 = GqElementFactory.fromValue(BigInteger.valueOf(12), groupP59);
			final GqElement gqElement16 = GqElementFactory.fromValue(BigInteger.valueOf(16), groupP59);
			final GqElement gqElement22 = GqElementFactory.fromValue(BigInteger.valueOf(22), groupP59);
			final GqElement gqElement21 = GqElementFactory.fromValue(BigInteger.valueOf(21), groupP59);

			final GroupVector<GqElement, GqGroup> phiFunction = GroupVector.of(gqElement36, gqElement48, gqElement12, gqElement16, gqElement22,
					gqElement21);

			assertEquals(phiFunction, computePhiFunction);
		}
	}

	@Nested
	@DisplayName("Generating a decryption proof...")
	class GenDecryptionProofTest {

		private ElGamalMultiRecipientCiphertext ciphertext;
		private ElGamalMultiRecipientKeyPair keyPair;
		private ElGamalMultiRecipientMessage message;

		private int keyLength;
		private int messageLength;

		@BeforeEach
		void setup() {
			final int maxLength = 10;
			keyLength = randomService.genRandomInteger(maxLength - 1) + 2;
			messageLength = randomService.genRandomInteger(keyLength) + 1;
			keyPair = elGamal.genKeyPair(gqGroup, keyLength, randomService);
			final GroupVector<GqElement, GqGroup> messageElements = gqGroupGenerator.genRandomGqElementVector(messageLength);
			message = new ElGamalMultiRecipientMessage(messageElements);
			ciphertext = elGamal.getCiphertext(message, zqGroupGenerator.genRandomZqElementMember(), keyPair.getPublicKey());

			final HashService hashService = TestHashService.create(gqGroup.getQ());
			decryptionProofService = new DecryptionProofService(randomService, hashService);
		}

		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void genDecryptionProofWithNullArguments() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.genDecryptionProof(null, keyPair, message, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.genDecryptionProof(ciphertext, null, message, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, null, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, null))
			);
		}

		@Test
		@DisplayName("with valid arguments does not throw")
		void genDecryptionProofWithValidArguments() {
			assertDoesNotThrow(() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, AuxiliaryInformation.of()));
			assertDoesNotThrow(() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, auxiliaryInformation));
		}

		@Test
		@DisplayName("with hash service with too long hash length throws IllegalArgumentException")
		void genDecryptionProofWithBadHashService() {
			final DecryptionProofService badService = new DecryptionProofService(randomService, HashService.getInstance());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> badService.genDecryptionProof(ciphertext, keyPair, message, auxiliaryInformation));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", exception.getMessage());
		}

		@Test
		@DisplayName("with a hashService that has a too long hash length throws an IllegalArgumentException")
		void genDecryptionProofWithHashServiceWithTooLongHashLength() {
			final HashService otherHashService = HashService.getInstance();
			final DecryptionProofService otherProofService = new DecryptionProofService(randomService, otherHashService);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> otherProofService.genDecryptionProof(ciphertext, keyPair, message, auxiliaryInformation));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", exception.getMessage());
		}

		@Test
		@DisplayName("with the message being different from the decrypted ciphertext throws an IllegalArgumentException")
		void genDecryptionProofWithMessageNotFromCiphertext() {
			final ElGamalMultiRecipientMessage differentMessage = Generators
					.genWhile(() -> new ElGamalMultiRecipientMessage(gqGroupGenerator.genRandomGqElementVector(messageLength)), message::equals);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, differentMessage, auxiliaryInformation));
			assertEquals("The message must be equal to the decrypted ciphertext.", exception.getMessage());
		}

		@Test
		@DisplayName("with the ciphertext longer than the secret key throws an IllegalArgumentException")
		void genDecryptionProofWithCiphertextTooLong() {
			final ElGamalMultiRecipientCiphertext tooLongCiphertext = elGamalGenerator.genRandomCiphertext(keyLength + 1);
			final ElGamalMultiRecipientMessage tooLongMessage = elGamalGenerator.genRandomMessage(keyLength + 1);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.genDecryptionProof(tooLongCiphertext, keyPair, tooLongMessage, auxiliaryInformation));
			assertEquals("The ciphertext length cannot be greater than the secret key length.", exception.getMessage());
		}

		@Test
		@DisplayName("with the ciphertext and secret key group orders being different throws an IllegalArgumentException")
		void genDecryptionProofWithCiphertextAndSecretKeyDifferentGroupOrder() {
			final ElGamalMultiRecipientKeyPair keyPair = elGamal.genKeyPair(otherGqGroup, keyLength, randomService);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, auxiliaryInformation));
			assertEquals("The ciphertext and the secret key group must have the same order.", exception.getMessage());
		}
	}

	@Nested
	@DisplayName("Verifying a decryption proof...")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class VerifyDecryptionTest {

		private ElGamalMultiRecipientCiphertext ciphertext;
		private ElGamalMultiRecipientPublicKey publicKey;
		private ElGamalMultiRecipientMessage message;
		private DecryptionProof decryptionProof;

		private int keyLength;
		private int messageLength;

		@BeforeEach
		void setup() {
			final int maxLength = 10;
			keyLength = randomService.genRandomInteger(maxLength - 1) + 1;
			messageLength = randomService.genRandomInteger(keyLength) + 1;
			final ElGamalMultiRecipientKeyPair keyPair = elGamal.genKeyPair(gqGroup, keyLength, randomService);
			publicKey = keyPair.getPublicKey();
			final GroupVector<GqElement, GqGroup> messageElements = gqGroupGenerator.genRandomGqElementVector(messageLength);
			message = new ElGamalMultiRecipientMessage(messageElements);
			ciphertext = elGamal.getCiphertext(message, zqGroupGenerator.genRandomZqElementMember(), keyPair.getPublicKey());
			decryptionProof = decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, auxiliaryInformation);
		}

		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void verifyDecryptionWithNullArguments() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.verifyDecryption(null, publicKey, message, decryptionProof, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.verifyDecryption(ciphertext, null, message, decryptionProof, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, null, decryptionProof, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, null, auxiliaryInformation)),
					() -> assertThrows(NullPointerException.class,
							() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, null))
			);
		}

		@Test
		@DisplayName("with valid input and non empty auxiliary information returns true")
		void verifyDecryptionWithValidInput() {
			assertTrue(decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation).verify()
					.isVerified());
		}

		@Test
		@DisplayName("with hash service with too long hash length throws IllegalArgumentException")
		void verifyDecryptionWithBadHashService() {
			final DecryptionProofService badService = new DecryptionProofService(randomService, HashService.getInstance());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> badService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", exception.getMessage());
		}

		@Test
		@DisplayName("with valid input and empty auxiliary information returns true")
		void verifyDecryptionWithValidInputNoAux() {
			final ElGamalMultiRecipientKeyPair keyPair = elGamal.genKeyPair(gqGroup, keyLength, randomService);
			publicKey = keyPair.getPublicKey();
			final GroupVector<GqElement, GqGroup> messageElements = gqGroupGenerator.genRandomGqElementVector(messageLength);
			message = new ElGamalMultiRecipientMessage(messageElements);
			ciphertext = elGamal.getCiphertext(message, zqGroupGenerator.genRandomZqElementMember(), keyPair.getPublicKey());
			decryptionProof = decryptionProofService.genDecryptionProof(ciphertext, keyPair, message, AuxiliaryInformation.of());
			assertTrue(decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, AuxiliaryInformation.of()).verify()
					.isVerified());
		}

		@Test
		@DisplayName("with the ciphertext from a different group throws an IllegalArgumentException")
		void verifyDecryptionWithCiphertextFromDifferentGroup() {
			ciphertext = otherGroupElGamalGenerator.genRandomCiphertext(messageLength);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the public key and the message must have the same group.", exception.getMessage());
		}

		@Test
		@DisplayName("with the public key from a different group throws an IllegalArgumentException")
		void verifyDecryptionWithPublicKeyFromDifferentGroup() {
			publicKey = elGamal.genKeyPair(otherGqGroup, keyLength, randomService).getPublicKey();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the public key and the message must have the same group.", exception.getMessage());
		}

		@Test
		@DisplayName("with the message from a different group throws an IllegalArgumentException")
		void verifyDecryptionWithMessageFromDifferentGroup() {
			message = otherGroupElGamalGenerator.genRandomMessage(messageLength);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the public key and the message must have the same group.", exception.getMessage());
		}

		@Test
		@DisplayName("with the decryption proof from a different group throws an IllegalArgumentException")
		void verifyDecryptionWithDecryptionProofFromDifferentGroup() {
			decryptionProof = new DecryptionProof(otherZqGroupGenerator.genRandomZqElementMember(),
					otherZqGroupGenerator.genRandomZqElementVector(messageLength));
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The decryption proof must have the same group order as the ciphertext, the message and the public key.",
					exception.getMessage());
		}

		@Test
		@DisplayName("with the ciphertext of a different size throws an IllegalArgumentException")
		void verifyDecryptionWithCiphertextOfDifferentSize() {
			ciphertext = elGamalGenerator.genRandomCiphertext(messageLength + 1);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the message and the decryption proof must have the same size.", exception.getMessage());
		}

		@Test
		@DisplayName("with a too long ciphertext and message throws an IllegalArgumentException")
		void verifyDecryptionWithTooShortPublicKey() {
			ciphertext = elGamalGenerator.genRandomCiphertext(keyLength + 1);
			message = elGamalGenerator.genRandomMessage(keyLength + 1);
			decryptionProof = new DecryptionProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementVector(keyLength + 1));
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the message and the decryption proof must be smaller than or equal to the public key.",
					exception.getMessage());
		}

		@Test
		@DisplayName("with a too short z in the decryption proof throws an IllegalArgumentException")
		void verifyDecryptionWithTooShortDecryptionProofZ() {
			decryptionProof = new DecryptionProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementVector(messageLength + 1));
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation));
			assertEquals("The ciphertext, the message and the decryption proof must have the same size.", exception.getMessage());
		}

		@Test
		@DisplayName("with another ciphertext returns false")
		void verifyDecryptionWithOtherCiphertext() {
			final TestValues values = new TestValues();
			final ElGamalMultiRecipientMessage m = values.m;
			final ElGamalMultiRecipientKeyPair keyPair = values.createKeyPair();
			final ElGamalMultiRecipientCiphertext c = values.c;
			final AuxiliaryInformation iAux = values.iAux;

			final DecryptionProofService service1 = values.createDecryptionProofService();
			final DecryptionProofService service2 = values.createDecryptionProofService();

			// Create expected output
			final DecryptionProof proof1 = service1.genDecryptionProof(c, keyPair, m, iAux);
			final DecryptionProof proof2 = service2.genDecryptionProof(c, keyPair, m, AuxiliaryInformation.of());

			final ElGamalMultiRecipientCiphertext cPrime = ElGamalMultiRecipientCiphertext.create(values.gEight, c.getPhis());

			final VerificationResult result1 = service1.verifyDecryption(cPrime, keyPair.getPublicKey(), m, proof1, iAux).verify();
			final VerificationResult result2 = service1.verifyDecryption(cPrime, keyPair.getPublicKey(), m, proof2, AuxiliaryInformation.of())
					.verify();

			assertFalse(result1.isVerified());
			assertFalse(result2.isVerified());

			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", cPrime), result1.getErrorMessages().getFirst());
			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", cPrime), result2.getErrorMessages().getFirst());
		}

		@Test
		@DisplayName("with another public key returns false")
		void verifyDecryptionWithOtherPublicKey() {
			final TestValues values = new TestValues();
			final ElGamalMultiRecipientMessage m = values.m;
			final ElGamalMultiRecipientKeyPair keyPair = values.createKeyPair();
			final ElGamalMultiRecipientCiphertext c = values.c;
			final AuxiliaryInformation iAux = values.iAux;

			final DecryptionProofService service1 = values.createDecryptionProofService();
			final DecryptionProofService service2 = values.createDecryptionProofService();

			// Create expected output
			final DecryptionProof proof1 = service1.genDecryptionProof(c, keyPair, m, iAux);
			final DecryptionProof proof2 = service2.genDecryptionProof(c, keyPair, m, AuxiliaryInformation.of());

			final ElGamalMultiRecipientPublicKey pkPrime = new ElGamalMultiRecipientPublicKey(
					GroupVector.of(values.gEight, values.gFour, values.gFour));

			assertFalse(service1.verifyDecryption(c, pkPrime, m, proof1, iAux).verify().isVerified());
			assertFalse(service2.verifyDecryption(c, pkPrime, m, proof2, AuxiliaryInformation.of()).verify().isVerified());

			final VerificationResult result1 = service1.verifyDecryption(c, pkPrime, m, proof1, iAux).verify();
			final VerificationResult result2 = service1.verifyDecryption(c, pkPrime, m, proof2, AuxiliaryInformation.of()).verify();

			assertFalse(result1.isVerified());
			assertFalse(result2.isVerified());

			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result1.getErrorMessages().getFirst());
			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result2.getErrorMessages().getFirst());
		}

		@Test
		@DisplayName("with another message returns false")
		void verifyDecryptionWithOtherMessage() {
			final TestValues values = new TestValues();
			final ElGamalMultiRecipientMessage m = values.m;
			final ElGamalMultiRecipientKeyPair keyPair = values.createKeyPair();
			final ElGamalMultiRecipientCiphertext c = values.c;
			final AuxiliaryInformation iAux = values.iAux;

			final DecryptionProofService service1 = values.createDecryptionProofService();
			final DecryptionProofService service2 = values.createDecryptionProofService();

			// Create expected output
			final DecryptionProof proof1 = service1.genDecryptionProof(c, keyPair, m, iAux);
			final DecryptionProof proof2 = service2.genDecryptionProof(c, keyPair, m, AuxiliaryInformation.of());

			final ElGamalMultiRecipientMessage mPrime = new ElGamalMultiRecipientMessage(GroupVector.of(values.gEight, values.gEight, values.gThree));

			final VerificationResult result1 = service1.verifyDecryption(c, keyPair.getPublicKey(), mPrime, proof1, iAux).verify();
			final VerificationResult result2 = service1.verifyDecryption(c, keyPair.getPublicKey(), mPrime, proof2, AuxiliaryInformation.of())
					.verify();

			assertFalse(result1.isVerified());
			assertFalse(result2.isVerified());

			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result1.getErrorMessages().getFirst());
			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result2.getErrorMessages().getFirst());
		}

		@Test
		@DisplayName("with another auxiliary information returns false")
		void verifyDecryptionWithOtherAuxiliaryInformation() {
			final TestValues values = new TestValues();
			final ElGamalMultiRecipientMessage m = values.m;
			final ElGamalMultiRecipientKeyPair keyPair = values.createKeyPair();
			final ElGamalMultiRecipientCiphertext c = values.c;
			final AuxiliaryInformation iAux = values.iAux;

			final DecryptionProofService service1 = values.createDecryptionProofService();
			final DecryptionProofService service2 = values.createDecryptionProofService();

			// Create expected output
			final DecryptionProof proof1 = service1.genDecryptionProof(c, keyPair, m, iAux);
			final DecryptionProof proof2 = service2.genDecryptionProof(c, keyPair, m, AuxiliaryInformation.of());

			final AuxiliaryInformation iAuxPrime = iAux.append(HashableString.from("primes"));

			final VerificationResult result1 = service1.verifyDecryption(c, keyPair.getPublicKey(), m, proof1, iAuxPrime).verify();
			final VerificationResult result2 = service1.verifyDecryption(c, keyPair.getPublicKey(), m, proof2, iAuxPrime).verify();

			assertFalse(result1.isVerified());
			assertFalse(result2.isVerified());

			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result1.getErrorMessages().getFirst());
			assertEquals(String.format("Could not verify decryption proof of ciphertext %s.", c), result2.getErrorMessages().getFirst());
		}

		private Stream<Arguments> jsonFileArgumentProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/zeroknowledgeproofs/verify-decryption.json");

			return parametersList.stream().parallel().map(testParameters -> {
				// Context.
				final JsonData context = testParameters.getContext();
				final BigInteger p = context.get("p", BigInteger.class);
				final BigInteger q = context.get("q", BigInteger.class);
				final BigInteger g = context.get("g", BigInteger.class);

				try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
					mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(testParameters.getSecurityLevel());
					final GqGroup gqGroup = new GqGroup(p, q, g);
					final ZqGroup zqGroup = new ZqGroup(q);

					final JsonData input = testParameters.getInput();

					// Parse ciphertext parameters.
					final JsonData ciphertextData = input.getJsonData("ciphertext");

					final GqElement gamma = GqElementFactory.fromValue(ciphertextData.get("gamma", BigInteger.class), gqGroup);
					final BigInteger[] phisAArray = ciphertextData.get("phis", BigInteger[].class);
					final GroupVector<GqElement, GqGroup> phi = Arrays.stream(phisAArray).map(phiA -> GqElementFactory.fromValue(phiA, gqGroup))
							.collect(toGroupVector());
					final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertext.create(gamma, phi);

					// Parse key pair parameters
					final BigInteger[] pkArray = input.get("public_key", BigInteger[].class);
					final GroupVector<GqElement, GqGroup> pkElements = Arrays.stream(pkArray)
							.map(skA -> GqElementFactory.fromValue(skA, gqGroup))
							.collect(toGroupVector());
					final ElGamalMultiRecipientPublicKey publicKey = new ElGamalMultiRecipientPublicKey(pkElements);

					// Parse message parameters
					final BigInteger[] messageArray = input.get("message", BigInteger[].class);
					final GroupVector<GqElement, GqGroup> messageElements = Arrays.stream(messageArray)
							.map(mA -> GqElementFactory.fromValue(mA, gqGroup))
							.collect(toGroupVector());
					final ElGamalMultiRecipientMessage message = new ElGamalMultiRecipientMessage(messageElements);

					// Parse decryption proof parameters
					final JsonData proof = input.getJsonData("proof");

					final ZqElement e = ZqElement.create(proof.get("e", BigInteger.class), zqGroup);

					final BigInteger[] zArray = proof.get("z", BigInteger[].class);
					final GroupVector<ZqElement, ZqGroup> z = Arrays.stream(zArray)
							.map(zA -> ZqElement.create(zA, zqGroup))
							.collect(toGroupVector());
					final DecryptionProof decryptionProof = new DecryptionProof(e, z);

					// Parse auxiliary information parameters
					final String[] auxInformation = input.get("additional_information", String[].class);
					final AuxiliaryInformation auxiliaryInformation = AuxiliaryInformation.of(auxInformation);

					// Parse output parameters
					final JsonData output = testParameters.getOutput();

					final Boolean result = output.get("verif_result", Boolean.class);

					return Arguments
							.of(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation, result, testParameters.getDescription());
				}
			});
		}

		private static class TestValues {
			// Create BigIntegers
			private final BigInteger TWO = BigInteger.valueOf(2);
			private final BigInteger THREE = BigInteger.valueOf(3);
			private final BigInteger FOUR = BigInteger.valueOf(4);
			private final BigInteger FIVE = BigInteger.valueOf(5);
			private final BigInteger SIX = BigInteger.valueOf(6);
			private final BigInteger SEVEN = BigInteger.valueOf(7);
			private final BigInteger EIGHT = BigInteger.valueOf(8);
			private final BigInteger TEN = BigInteger.TEN;
			// Create groups
			private final BigInteger p = BigInteger.valueOf(23);
			private final BigInteger q = BigInteger.valueOf(11);
			private final BigInteger g = BigInteger.valueOf(2);
			private final GqGroup gqGroup = new GqGroup(p, q, g);
			// Create GqElements
			private final GqElement gThree = GqElementFactory.fromValue(THREE, gqGroup);
			private final GqElement gFour = GqElementFactory.fromValue(FOUR, gqGroup);
			private final GqElement gEight = GqElementFactory.fromValue(EIGHT, gqGroup);
			// Create input arguments
			// c = {9, (18, 9, 13)}
			// sk = (3, 7, 2)
			// pk = (8, 13, 4)
			// m = (4, 8, 3)
			// iAux = "Auxiliary Data"
			private final ElGamalMultiRecipientMessage m = new ElGamalMultiRecipientMessage(GroupVector.of(gFour, gEight, gThree));
			private final GqElement gThirteen = GqElementFactory.fromValue(BigInteger.valueOf(13), gqGroup);
			private final ElGamalMultiRecipientPublicKey pk = new ElGamalMultiRecipientPublicKey(GroupVector.of(gEight, gThirteen, gFour));
			private final ZqGroup zqGroup = new ZqGroup(q);
			// Create ZqElements
			private final ZqElement zOne = ZqElement.create(BigInteger.ONE, zqGroup);
			private final ZqElement zTwo = ZqElement.create(TWO, zqGroup);
			private final ZqElement zThree = ZqElement.create(THREE, zqGroup);
			private final ZqElement zFive = ZqElement.create(FIVE, zqGroup);
			private final ElGamalMultiRecipientCiphertext c = elGamal.getCiphertext(m, zFive, pk);
			private final ZqElement zSeven = ZqElement.create(SEVEN, zqGroup);
			private final ElGamalMultiRecipientPrivateKey sk = new ElGamalMultiRecipientPrivateKey(GroupVector.of(zThree, zSeven, zTwo));
			// Create output arguments
			private final ZqElement e = zSeven;
			private final ZqElement zEight = ZqElement.create(EIGHT, zqGroup);
			private final GroupVector<ZqElement, ZqGroup> z = GroupVector.of(zThree, zOne, zEight);

			private final AuxiliaryInformation iAux = AuxiliaryInformation.of("Auxiliary Data");
			private final List<BigInteger> randomValues = Arrays.asList(FOUR, SEVEN, FIVE);

			private ElGamalMultiRecipientKeyPair createKeyPair() {
				final ElGamalMultiRecipientKeyPair keyPair = mock(ElGamalMultiRecipientKeyPair.class);
				when(keyPair.getPrivateKey()).thenReturn(sk);
				when(keyPair.getPublicKey()).thenReturn(pk);
				return keyPair;
			}

			private RandomService getSpecificRandomService() {
				return new RandomService() {
					final Iterator<BigInteger> values = randomValues.iterator();

					@Override
					public BigInteger genRandomInteger(final BigInteger upperBound) {
						return values.next();
					}
				};
			}

			private DecryptionProofService createDecryptionProofService() {
				final RandomService randomService = getSpecificRandomService();
				final HashService hashService = TestHashService.create(q);
				return new DecryptionProofService(randomService, hashService);
			}

		}

		@ParameterizedTest()
		@MethodSource("jsonFileArgumentProvider")
		@DisplayName("with real values gives expected result")
		void verifyDecryptionProofWithRealValues(final ElGamalMultiRecipientCiphertext ciphertext, final ElGamalMultiRecipientPublicKey publicKey,
				final ElGamalMultiRecipientMessage message, final DecryptionProof decryptionProof, final AuxiliaryInformation auxiliaryInformation,
				final boolean expected, final String description) {
			final DecryptionProofService decryptionProofService = new DecryptionProofService(randomService, HashService.getInstance());
			final boolean actual = assertDoesNotThrow(
					() -> decryptionProofService.verifyDecryption(ciphertext, publicKey, message, decryptionProof, auxiliaryInformation).verify()
							.isVerified());
			assertEquals(expected, actual, String.format("assertion failed for: %s", description));
		}
	}
}
