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
package ch.post.it.evoting.cryptoprimitives.elgamal;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientMessages;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

class ElGamalMultiRecipientMessageTest extends TestGroupSetup {

	private static final int NUM_ELEMENTS = 2;

	private static GroupVector<GqElement, GqGroup> validMessageElements;
	private static ElGamalMultiRecipientMessage message;

	@BeforeEach
	void setUp() {
		final GqElement m1 = gqGroupGenerator.genMember();
		final GqElement m2 = gqGroupGenerator.genMember();

		validMessageElements = GroupVector.of(m1, m2);
		message = new ElGamalMultiRecipientMessage(validMessageElements);
	}

	@Test
	@DisplayName("contains the correct message")
	void constructionTest() {
		final ElGamalMultiRecipientMessage correctMessage = new ElGamalMultiRecipientMessage(validMessageElements);

		assertEquals(validMessageElements, correctMessage.stream().collect(toGroupVector()));
	}

	// Provides parameters for the withInvalidParameters test.
	static Stream<Arguments> createInvalidArgumentsProvider() {
		return Stream.of(
				Arguments.of(null, NullPointerException.class, null),
				Arguments.of(GroupVector.empty(), IllegalArgumentException.class, "An ElGamal message must not be empty.")
		);
	}

	@ParameterizedTest(name = "message = {0} throws {1}")
	@MethodSource("createInvalidArgumentsProvider")
	@DisplayName("created with invalid parameters")
	void constructionWithInvalidParametersTest(final GroupVector<GqElement, GqGroup> messageElements,
			final Class<? extends RuntimeException> exceptionClass, final String errorMsg) {
		final Exception exception = assertThrows(exceptionClass, () -> new ElGamalMultiRecipientMessage(messageElements));
		assertEquals(errorMsg, exception.getMessage());
	}

	@Test
	@DisplayName("create from ones contains only 1s")
	void onesTest() {
		final int n = randomService.genRandomInteger(10) + 1;
		final ElGamalMultiRecipientMessage ones = ElGamalMultiRecipientMessages.ones(gqGroup, n);

		final ImmutableList<GqElement> onesList = Stream.generate(gqGroup::getIdentity).limit(n).collect(toImmutableList());

		assertEquals(onesList, ones.stream().collect(toImmutableList()));
		assertEquals(n, ones.size());
	}

	@Test
	@DisplayName("create from ones with bad input throws")
	void onesWithBadInputTest() {
		assertThrows(NullPointerException.class, () -> ElGamalMultiRecipientMessages.ones(null, 1));
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> ElGamalMultiRecipientMessages.ones(gqGroup, 0));
		assertEquals("Cannot generate a message of constants of non positive length.", exception.getMessage());
	}

	@Test
	@DisplayName("create from constant contains only constant")
	void constantsTest() {
		final int n = randomService.genRandomInteger(10) + 1;
		final GqElement constant = gqGroupGenerator.genMember();
		final ElGamalMultiRecipientMessage constants = ElGamalMultiRecipientMessages.constantMessage(constant, n);

		final ImmutableList<GqElement> constantsList = Stream.generate(() -> constant).limit(n).collect(toImmutableList());

		assertEquals(constantsList, constants.stream().collect(toImmutableList()));
		assertEquals(n, constants.size());
	}

	@Test
	@DisplayName("create from constant with bad input throws")
	void constantsWithBadInputTest() {
		assertThrows(NullPointerException.class, () -> ElGamalMultiRecipientMessages.constantMessage(null, 1));
		final GqElement constant = gqGroupGenerator.genMember();
		final Exception exception =
				assertThrows(IllegalArgumentException.class, () -> ElGamalMultiRecipientMessages.constantMessage(constant, 0));
		assertEquals("Cannot generate a message of constants of non positive length.", exception.getMessage());
	}

	// Provides parameters for the invalid decryption parameters test.
	static Stream<Arguments> createInvalidDecryptionArgumentsProvider() {
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_ELEMENTS, randomService);
		final ElGamalMultiRecipientPrivateKey secretKey = keyPair.getPrivateKey();
		final ElGamalMultiRecipientPrivateKey tooShortSecretKey = new ElGamalMultiRecipientPrivateKey(GroupVector.of(secretKey.get(0)));
		final ZqElement exponent = ZqElement.create(randomService.genRandomInteger(zqGroup.getQ()), zqGroup);
		final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(message, exponent, keyPair.getPublicKey());

		final GqGroup differentGroup = GroupTestData.getDifferentGqGroup(gqGroup);
		final ElGamalMultiRecipientKeyPair differentGroupKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(differentGroup, NUM_ELEMENTS,
				randomService);
		final ElGamalMultiRecipientPrivateKey differentGroupSecretKey = differentGroupKeyPair.getPrivateKey();

		return Stream.of(
				Arguments.of(null, secretKey, NullPointerException.class),
				Arguments.of(ciphertext, null, NullPointerException.class),
				Arguments.of(ciphertext, tooShortSecretKey, IllegalArgumentException.class),
				Arguments.of(ciphertext, differentGroupSecretKey, IllegalArgumentException.class)
		);
	}

	@ParameterizedTest(name = "ciphertext = {0} and secret key = {1} throws {2}")
	@MethodSource("createInvalidDecryptionArgumentsProvider")
	@DisplayName("get message with invalid parameters")
	void whenGetMessageWithInvalidParametersTest(final ElGamalMultiRecipientCiphertext c, final ElGamalMultiRecipientPrivateKey sk,
			final Class<? extends RuntimeException> exceptionClass) {
		assertThrows(exceptionClass, () -> ElGamalMultiRecipientMessages.getMessage(c, sk));
	}

	@RepeatedTest(10)
	void testMessageDifferentFromCiphertext() {
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_ELEMENTS, randomService);
		final ZqElement exponent = ZqElement.create(randomService.genRandomInteger(zqGroup.getQ()), zqGroup);
		final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(message, exponent, keyPair.getPublicKey());
		final ElGamalMultiRecipientMessage newMessage = ElGamalMultiRecipientMessages.getMessage(ciphertext, keyPair.getPrivateKey());

		assertNotEquals(ciphertext.stream(), newMessage.stream());
	}

	@Test
	void whenGetMessageFromUnityCiphertextTest() {
		final ElGamalMultiRecipientMessage onesMessage = ElGamalMultiRecipientMessages.ones(gqGroup, 2);
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_ELEMENTS, randomService);
		final ZqElement zero = zqGroup.getIdentity();
		final ElGamalMultiRecipientCiphertext unityCiphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(onesMessage, zero,
				keyPair.getPublicKey());
		assertEquals(onesMessage, ElGamalMultiRecipientMessages.getMessage(unityCiphertext, keyPair.getPrivateKey()));
	}
}
