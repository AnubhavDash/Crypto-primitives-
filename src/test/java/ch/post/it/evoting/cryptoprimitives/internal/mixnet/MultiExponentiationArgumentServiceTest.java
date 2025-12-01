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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static ch.post.it.evoting.cryptoprimitives.internal.mixnet.TestMultiExponentiationStatementWitnessPairGenerator.StatementWitnessPair;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationStatement;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationWitness;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.Generators;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;
import ch.post.it.evoting.cryptoprimitives.utils.VerificationResult;

class MultiExponentiationArgumentServiceTest extends TestGroupSetup {

	private static final int COMMITMENT_KEY_SIZE = 11;
	private static final ZqElement zqTwo = ZqElement.create(2, zqGroup);
	private static final ZqElement zqOne = ZqElement.create(1, zqGroup);

	private static MultiExponentiationArgumentService argumentService;
	private static TestMultiExponentiationStatementGenerator statementGenerator;
	private static TestMultiExponentiationWitnessGenerator witnessGenerator;
	private static ElGamalMultiRecipientPublicKey publicKey;
	private static CommitmentKey commitmentKey;
	private static TestMultiExponentiationStatementWitnessPairGenerator statementWitnessPairGenerator;
	private static TestMultiExponentiationArgumentGenerator argumentGenerator;
	private static HashService hashService;
	private static int publicKeySize;

	private int n;
	private int m;
	private int l;

	@BeforeAll
	static void setUpAll() {
		publicKeySize = randomService.genRandomInteger(10) + 1;
		publicKey = elGamalGenerator.genRandomPublicKey(publicKeySize);

		final TestCommitmentKeyGenerator commitmentKeyGenerator = new TestCommitmentKeyGenerator(gqGroup);
		commitmentKey = commitmentKeyGenerator.genCommitmentKey(COMMITMENT_KEY_SIZE);

		hashService = TestHashService.create(gqGroup.getQ());
		argumentService = new MultiExponentiationArgumentService(publicKey, commitmentKey, randomService, hashService);

		statementGenerator = new TestMultiExponentiationStatementGenerator(gqGroup);
		witnessGenerator = new TestMultiExponentiationWitnessGenerator(zqGroup);
		statementWitnessPairGenerator = new TestMultiExponentiationStatementWitnessPairGenerator(gqGroup, argumentService, commitmentKey);

		argumentGenerator = new TestMultiExponentiationArgumentGenerator(gqGroup);
	}

	@BeforeEach
	void setup() {
		n = randomService.genRandomInteger(COMMITMENT_KEY_SIZE - 1) + 1;
		m = randomService.genRandomInteger(COMMITMENT_KEY_SIZE - 1) + 1;
		l = randomService.genRandomInteger(publicKeySize) + 1;
	}

	private void assertThrowsIllegalArgumentExceptionWithMessage(final String errorMsg, final Executable executable) {
		final Exception exception = assertThrows(IllegalArgumentException.class, executable);
		assertEquals(errorMsg, exception.getMessage());
	}

	@Nested
	@DisplayName("getMultiExponentiationArgument...")
	class GetMultiExponentiationArgument {
		private MultiExponentiationStatement randomStatement;
		private MultiExponentiationWitness randomWitness;

		@BeforeEach
		void setup() {
			randomStatement = statementGenerator.genRandomStatement(n, m, l);
			randomWitness = witnessGenerator.genRandomWitness(n, m);
		}

