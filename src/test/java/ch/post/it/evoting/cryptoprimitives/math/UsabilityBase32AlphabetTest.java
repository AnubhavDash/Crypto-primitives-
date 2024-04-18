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
package ch.post.it.evoting.cryptoprimitives.math;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.base.Throwables;

@DisplayName("A UsabilityBase32Alphabet calling")
class UsabilityBase32AlphabetTest {

	private final UsabilityBase32Alphabet usabilityBase32Alphabet = UsabilityBase32Alphabet.getInstance();

	@DisplayName("get with a negative index throws an IllegalArgumentException")
	@Test
	void getWithNegativeIndexThrows() {

		final int index = -1;
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> usabilityBase32Alphabet.get(index));

		assertEquals(String.format("The index cannot be negative. [index: %s]", index),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("get with a bigger index throws an IllegalArgumentException")
	@Test
	void getWithBiggerIndexThrows() {
		final int size = usabilityBase32Alphabet.size();

		final int index = size;
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> usabilityBase32Alphabet.get(index));

		assertEquals(String.format("The index must be strictly smaller than the alphabet size. [index: %s, size: %s]", index, size),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("get with a valid index behaves as expected")
	@Test
	void getHappyPath() {
		final String expected = Character.toString(0x0063); // c (U+0063) -> 047

		final int index = 2;
		final String character = assertDoesNotThrow(() -> usabilityBase32Alphabet.get(index));

		assertEquals(expected, character);
	}

	@DisplayName("size behaves as expected")
	@Test
	void sizeHappyPath() {
		final int expected = 32;

		final int size = assertDoesNotThrow(usabilityBase32Alphabet::size);

		assertEquals(expected, size);
	}

	@DisplayName("contains with a character non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(strings = { "l", "o", "0", "1" })
	void containsWithNonCharacter(final String character) {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> usabilityBase32Alphabet.contains(character));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a null character behaves as expected")
	@Test
	void containsWithNullCharacter() {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> usabilityBase32Alphabet.contains(null));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a character part of the alphabet behaves as expected")
	@Test
	void containsCharacterHappyPath() {
		final boolean expected = true;

		final boolean isContained = assertDoesNotThrow(() -> usabilityBase32Alphabet.contains("a"));

		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a code point non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(ints = { 0x006C, 0x006F, 0x0030, 0x0031 })
	void containsWithNonCodePoint(final int codePoint) {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> usabilityBase32Alphabet.contains(codePoint));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a negative code point throws an IllegalArgumentException")
	@Test
	void containsCodePointNegativeThrows() {
		final int codePoint = -1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> usabilityBase32Alphabet.contains(codePoint));

		assertEquals(String.format("The provided code point is out-of-range. [codePoint: %s]", codePoint),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("contains with a bigger code point throws an IllegalArgumentException")
	@Test
	void containsCodePointBiggerThrows() {
		final int codePoint = Character.MAX_CODE_POINT + 1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> usabilityBase32Alphabet.contains(codePoint));

		assertEquals(String.format("The provided code point is out-of-range. [codePoint: %s]", codePoint),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("contains with a code point part of the alphabet behaves as expected")
	@Test
	void containsCodePointHappyPath() {
		final boolean expected = true;

		final boolean isContained = assertDoesNotThrow(() -> usabilityBase32Alphabet.contains(0x0034));

		assertEquals(expected, isContained);
	}

	@DisplayName("indexOf with a character non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(strings = { "l", "o", "0", "1" })
	void indexOfNonPartHappyPath(final String character) {
		final int expected = -1;

		final int index = assertDoesNotThrow(() -> usabilityBase32Alphabet.indexOf(character));

		assertEquals(expected, index);
	}

	@DisplayName("indexOf with a null character behaves as expected")
	@Test
	void indexOfNullHappyPath() {
		final int expected = -1;

		final int index = assertDoesNotThrow(() -> usabilityBase32Alphabet.indexOf(null));

		assertEquals(expected, index);
	}

	@DisplayName("indexOf with a character part of the alphabet behaves as expected")
	@Test
	void indexOfHappyPath() {
		final int expected = 2;

		final int index = assertDoesNotThrow(() -> usabilityBase32Alphabet.indexOf(Character.toString(0x0063)));

		assertEquals(expected, index);
	}

}