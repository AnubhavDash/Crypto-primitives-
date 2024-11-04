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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalUtils;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;
import ch.post.it.evoting.cryptoprimitives.mixnet.Shuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;

class ShuffleServiceTest extends TestGroupSetup {

	static int NUM_ELEMENTS = 10;
	static int NUM_CIPHERTEXTS = 10;
	static TestRandomService randomService = new TestRandomService();
	static PermutationService permutationService = new PermutationService(randomService);
	static ShuffleService shuffleService = new ShuffleService(randomService, permutationService);

	private static ElGamalMultiRecipientPublicKey randomPublicKey;
	private static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> randomCiphertexts;

	@BeforeAll
	static void setUp() {
		randomPublicKey = elGamalGenerator.genRandomPublicKey(NUM_ELEMENTS);
		randomCiphertexts = GroupVector.of(elGamalGenerator.genRandomCiphertext(NUM_ELEMENTS));
	}

	@Test
	void testNullCiphertextsThrows() {
		assertThrows(NullPointerException.class, () -> shuffleService.genShuffle(null, randomPublicKey));
	}

	@Test
	void testNullPublicKeyThrows() {
		assertThrows(NullPointerException.class, () -> shuffleService.genShuffle(randomCiphertexts, null));
	}

	@Test
	void testNoCiphertextsReturnsEmptyShuffle() {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = GroupVector.empty();
		assertEquals(Shuffle.EMPTY, shuffleService.genShuffle(ciphertexts, randomPublicKey));
	}

	@Test
	void testCiphertextLongerThanKeyThrows() {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = GroupVector.of(
				elGamalGenerator.genRandomCiphertext(NUM_ELEMENTS + 1));
		assertThrows(IllegalArgumentException.class, () -> shuffleService.genShuffle(ciphertexts, randomPublicKey));
	}

	@Test
	void testCiphertextAndKeyFromDifferentGroupsThrows() {
		final ElGamalMultiRecipientPublicKey otherGroupKey = otherGroupElGamalGenerator.genRandomPublicKey(NUM_ELEMENTS);
		assertThrows(IllegalArgumentException.class, () -> shuffleService.genShuffle(randomCiphertexts, otherGroupKey));
	}

	@Test
	void testShuffleCiphertextIsNotEqualToOriginal() {
		final ElGamalMultiRecipientPublicKey publicKey = elGamalGenerator.genRandomPublicKey(NUM_ELEMENTS);
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertexts(publicKey, NUM_ELEMENTS,
				NUM_CIPHERTEXTS).stream().collect(toGroupVector());
		final Shuffle shuffle = shuffleService.genShuffle(ciphertexts, publicKey);
		assertNotEquals(ciphertexts, shuffle.getCiphertexts());
	}

	@Test
	void testSpecificValues() {
		//Define group
		final BigInteger p = BigInteger.valueOf(23);
		final BigInteger q = BigInteger.valueOf(11);
		final BigInteger g = BigInteger.TWO;

		final GqGroup localGroup = new GqGroup(p, q, g);

		//Define N
		final int numCiphertexts = 3;

		//Mock the permutation
		final Permutation permutation = new Permutation(ImmutableList.of(1, 2, 0));
		final PermutationService permutationService = mock(PermutationService.class);
		when(permutationService.genPermutation(numCiphertexts)).thenReturn(permutation);

		//Mock random exponents
		final RandomService randomService = mock(RandomService.class);
		final ZqGroup exponentGroup = ZqGroup.sameOrderAs(localGroup);
		final List<BigInteger> randomIntegers = IntStream.range(0, permutation.size()).mapToObj(i -> BigInteger.valueOf(7)).toList();
		when(randomService.genRandomInteger(exponentGroup.getQ()))
				.thenReturn(randomIntegers.get(0), randomIntegers.subList(1, randomIntegers.size()).toArray(new BigInteger[] {}));

		//Create public key
		final GroupVector<GqElement, GqGroup> pkElements =
				Stream.of(6, 4, 3)
						.map(pki -> GqElement.GqElementFactory.fromValue(BigInteger.valueOf(pki), localGroup))
						.collect(toGroupVector());
		final ElGamalMultiRecipientPublicKey publicKey = new ElGamalMultiRecipientPublicKey(pkElements);

		//Create ciphertexts
		final Stream<ImmutableList<Integer>> ciphertextValues = Stream.of(
				ImmutableList.of(16, 18, 2, 2),
				ImmutableList.of(13, 1, 3, 4),
				ImmutableList.of(3, 3, 6, 6)
		);
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = ElGamalUtils.valuesToCiphertext(ciphertextValues, localGroup);

		//Expected ciphertexts
		final Stream<ImmutableList<Integer>> expectedCiphertextValues = Stream.of(
				ImmutableList.of(8, 3, 1, 8),
				ImmutableList.of(16, 9, 2, 12),
				ImmutableList.of(1, 8, 16, 4)
		);
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> expectedCiphertexts = ElGamalUtils.valuesToCiphertext(expectedCiphertextValues,
				localGroup);

		//Create shuffle
		final ShuffleService shuffleService = new ShuffleService(randomService, permutationService);
		final Shuffle shuffle = shuffleService.genShuffle(ciphertexts, publicKey);

		assertEquals(expectedCiphertexts, shuffle.getCiphertexts());
		assertEquals(permutation, shuffle.getPermutation());
		assertEquals(randomIntegers.stream().map(r -> ZqElement.create(r, exponentGroup)).collect(toGroupVector()),
				shuffle.getReEncryptionExponents());
	}
}
