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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.internal.utils.ByteArrays;
import ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.UsabilityBase32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

class RandomServiceTest {

	// RFC 4648 Table 1, Table 3 and Table 5.
	private static final Pattern base64Alphabet = Pattern.compile("^[A-Za-z0-9+/=]+$");
	private static final Pattern base32Alphabet = Pattern.compile("^[A-Z2-7=]+$");
	private static final Pattern base16Alphabet = Pattern.compile("^[A-F0-9=]+$");
	private final SecureRandom secureRandom = new SecureRandom();
	private final RandomService randomService = new RandomService();

	@RepeatedTest(1000)
	void genRandomIntegerTest() {
		final BigInteger upperBound = BigInteger.valueOf(100);
		final BigInteger randomInteger = randomService.genRandomInteger(upperBound);

		assertTrue(randomInteger.compareTo(upperBound) < 0);
		assertTrue(randomInteger.compareTo(BigInteger.ZERO) >= 0);
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

	@RepeatedTest(1000)
	void genRandomIntegerIsEquivalentToSpecification() {
		final BigInteger upperBound = BigInteger.valueOf(1_000_000);
		final List<byte[]> randomBytesList = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			randomBytesList.add(randomService.randomBytes(ByteArrays.byteLength(upperBound)));
		}
		try (MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
				this.prepareSecureRandom(randomBytesList))) {
			final SecureRandom secureRandom1 = new SecureRandom();
			final RandomService randomService1 = new RandomService(secureRandom1);
			final BigInteger result = randomService1.genRandomInteger(upperBound);

			final SecureRandom secureRandom2 = new SecureRandom();
			final RandomService randomService2 = new RandomService(secureRandom2);
			final BigInteger expectedResult = genRandomIntegerSpec(upperBound, randomService2);

			assertEquals(0, expectedResult.compareTo(result));
			assertEquals(2, mockedSecureRandom.constructed().size());
		}
	}

	@Test
	void genRandomIntegerAreEquivalent() {
		final BigInteger upperBound = BigInteger.valueOf(1_000_000);
		final List<byte[]> randomBytesList = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			randomBytesList.add(randomService.randomBytes(ByteArrays.byteLength(upperBound)));
		}
		try (MockedConstruction<SecureRandom> mockedSecureRandom = Mockito.mockConstruction(SecureRandom.class,
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

	private MockedConstruction.MockInitializer<SecureRandom> prepareSecureRandom(final List<byte[]> randomBytesList) {
		checkArgument(randomBytesList.size() >= 3);
		return (SecureRandom mockSecureRandom, MockedConstruction.Context context) ->
				doAnswer(invocation -> {
					byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(0), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(1), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(2), 0, byteArray, 0, byteArray.length);
					return null;
				}).doAnswer(invocation -> {
					byte[] byteArray = invocation.getArgument(0, byte[].class);
					System.arraycopy(randomBytesList.get(0), 1, byteArray, 1, byteArray.length - 1);
					return null;
				}).when(mockSecureRandom).nextBytes(Mockito.any());
	}

	private BigInteger genRandomIntegerSpec(final BigInteger upperBound, final RandomService randomService) {
		final BigInteger m = checkNotNull(upperBound);
		final BigInteger m_minus_one = upperBound.subtract(BigInteger.ONE);
		final int length = ByteArrays.byteLength(m_minus_one);
		final int bitLength = m_minus_one.bitLength();
		BigInteger r;
		do {
			final byte[] rBytes = ByteArrays.cutToBitLength(randomService.randomBytes(length), bitLength);
			r = ConversionsInternal.byteArrayToInteger(rBytes);
		} while (r.compareTo(m) >= 0);
		return r;
	}

	@Test
	void genRandomVector() {
		final BigInteger upperBound = BigInteger.valueOf(100);
		final int length = 20;
		final List<ZqElement> randomVector = randomService.genRandomVector(upperBound, length);

		assertEquals(length, randomVector.size());
		assertEquals(0, (int) randomVector.stream().filter(zq -> zq.getValue().compareTo(upperBound) >= 0).count());
		assertEquals(1, randomVector.stream().map(ZqElement::getGroup).distinct().count());
	}

	@Test
	void checkGenRandomVectorParameterChecks() {
		assertThrows(NullPointerException.class, () -> randomService.genRandomVector(null, 1));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.ZERO, 1));
		assertThrows(IllegalArgumentException.class, () -> randomService.genRandomVector(BigInteger.ONE, 0));
	}

	@Test
	void randomBytes() {
		final int RANDOM_BYTES_LENGTH_NINTY_SIX = 96;
		final int RANDOM_BYTES_LENGTH_ZERO = 0;

		byte[] randomBytes = randomService.randomBytes(RANDOM_BYTES_LENGTH_NINTY_SIX);
		assertEquals(RANDOM_BYTES_LENGTH_NINTY_SIX, randomBytes.length);

		randomBytes = randomService.randomBytes(RANDOM_BYTES_LENGTH_ZERO);
		assertEquals(RANDOM_BYTES_LENGTH_ZERO, randomBytes.length);
	}

	@Test
	void genUniqueDecimalStringsWithTooSmallDesiredCodeLengthThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(0, 1));
		assertEquals("The desired length of the unique codes must be strictly positive.", exception.getMessage());
	}

	@Test
	void genUniqueDecimalStringsWithTooSmallNumberOfUniqueCodesThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(1, 0));
		assertEquals("The number of unique codes must be strictly positive.", exception.getMessage());
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsReturnsStringsOfCorrectSize() {
		final int desiredCodesLength = secureRandom.nextInt(10) + 1;
		final int numberOfCodes = secureRandom.nextInt(10) + 1;
		final List<String> uniqueStrings = assertDoesNotThrow(() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
		final boolean allHaveCorrectSize = uniqueStrings.stream().map(String::length).allMatch(codeSize -> codeSize == desiredCodesLength);
		assertTrue(allHaveCorrectSize);
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsReturnsDesiredNumberOfStrings() {
		final int desiredCodesLength = secureRandom.nextInt(10) + 1;
		final int numberOfCodes = secureRandom.nextInt(10) + 1;
		final List<String> uniqueStrings = assertDoesNotThrow(() -> randomService.genUniqueDecimalStrings(desiredCodesLength, numberOfCodes));
		assertEquals(numberOfCodes, uniqueStrings.size());
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsGeneratesUniqueStrings() {
		final int desiredCodesLength = secureRandom.nextInt(10) + 1;
		final List<String> uniqueStrings = assertDoesNotThrow(() -> randomService.genUniqueDecimalStrings(desiredCodesLength, 3));
		final String s1 = uniqueStrings.get(0);
		final String s2 = uniqueStrings.get(1);
		final String s3 = uniqueStrings.get(2);

		assertNotEquals(s1, s2);
		assertNotEquals(s1, s3);
		assertNotEquals(s2, s3);
	}

	@RepeatedTest(10)
	void genUniqueDecimalStringsWithTooManyCodesThrows() {
		final int desiredCodesLength = secureRandom.nextInt(9) + 1;
		final int tooBigNumberOfUniqueCodes = (int) Math.pow(10, desiredCodesLength) + 1;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> randomService.genUniqueDecimalStrings(desiredCodesLength, tooBigNumberOfUniqueCodes));
		assertEquals("There cannot be more than 10^l codes.", Throwables.getRootCause(exception).getMessage());
	}

	@Nested
	class GenRandomStringAlgorithmTest {

		private static final Alphabet alphabet = UsabilityBase32Alphabet.getInstance();
		private static final int LENGTH = alphabet.size();

		@ParameterizedTest
		@ValueSource(ints = { -1, 0 })
		@DisplayName("an invalid length throws an IllegalArgumentException")
		void invalidLengthThrows(final int length) {
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> randomService.genRandomString(length, alphabet));

			assertEquals(String.format("The desired length of string must be strictly positive. [length: %s]", length),
					Throwables.getRootCause(illegalArgumentException).getMessage());
		}

		@Test
		@DisplayName("a null alphabet throws a NullPointerException")
		void nullAlphabetThrows() {
			assertThrows(NullPointerException.class, () -> randomService.genRandomString(LENGTH, null));
		}

		@RepeatedTest(100)
		@DisplayName("valid input behaves as expected")
		void happyPath() {

			int length = secureRandom.nextInt(1, 10000);

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
