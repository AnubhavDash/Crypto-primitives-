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
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;

class StringsTest {

	private static final TestRandomService randomService = new TestRandomService();

	@Test
	@DisplayName("truncate with a null input String throws a NullPointerException.")
	void truncateNullInputThrows() {
		final String string = null;
		final int length = 1;

		assertThrows(NullPointerException.class, () -> Strings.truncate(string, length));
	}

	@Test
	@DisplayName("truncate with an input length negative throws an IllegalArgumentException.")
	void truncateNegativeLengthThrows() {
		final String string = "string";
		final int length = -1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> Strings.truncate(string, length));
		assertEquals(String.format("The input length must be positive. [l: %s]", length),
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
		final int stringLength = randomService.genRandomInteger(1, 10000);
		final String string = new String(randomService.randomBytes(stringLength).elements());
		final int length = randomService.genRandomInteger(1, 10000);

		assertEquals(truncateFromSpecification(string, length), Strings.truncate(string, length));
	}

	@Test
	@DisplayName("truncate with an empty input string returns an empty string.")
	void truncateEmptyStringInput() {
		final String string = "";
		final int length = 1;

		assertEquals(Strings.truncate(string, length), string);
	}

	@Test
	@DisplayName("truncate with an input length of zero returns an empty string.")
	void truncateZeroLengthInput() {
		final String string = "string";
		final int length = 0;

		assertEquals("", Strings.truncate(string, length));
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
