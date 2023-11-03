/*
 * Copyright 2023 Post CH Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;

class StringsTest {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final RandomService randomService = new RandomService();

	@Test
	void leftPadWithNullStringThrows() {
		assertThrows(NullPointerException.class, () -> Strings.leftPad(null, 1, 'c'));
	}

	@Test
	void leftPadWithEmptyStringThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Strings.leftPad("", 1, 'c'));
		assertEquals("The string to be padded must contain at least one character.", exception.getMessage());
	}

	@Test
	void leftPadWithStringLengthGreaterThanDesiredLengthThrows() {
		final String string = "Test too short desired length";
		final int desiredLength = string.length() - 1;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> Strings.leftPad(string, desiredLength, 'c'));
		assertEquals("The desired string length must not be smaller than the string.", exception.getMessage());
	}

	@Test
	void leftPadWithStringLengthEqualsDesiredLengthReturnsString() {
		final String string = "test";
		assertEquals(string, Strings.leftPad(string, string.length(), 'c'));
	}

	@Test
	void leftPadWithStringLengthGreaterThanDesiredLengthReturnsPaddedString() {
		final String string = "Test short string";
		final int paddingSize = secureRandom.nextInt(10) + 1;
		final int desiredStringLength = string.length() + paddingSize;
		final char paddingCharacter = '&';
		final String paddedString = Strings.leftPad(string, desiredStringLength, paddingCharacter);

		for (int i = 0; i < paddingSize; i++) {
			assertEquals(paddingCharacter, paddedString.charAt(i));
		}

		assertTrue(paddedString.contains(string));
		assertEquals(desiredStringLength, paddedString.length());
	}

	@Test
	@DisplayName("truncate with a null input String throws a NullPointerException.")
	void truncateNullInputThrows() {
		final String string = null;
		final int length = 1;

		assertThrows(NullPointerException.class, () -> Strings.truncate(string, length));
	}

	@Test
	@DisplayName("truncate with an empty input String throws an IllegalArgumentException.")
	void truncateEmptyInputThrows() {
		final String string = "";
		final int length = 1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> Strings.truncate(string, length));
		assertEquals(String.format("The input string must be non-empty. [u: %s]", string.length()),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("truncate with an input length of zero throws an IllegalArgumentException.")
	void truncateZeroLengthThrows() {
		final String string = "string";
		final int length = 0;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> Strings.truncate(string, length));
		assertEquals(String.format("The input length must be strictly positive. [l: %s]", length),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("truncate with an input length negative throws an IllegalArgumentException.")
	void truncateNegativeLengthThrows() {
		final String string = "string";
		final int length = -1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> Strings.truncate(string, length));
		assertEquals(String.format("The input length must be strictly positive. [l: %s]", length),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("truncate with length greater than string length returns string.")
	void truncateUnsatisfiedRequirementThrows() {
		final String string = "string";
		final int length = string.length() + 1;

		final String output = assertDoesNotThrow(() -> Strings.truncate(string, length));
		assertEquals(string, output);
	}

	@RepeatedTest(1000)
	@DisplayName("truncate implementation is equivalent to specification.")
	void truncateEnsureEqualityOfImplementation() {
		final int stringLength = secureRandom.nextInt(1, 10000);
		final String string = new String(randomService.randomBytes(stringLength));
		final int length = 5000;

		assertEquals(truncateFromSpecification(string, length), Strings.truncate(string, length));
	}

	@ParameterizedTest
	@MethodSource("happyPathArgumentProvider")
	@DisplayName("truncate with valid inputs does not throw and behaves as expected.")
	void truncateHappyPath(final String string, final int length, final String expectedTruncated) {

		final String truncated = assertDoesNotThrow(() -> Strings.truncate(string, length));

		assertEquals(expectedTruncated, truncated);
	}

	static Stream<Arguments> happyPathArgumentProvider() {

		return Stream.of(
				Arguments.of("string", 1, "s"),
				Arguments.of("string", 3, "str"),
				Arguments.of("string", 6, "string"),
				Arguments.of("string", 7, "string"),
				Arguments.of("string", 55, "string"),
				Arguments.of("string", Integer.MAX_VALUE, "string")
		);
	}

	private String truncateFromSpecification(final String S, final int l) {
		final int m = Math.min(S.length(), l);
		return IntStream.range(0, m)
				.mapToObj(S::charAt)
				.map(String::valueOf)
				.collect(Collectors.joining());
	}
}
