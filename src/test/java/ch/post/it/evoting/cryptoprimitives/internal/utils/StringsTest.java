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
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base16Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.LatinAlphabet;
import ch.post.it.evoting.cryptoprimitives.math.UsabilityBase32Alphabet;

class StringsTest {

	private static final TestRandomService randomService = new TestRandomService();

	/**
	 * Tests for the {@link Strings#truncate(String, int)} method.
	 * <p>
	 * Covers:
	 * <ul>
	 *   <li>Null input string throws NullPointerException.</li>
	 *   <li>Negative length throws IllegalArgumentException.</li>
	 *   <li>Length greater than string length returns the full string.</li>
	 *   <li>Empty string input returns an empty string.</li>
	 *   <li>Length zero returns an empty string.</li>
	 *   <li>Implementation matches the specification pseudocode (1000 random inputs).</li>
	 *   <li>Valid inputs produce correctly truncated strings (parameterized).</li>
	 * </ul>
	 */
	@Nested
	class TruncateTest {

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
			assertEquals(String.format("The input length must be non-negative. [l: %s]", length),
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
			final int u = S.length();

			// Operation.
			final int m = Math.min(u, l);
			return IntStream.range(0, m)
					.mapToObj(S::charAt)
					.map(String::valueOf)
					.collect(Collectors.joining());
		}
	}

	/**
	 * Tests for the {@link Strings#getMergedString(ImmutableList, Alphabet)} method.
	 * <p>
	 * Covers:
	 * <ul>
	 *   <li>Null strings list or null alphabet throws NullPointerException.</li>
	 *   <li>Alphabet of size 0 throws an IllegalArgumentException</li>
	 *   <li>Empty strings list throws IllegalArgumentException.</li>
	 *   <li>List of empty strings throws IllegalArgumentException.</li>
	 *   <li>Single empty string throws IllegalArgumentException.</li>
	 *   <li>Strings of different lengths throw IllegalArgumentException.</li>
	 *   <li>Non-compliant character at beginning, middle, or end throws IllegalArgumentException (parameterized).</li>
	 *   <li>Single input string returns that string unchanged.</li>
	 *   <li>Output length equals input string length.</li>
	 *   <li>Permuted input strings give the same output.</li>
	 *   <li>Output string is in the same alphabet as the input strings.</li>
	 *   <li>Valid inputs across multiple alphabets produce the expected merged string (parameterized).</li>
	 * </ul>
	 */
	@Nested
	class GetMergedStringTest {

		private static final ImmutableList<Alphabet> alphabets = ImmutableList.of(Base10Alphabet.getInstance(), Base16Alphabet.getInstance(),
				Base32Alphabet.getInstance(),
				UsabilityBase32Alphabet.getInstance(), Base64Alphabet.getInstance(), LatinAlphabet.getInstance());

		private Alphabet alphabet;
		private ImmutableList<String> strings;

		@BeforeEach
		void setUp() {
			alphabet = alphabets.get(randomService.genRandomInteger(alphabets.size()));
			final int n = randomService.genRandomInteger(1, 101);
			final int k = randomService.genRandomInteger(1, 11);
			strings = Stream.generate(() -> randomService.genRandomString(n, alphabet))
					.limit(k)
					.collect(toImmutableList());
		}

		@Test
		@DisplayName("getMergedString with null input throws a NullPointerException.")
		void getMergedStringWithNullInputThrows() {
			assertThrows(NullPointerException.class, () -> Strings.getMergedString(null, alphabet));
			assertThrows(NullPointerException.class, () -> Strings.getMergedString(strings, null));
		}

