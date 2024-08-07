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
package ch.post.it.evoting.cryptoprimitives.elgamal;

import static ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts.getCiphertext;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

class ElGamalMultiRecipientCiphertextCreationTest extends TestGroupSetup {

	private static final int NUM_RECIPIENTS = 10;

	private static GqElement gqIdentity;
	private static ElGamalMultiRecipientMessage onesMessage;

	private ElGamalMultiRecipientMessage validMessage;
	private ZqElement validExponent;
	private ElGamalMultiRecipientPublicKey validPK;

	@BeforeAll
	static void setUp() {
		gqIdentity = gqGroup.getIdentity();
		final GroupVector<GqElement, GqGroup> ones = Stream.generate(() -> gqGroup.getIdentity())
				.limit(NUM_RECIPIENTS)
				.collect(toGroupVector());
		onesMessage = new ElGamalMultiRecipientMessage(ones);
	}

	@BeforeEach
	void setUpEach() {
		final GroupVector<GqElement, GqGroup> messageElements = Stream.generate(() -> gqGroupGenerator.genMember())
				.limit(NUM_RECIPIENTS)
				.collect(toGroupVector());
		validMessage = new ElGamalMultiRecipientMessage(messageElements);

		validExponent = ZqElement.create(randomService.genRandomInteger(zqGroup.getQ()), zqGroup);

		validPK = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_RECIPIENTS, randomService).getPublicKey();
	}

	@Test
	void testNullMessageThrows() {
		assertThrows(NullPointerException.class, () -> getCiphertext(null, validExponent, validPK));
	}

	@Test
	void testNullExponentThrows() {
		assertThrows(NullPointerException.class, () -> getCiphertext(validMessage, null, validPK));
	}

	@Test
	void testNullPublicKeyThrows() {
		assertThrows(NullPointerException.class, () -> getCiphertext(validMessage, validExponent, null));
	}

	@Test
	void testExponentFromDifferentQThrows() {
		final ZqGroup otherGroup = GroupTestData.getDifferentZqGroup(zqGroup);
		final ZqElement otherGroupExponent = ZqElement.create(randomService.genRandomInteger(otherGroup.getQ()), otherGroup);

		assertThrows(IllegalArgumentException.class, () -> getCiphertext(validMessage, otherGroupExponent, validPK));
	}

	@Test
	void testMessageAndPublicKeyFromDifferentGroupsThrows() {
		final GqGroup otherGroup = GroupTestData.getDifferentGqGroup(gqGroup);
		final ElGamalMultiRecipientPublicKey otherGroupPublicKey =
				ElGamalMultiRecipientKeyPair.genKeyPair(otherGroup, 1, randomService).getPublicKey();

		assertThrows(IllegalArgumentException.class, () -> getCiphertext(validMessage, validExponent, otherGroupPublicKey));
	}

	@Test
	void testPublicKeyAndExponentFromDifferentGroupsThrows() {
		final ZqGroup otherGroup = GroupTestData.getDifferentZqGroup(zqGroup);
		final ZqElement otherGroupExponent = ZqElement.create(randomService.genRandomInteger(otherGroup.getQ()), otherGroup);

		assertThrows(IllegalArgumentException.class, () -> getCiphertext(validMessage, otherGroupExponent, validPK));
	}

	@Test
	void testMoreMessageElementsThenPublicKeyElementsThrows() {
		final ElGamalMultiRecipientPublicKey tooShortPK =
				ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_RECIPIENTS - 1, randomService).getPublicKey();

		assertThrows(IllegalArgumentException.class, () -> getCiphertext(validMessage, validExponent, tooShortPK));
	}

	@Test
	void testIdentityRandomnessWithNoCompressionAndIdentityMessageElementsThenGammaIsGeneratorAndCiphertextIsPrivateKey() {
		final ZqElement one = ZqElement.create(BigInteger.ONE, zqGroup);
		final ElGamalMultiRecipientCiphertext ciphertext = getCiphertext(onesMessage, one, validPK);

		assertEquals(gqGroup.getGenerator(), ciphertext.getGamma());
		assertEquals(validPK.stream().collect(toGroupVector()), ciphertext.stream().skip(1).collect(toGroupVector()));
	}

	@Test
	void testFewerMessagesThanKeysWithIdentityRandomnessAndIdentityMessageElementsThenCut() {
		final int nMessages = NUM_RECIPIENTS / 2;
		final GroupVector<GqElement, GqGroup> oneElements = Stream.generate(() -> GqElementFactory.fromValue(BigInteger.ONE, gqGroup))
				.limit(nMessages)
				.collect(toGroupVector());
		final ElGamalMultiRecipientMessage smallOneMessage = new ElGamalMultiRecipientMessage(oneElements);
		final ZqElement oneExponent = ZqElement.create(BigInteger.ONE, zqGroup);
		final ElGamalMultiRecipientCiphertext ciphertext = getCiphertext(smallOneMessage, oneExponent, validPK);

		//With a exponent of one and message of ones, the ciphertext phis is just the public key
		assertEquals(validPK.stream().limit(nMessages).collect(toGroupVector()), ciphertext.getPhis());
	}

	@Test
	void testZeroExponentGivesMessage() {
		final ZqElement zeroExponent = ZqElement.create(BigInteger.ZERO, zqGroup);
		final ElGamalMultiRecipientCiphertext ciphertext = getCiphertext(validMessage, zeroExponent, validPK);
		assertEquals(validMessage.stream().collect(toGroupVector()), ciphertext.stream().skip(1).collect(toGroupVector()));
		assertEquals(gqIdentity, ciphertext.getGamma());
	}

	@Test
	void testSpecificValues() {
		final GqGroup group = new GqGroup(BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(3));
		final ElGamalMultiRecipientMessage message =
				new ElGamalMultiRecipientMessage(
						GroupVector.of(
								GqElementFactory.fromValue(BigInteger.valueOf(4), group),
								GqElementFactory.fromValue(BigInteger.valueOf(5), group)
						)
				);
		final ZqElement exponent = ZqElement.create(BigInteger.TWO, ZqGroup.sameOrderAs(group));
		final ElGamalMultiRecipientPublicKey publicKey =
				new ElGamalMultiRecipientPublicKey(
						GroupVector.of(
								GqElementFactory.fromValue(BigInteger.valueOf(5), group),
								GqElementFactory.fromValue(BigInteger.valueOf(9), group)
						)
				);
		final ElGamalMultiRecipientCiphertext ciphertext =
				ElGamalMultiRecipientCiphertext.create(
						GqElementFactory.fromValue(BigInteger.valueOf(9), group),
						GroupVector.of(
								GqElementFactory.fromValue(BigInteger.ONE, group),
								GqElementFactory.fromValue(BigInteger.valueOf(9), group)
						)
				);

		assertEquals(ciphertext, getCiphertext(message, exponent, publicKey));
	}

	// Provides parameters for the testGetCiphertextWithRealValues.
	static Stream<Arguments> jsonFileArgumentProvider() {

		final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/elgamal/get-ciphertext.json");

		return parametersList.stream().parallel().map(testParameters -> {
			// Context.
			final JsonData context = testParameters.getContext();
			final BigInteger p = context.get("p", BigInteger.class);
			final BigInteger q = context.get("q", BigInteger.class);
			final BigInteger g = context.get("g", BigInteger.class);

			try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = Mockito.mockStatic(SecurityLevelConfig.class)) {
				mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(testParameters.getSecurityLevel());
				final GqGroup gqGroup = new GqGroup(p, q, g);
				final ZqGroup zqGroup = new ZqGroup(q);

				// Parse first message parameters.
				final JsonData input = testParameters.getInput();

				final BigInteger[] boldM = input.get("bold_m", BigInteger[].class);
				final GroupVector<GqElement, GqGroup> message = Arrays.stream(boldM)
						.map(m -> GqElementFactory.fromValue(m, gqGroup))
						.collect(toGroupVector());

				// Parse random exponent.
				final BigInteger r = input.get("r", BigInteger.class);
				final ZqElement exponent = ZqElement.create(r, zqGroup);

				// Parse public key.
				final BigInteger[] boldPk = input.get("bold_pk", BigInteger[].class);
				final GroupVector<GqElement, GqGroup> publicKey = Arrays.stream(boldPk)
						.map(pk -> GqElementFactory.fromValue(pk, gqGroup))
						.collect(toGroupVector());

				// Parse resulting ciphertext.
				final JsonData outputJsonData = testParameters.getOutput();

				final GqElement gammaRes = GqElementFactory.fromValue(outputJsonData.get("gamma", BigInteger.class), gqGroup);
				final BigInteger[] phisOutput = outputJsonData.get("phis", BigInteger[].class);
				final GroupVector<GqElement, GqGroup> phisRes = Arrays.stream(phisOutput)
						.map(phi -> GqElementFactory.fromValue(phi, gqGroup))
						.collect(toGroupVector());

				return Arguments.of(message, exponent, publicKey, gammaRes, phisRes, testParameters.getDescription());
			}
		});
	}

	@ParameterizedTest
	@MethodSource("jsonFileArgumentProvider")
	@DisplayName("with a valid other ciphertext gives expected result")
	void testGetCiphertextWithRealValues(final GroupVector<GqElement, GqGroup> messageVector, final ZqElement exponent,
			final GroupVector<GqElement, GqGroup> publicKeyVector, final GqElement gammaRes, final GroupVector<GqElement, GqGroup> phisRes,
			final String description) {

		// Create first ciphertext.
		final ElGamalMultiRecipientMessage message = new ElGamalMultiRecipientMessage(messageVector);

		// Create second ciphertext.
		final ElGamalMultiRecipientPublicKey publicKey = new ElGamalMultiRecipientPublicKey(publicKeyVector);

		// Expected multiplication result.
		final ElGamalMultiRecipientCiphertext ciphertextRes = ElGamalMultiRecipientCiphertext.create(gammaRes, phisRes);

		assertEquals(ciphertextRes, ElGamalMultiRecipientCiphertexts.getCiphertext(message, exponent, publicKey),
				String.format("assertion failed for: %s", description));
	}
}
