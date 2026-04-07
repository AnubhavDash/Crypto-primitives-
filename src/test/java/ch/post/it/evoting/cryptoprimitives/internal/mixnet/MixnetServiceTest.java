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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.mixnet.Mixnet;
import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetOptimizationMode;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

class MixnetServiceTest extends TestGroupSetup {

	private static ElGamalMultiRecipientPublicKey publicKey;
	private static int keySize;

	@BeforeEach
	void setUpAll() {
		keySize = randomService.genRandomInteger(10) + 1;
		publicKey = elGamalGenerator.genRandomPublicKey(keySize);
	}

	@Nested
	class GetVerifiableShuffleTest {

		@Test
		@DisplayName("throws when shuffle inputs are null")
		void testNullChecking() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = elGamalGenerator.genRandomCiphertextVector(5, 5);
			assertThrows(NullPointerException.class, () -> mixnet.genVerifiableShuffle(null, publicKey));
			assertThrows(NullPointerException.class, () -> mixnet.genVerifiableShuffle(inputCiphertextList, null));
		}

		@Test
		@DisplayName("throws when q is too small for the shuffle hash length")
		void testTooSmallGqGroup() {
			final MixnetService mixnetService = new MixnetService();
			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnetService.genVerifiableShuffle(ciphertexts, publicKey));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when fewer than two ciphertexts are provided for shuffle generation")
		void testMultipleCipherTextsCheck() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);
			final ElGamalMultiRecipientCiphertext cipherText = mock(ElGamalMultiRecipientCiphertext.class);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = GroupVector.of(cipherText);

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> emptyCiphertextList = GroupVector.empty();
			IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.genVerifiableShuffle(emptyCiphertextList, publicKey));
			assertEquals("N must be >= 2", illegalArgumentException.getMessage());

			illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.genVerifiableShuffle(inputCiphertextList, publicKey));
			assertEquals("N must be >= 2", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when the number of ciphertexts exceeds q minus 3")
		void testNumberOfCiphertextsTooLargeThrows() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int maxNumberCiphertexts = gqGroup.getQ().intValueExact() - 3;
			final int Nc = maxNumberCiphertexts + 1;
			final int l = keySize;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.genVerifiableShuffle(ciphertexts, publicKey));
			assertEquals("N must be smaller or equal to q - 3", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when ciphertexts and public key are from different groups")
		void testSameGroup() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = otherGqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = otherGroupElGamalGenerator.genRandomCiphertextVector(Nc,
					l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.genVerifiableShuffle(inputCiphertextList, publicKey));
			assertEquals("Ciphertexts must have the same group as the publicKey", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("generates a verifiable shuffle for valid inputs")
		void testValidShuffle() {
			final GqGroup group = GroupTestData.getLargeGqGroup();
			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);

			publicKey = elGamalGenerator.genRandomPublicKey(keySize);

			final HashService hashService = TestHashService.create(gqGroup.getQ());
			final Mixnet mixnet = new MixnetService(hashService);

			final int Nc = randomService.genRandomInteger(10) + 2;
			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			final VerifiableShuffle verifiableShuffle = mixnet.genVerifiableShuffle(inputCiphertextList, publicKey);

			assertNotNull(verifiableShuffle);
			assertNotNull(verifiableShuffle.shuffleArgument());
			assertEquals(inputCiphertextList.size(), verifiableShuffle.shuffledCiphertexts().size());
		}

		@Test
		@DisplayName("generates a verifiable shuffle with computation optimized mode")
		void testValidShuffleWithComputationOptimized() {
			final GqGroup group = GroupTestData.getLargeGqGroup();
			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);
			final ElGamalMultiRecipientPublicKey pk = elGamalGenerator.genRandomPublicKey(keySize);

			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final int Nc = 9; // square
			final int l = Math.min(keySize, 3);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
				final VerifiableShuffle verifiableShuffle = mixnet.genVerifiableShuffle(inputCiphertextList, pk);

				assertNotNull(verifiableShuffle);
				assertNotNull(verifiableShuffle.shuffleArgument());
				assertEquals(inputCiphertextList.size(), verifiableShuffle.shuffledCiphertexts().size());
			}
		}

		@Test
		@DisplayName("throws when ciphertexts contain more elements than the public key")
		void testNumberOfCipherTextsGreaterThanPublicKey() {
			final HashService hashService = TestHashService.create(gqGroup.getQ());
			final Mixnet mixnet = new MixnetService(hashService);

			final int Nc = randomService.genRandomInteger(gqGroup.getQ().intValueExact() - 4) + 2;
			final int l = keySize + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> inputCiphertextList = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.genVerifiableShuffle(inputCiphertextList, publicKey));

			assertEquals("Ciphertexts must not contain more elements than the publicKey", illegalArgumentException.getMessage());
		}
	}

	@Nested
	class VerifyShuffleTest {

		@Test
		@DisplayName("throws when verification inputs are null")
		void testNullChecking() {
			final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(2, keySize);
			final ElGamalMultiRecipientPublicKey randomPublicKey = elGamalGenerator.genRandomPublicKey(keySize);
			final VerifiableShuffle verifiableShuffle = mixnet.genVerifiableShuffle(ciphertexts, randomPublicKey);
			final ShuffleArgument shuffleArgument = verifiableShuffle.shuffleArgument();
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = verifiableShuffle.shuffledCiphertexts();

			assertThrows(NullPointerException.class, () -> mixnet.verifyShuffle(null, shuffledCiphertexts, shuffleArgument, randomPublicKey));
			assertThrows(NullPointerException.class, () -> mixnet.verifyShuffle(ciphertexts, null, shuffleArgument, randomPublicKey));
			assertThrows(NullPointerException.class, () -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, null, randomPublicKey));
			assertThrows(NullPointerException.class, () -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, null));
		}

		@Test
		@DisplayName("throws when q is too small during shuffle verification")
		void testTooSmallGqGroup() {
			final MixnetService mixnetService = new MixnetService();
			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnetService.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("The exclusive upper bound must have a bit length of at least 512.", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when no ciphertexts are provided for verification")
		void testEmptyCipherTextsCheck() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> emptyCiphertextList = GroupVector.empty();
			final ShuffleArgument emptyShuffleArgument = mock(ShuffleArgument.class);
			when(emptyShuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> emptyShuffledCiphertextList = GroupVector.empty();
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(emptyCiphertextList, emptyShuffledCiphertextList, emptyShuffleArgument, publicKey));
			assertEquals("N must be >= 2", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when only one ciphertext is provided for verification")
		void testOnlyOneCipherTextCheck() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final ElGamalMultiRecipientCiphertext cipherText = mock(ElGamalMultiRecipientCiphertext.class);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> singletonCiphertextList = GroupVector.of(cipherText);
			final ShuffleArgument singletonShuffleArgument = mock(ShuffleArgument.class);
			when(singletonShuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> singletonShuffledCiphertextList = GroupVector.of(cipherText);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(singletonCiphertextList, singletonShuffledCiphertextList, singletonShuffleArgument, publicKey));
			assertEquals("N must be >= 2", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when too many ciphertexts are provided for verification")
		void testNumberOfCiphertextsTooLargeThrows() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int maxNumberCiphertexts = gqGroup.getQ().intValueExact() + 3;
			final int Nc = maxNumberCiphertexts + 1;
			final int l = keySize;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("N must be smaller or equal to q - 3", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when original and shuffled ciphertexts are from different groups")
		void testCiphertextsSameGroup() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = otherGroupElGamalGenerator
					.genRandomCiphertextVector(Nc, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("The shuffled and re-encrypted ciphertexts must have the same group than the un-shuffled ciphertexts.",
					illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when shuffle argument and ciphertexts are from different groups")
		void testShuffleArgumentSameGroup() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(otherGqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("The ciphertexts and the shuffle argument must have the same group.", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when public key and ciphertexts are from different groups")
		void testPublicKeySameGroup() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = otherGroupElGamalGenerator.genRandomCiphertextVector(2, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(otherGqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = otherGroupElGamalGenerator.genRandomCiphertextVector(2,
					l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("The public key and the ciphertexts must have to the same group.", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when ciphertext vectors have different sizes")
		void testCiphertextVectorDimensions() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = elGamalGenerator.genRandomCiphertextVector(Nc + 1, l);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("There must be as many shuffled and re-encrypted ciphertexts, as un-shuffled ciphertexts.",
					illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("throws when ciphertext element sizes differ")
		void testCiphertextDimensions() {
			final HashService hashService = mock(HashService.class);
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = gqGroup.getQ().intValueExact() - 3;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ShuffleArgument shuffleArgument = mock(ShuffleArgument.class);
			when(shuffleArgument.getGroup()).thenReturn(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l + 1);

			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, publicKey));
			assertEquals("All ciphertexts must have the same number of elements.", illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("verifies a correctly generated shuffle argument")
		void testVerifiesCorrectlyGeneratedArgument() {
			final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final int minNumberOfVotes = 2;
			final int maxGroupCommitmentKeySize = 5;
			final int Nc = randomService.genRandomInteger(maxGroupCommitmentKeySize - minNumberOfVotes + 1) + minNumberOfVotes;
			final int l = randomService.genRandomInteger(keySize) + 1;

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ElGamalMultiRecipientPublicKey randomPublicKey = elGamalGenerator.genRandomPublicKey(keySize);
			final VerifiableShuffle verifiableShuffle = mixnet.genVerifiableShuffle(ciphertexts, randomPublicKey);
			final ShuffleArgument shuffleArgument = verifiableShuffle.shuffleArgument();
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = verifiableShuffle.shuffledCiphertexts();

			assertTrue(() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, randomPublicKey).isVerified());
		}

		@Test
		@DisplayName("verifies a correctly generated shuffle argument with computation optimized mode")
		void testVerifiesCorrectlyGeneratedArgumentWithComputationOptimized() {
			final GqGroup group = GroupTestData.getLargeGqGroup();
			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);
			final int Nc = 9; // square
			final int l = Math.min(keySize, 3);

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ElGamalMultiRecipientPublicKey pk = elGamalGenerator.genRandomPublicKey(keySize);

			try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
				final VerifiableShuffle verifiableShuffle = mixnet.genVerifiableShuffle(ciphertexts, pk);
				final ShuffleArgument shuffleArgument = verifiableShuffle.shuffleArgument();
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = verifiableShuffle.shuffledCiphertexts();

				assertTrue(mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, pk).isVerified());
			}
		}

		@Test
		@DisplayName("cross-mode verification succeeds for a prime number of ciphertexts")
		void testPrimeCiphertextsCrossModeVerificationSucceeds() {
			final GqGroup group = GroupTestData.getLargeGqGroup();
			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);
			final int Nc = 11; // prime
			final int l = Math.min(keySize, 3);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ElGamalMultiRecipientPublicKey pk = elGamalGenerator.genRandomPublicKey(keySize);

			// Generate in memory optimized mode, verify in compute optimized mode
			try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.MEMORY_OPTIMIZED)) {
				final VerifiableShuffle verifiableShuffleMemory = mixnet.genVerifiableShuffle(ciphertexts, pk);
				try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
					assertTrue(mixnet.verifyShuffle(ciphertexts,
							verifiableShuffleMemory.shuffledCiphertexts(),
							verifiableShuffleMemory.shuffleArgument(),
							pk).isVerified());
				}
			}

			// Generate in compute optimized mode, verify in memory optimized mode
			try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
				final VerifiableShuffle verifiableShuffleCompute = mixnet.genVerifiableShuffle(ciphertexts, pk);
				try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.MEMORY_OPTIMIZED)) {
					assertTrue(mixnet.verifyShuffle(ciphertexts,
							verifiableShuffleCompute.shuffledCiphertexts(),
							verifiableShuffleCompute.shuffleArgument(),
							pk).isVerified());
				}
			}
		}

		@Test
		@DisplayName("cross-mode verification fails for a composite number of ciphertexts")
		void testCompositeCiphertextsCrossModeVerificationFails() {
			final GqGroup group = GroupTestData.getLargeGqGroup();
			final HashService hashService = HashService.getInstance();
			final Mixnet mixnet = new MixnetService(hashService);

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);
			final int Nc = 12; // composite
			final int l = Math.min(keySize, 3);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts =
					elGamalGenerator.genRandomCiphertextVector(Nc, l);
			final ElGamalMultiRecipientPublicKey pk = elGamalGenerator.genRandomPublicKey(keySize);

			try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.MEMORY_OPTIMIZED)) {
				final VerifiableShuffle verifiableShuffleMemory = mixnet.genVerifiableShuffle(ciphertexts, pk);
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = verifiableShuffleMemory.shuffledCiphertexts();
				final ShuffleArgument shuffleArgument = verifiableShuffleMemory.shuffleArgument();

				assertTrue(mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, pk).isVerified());

				// Cross-mode is expected to fail: dimensions (m,n) mismatch
				try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
					final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
							() -> mixnet.verifyShuffle(ciphertexts, shuffledCiphertexts, shuffleArgument, pk));

					assertEquals("The m dimension of the argument must be equal to the input parameter m.", ex.getMessage());
				}
			}
		}
	}
}