		@Test
		@DisplayName("getMergedString with an alphabet of size zero throws an IllegalArgumentException.")
		void getMergedStringWithTooSmallAlphabetThrows() {
			final Alphabet emptyAlphabet = Mockito.mock(Base16Alphabet.class);
			Mockito.when(emptyAlphabet.size()).thenReturn(0);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Strings.getMergedString(strings, emptyAlphabet));
			assertEquals("The alphabet must contain at least one character.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("getMergedString with an empty list of strings throws an IllegalArgumentException.")
		void getMergedStringWithEmptyInputThrows() {
			final ImmutableList<String> emptyList = ImmutableList.of();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Strings.getMergedString(emptyList, alphabet));
			assertEquals("The list of strings must not be empty.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("getMergedString with a list of empty strings throws an IllegalArgumentException.")
		void getMergedStringWithEmptyStringsThrows() {
			final ImmutableList<String> emptyStrings = ImmutableList.of("", "");
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Strings.getMergedString(emptyStrings, alphabet));
			assertEquals("The strings must have at least one character.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("getMergedString with a single empty string throws.")
		void getMergedStringWithSingleEmptyStringThrows() {
			final ImmutableList<String> listWithEmptyString = ImmutableList.of("A", "", "B");
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Strings.getMergedString(listWithEmptyString, alphabet));
			assertEquals("The strings must have at least one character.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("getMergedString with strings of different lengths throws an IllegalArgumentException.")
		void getMergedStringWithDifferentStringLengthsThrows() {
			final ImmutableList<String> listOfDifferentSizeStrings = ImmutableList.of("1", "23");
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Strings.getMergedString(listOfDifferentSizeStrings, alphabet));
			assertEquals("All strings must have the same length.", Throwables.getRootCause(exception).getMessage());
		}

		@ParameterizedTest
		@MethodSource("nonCompliantCharacterPositionProvider")
		@DisplayName("getMergedString with a non-compliant character at any position throws.")
		void getMergedStringWithStringNotInAlphabetThrows(final ImmutableList<String> strings, final Alphabet alphabet) {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Strings.getMergedString(strings, alphabet));
			assertEquals("All strings must be defined in the given alphabet.", Throwables.getRootCause(exception).getMessage());
		}

		private static Stream<Arguments> nonCompliantCharacterPositionProvider() {
			final Base10Alphabet base10Alphabet = Base10Alphabet.getInstance();
			return Stream.of(
					Arguments.of(ImmutableList.of("0123", "a789"), base10Alphabet),   // non-compliant at beginning
					Arguments.of(ImmutableList.of("0123", "7a89"), base10Alphabet),   // non-compliant in the middle
					Arguments.of(ImmutableList.of("0123", "789a"), base10Alphabet)    // non-compliant at end
			);
		}

		@Test
		@DisplayName("getMergedString with a single input string returns that string.")
		void getMergedStringWithSingleInputStringReturnsItself() {
			final String inputString = randomService.genRandomString(randomService.genRandomInteger(1, 10), alphabet);
			assertEquals(inputString, Strings.getMergedString(ImmutableList.of(inputString), alphabet));
		}

		@RepeatedTest(100)
		@DisplayName("getMergedString output length equals input string length.")
		void getMergedStringOutputLengthEqualsInputStringLength() {
			assertEquals(strings.getFirst().length(), Strings.getMergedString(strings, alphabet).length());
		}

		@RepeatedTest(100)
		@DisplayName("getMergedString with permuted input strings gives the same output.")
		void getMergedStringWithPermutedInputStringsGivesSameOutput() {
			final String a = randomService.genRandomString(10, alphabet);
			final String b = randomService.genRandomString(10, alphabet);
			final String c = randomService.genRandomString(10, alphabet);

			final String abcString = Strings.getMergedString(ImmutableList.of(a, b, c), alphabet);
			final String acbString = Strings.getMergedString(ImmutableList.of(a, c, b), alphabet);
			final String cabString = Strings.getMergedString(ImmutableList.of(c, a, b), alphabet);

			assertEquals(abcString, acbString);
			assertEquals(abcString, cabString);
		}

		@RepeatedTest(100)
		@DisplayName("getMergedString with valid inputs produces string in the given alphabet.")
		void getMergedStringWithValidInputsProducesStringInAlphabet() {
			final String mergedString = Strings.getMergedString(strings, alphabet);
			assertTrue(mergedString.chars().allMatch(alphabet::contains));
		}

		@ParameterizedTest
		@MethodSource("happyPathArgumentProvider")
		void happyPath(final ImmutableList<String> strings, final Alphabet alphabet, final String expectedMergedString) {
			assertEquals(expectedMergedString, Strings.getMergedString(strings, alphabet));
		}

		private static Stream<Arguments> happyPathArgumentProvider() {
			final Base10Alphabet base10Alphabet = Base10Alphabet.getInstance();
			final Base16Alphabet base16Alphabet = Base16Alphabet.getInstance();
			final Base32Alphabet base32Alphabet = Base32Alphabet.getInstance();
			final UsabilityBase32Alphabet usabilityBase32Alphabet = UsabilityBase32Alphabet.getInstance();
			final Base64Alphabet base64Alphabet = Base64Alphabet.getInstance();

			return Stream.of(
					Arguments.of(ImmutableList.of("0"), base10Alphabet, "0"),
					Arguments.of(ImmutableList.of("0"), base16Alphabet, "0"),
					Arguments.of(ImmutableList.of("A"), base32Alphabet, "A"),
					Arguments.of(ImmutableList.of("a"), usabilityBase32Alphabet, "a"),
					Arguments.of(ImmutableList.of("0"), base64Alphabet, "0"),
					Arguments.of(ImmutableList.of("0123", "6789"), base10Alphabet, "6802"),
					Arguments.of(ImmutableList.of("0123", "6789"), base16Alphabet, "68AC"),
					Arguments.of(ImmutableList.of("ABCD", "WXYZ"), base32Alphabet, "WY24"),
					Arguments.of(ImmutableList.of("abcd", "wxyz"), usabilityBase32Alphabet, "wy24"),
					Arguments.of(ImmutableList.of("0123", "6789"), base64Alphabet, "uwy0"),
					Arguments.of(ImmutableList.of("258", "480", "472"), base10Alphabet, "000"),
					Arguments.of(ImmutableList.of("5FD", "AA4", "17F"), base16Alphabet, "000"),
					Arguments.of(ImmutableList.of("ICH", "BIN", "XWM"), base32Alphabet, "AAA"),
					Arguments.of(ImmutableList.of("ich", "bin", "zyp"), usabilityBase32Alphabet, "aaa"),
					Arguments.of(ImmutableList.of("Duh", "ast", "jmy"), base64Alphabet, "AAA")
			);
		}
	}
}