		@Test
		void constructorDoesntAcceptNullValues() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> new MultiExponentiationArgumentService(null, commitmentKey, randomService, hashService)),
					() -> assertThrows(NullPointerException.class,
							() -> new MultiExponentiationArgumentService(publicKey, null, randomService, hashService)),
					() -> assertThrows(NullPointerException.class,
							() -> new MultiExponentiationArgumentService(publicKey, commitmentKey, null, hashService)),
					() -> assertThrows(NullPointerException.class,
							() -> new MultiExponentiationArgumentService(publicKey, commitmentKey, randomService, null))
			);
		}

		@Test
		void hashServiceWithTooLongHashLengthThrows() {
			final HashService otherHashService = HashService.getInstance();
			assertThrowsIllegalArgumentExceptionWithMessage("The hash service's bit length must be smaller than the bit length of q.",
					() -> new MultiExponentiationArgumentService(publicKey, commitmentKey, randomService, otherHashService));
		}

		@Test
		void publicKeyAndCommitmentKeyFromDifferentGroupsThrows() {
			final TestCommitmentKeyGenerator otherGenerator = new TestCommitmentKeyGenerator(otherGqGroup);
			final CommitmentKey otherKey = otherGenerator.genCommitmentKey(COMMITMENT_KEY_SIZE);
			assertThrowsIllegalArgumentExceptionWithMessage("The public key and commitment key must belong to the same group",
					() -> new MultiExponentiationArgumentService(publicKey, otherKey, randomService, hashService));
		}

		@Test
		void testStatementAndWitnessOfGroupsOfDifferentOrderThrows() {
			final TestMultiExponentiationWitnessGenerator otherGroupWitnessGenerator = new TestMultiExponentiationWitnessGenerator(otherZqGroup);
			final MultiExponentiationWitness otherWitness = otherGroupWitnessGenerator.genRandomWitness(n, m);
			assertThrowsIllegalArgumentExceptionWithMessage("The witness must belong to a ZqGroup of order q.",
					() -> argumentService.getMultiExponentiationArgument(randomStatement, otherWitness));
		}

		@Test
		void testStatementAndKeysOfDifferentOrderThrows() {
			final TestMultiExponentiationStatementGenerator otherStatementGenerator = new TestMultiExponentiationStatementGenerator(otherGqGroup);
			final MultiExponentiationStatement otherGroupStatement = otherStatementGenerator.genRandomStatement(n, m, l);
			assertThrowsIllegalArgumentExceptionWithMessage("The statement must belong to the same group as the public key and commitment key.",
					() -> argumentService.getMultiExponentiationArgument(otherGroupStatement, randomWitness));
		}

		@Test
		void testStatementAndWitnessWithDifferentMThrows() {
			final MultiExponentiationStatement statement = statementGenerator.genRandomStatement(n, m, l);
			final MultiExponentiationWitness witness = witnessGenerator.genRandomWitness(n, m + 1);
			assertThrowsIllegalArgumentExceptionWithMessage("Statement and witness do not have compatible m dimension.",
					() -> argumentService.getMultiExponentiationArgument(statement, witness));
		}

		@Test
		void testStatementAndWitnessWithDifferentNThrows() {
			final MultiExponentiationStatement statement = statementGenerator.genRandomStatement(n, m, l);
			final MultiExponentiationWitness witness = witnessGenerator.genRandomWitness(n + 1, m);
			assertThrowsIllegalArgumentExceptionWithMessage("Statement and witness do not have compatible n dimension.",
					() -> argumentService.getMultiExponentiationArgument(statement, witness));
		}

		@Test
		void testExponentsMatrixNSizeNotSmallerThanCommitmentKeySizeThrows() {
			final int tooLargeN = COMMITMENT_KEY_SIZE + 1;
			final MultiExponentiationStatement statement = statementGenerator.genRandomStatement(tooLargeN, m, l);
			final MultiExponentiationWitness witness = witnessGenerator.genRandomWitness(tooLargeN, m);
			assertThrowsIllegalArgumentExceptionWithMessage(
					"The number of rows of matrix A must be smaller or equal to the size of the commitment key.",
					() -> argumentService.getMultiExponentiationArgument(statement, witness));
		}

		@Test
		void testNullValuesThrows() {
			assertAll(
					() -> assertThrows(NullPointerException.class, () -> argumentService.getMultiExponentiationArgument(null, randomWitness)),
					() -> assertThrows(NullPointerException.class, () -> argumentService.getMultiExponentiationArgument(randomStatement, null))
			);
		}

		@Test
		void testCIsNotMultiExponentiationProductThrows() {
			final StatementWitnessPair statementWitnessPair = statementWitnessPairGenerator.genPair(n, m, l);
			final MultiExponentiationStatement statement = statementWitnessPair.statement();
			final MultiExponentiationWitness witness = statementWitnessPair.witness();

			final ElGamalMultiRecipientCiphertext computedC = statement.get_C();
			final ElGamalMultiRecipientCiphertext differentC = Generators.genWhile(
					() -> elGamalGenerator.genRandomCiphertext(l), ciphertext -> ciphertext.equals(computedC));
			final MultiExponentiationStatement statementWithInvalidC = new MultiExponentiationStatement(
					statement.get_C_matrix(), differentC, statement.get_c_A());

			assertThrowsIllegalArgumentExceptionWithMessage(
					"The computed multi exponentiation ciphertext does not correspond to the one provided in the statement.",
					() -> argumentService.getMultiExponentiationArgument(statementWithInvalidC, witness));
		}

		@Test
		void testCommitmentCAIsNotCommitmentOfMatrixAThrows() {
			final StatementWitnessPair statementWitnessPair = statementWitnessPairGenerator.genPair(n, m, l);
			final MultiExponentiationStatement statement = statementWitnessPair.statement();
			final MultiExponentiationWitness witness = statementWitnessPair.witness();

			final GroupVector<GqElement, GqGroup> computeCommitmentToA = statement.get_c_A();
			final GqElement firstElement = computeCommitmentToA.getFirst();
			final GqElement differentFirstElement = Generators.genWhile(gqGroupGenerator::genMember, element -> element.equals(firstElement));

			final GroupVector<GqElement, GqGroup> differentCommitmentToA =
					Stream.concat(
							Stream.of(differentFirstElement),
							computeCommitmentToA
									.stream()
									.skip(1)
					).collect(toGroupVector());
			final MultiExponentiationStatement invalidStatement = new MultiExponentiationStatement(
					statement.get_C_matrix(), statement.get_C(), differentCommitmentToA);

			assertThrowsIllegalArgumentExceptionWithMessage("The commitment provided does not correspond to the matrix A.",
					() -> argumentService.getMultiExponentiationArgument(invalidStatement, witness));
		}

		@Test
		void sanityCheck() {
			final MultiExponentiationArgumentService argumentServiceForSanityCheck = new MultiExponentiationArgumentService(
					publicKey, commitmentKey, randomService, hashService);
			final StatementWitnessPair pair = statementWitnessPairGenerator.genPair(n, m, l);
			final MultiExponentiationStatement statement = pair.statement();
			final MultiExponentiationWitness witness = pair.witness();
			assertDoesNotThrow(() -> argumentServiceForSanityCheck.getMultiExponentiationArgument(statement, witness));
		}

		@Test
		void testThatLongerCiphertextsThanKeyThrows() {
			final int longerCiphertext_l = COMMITMENT_KEY_SIZE + 1;
			final MultiExponentiationStatement statement = statementGenerator.genRandomStatement(n, m, longerCiphertext_l);
			final MultiExponentiationWitness witness = witnessGenerator.genRandomWitness(n, m);
			assertThrowsIllegalArgumentExceptionWithMessage("The ciphertexts must be smaller than the public key.",
					() -> argumentService.getMultiExponentiationArgument(statement, witness));
		}
	}

	@Nested
	@DisplayName("verifyMultiExponentiationArgument...")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class VerifyMultiExponentiationArgument {
		private MultiExponentiationArgument randomArgument;
		private MultiExponentiationArgument validArgument;
		private MultiExponentiationStatement validStatement;
		private MultiExponentiationArgument.Builder argumentBuilder;

		@BeforeEach
		void setup() {
			randomArgument = argumentGenerator.genRandomArgument(n, m, l);
			final StatementWitnessPair statementWitnessPair = statementWitnessPairGenerator.genPair(n, m, l);
			validStatement = statementWitnessPair.statement();
			validArgument = argumentService.getMultiExponentiationArgument(validStatement, statementWitnessPair.witness());
			argumentBuilder = new MultiExponentiationArgument.Builder()
					.with_c_A_0(validArgument.getc_A_0())
					.with_c_B(validArgument.get_c_B())
					.with_E(validArgument.get_E())
					.with_a(validArgument.get_a())
					.with_r(validArgument.get_r())
					.with_b(validArgument.get_b())
					.with_s(validArgument.get_s())
					.with_tau(validArgument.get_tau());
		}

		@Test
		void testValidGeneratedValues() {
			final VerificationResult verificationResult = argumentService.verifyMultiExponentiationArgument(validStatement, validArgument).verify();
			assertTrue(verificationResult.isVerified());
		}

		@Test
		void testNullValuesThrows() {
			assertThrows(NullPointerException.class, () -> argumentService.verifyMultiExponentiationArgument(null, randomArgument));

			final MultiExponentiationStatement multiExponentiationStatement = statementGenerator.genRandomStatement(n, m, l);
			assertThrows(NullPointerException.class, () -> argumentService.verifyMultiExponentiationArgument(multiExponentiationStatement, null));
		}

		@Test
		void testStatmentAndArgumentFromDifferentGroupsThrows() {
			final MultiExponentiationStatement otherStatement = new TestMultiExponentiationStatementGenerator(otherGqGroup).genRandomStatement(n, m,
					l);
			assertThrowsIllegalArgumentExceptionWithMessage("Statement and argument must belong to the same group.",
					() -> argumentService.verifyMultiExponentiationArgument(otherStatement, randomArgument));
		}

		@Test
		void testStatementAndArgumentWithDifferentNThrows() {
			final MultiExponentiationStatement otherStatement = statementGenerator.genRandomStatement(n + 1, m, l);
			assertThrowsIllegalArgumentExceptionWithMessage("n dimension doesn't match.",
					() -> argumentService.verifyMultiExponentiationArgument(otherStatement, randomArgument));
		}

		@Test
		void testStatementAndArgumentWithDifferentMThrows() {
			final MultiExponentiationStatement otherStatement = statementGenerator.genRandomStatement(n, m + 1, l);
			assertThrowsIllegalArgumentExceptionWithMessage("m dimension doesn't match.",
					() -> argumentService.verifyMultiExponentiationArgument(otherStatement, randomArgument));
		}

		@Test
		void testStatementAndArgumentWithDifferentLThrows() {
			final MultiExponentiationStatement otherStatement = statementGenerator.genRandomStatement(n, m, l + 1);
			assertThrowsIllegalArgumentExceptionWithMessage("l dimension doesn't match.",
					() -> argumentService.verifyMultiExponentiationArgument(otherStatement, randomArgument));
		}

		@Test
		void testArgumentGenerationAndVerificationIsVerified() {
			final StatementWitnessPair pair = statementWitnessPairGenerator.genPair(n, m, l);
			final MultiExponentiationStatement statement = pair.statement();
			final MultiExponentiationWitness witness = pair.witness();
			final MultiExponentiationArgument argument = argumentService.getMultiExponentiationArgument(statement, witness);
			assertTrue(argumentService.verifyMultiExponentiationArgument(statement, argument).verify().isVerified());
		}

		@Test
		@SuppressWarnings("java:S117")
		void testStatementWithModified_C_ElementDoesNotVerify() {
			final GqGroup g29 = GroupTestData.getGroupP59();
			final GqElement gqFour = GqElementFactory.fromValue(BigInteger.valueOf(4), g29);
			final GqElement gqFive = GqElementFactory.fromValue(BigInteger.valueOf(5), g29);
			final GqElement gqTwelve = GqElementFactory.fromValue(BigInteger.valueOf(12), g29);
			final GqElement gqSeventeen = GqElementFactory.fromValue(BigInteger.valueOf(17), g29);
			final GqElement gqFiftyOne = GqElementFactory.fromValue(BigInteger.valueOf(51), g29);
			final ZqGroup z29 = ZqGroup.sameOrderAs(g29);
			final ZqElement two = ZqElement.create(2, z29);
			final ZqElement three = ZqElement.create(3, z29);
			final ZqElement four = ZqElement.create(4, z29);
			final GroupMatrix<ZqElement, ZqGroup> AMatrix = GroupMatrix.fromColumns(
					GroupVector.of(
							GroupVector.of(two, three),
							GroupVector.of(three, four)
					)
			);
			final GroupVector<ZqElement, ZqGroup> rExponents = GroupVector.of(four, ZqElement.create(11, z29));
			final ZqElement rhoExponents = ZqElement.create(23, z29);
			final MultiExponentiationWitness witness = new MultiExponentiationWitness(AMatrix, rExponents, rhoExponents);

			final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> CMatrix = GroupMatrix.fromRows(
					GroupVector.of(
							GroupVector.of(
									ElGamalMultiRecipientCiphertext.create(gqFour, GroupVector.of(gqTwelve, gqFive)),
									ElGamalMultiRecipientCiphertext.create(gqFiftyOne, GroupVector.of(gqFive, gqFour))
							),
							GroupVector.of(
									ElGamalMultiRecipientCiphertext.create(gqSeventeen, GroupVector.of(gqTwelve, gqTwelve)),
									ElGamalMultiRecipientCiphertext.create(gqFiftyOne, GroupVector.of(gqFive, gqSeventeen))
							))
			);

			final ElGamalMultiRecipientPublicKey pk = new ElGamalMultiRecipientPublicKey(GroupVector.of(gqFiftyOne, gqFive));
			final CommitmentKey ck = new CommitmentKey(gqFive, GroupVector.of(gqTwelve, gqSeventeen));
			final HashService testHashService = TestHashService.create(g29.getQ());
			final MultiExponentiationArgumentService testArgumentService = new MultiExponentiationArgumentService(pk, ck, randomService,
					testHashService);
			final ElGamalMultiRecipientCiphertext computedC = testArgumentService.multiExponentiation(CMatrix, AMatrix, rhoExponents, 2);
			final GroupVector<GqElement, GqGroup> commitmentToA = CommitmentService.getCommitmentMatrix(AMatrix, rExponents, ck);
			final MultiExponentiationStatement statement = new MultiExponentiationStatement(CMatrix, computedC, commitmentToA);
			final MultiExponentiationArgument argument = testArgumentService.getMultiExponentiationArgument(statement, witness);
			final VerificationResult verificationResult = testArgumentService.verifyMultiExponentiationArgument(statement, argument).verify();
			assertTrue(verificationResult.isVerified());

			final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> modifiedCMatrix = GroupMatrix.fromRows(
					statement.get_C_matrix().rowStream()
							.map(r -> r.stream().map(c -> c.getCiphertextExponentiation(two)).collect(toGroupVector()))
							.collect(toGroupVector()));
			final MultiExponentiationStatement modifiedStatement = new MultiExponentiationStatement(
					modifiedCMatrix,
					statement.get_C(),
					statement.get_c_A()
			);
			final VerificationResult verificationResultModified = testArgumentService.verifyMultiExponentiationArgument(modifiedStatement, argument)
					.verify();
			assertFalse(verificationResultModified.isVerified());
		}

		@Test
		void testStatementWithModified_C_DoesNotVerify() {
			final ElGamalMultiRecipientCiphertext modifiedC = ElGamalMultiRecipientCiphertext.create(
					validStatement.get_C().getGamma(),
					validStatement.get_C().stream().skip(1).map(gqGroupGenerator::otherElement).collect(toGroupVector()));
			final MultiExponentiationStatement modifiedStatement = new MultiExponentiationStatement(
					validStatement.get_C_matrix(),
					modifiedC,
					validStatement.get_c_A()
			);
			final VerificationResult verificationResult = argumentService.verifyMultiExponentiationArgument(modifiedStatement, validArgument)
					.verify();
			assertFalse(verificationResult.isVerified());
			assertEquals("E_m must equal C.", verificationResult.getErrorMessages().get(0));
		}

		@Test
		void testStatementWithModified_cA_ElementDoesNotVerify() {
			//Need large group, because in small group the probability of collision is very high and hence the probability of false positive is high
			final GqGroup largeGqGroup = GroupTestData.getLargeGqGroup();
			final GqGroupGenerator localGqGroupGenerator = new GqGroupGenerator(largeGqGroup);

			final ElGamalGenerator localElGamalGenerator = new ElGamalGenerator(largeGqGroup);
			final ElGamalMultiRecipientPublicKey localPublicKey = localElGamalGenerator.genRandomPublicKey(publicKeySize);

			final TestCommitmentKeyGenerator commitmentKeyGenerator = new TestCommitmentKeyGenerator(largeGqGroup);
			final CommitmentKey localCommitmentKey = commitmentKeyGenerator.genCommitmentKey(COMMITMENT_KEY_SIZE);
			final HashService localHashService = HashService.getInstance();
			final MultiExponentiationArgumentService multiExponentiationArgumentService =
					new MultiExponentiationArgumentService(localPublicKey, localCommitmentKey, randomService, localHashService);

			final MultiExponentiationArgumentService localArgumentService = new MultiExponentiationArgumentService(localPublicKey,
					localCommitmentKey, randomService, localHashService);

			final TestMultiExponentiationStatementWitnessPairGenerator localStatementWitnessPairGenerator = new TestMultiExponentiationStatementWitnessPairGenerator(
					largeGqGroup, multiExponentiationArgumentService, localCommitmentKey);

			final StatementWitnessPair localStatementWitnessPair = localStatementWitnessPairGenerator.genPair(n, m, l);
			final MultiExponentiationStatement localValidStatement = localStatementWitnessPair.statement();
			final MultiExponentiationArgument localValidArgument = localArgumentService.getMultiExponentiationArgument(localValidStatement,
					localStatementWitnessPair.witness());

			final GroupVector<GqElement, GqGroup> modifiedCommitmentA = localValidStatement.get_c_A().stream()
					.map(localGqGroupGenerator::otherElement)
					.collect(toGroupVector());
			final MultiExponentiationStatement modifiedStatement = new MultiExponentiationStatement(localValidStatement.get_C_matrix(),
					localValidStatement.get_C(), modifiedCommitmentA);
			final VerificationResult verificationResult = localArgumentService
					.verifyMultiExponentiationArgument(modifiedStatement, localValidArgument).verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		@SuppressWarnings("java:S117")
		void testArgumentWithModified_cA0_ElementDoesNotVerify() {
			final GqElement modifiedC_A_0 = gqGroupGenerator.otherElement(validArgument.getc_A_0());
			argumentBuilder.with_c_A_0(modifiedC_A_0);
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_cB_ElementDoesNotVerify() {
			argumentBuilder.with_c_B(
					validArgument.get_c_B().stream().map(gqGroupGenerator::otherElement).collect(toGroupVector()));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_E_ElementDoesNotVerify() {
			argumentBuilder.with_E(
					validArgument.get_E().stream().map(e -> e.getCiphertextExponentiation(zqTwo)).collect(toGroupVector()));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_a_ElementDoesNotVerify() {
			argumentBuilder.with_a(validArgument.get_a().stream().map(e -> e.add(zqOne)).collect(toGroupVector()));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_r_DoesNotVerify() {
			argumentBuilder.with_r(validArgument.get_r().add(zqOne));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_b_ElementDoesNotVerify() {
			argumentBuilder.with_b(validArgument.get_b().add(zqOne));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_s_DoesNotVerify() {
			argumentBuilder.with_s(validArgument.get_s().add(zqOne));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@Test
		void testArgumentWithModified_tau_DoesNotVerify() {
			argumentBuilder.with_tau(validArgument.get_tau().add(zqOne));
			final VerificationResult verificationResult = argumentService
					.verifyMultiExponentiationArgument(validStatement, argumentBuilder.build())
					.verify();
			assertFalse(verificationResult.isVerified());
		}

		@ParameterizedTest(name = "{5}")
		@MethodSource("verifyMultiExponentiationArgumentRealValueProvider")
		@DisplayName("with real values gives expected result")
		void verifyRealValues(final ElGamalMultiRecipientPublicKey publicKey, final CommitmentKey commitmentKey,
				final MultiExponentiationStatement statement, final MultiExponentiationArgument argument, final boolean expectedOutput,
				final String description) {

			final HashService realHashService = HashService.getInstance();

			final MultiExponentiationArgumentService service = new MultiExponentiationArgumentService(publicKey, commitmentKey, randomService,
					realHashService);

			assertEquals(expectedOutput, service.verifyMultiExponentiationArgument(statement, argument).verify().isVerified(),
					String.format("assertion failed for: %s", description));
		}

		Stream<Arguments> verifyMultiExponentiationArgumentRealValueProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/mixnet/verify-multiexp-argument.json");

			return parametersList.stream().parallel().map(testParameters -> {
				// TestContextParser.
				final JsonData contextData = testParameters.getContext();
				final TestContextParser context = new TestContextParser(contextData);
				final GqGroup realGqGroup = context.getGqGroup();

				final ElGamalMultiRecipientPublicKey realPublicKey = context.parsePublicKey();
				final CommitmentKey realCommitmentKey = context.parseCommitmentKey();

				// Inputs.
				final JsonData input = testParameters.getInput();
				final JsonData statement = input.getJsonData("statement");
				final TestArgumentParser testArgumentParser = new TestArgumentParser(realGqGroup);

				final MultiExponentiationArgument multiExpArgument = testArgumentParser
						.parseMultiExponentiationArgument(input.getJsonData("argument"));
				final MultiExponentiationStatement multiExpStatement = parseMultiExpStatement(realGqGroup, statement, testArgumentParser);

				// Output.
				final JsonData output = testParameters.getOutput();
				final boolean outputValue = Boolean.parseBoolean(output.getJsonData("result").toString());

				return Arguments.of(realPublicKey, realCommitmentKey, multiExpStatement, multiExpArgument, outputValue,
						testParameters.getDescription());
			});
		}

		private MultiExponentiationStatement parseMultiExpStatement(final GqGroup realGqGroup, final JsonData statement,
				final TestArgumentParser testArgumentParser) {

			final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> ciphertextMatrix = testArgumentParser
					.parseCiphertextMatrix(statement.getJsonData("ciphertexts"));

			final ElGamalMultiRecipientCiphertext ciphertextC = testArgumentParser.parseCiphertext(statement.getJsonData("ciphertext_product"));

			final BigInteger[] commitmentAValues = statement.get("c_a", BigInteger[].class);
			final GroupVector<GqElement, GqGroup> commitmentA = Arrays.stream(commitmentAValues)
					.map(bi -> GqElementFactory.fromValue(bi, realGqGroup))
					.collect(toGroupVector());

			return new MultiExponentiationStatement(ciphertextMatrix, ciphertextC, commitmentA);
		}

	}
}
