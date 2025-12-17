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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.byteLength;
import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base16Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.UsabilityBase32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

@DisplayName("RandomService calling")
class RandomServiceTest {

	private final SecureRandom secureRandom = new SecureRandom();
	private final RandomService randomService = new RandomService();

	/**
	 * Tests the genRandomInteger methods.
	 * <p>
	 * The tests run for genRandomInteger are:
	 * <ul>
	 *     <li>null argument throws a {@link NullPointerException}</li>
	 *     <li>upperBound < 1 throws an {@link IllegalArgumentException}</li>
	 *     <li>valid upperBounds stays within given bounds</li>
	 *     <li>upperBound = 1 returns 0</li>
	 *     <li>the BigInteger and int methods are equivalent</li>
	 *     <li>specific input provides expected output - the SecureRandom is mocked for this test</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("genRandomInteger with")
	class GenRandomIntegerTest {

		@Test
		@DisplayName("null argument throws a NullPointerException")
		void genRandomIntegerNullArgumentThrows() {
			assertThrows(NullPointerException.class, () -> randomService.genRandomInteger(null));
		}

		@Test
		@DisplayName("invalid BigInteger upperBounds throws an IllegalArgumentException")
		void genRandomIntegerBigIntegerInvalidUpperBounds() {
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(BigInteger.ZERO));
			final BigInteger minusOne = BigInteger.ONE.negate();
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(minusOne));
		}

		@Test
		@DisplayName("invalid int upperBounds throws an IllegalArgumentException")
		void genRandomIntegerIntInvalidUpperBounds() {
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(0));
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(-1));
		}

		@RepeatedTest(1000)
		@DisplayName("valid BigInteger arguments gives valid outputs")
		void genRandomIntegerBigIntegerValidArguments() {
			final BigInteger upperBound = BigInteger.valueOf(100);
			final BigInteger randomInteger = randomService.genRandomInteger(upperBound);

			assertTrue(randomInteger.compareTo(upperBound) < 0);
			assertTrue(randomInteger.signum() >= 0);
		}

		@RepeatedTest(1000)
		@DisplayName("valid int arguments gives valid outputs")
		void genRandomIntegerIntValidArguments() {
			final int upperBound = 100;
			final int randomInteger = randomService.genRandomInteger(upperBound);

			assertTrue(randomInteger < upperBound);
			assertTrue(0 <= randomInteger);
		}

		@Test
		@DisplayName("upperBound=BigInteger.ONE returns BigInteger.ZERO")
		void genRandomIntegerBigIntegerUpperBoundOne() {
			final BigInteger upperBound = BigInteger.ONE;
			final BigInteger expected = BigInteger.ZERO;

			assertEquals(expected, randomService.genRandomInteger(upperBound));
		}

		@Test
		@DisplayName("upperBound=1 returns 0")
		void genRandomIntegerIntUpperBoundOne() {
			assertEquals(0, randomService.genRandomInteger(1));
		}

		@Test
		@DisplayName("equivalent BigInteger and int inputs returns equivalent BigInteger and int outputs")
		void genRandomIntegerAreEquivalent() {
			final BigInteger upperBound = BigInteger.valueOf(1_000_000);
			final ImmutableList<ImmutableByteArray> randomBytesList = ImmutableList.of(randomService.randomBytes(byteLength(upperBound)),
					randomService.randomBytes(byteLength(upperBound)), randomService.randomBytes(byteLength(upperBound)));
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(randomBytesList))) {
				final SecureRandom secureRandom1 = new SecureRandom();
				final RandomService randomService1 = new RandomService(secureRandom1);
				final int result = randomService1.genRandomInteger(upperBound.intValueExact());

				final SecureRandom secureRandom2 = new SecureRandom();
				final RandomService randomService2 = new RandomService(secureRandom2);
				final int expectedResult = randomService2.genRandomInteger(upperBound).intValueExact();

				assertEquals(expectedResult, result);
				assertEquals(2, mockedSecureRandom.constructed().size());
			}
		}

		static Stream<Arguments> provideGenRandomIntegerArguments() {
			final BigInteger upperBound1 = BigInteger.valueOf(256);
			final BigInteger upperBound2 = BigInteger.valueOf(257);
			final BigInteger upperBound3 = BigInteger.TWO.pow(256); // 32 bytes
			final byte[] expectedByteArray3 = { -24, -14, 41, 72, -118, 51, -34, -1, 21, -83, 87, -35, 73, 3, -47, 98, 95, 64, 14, -34, 88, -49, 11,
					-49, 57, 6, 56, 14, 79, -118, 37, 118 };
			final BigInteger expected3 = new BigInteger(1, expectedByteArray3);

			final BigInteger upperBound4 = BigInteger.TWO.pow(32).multiply(BigInteger.TEN); // 5 bytes
			final BigInteger expected4 = new BigInteger(1, new byte[] { 1, 45, -16, -121, 99 });
			return Stream.of(Arguments.of(ImmutableList.of(ImmutableByteArray.of(new byte[] { 3 })), upperBound1, BigInteger.valueOf(3)),
					Arguments.of(ImmutableList.of(ImmutableByteArray.of(new byte[] { 15, -128 }), ImmutableByteArray.of(new byte[] { 0, -120 })),
							upperBound2, BigInteger.valueOf(136)),
					Arguments.of(ImmutableList.of(ImmutableByteArray.of(expectedByteArray3)), upperBound3, expected3), Arguments.of(
							ImmutableList.of(ImmutableByteArray.of(new byte[] { -1, -128, -128, -128, -128 }),
									ImmutableByteArray.of(new byte[] { 15, -128, -64, 100, 0 }),
									ImmutableByteArray.of(new byte[] { 65, 45, -16, -121, 99 })), upperBound4, expected4));
		}

		@ParameterizedTest
		@MethodSource("provideGenRandomIntegerArguments")
		@DisplayName("specific input returns expected output")
		void genRandomIntegerBigIntegerExpectedValue(final ImmutableList<ImmutableByteArray> randomBytesList, final BigInteger upperBound,
				final BigInteger expectedResult) {
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(randomBytesList))) {
				final SecureRandom secureRandom2 = new SecureRandom();
				final RandomService randomService2 = new RandomService(secureRandom2);
				final BigInteger result = randomService2.genRandomInteger(upperBound);

				assertEquals(expectedResult, result);
				assertEquals(1, mockedSecureRandom.constructed().size());
			}
		}
	}

	/**
	 * Tests the genRandomVector method.
	 * <p>
	 * The tests run for genRandomVector are:
	 * <ul>
	 *     <li>null argument throws a {@link NullPointerException}</li>
	 *     <li>invalid arguments throws an {@link IllegalArgumentException}</li>
	 *     <li>valid arguments returns valid output</li>
	 *     <li>length = 0 returns an empty {@link GroupVector}</li>
	 *     <li>specific input provides expected output - the SecureRandom is mocked for this test</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("GenRandomVector with")
	class GenRandomVectorTest {

		@Test
		@DisplayName("null argument throws a NullPointerException")
		void genRandomVectorNullArgument() {
			assertThrows(NullPointerException.class, () -> randomService.genRandomVector(null, 1));
		}

		@Test
		@DisplayName("invalid arguments throws an IllegalArgumentException")
		void checkGenRandomVectorInvalidArguments() {
			final BigInteger minusOne = BigInteger.ONE.negate();
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(minusOne, 1));
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.ZERO, 1));
			assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.ONE, -1));
		}

		@RepeatedTest(100)
		@DisplayName("valid arguments returns valid output")
		void genRandomVectorValidArguments() {
			final BigInteger upperBound = BigInteger.valueOf(100);
			final int length = 20;
			final GroupVector<ZqElement, ZqGroup> randomVector = randomService.genRandomVector(upperBound, length);

			assertEquals(length, randomVector.size());
			assertEquals(0, (int) randomVector.stream().filter(zq -> zq.getValue().compareTo(upperBound) >= 0).count());
			assertEquals(1, randomVector.stream().map(ZqElement::getGroup).distinct().count());
		}

		@Test
		@DisplayName("zero length returns an empty vector")
		void genRandomVectorZeroLength() {
			final GroupVector<ZqElement, ZqGroup> randomVector = randomService.genRandomVector(BigInteger.TWO, 0);

			assertTrue(randomVector.isEmpty());
		}

		static Stream<Arguments> provideGenRandomVectorArguments() {
			final BigInteger upperBound1 = BigInteger.valueOf(256);

			final BigInteger upperBound2 = BigInteger.valueOf(257);
			final ImmutableList<ImmutableByteArray> byteArrayList2 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 15, -128 }),
					ImmutableByteArray.of(new byte[] { 0, -120 }),
					ImmutableByteArray.of(new byte[] { 1, 0 }));
			final ZqGroup zqGroup2 = new ZqGroup(upperBound2);
			final GroupVector<ZqElement, ZqGroup> expected2 = GroupVector.of(ZqElement.create(136, zqGroup2), ZqElement.create(256, zqGroup2));

			final BigInteger upperBound3 = BigInteger.TWO.pow(256); // 32 bytes
			final ZqGroup zqGroup3 = new ZqGroup(upperBound3);
			final byte[] expectedByteArray31 = { -24, -14, 41, 72, -118, 51, -34, -1, 21, -83, 87, -35, 73, 3, -47, 98, 95, 64, 14, -34, 88, -49, 11,
					-49, 57, 6, 56, 14, 79, -118, 37, 118 };
			final ZqElement expected31 = ZqElement.create(new BigInteger(1, expectedByteArray31), zqGroup3);
			final byte[] expectedByteArray32 = { 16, -14, 41, -72, 0, 51, -34, -1, 21, -83, 87, -35, 73, 19, -47, -87, 95, 64, 14, 49, 88, -49, 38,
					-9, 57, 2, 59, 14, 79, 118, 73, -118 };
			final ZqElement expected32 = ZqElement.create(new BigInteger(1, expectedByteArray32), zqGroup3);
			final byte[] expectedByteArray33 = { -13, -14, 14, 27, 118, -51, -78, 1, 22, -90, 83, -45, 72, 1, -48, 18, 99, -104, 14, -33, 82, -119, 9,
					-49, 50, 16, 58, 13, 97, -110, 87, 112 };
			final ZqElement expected33 = ZqElement.create(new BigInteger(1, expectedByteArray33), zqGroup3);

			final BigInteger upperBound4 = BigInteger.TWO.pow(32).multiply(BigInteger.TEN); // 5 bytes
			final ZqGroup zqGroup4 = new ZqGroup(upperBound4);
			final ZqElement expected41 = ZqElement.create(new BigInteger(1, new byte[] { 1, 45, -16, -121, 99 }), zqGroup4);
			final ZqElement expected42 = ZqElement.create(new BigInteger(1, new byte[] { 9, -44, 123, -17, 2 }), zqGroup4);
			return Stream.of(
					Arguments.of(ImmutableList.of(ImmutableByteArray.of(new byte[] { 3 })), upperBound1, 1,
							GroupVector.of(ZqElement.create(3, new ZqGroup(upperBound1)))),

					Arguments.of(byteArrayList2, upperBound2, 2, expected2),

					Arguments.of(ImmutableList.of(ImmutableByteArray.of(expectedByteArray31), ImmutableByteArray.of(expectedByteArray32),
									ImmutableByteArray.of(expectedByteArray33)),
							upperBound3, 3, GroupVector.of(expected31, expected32, expected33)),

					Arguments.of(ImmutableList.of(ImmutableByteArray.of(new byte[] { -1, -128, -128, -128, -128 }),
							ImmutableByteArray.of(new byte[] { 15, -128, -64, 100, 0 }),
							ImmutableByteArray.of(new byte[] { 65, 45, -16, -121, 99 }),
							ImmutableByteArray.of(new byte[] { 76, 0, 7, 15, 31 }),
							ImmutableByteArray.of(new byte[] { -119, -44, 123, -17, 2 })), upperBound4, 2, GroupVector.of(expected41, expected42))
			);
		}

		@ParameterizedTest
		@MethodSource("provideGenRandomVectorArguments")
		@DisplayName("specific values returns expected output")
		void genRandomVectorExpectedValue(final ImmutableList<ImmutableByteArray> randomBytesList, final BigInteger upperBound, final int length,
				final GroupVector<ZqElement, ZqGroup> expectedResult) {
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(randomBytesList))) {
				final SecureRandom secureRandom2 = new SecureRandom();
				final RandomService randomService2 = new RandomService(secureRandom2);
				final GroupVector<ZqElement, ZqGroup> result = randomService2.genRandomVector(upperBound, length);

				assertEquals(expectedResult, result);
				assertEquals(1, mockedSecureRandom.constructed().size());
			}
		}
	}

	/**
	 * Tests the genRandomString method.
	 * <p>
	 * The tests run for genRandomString are:
	 * <ul>
	 *     <li>null alphabet throws a {@link NullPointerException}</li>
	 *     <li>negative length throws an {@link IllegalArgumentException}</li>
	 *     <li>length = 0 returns an empty {@link String}</li>
	 *     <li>valid arguments returns valid output: string has specified length and is in specified alphabet</li>
	 *     <li>specific input provides expected output - the SecureRandom is mocked for this test</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("genRandomString with")
	class GenRandomStringTest {

		private static final Alphabet alphabet = UsabilityBase32Alphabet.getInstance();
		private static final int LENGTH = alphabet.size();

		@Test
		@DisplayName("a null alphabet throws a NullPointerException")
		void genRandomStringNullAlphabet() {
			assertThrows(NullPointerException.class, () -> randomService.genRandomString(LENGTH, null));
		}

		@Test
		@DisplayName("an invalid length throws an IllegalArgumentException")
		void genRandomStringInvalidLength() {
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> randomService.genRandomString(-2, alphabet));

			assertEquals(String.format("The desired length of string must be greater than or equal to 0. [length: %s]", -2),
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}

		@Test
		@DisplayName("a zero length does not throw an IllegalArgumentException")
		void genRandomStringZeroLength() {
			final String result = assertDoesNotThrow(() -> randomService.genRandomString(0, alphabet));
			assertEquals("", result);
		}

		@RepeatedTest(100)
		@DisplayName("different valid lengths behaves as expected")
		void genRandomStringValidInputDifferentLength() {

			final int length = secureRandom.nextInt(1, 10000);

			final String S_prime = assertDoesNotThrow(() -> randomService.genRandomString(length, alphabet));

			// S_prime must have length l.
			assertEquals(length, S_prime.length());

			// each element of S_prime must be part of the Alphabet.
			final char[] chars = S_prime.toCharArray();
			for (final char S_prime_i_char : chars) {
				final String S_prime_i = String.valueOf(S_prime_i_char);
				assertTrue(alphabet.contains(S_prime_i));
			}
		}

		static Stream<Alphabet> provideAlphabets() {
			return Stream.of(Base10Alphabet.getInstance(), Base16Alphabet.getInstance(), Base32Alphabet.getInstance(),
					UsabilityBase32Alphabet.getInstance(), Base64Alphabet.getInstance());
		}

		@ParameterizedTest
		@MethodSource("provideAlphabets")
		@DisplayName("different alphabets behaves as expected")
		void genRandomStringValidInputDifferentAlphabets(final Alphabet alphabet) {

			final int length = 100;
			final String S_prime = assertDoesNotThrow(() -> randomService.genRandomString(length, alphabet));

			// S_prime must have length l.
			assertEquals(length, S_prime.length());

			// each element of S_prime must be part of the Alphabet.
			final char[] chars = S_prime.toCharArray();
			for (final char S_prime_i_char : chars) {
				final String S_prime_i = String.valueOf(S_prime_i_char);
				assertTrue(alphabet.contains(S_prime_i));
			}
		}

		static Stream<Arguments> provideGenRandomStringArguments() {
			final ImmutableList<Object> randomBytesList1 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 1 }),
					ImmutableByteArray.of(new byte[] { 9 }), ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 2 }));

			final ImmutableList<Object> randomBytesList2 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 8 }),
					ImmutableByteArray.of(new byte[] { 7 }), ImmutableByteArray.of(new byte[] { 98 }), ImmutableByteArray.of(new byte[] { 53 }),
					ImmutableByteArray.of(new byte[] { 17 }), ImmutableByteArray.of(new byte[] { -93 }), ImmutableByteArray.of(new byte[] { -124 }),
					ImmutableByteArray.of(new byte[] { -8 }));

			final ImmutableList<Object> randomBytesList3 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 10 }),
					ImmutableByteArray.of(new byte[] { -14 }));

			final ImmutableList<Object> randomBytesList4 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 80 }),
					ImmutableByteArray.of(new byte[] { 7 }), ImmutableByteArray.of(new byte[] { 10 }), ImmutableByteArray.of(new byte[] { -118 }),
					ImmutableByteArray.of(new byte[] { 19 }), ImmutableByteArray.of(new byte[] { 6 }), ImmutableByteArray.of(new byte[] { -60 }),
					ImmutableByteArray.of(new byte[] { 12 }), ImmutableByteArray.of(new byte[] { 13 }), ImmutableByteArray.of(new byte[] { -15 }));

			final ImmutableList<Object> randomBytesList5 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 71 }),
					ImmutableByteArray.of(new byte[] { 118 }), ImmutableByteArray.of(new byte[] { -31 }), ImmutableByteArray.of(new byte[] { 26 }),
					ImmutableByteArray.of(new byte[] { 8 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 0 }),
					ImmutableByteArray.of(new byte[] { -122 }), ImmutableByteArray.of(new byte[] { 21 }), ImmutableByteArray.of(new byte[] { 24 }),
					ImmutableByteArray.of(new byte[] { 15 }), ImmutableByteArray.of(new byte[] { 94 }), ImmutableByteArray.of(new byte[] { -1 }),
					ImmutableByteArray.of(new byte[] { 9 }), ImmutableByteArray.of(new byte[] { 50 }), ImmutableByteArray.of(new byte[] { 5 }));

			final ImmutableList<Object> randomBytesList6 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 1 }),
					ImmutableByteArray.of(new byte[] { 31 }), ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 0 }),
					ImmutableByteArray.of(new byte[] { 27 }), ImmutableByteArray.of(new byte[] { 30 }), ImmutableByteArray.of(new byte[] { 28 }),
					ImmutableByteArray.of(new byte[] { 2 }), ImmutableByteArray.of(new byte[] { 3 }), ImmutableByteArray.of(new byte[] { 10 }),
					ImmutableByteArray.of(new byte[] { 16 }), ImmutableByteArray.of(new byte[] { 9 }), ImmutableByteArray.of(new byte[] { 15 }),
					ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 11 }),
					ImmutableByteArray.of(new byte[] { 26 }), ImmutableByteArray.of(new byte[] { 1 }), ImmutableByteArray.of(new byte[] { 23 }),
					ImmutableByteArray.of(new byte[] { 13 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 31 }),
					ImmutableByteArray.of(new byte[] { 28 }), ImmutableByteArray.of(new byte[] { 12 }), ImmutableByteArray.of(new byte[] { 13 }),
					ImmutableByteArray.of(new byte[] { 25 }), ImmutableByteArray.of(new byte[] { 17 }), ImmutableByteArray.of(new byte[] { 29 }),
					ImmutableByteArray.of(new byte[] { 11 }), ImmutableByteArray.of(new byte[] { 2 }), ImmutableByteArray.of(new byte[] { 30 }),
					ImmutableByteArray.of(new byte[] { 23 }));

			final ImmutableList<Object> randomBytesList7 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 71 }),
					ImmutableByteArray.of(new byte[] { 118 }), ImmutableByteArray.of(new byte[] { -31 }), ImmutableByteArray.of(new byte[] { 26 }),
					ImmutableByteArray.of(new byte[] { 8 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 0 }),
					ImmutableByteArray.of(new byte[] { -122 }), ImmutableByteArray.of(new byte[] { 21 }), ImmutableByteArray.of(new byte[] { 24 }),
					ImmutableByteArray.of(new byte[] { 15 }), ImmutableByteArray.of(new byte[] { 94 }));

			final ImmutableList<Object> randomBytesList8 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 1 }),
					ImmutableByteArray.of(new byte[] { 31 }), ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 0 }),
					ImmutableByteArray.of(new byte[] { 27 }), ImmutableByteArray.of(new byte[] { 30 }), ImmutableByteArray.of(new byte[] { 28 }),
					ImmutableByteArray.of(new byte[] { 2 }), ImmutableByteArray.of(new byte[] { 3 }), ImmutableByteArray.of(new byte[] { 10 }),
					ImmutableByteArray.of(new byte[] { 16 }), ImmutableByteArray.of(new byte[] { 9 }), ImmutableByteArray.of(new byte[] { 15 }),
					ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 11 }),
					ImmutableByteArray.of(new byte[] { 26 }), ImmutableByteArray.of(new byte[] { 1 }), ImmutableByteArray.of(new byte[] { 23 }),
					ImmutableByteArray.of(new byte[] { 13 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 31 }),
					ImmutableByteArray.of(new byte[] { 28 }), ImmutableByteArray.of(new byte[] { 12 }));

			final ImmutableList<Object> randomBytesList9 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 1 }),
					ImmutableByteArray.of(new byte[] { 59 }), ImmutableByteArray.of(new byte[] { 63 }));

			final ImmutableList<Object> randomBytesList10 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 36 }),
					ImmutableByteArray.of(new byte[] { 72 }), ImmutableByteArray.of(new byte[] { 28 }), ImmutableByteArray.of(new byte[] { 62 }),
					ImmutableByteArray.of(new byte[] { -68 }), ImmutableByteArray.of(new byte[] { 56 }), ImmutableByteArray.of(new byte[] { -61 }),
					ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { 26 }));

			return Stream.of(Arguments.of(randomBytesList1, 4, Base10Alphabet.getInstance(), "1902"),
					Arguments.of(randomBytesList2, 8, Base10Alphabet.getInstance(), "87251348"),
					Arguments.of(randomBytesList3, 2, Base16Alphabet.getInstance(), "A2"),
					Arguments.of(randomBytesList4, 10, Base16Alphabet.getInstance(), "07AA364CD1"),
					Arguments.of(randomBytesList5, 16, Base32Alphabet.getInstance(), "HWB2ISAGVYP67JSF"),
					Arguments.of(randomBytesList6, 32, Base32Alphabet.getInstance(), "B7AA364CDKQJPASL2BXNS74MNZR5LC6X"),
					Arguments.of(randomBytesList7, 12, UsabilityBase32Alphabet.getInstance(), "hyb4iuagx2r8"),
					Arguments.of(randomBytesList8, 24, UsabilityBase32Alphabet.getInstance(), "b9aa586cdksjraum4bzpu96n"),
					Arguments.of(randomBytesList9, 3, Base64Alphabet.getInstance(), "B7/"),
					Arguments.of(randomBytesList10, 9, Base64Alphabet.getInstance(), "kIc+84DSa"));
		}

		@ParameterizedTest
		@MethodSource("provideGenRandomStringArguments")
		@DisplayName("specific values returns expected output")
		void genRandomStringExpectedOutput(final ImmutableList<ImmutableByteArray> randomBytesList, final int length, final Alphabet alphabet,
				final String expected) {
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(randomBytesList))) {
				final SecureRandom secureRandom1 = new SecureRandom();
				final RandomService randomService1 = new RandomService(secureRandom1);

				final String result = randomService1.genRandomString(length, alphabet);
				assertEquals(expected, result);
				assertEquals(1, mockedSecureRandom.constructed().size());
			}
		}

	}

	/**
	 * Tests the genUniqueDecimalStrings method.
	 * <p>
	 * The tests run for genUniqueDecimalStrings are:
	 * <ul>
	 *     <li>desiredCodeLength < 0 throws an {@link IllegalArgumentException}</li>
	 *     <li>numberOfUniqueCodes < 1 throws an {@link IllegalArgumentException}</li>
	 *     <li>numberOfUniqueCodes > 10<sup>desiredCodeLength</sup> throws an {@link IllegalArgumentException}</li>
	 *     <li>desiredCodeLength = 0 and numberOfCodes = 1 returns a vector with one empty string</li>
	 *     <li>valid arguments returns a vector of unique decimal strings of the desired length and size</li>
	 *     <li>specific input provides expected output - the SecureRandom is mocked for this test</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("genUniqueDecimalStrings with")
	class GenUniqueDecimalStringsTest {

		@Test
		@DisplayName("invalid desiredCodeLength throws an IllegalArgumentException")
		void genUniqueDecimalStringsInvalidDesiredCodeLength() {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> randomService.genUniqueDecimalStrings(-1, 1));
			assertEquals("The desired length of the unique codes must be greater than or equal to 0.", exception.getMessage());
		}

		@Test
		@DisplayName("to small numberOfUniqueCodes throws an IllegalArgumentException")
		void genUniqueDecimalStringsNumberOfUniqueCodesTooSmall() {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> randomService.genUniqueDecimalStrings(1, 0));
			assertEquals("The number of unique codes must be strictly positive.", exception.getMessage());
		}

		@RepeatedTest(10)
		@DisplayName("too big numberOfUniqueCodes throws an IllegalArgumentException")
		void genUniqueDecimalStringsNumberOfUniqueCodesTooBig() {
			final int desiredCodesLength = randomService.genRandomInteger(9);
			final int tooBigNumberOfUniqueCodes = (int) Math.pow(10, desiredCodesLength) + 1;
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> randomService.genUniqueDecimalStrings(desiredCodesLength, tooBigNumberOfUniqueCodes));
			assertEquals("There cannot be more than 10^l codes.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("desiredCodeLength=0 and numberOfCodes=1 returns a vector with one empty string")
		void genUniqueDecimalStringsZeroDesiredCodeLength() {
			final int desiredCodesLength = 0;
			final int numberOfCodes = 1;
			final ImmutableList<String> uniqueStrings = assertDoesNotThrow(
					() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
			assertNotNull(uniqueStrings);
			assertEquals(numberOfCodes, uniqueStrings.size());
			assertTrue(uniqueStrings.getFirst().isEmpty());
		}

		@RepeatedTest(10)
		@DisplayName("valid arguments returns vector of unique Base10 strings")
		void genUniqueDecimalStringsValidInputs() {
			final int desiredCodesLength = randomService.genRandomInteger(10) + 1;
			final int numberOfCodes = randomService.genRandomInteger(10) + 1;
			final ImmutableList<String> uniqueStrings = assertDoesNotThrow(
					() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
			final boolean allHaveCorrectSize = uniqueStrings.stream().map(String::length).allMatch(codeSize -> codeSize == desiredCodesLength);

			// All strings have size desiredCodeLength
			assertTrue(allHaveCorrectSize);
			// There are numberOfCodes strings in the list
			assertEquals(numberOfCodes, uniqueStrings.size());
			// All codes are unique
			assertEquals(numberOfCodes, uniqueStrings.stream().distinct().count());
			// All codes belong to the Base10Alphabet
			final Base10Alphabet base10 = Base10Alphabet.getInstance();
			uniqueStrings.forEach(str -> {
				final char[] chars = str.toCharArray();
				for (final char S_prime_i_char : chars) {
					final String S_prime_i = String.valueOf(S_prime_i_char);
					assertTrue(base10.contains(S_prime_i));
				}
			});
		}

		static Stream<Arguments> provideGenUniqueDecimalStringsArguments() {
			final ImmutableList<Object> randomBytesList1 = ImmutableList.of(ImmutableByteArray.of(new byte[] { 1 }),
					ImmutableByteArray.of(new byte[] { 9 }), ImmutableByteArray.of(new byte[] { 0 }), ImmutableByteArray.of(new byte[] { 2 }),
					ImmutableByteArray.of(new byte[] { 8 }), ImmutableByteArray.of(new byte[] { 7 }), ImmutableByteArray.of(new byte[] { 98 }),
					ImmutableByteArray.of(new byte[] { 53 }), ImmutableByteArray.of(new byte[] { 17 }), ImmutableByteArray.of(new byte[] { -93 }),
					ImmutableByteArray.of(new byte[] { -124 }), ImmutableByteArray.of(new byte[] { -8 }));

			final ImmutableList<Object> randomBytesList2 = ImmutableList.of(ImmutableByteArray.of(new byte[] { -127 }),
					ImmutableByteArray.of(new byte[] { 73 }), ImmutableByteArray.of(new byte[] { 8 }), ImmutableByteArray.of(new byte[] { -16 }),
					ImmutableByteArray.of(new byte[] { 20 }), ImmutableByteArray.of(new byte[] { 104 }), ImmutableByteArray.of(new byte[] { -119 }),
					ImmutableByteArray.of(new byte[] { 1 }), ImmutableByteArray.of(new byte[] { 18 }), ImmutableByteArray.of(new byte[] { -88 }),
					ImmutableByteArray.of(new byte[] { 51 }), ImmutableByteArray.of(new byte[] { -89 }), ImmutableByteArray.of(new byte[] { 8 }),
					ImmutableByteArray.of(new byte[] { -124 }), ImmutableByteArray.of(new byte[] { -107 }));

			return Stream.of(Arguments.of(randomBytesList1, 4, 3, ImmutableList.of("1902", "8725", "1348")),
					Arguments.of(randomBytesList1, 6, 2, ImmutableList.of("190287", "251348")),
					Arguments.of(randomBytesList1, 2, 6, ImmutableList.of("19", "02", "87", "25", "13", "48")),
					Arguments.of(randomBytesList1, 1, 9, ImmutableList.of("1", "9", "0", "2", "8", "7", "5", "3", "4")),
					Arguments.of(randomBytesList2, 15, 1, ImmutableList.of("198048912837845")),
					Arguments.of(randomBytesList2, 5, 3, ImmutableList.of("19804", "89128", "37845")),
					Arguments.of(randomBytesList2, 3, 5, ImmutableList.of("198", "048", "912", "837", "845")),
					Arguments.of(randomBytesList2, 1, 9, ImmutableList.of("1", "9", "8", "0", "4", "2", "3", "7", "5")));
		}

		@ParameterizedTest
		@MethodSource("provideGenUniqueDecimalStringsArguments")
		@DisplayName("specific values returns expected output")
		void genUniqueDecimalStringsExpectedOutput(final ImmutableList<ImmutableByteArray> randomBytesList, final int desiredCodeLength,
				final int numberOfCodes, final ImmutableList<String> expected) {
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(randomBytesList))) {
				final SecureRandom secureRandom1 = new SecureRandom();
				final RandomService randomService1 = new RandomService(secureRandom1);

				final ImmutableList<String> result = randomService1.genUniqueDecimalStrings(desiredCodeLength, numberOfCodes);
				assertEquals(expected, result);
				assertEquals(1, mockedSecureRandom.constructed().size());
			}
		}
	}

	/**
	 * Tests the randomBytes method.
	 * <p>
	 * The tests run for randomBytes are:
	 * <ul>
	 *     <li>byteLength < 0 throws an {@link IllegalArgumentException}</li>
	 *     <li>valid argument returns valid output: byte array has the desired size</li>
	 *     <li>length = 0 returns an empty {@link ImmutableByteArray}</li>
	 *     <li>specific input provides expected output - the SecureRandom is mocked for this test</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("randomBytes with")
	class RandomBytesTest {

		@Test
		@DisplayName("negative byteLength throws an IllegalArgumentException")
		void randomBytesInvalidInput() {
			assertThrows(IllegalArgumentException.class, () -> randomService.randomBytes(-1));
		}

		@RepeatedTest(100)
		@DisplayName("valid byteLength returns a byte array of the desired length")
		void randomBytesValidInput() {
			final int length = secureRandom.nextInt(10000);

			final ImmutableByteArray randomBytes = randomService.randomBytes(length);
			assertEquals(length, randomBytes.length());
		}

		@Test
		@DisplayName("byteLength=0 returns an empty byte array")
		void randomBytesZeroLength() {
			final ImmutableByteArray randomBytes = randomService.randomBytes(0);
			assertTrue(randomBytes.isEmpty());
		}

		static Stream<ImmutableByteArray> provideRandomBytesArguments() {
			return Stream.of(ImmutableByteArray.EMPTY, ImmutableByteArray.of(new byte[] { 0 }),
					ImmutableByteArray.of(new byte[] { -128, 127, -126, 125 }), ImmutableByteArray.of(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }),
					ImmutableByteArray.of(
							new byte[] { -24, -14, 41, 72, -118, 51, -34, -1, 21, -83, 87, -35, 73, 3, -47, 98, 95, 64, 14, -34, 88, -49, 11, -49, 57,
									6, 56, 14, 79, -118, 37, 118 }));
		}

		@ParameterizedTest
		@MethodSource("provideRandomBytesArguments")
		@DisplayName("specific values returns expected output")
		void randomBytesExpectedOutput(final ImmutableByteArray randomBytes) {
			try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
					prepareSecureRandom(ImmutableList.of(randomBytes)))) {
				final SecureRandom secureRandom1 = new SecureRandom();
				final RandomService randomService1 = new RandomService(secureRandom1);

				final ImmutableByteArray result = randomService1.randomBytes(randomBytes.length());
				assertEquals(randomBytes, result);
				assertEquals(1, mockedSecureRandom.constructed().size());
			}
		}
	}

	private MockedConstruction.MockInitializer<SecureRandom> prepareSecureRandom(final ImmutableList<ImmutableByteArray> randomBytesList) {
		checkArgument(!randomBytesList.isEmpty(), "List must not be empty");

		return (SecureRandom mockSecureRandom, MockedConstruction.Context context) -> {
			final AtomicInteger callIndex = new AtomicInteger(0);

			doAnswer(invocation -> {
				final byte[] byteArray = invocation.getArgument(0, byte[].class);
				int index = callIndex.getAndIncrement();

				if (index >= randomBytesList.size()) {
					index = randomBytesList.size() - 1;
				}

				System.arraycopy(randomBytesList.get(index).elements(), 0, byteArray, 0, byteArray.length);
				return null;
			}).when(mockSecureRandom).nextBytes(Mockito.any());
		};
	}
}
