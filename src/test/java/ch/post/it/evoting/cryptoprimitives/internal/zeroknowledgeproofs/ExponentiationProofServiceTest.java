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
package ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs;

import static ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs.ExponentiationProofService.computePhiExponentiation;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
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
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
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
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

class ExponentiationProofServiceTest extends TestGroupSetup {

	private static final int MAX_NUMBER_EXPONENTIATIONS = 10;
	private static HashService hashService;
	private static ZeroKnowledgeProof proofService;

	@BeforeAll
	static void setupAll() {
		hashService = TestHashService.create(gqGroup.getQ());
		proofService = new ZeroKnowledgeProofService(randomService, hashService);
	}

	@Test
	void constructorNotNullChecks() {
		assertThrows(NullPointerException.class, () -> new ExponentiationProofService(null, hashService));
		assertThrows(NullPointerException.class, () -> new ExponentiationProofService(randomService, null));
	}

	private static class TestValues {
		private final BigInteger p = BigInteger.valueOf(11);
		private final BigInteger q = BigInteger.valueOf(5);
		private final BigInteger g = BigInteger.valueOf(3);
		private final GqGroup gqGroup = new GqGroup(p, q, g);
		private final GqElement gThree = GqElementFactory.fromValue(BigInteger.valueOf(3), gqGroup);
		private final GqElement gFour = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		// Input arguments:
		// bases = (4, 3)
		// exponent = 3
		// exponentiations = (9, 5)
		// auxiliaryInformation = ("specific", "test", "values")
		private final GroupVector<GqElement, GqGroup> bases = GroupVector.of(gFour, gThree);
		private final GqElement gFive = GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup);
		private final GqElement gNine = GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup);
		private final GroupVector<GqElement, GqGroup> exponentiations = GroupVector.of(gNine, gFive);
		private final ZqGroup zqGroup = new ZqGroup(q);
		private final ZqElement zTwo = ZqElement.create(BigInteger.TWO, zqGroup);
		// Output:
		// e = 2
		// z = 3
		private final ZqElement e = zTwo;
		private final ZqElement zThree = ZqElement.create(BigInteger.valueOf(3), zqGroup);
		private final ZqElement exponent = zThree;
		private final ZqElement z = zThree;
		private final AuxiliaryInformation auxiliaryInformation = AuxiliaryInformation.of("specific", "test", "values");
		private final ImmutableList<BigInteger> randomValues = ImmutableList.of(BigInteger.TWO);

		private TestRandomService getSpecificRandomService() {
			return new TestRandomService() {
				final Iterator<BigInteger> values = randomValues.iterator();

				@Override
				public BigInteger genRandomInteger(final BigInteger upperBound) {
					return values.next();
				}
			};
		}

		private ExponentiationProofService createExponentiationProofService() {
			final TestRandomService randomService = getSpecificRandomService();
			final HashService hashService = TestHashService.create(q);
			return new ExponentiationProofService(randomService, hashService);
		}

		private ExponentiationProof createExponentiationProof() {
			return new ExponentiationProof(e, z);
		}
	}

	@Nested
	class ComputePhiExponentiationTest {
		private ZqElement preimage;
		private GroupVector<GqElement, GqGroup> bases;

		@BeforeEach
		void setup() {
			final int n = randomService.genRandomInteger(10) + 1;
			preimage = zqGroupGenerator.genRandomZqElementMember();
			bases = gqGroupGenerator.genRandomGqElementVector(n);
		}

		@Test
		void notNullChecks() {
			assertThrows(NullPointerException.class, () -> computePhiExponentiation(null, bases));
			assertThrows(NullPointerException.class, () -> computePhiExponentiation(preimage, null));
		}

		@Test
		void basesNotEmptyCheck() {
			final GroupVector<GqElement, GqGroup> emptyBases = GroupVector.empty();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> computePhiExponentiation(preimage, emptyBases));
			assertEquals("The vector of bases must contain at least 1 element.", exception.getMessage());
		}

		@Test
		void sameGroupOrderCheck() {
			final ZqElement otherpreimage = otherZqGroupGenerator.genRandomZqElementMember();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> computePhiExponentiation(otherpreimage, bases));
			assertEquals("The preimage and the bases must have the same group order.", exception.getMessage());
		}

		@RepeatedTest(10)
		void phiFunctionSize() {
			assertEquals(bases.size(), computePhiExponentiation(preimage, bases).size());
		}

		@Test
		void withSpecificValues() {
			final GqGroup gqGroup = GroupTestData.getGroupP59();
			final ZqElement specificPreimage = ZqElement.create(3, ZqGroup.sameOrderAs(gqGroup));
			final GroupVector<GqElement, GqGroup> specificBases = GroupVector.of(GqElementFactory.fromValue(BigInteger.ONE, gqGroup),
					GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup),
					GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup));

			final GroupVector<GqElement, GqGroup> expected = GroupVector.of(GqElementFactory.fromValue(BigInteger.ONE, gqGroup),
					GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup),
					GqElementFactory.fromValue(BigInteger.valueOf(21), gqGroup));
			assertEquals(expected, computePhiExponentiation(specificPreimage, specificBases));
		}
	}

	@Nested
	class GenExponentiationProofTest {

		private final AuxiliaryInformation auxiliaryInformation = AuxiliaryInformation.of("aux", "1");
		private int n;
		private GroupVector<GqElement, GqGroup> bases;
		private ZqElement exponent;
		private GroupVector<GqElement, GqGroup> exponentiations;

		@BeforeEach
		void setup() {
			n = randomService.genRandomInteger(MAX_NUMBER_EXPONENTIATIONS) + 1;
			bases = gqGroupGenerator.genRandomGqElementVector(n);
			exponent = zqGroupGenerator.genRandomZqElementMember();
			exponentiations = computePhiExponentiation(exponent, bases);
		}

		@Test
		void notNullChecks() {
			assertThrows(NullPointerException.class,
					() -> proofService.genExponentiationProof(null, exponent, exponentiations, auxiliaryInformation));
			assertThrows(NullPointerException.class, () -> proofService.genExponentiationProof(bases, null, exponentiations, auxiliaryInformation));
			assertThrows(NullPointerException.class, () -> proofService.genExponentiationProof(bases, exponent, null, auxiliaryInformation));
			assertThrows(NullPointerException.class, () -> proofService.genExponentiationProof(bases, exponent, exponentiations, null));
		}

		@Test
		void validArguments() {
			assertDoesNotThrow(() -> proofService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation));
			assertDoesNotThrow(() -> proofService.genExponentiationProof(bases, exponent, exponentiations, AuxiliaryInformation.of()));
		}

		@Test
		void hashLengthCheck() {
			final ExponentiationProofService badService = new ExponentiationProofService(randomService, HashService.getInstance());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> badService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", exception.getMessage());
		}

		@Test
		void basesNotEmptyCheck() {
			final GroupVector<GqElement, GqGroup> emptyBases = GroupVector.empty();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.genExponentiationProof(emptyBases, exponent, exponentiations, auxiliaryInformation));
			assertEquals("The bases must contain at least 1 element.", exception.getMessage());
		}

		@Test
		void basesAndExponentiationsSameSizeCheck() {
			bases = bases.append(gqGroupGenerator.genMember());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation));
			assertEquals("Bases and exponentiations must have the same size.", exception.getMessage());
		}

		@Test
		void basesAndExponentiationsSameGroupCheck() {
			exponentiations = otherGqGroupGenerator.genRandomGqElementVector(n);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation));
			assertEquals("Bases and exponentiations must have the same group.", exception.getMessage());
		}

		@Test
		void exponentSameGroupOrderThanExponentiationsCheck() {
			exponent = otherZqGroupGenerator.genRandomZqElementMember();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation));
			assertEquals("The exponent and the exponentiations must have the same group order.", exception.getMessage());
		}

		@Test
		void specificValuesGiveExpectedResult() {
			final TestValues testValues = new TestValues();
			// Input.
			final GroupVector<GqElement, GqGroup> specificBases = testValues.bases;
			final ZqElement specificExponent = testValues.exponent;
			final GroupVector<GqElement, GqGroup> specificExponentiations = testValues.exponentiations;
			final AuxiliaryInformation specificAuxiliaryInformation = testValues.auxiliaryInformation;

			final ExponentiationProofService specificProofService = testValues.createExponentiationProofService();

			final ExponentiationProof expected = testValues.createExponentiationProof();

			assertEquals(expected, specificProofService.genExponentiationProof(specificBases, specificExponent, specificExponentiations, specificAuxiliaryInformation));
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class VerifyExponentiationProofTest {

		private final AuxiliaryInformation auxiliaryInformation = AuxiliaryInformation.of("aux", "2");
		private int n;
		private GroupVector<GqElement, GqGroup> bases;
		private GroupVector<GqElement, GqGroup> exponentiations;
		private ExponentiationProof proof;

		@BeforeEach
		void setup() {
			n = randomService.genRandomInteger(MAX_NUMBER_EXPONENTIATIONS) + 1;
			bases = gqGroupGenerator.genRandomGqElementVector(n);
			exponentiations = gqGroupGenerator.genRandomGqElementVector(n);
			final ZqElement e = zqGroupGenerator.genRandomZqElementMember();
			final ZqElement z = zqGroupGenerator.genRandomZqElementMember();
			proof = new ExponentiationProof(e, z);
		}

		@Test
		void notNullChecks() {
			assertThrows(NullPointerException.class,
					() -> proofService.verifyExponentiation(null, exponentiations, proof, auxiliaryInformation));
			assertThrows(NullPointerException.class, () -> proofService.verifyExponentiation(bases, null, proof, auxiliaryInformation));
			assertThrows(NullPointerException.class,
					() -> proofService.verifyExponentiation(bases, exponentiations, null, auxiliaryInformation));
			assertThrows(NullPointerException.class, () -> proofService.verifyExponentiation(bases, exponentiations, proof, null));
		}

		@Test
		void hashLengthCheck() {
			final ExponentiationProofService badService = new ExponentiationProofService(randomService, HashService.getInstance());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> badService.verifyExponentiation(bases, exponentiations, proof, auxiliaryInformation));
			assertEquals("The hash service's bit length must be smaller than the bit length of q.", exception.getMessage());
		}

		@Test
		void basesNotEmptyCheck() {
			final GroupVector<GqElement, GqGroup> emptyBases = GroupVector.empty();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.verifyExponentiation(emptyBases, exponentiations, proof, auxiliaryInformation));
			assertEquals("The bases must contain at least 1 element.", exception.getMessage());
		}

		@Test
		void basesAndExponentiationsSameSizeCheck() {
			final GroupVector<GqElement, GqGroup> tooLongBases = bases.append(gqGroupGenerator.genMember());
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.verifyExponentiation(tooLongBases, exponentiations, proof, auxiliaryInformation));
			assertEquals("Bases and exponentiations must have the same size.", exception.getMessage());
		}

		@Test
		void basesAndExponentiationsSameGroupCheck() {
			final GroupVector<GqElement, GqGroup> otherExponentiations = otherGqGroupGenerator.genRandomGqElementVector(n);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.verifyExponentiation(bases, otherExponentiations, proof, auxiliaryInformation));
			assertEquals("Bases and exponentiations must belong to the same group.", exception.getMessage());
		}

		@Test
		void proofSameGroupOrderAsBasesCheck() {
			final ZqElement otherE = otherZqGroupGenerator.genRandomZqElementMember();
			final ZqElement otherZ = otherZqGroupGenerator.genRandomZqElementMember();
			final ExponentiationProof otherProof = new ExponentiationProof(otherE, otherZ);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> proofService.verifyExponentiation(bases, exponentiations, otherProof, auxiliaryInformation));
			assertEquals("The proof must have the same group order as the bases.", exception.getMessage());
		}

		@Test
		void validProofReturnsTrue() {
			final ZqElement exponent = zqGroupGenerator.genRandomZqElementMember();
			exponentiations = computePhiExponentiation(exponent, bases);
			proof = proofService.genExponentiationProof(bases, exponent, exponentiations, auxiliaryInformation);
			assertTrue(proofService.verifyExponentiation(bases, exponentiations, proof, auxiliaryInformation));

			proof = proofService.genExponentiationProof(bases, exponent, exponentiations, AuxiliaryInformation.of());
			assertTrue(proofService.verifyExponentiation(bases, exponentiations, proof, AuxiliaryInformation.of()));
		}

		@Test
		void differentAuxiliaryInformationReturnsFalse() {
			final TestValues testValues = new TestValues();
			final GroupVector<GqElement, GqGroup> differentAuxiliaryInformationBases = testValues.bases;
			final GroupVector<GqElement, GqGroup> differentAuxiliaryInformationExponentiations = testValues.exponentiations;
			final AuxiliaryInformation differentAuxiliaryInformation = AuxiliaryInformation.of("random");
			final ExponentiationProof differentAuxiliaryInformationProof = testValues.createExponentiationProof();
			final ExponentiationProofService differentAuxiliaryInformationProofService = testValues.createExponentiationProofService();
			assertFalse(differentAuxiliaryInformationProofService.verifyExponentiation(differentAuxiliaryInformationBases, differentAuxiliaryInformationExponentiations, differentAuxiliaryInformationProof, differentAuxiliaryInformation));
		}

		@Test
		void invalidProofReturnsFalse() {
			final TestValues testValues = new TestValues();
			final GroupVector<GqElement, GqGroup> invalidProofBases = testValues.bases;
			final GroupVector<GqElement, GqGroup> invalidProofExponentiations = testValues.exponentiations;
			final AuxiliaryInformation invalidProofAuxiliaryInformation = testValues.auxiliaryInformation;
			final ExponentiationProof exponentiationProof = testValues.createExponentiationProof();
			final ZqElement hashValue = exponentiationProof.get_e().add(testValues.zThree);
			final ExponentiationProof invalidProof = new ExponentiationProof(hashValue, exponentiationProof.get_z());
			final ExponentiationProofService invalidProofProofService = testValues.createExponentiationProofService();
			assertFalse(invalidProofProofService.verifyExponentiation(invalidProofBases, invalidProofExponentiations, invalidProof, invalidProofAuxiliaryInformation));
		}

		@Test
		void differentEponentiationsReturnsFalse() {
			final TestValues testValues = new TestValues();
			final GroupVector<GqElement, GqGroup> differentEponentiationsBases = testValues.bases;
			final GroupVector<GqElement, GqGroup> testExponentiations = testValues.exponentiations;
			final GroupVector<GqElement, GqGroup> differentExponentiations = testExponentiations.stream().map(y -> y.multiply(testValues.gNine))
					.collect(toGroupVector());
			final AuxiliaryInformation differentEponentiationsAuxiliaryInformation = testValues.auxiliaryInformation;
			final ExponentiationProof exponentiationProof = testValues.createExponentiationProof();
			final ExponentiationProofService differentEponentiationsProofService = testValues.createExponentiationProofService();
			assertFalse(differentEponentiationsProofService.verifyExponentiation(differentEponentiationsBases, differentExponentiations, exponentiationProof, differentEponentiationsAuxiliaryInformation));
		}

		@Test
		void differentBasesReturnsFalse() {
			final TestValues testValues = new TestValues();
			final GroupVector<GqElement, GqGroup> testBases = testValues.bases;
			final GroupVector<GqElement, GqGroup> differentBases = testBases.stream().map(g -> g.multiply(testValues.gFive))
					.collect(toGroupVector());
			final GroupVector<GqElement, GqGroup> differentBasesExponentiations = testValues.exponentiations;
			final AuxiliaryInformation differentBasesAuxiliaryInformation = testValues.auxiliaryInformation;
			final ExponentiationProof differentBasesProof = testValues.createExponentiationProof();
			final ExponentiationProofService differentBasesProofService = testValues.createExponentiationProofService();
			assertFalse(differentBasesProofService.verifyExponentiation(differentBases, differentBasesExponentiations, differentBasesProof, differentBasesAuxiliaryInformation));
		}

		private Stream<Arguments> jsonFileArgumentProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/zeroknowledgeproofs/verify-exponentiation.json");

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

					// Parse bases parameters.

					final BigInteger[] basesArray = input.get("bases", BigInteger[].class);
					final GroupVector<GqElement, GqGroup> realBases = Arrays.stream(basesArray).map(basesA -> GqElementFactory.fromValue(basesA, gqGroup))
							.collect(toGroupVector());

					// Parse exponentiations parameters
					final BigInteger[] exponentiationsArray = input.get("statement", BigInteger[].class);
					final GroupVector<GqElement, GqGroup> realExponentiations = Arrays.stream(exponentiationsArray)
							.map(eA -> GqElementFactory.fromValue(eA, gqGroup))
							.collect(toGroupVector());

					// Parse decryption proof parameters
					final JsonData realProof = input.getJsonData("proof");

					final ZqElement e = ZqElement.create(realProof.get("e", BigInteger.class), zqGroup);
					final ZqElement z = ZqElement.create(realProof.get("z", BigInteger.class), zqGroup);
					final ExponentiationProof exponentiationProof = new ExponentiationProof(e, z);

					// Parse auxiliary information parameters
					final String[] auxInformation = input.get("additional_information", String[].class);
					final AuxiliaryInformation realAuxiliaryInformation = AuxiliaryInformation.of(auxInformation);

					// Parse output parameters
					final JsonData output = testParameters.getOutput();

					final Boolean result = output.get("verif_result", Boolean.class);

					return Arguments.of(realBases, realExponentiations, exponentiationProof, realAuxiliaryInformation, result, testParameters.getDescription());
				}
			});

		}

		@ParameterizedTest(name = "{5}")
		@MethodSource("jsonFileArgumentProvider")
		@DisplayName("with real values gives expected result")
		void verifyExponentiationProofWithRealValues(final GroupVector<GqElement, GqGroup> bases,
				final GroupVector<GqElement, GqGroup> exponentiations, final ExponentiationProof exponentiationProof,
				final AuxiliaryInformation auxiliaryInformation,
				final boolean expected, final String description) {
			final ExponentiationProofService exponentiationProofService = new ExponentiationProofService(randomService, HashService.getInstance());
			final boolean actual = assertDoesNotThrow(
					() -> exponentiationProofService.verifyExponentiation(bases, exponentiations, exponentiationProof, auxiliaryInformation));
			assertEquals(expected, actual, String.format("assertion failed for: %s", description));
		}
	}
}
