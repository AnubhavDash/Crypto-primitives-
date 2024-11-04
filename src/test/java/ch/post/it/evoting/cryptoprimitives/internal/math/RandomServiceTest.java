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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays.byteLength;
import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.UsabilityBase32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

class RandomServiceTest {

	private final SecureRandom secureRandom = new SecureRandom();
	private final RandomService randomService = new RandomService();

	@RepeatedTest(1000)
	void genRandomIntegerTest() {
		final BigInteger upperBound = BigInteger.valueOf(100);
		final BigInteger randomInteger = randomService.genRandomInteger(upperBound);

		assertTrue(randomInteger.compareTo(upperBound) < 0);
		assertTrue(randomInteger.signum() >= 0);
	}

	@Test
	void genRandomIntegerWithInvalidUpperBounds() {
		assertThrows(NullPointerException.class, () -> randomService.genRandomInteger(null));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(BigInteger.ZERO));
		final BigInteger minusOne = BigInteger.ONE.negate();
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(minusOne));

		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(0));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomInteger(-1));
	}

	@Test
	void genRandomIntegerWithUpperBoundOne() {
		final BigInteger upperBound = BigInteger.ONE;
		final BigInteger expected = BigInteger.ZERO;

		assertEquals(expected, randomService.genRandomInteger(upperBound));
	}

	@Test
	void genRandomIntegerAreEquivalent() {
		final BigInteger upperBound = BigInteger.valueOf(1_000_000);
		final ImmutableList<ImmutableByteArray> randomBytesList = ImmutableList.of(
				randomService.randomBytes(byteLength(upperBound)),
				randomService.randomBytes(byteLength(upperBound)),
				randomService.randomBytes(byteLength(upperBound))
		);
		try (final MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
				this.prepareSecureRandom(randomBytesList))) {
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

	private MockedConstruction.MockInitializer<SecureRandom> prepareSecureRandom(final ImmutableList<ImmutableByteArray> randomBytesList) {
		checkArgument(randomBytesList.size() >= 3);
		return (SecureRandom mockSecureRandom, MockedConstruction.Context context) ->
				doAnswer(invocation -> {
					final byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(0).elements(), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					final byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(1).elements(), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					final byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(2).elements(), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					final byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(0).elements(), 1, byteArray, 1, byteArray.length - 1);
					return null;
				}).when(mockSecureRandom).nextBytes(Mockito.any());
	}

	@Test
	void genRandomVector() {
		final BigInteger upperBound = BigInteger.valueOf(100);
		final int length = 20;
		final GroupVector<ZqElement, ZqGroup> randomVector = randomService.genRandomVector(upperBound, length);

		assertEquals(length, randomVector.size());
		assertEquals(0, (int) randomVector.stream().filter(zq -> zq.getValue().compareTo(upperBound) >= 0).count());
		assertEquals(1, randomVector.stream().map(ZqElement::getGroup).distinct().count());
	}

	@Test
	void genEmptyRandomVector() {
		final GroupVector<ZqElement, ZqGroup> randomVector = randomService.genRandomVector(BigInteger.TWO, 0);

		assertTrue(randomVector.isEmpty());
	}

	@Test
	void checkGenRandomVectorParameterChecks() {
		assertThrows(NullPointerException.class, () -> randomService.genRandomVector(null, 1));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.ZERO, 1));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.TWO, -1));
	}

	@Test
	void randomBytes() {
		final int RANDOM_BYTES_LENGTH_NINTY_SIX = 96;
		final int RANDOM_BYTES_LENGTH_ZERO = 0;

		ImmutableByteArray randomBytes = randomService.randomBytes(RANDOM_BYTES_LENGTH_NINTY_SIX);
		assertEquals(RANDOM_BYTES_LENGTH_NINTY_SIX, randomBytes.length());

		randomBytes = randomService.randomBytes(RANDOM_BYTES_LENGTH_ZERO);
		assertEquals(RANDOM_BYTES_LENGTH_ZERO, randomBytes.length());
	}

	@Test
	void genUniqueDecimalStringsWithZeroCodeLengthDoesNotThrow() {
		final int desiredCodesLength = 0;
		final int numberOfCodes = 1;
		final ImmutableList<String> uniqueStrings = assertDoesNotThrow(
				() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
		final boolean allHaveCorrectSize = uniqueStrings.stream()
				.map(String::length)
				.allMatch(codeSize -> codeSize == desiredCodesLength);
		assertTrue(allHaveCorrectSize);
	}

	@Test
	void genUniqueDecimalStringsWithTooSmallDesiredCodeLengthThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(-1, 1));
		assertEquals("The desired length of the unique codes must be greater than or equal to 0.", exception.getMessage());
	}

	@Test
	void genUniqueDecimalStringsWithTooSmallNumberOfUniqueCodesThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(1, 0));
		assertEquals("The number of unique codes must be strictly positive.", exception.getMessage());
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsReturnsStringsOfCorrectSize() {
		final int desiredCodesLength = randomService.genRandomInteger(10) + 1;
		final int numberOfCodes = randomService.genRandomInteger(10) + 1;
		final ImmutableList<String> uniqueStrings = assertDoesNotThrow(
				() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
		final boolean allHaveCorrectSize = uniqueStrings.stream().map(String::length).allMatch(codeSize -> codeSize == desiredCodesLength);
		assertTrue(allHaveCorrectSize);
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsReturnsDesiredNumberOfStrings() {
		final int desiredCodesLength = randomService.genRandomInteger(10) + 1;
		final int numberOfCodes = randomService.genRandomInteger(10) + 1;
		final ImmutableList<String> uniqueStrings = assertDoesNotThrow(
				() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
		assertEquals(numberOfCodes, uniqueStrings.size());
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsGeneratesUniqueStrings() {
		final int desiredCodesLength = randomService.genRandomInteger(10) + 1;
		final ImmutableList<String> uniqueStrings = assertDoesNotThrow(() -> randomService.genUniqueDecimalStrings(desiredCodesLength, 3));
		final String s1 = uniqueStrings.get(0);
		final String s2 = uniqueStrings.get(1);
		final String s3 = uniqueStrings.get(2);

		assertNotEquals(s1, s2);
		assertNotEquals(s1, s3);
		assertNotEquals(s2, s3);
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsWithTooManyCodesThrows() {
		final int desiredCodesLength = randomService.genRandomInteger(9);
		final int tooBigNumberOfUniqueCodes = (int) Math.pow(10, desiredCodesLength) + 1;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(desiredCodesLength, tooBigNumberOfUniqueCodes));
		assertEquals("There cannot be more than 10^l codes.", Throwables.getRootCause(exception).getMessage());
	}

	@Nested
	class GenRandomStringAlgorithmTest {

		private static final Alphabet alphabet = UsabilityBase32Alphabet.getInstance();
		private static final int LENGTH = alphabet.size();

		@Test
		@DisplayName("an invalid length throws an IllegalArgumentException")
		void invalidLengthThrows() {
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> randomService.genRandomString(-2, alphabet));

			assertEquals(String.format("The desired length of string must be greater than or equal to 0. [length: %s]", -2),
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}

		@Test
		@DisplayName("a zero length does not throw an IllegalArgumentException")
		void zeroLengthdoesNotThrow() {
			assertDoesNotThrow(() -> randomService.genRandomString(0, alphabet));
		}

		@Test
		@DisplayName("a null alphabet throws a NullPointerException")
		void nullAlphabetThrows() {
			assertThrows(NullPointerException.class, () -> randomService.genRandomString(LENGTH, null));
		}

		@RepeatedTest(100)
		@DisplayName("valid input behaves as expected")
		void happyPath() {

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

			// a second call should return a different S_prime
			assertNotEquals(S_prime, randomService.genRandomString(length, alphabet));
		}

	}
}
