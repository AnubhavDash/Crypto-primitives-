/*
 * (c) Copyright 2023 Swiss Post Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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

import ch.post.it.evoting.cryptoprimitives.math.WriteInAlphabet;

@DisplayName("A WriteInAlphabet calling")
class WriteInAlphabetTest {

	private final WriteInAlphabet writeInAlphabet = WriteInAlphabet.getInstance();

	@DisplayName("get with a negative index throws an IllegalArgumentException")
	@Test
	void getWithNegativeIndexThrows() {

		final int index = -1;
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> writeInAlphabet.get(index));

		assertEquals(String.format("The index cannot be negative. [index: %s]", index),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("get with a bigger index throws an IllegalArgumentException")
	@Test
	void getWithBiggerIndexThrows() {
		final int size = writeInAlphabet.size();

		final int index = size;
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> writeInAlphabet.get(index));

		assertEquals(String.format("The index must be strictly smaller than the alphabet size. [index: %s, size: %s]", index, size),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("get with a valid index behaves as expected")
	@Test
	void getHappyPath() {
		final String expected = Character.toString(0x0027); // 4 (U+0034) -> 013

		final int index = 2;
		final String character = assertDoesNotThrow(() -> writeInAlphabet.get(index));

		assertEquals(expected, character);
	}

	@DisplayName("size behaves as expected")
	@Test
	void sizeHappyPath() {
		final int expected = 142;

		final int size = assertDoesNotThrow(writeInAlphabet::size);

		assertEquals(expected, size);
	}

	@DisplayName("contains with a character non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(strings = { "@", "?", "*", "!" })
	void containsWithNonCharacter(final String character) {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> writeInAlphabet.contains(character));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a null character behaves as expected")
	@Test
	void containsWithNullCharacter() {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> writeInAlphabet.contains(null));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a character part of the alphabet behaves as expected")
	@Test
	void containsCharacterHappyPath() {
		final boolean expected = true;

		final boolean isContained = assertDoesNotThrow(() -> writeInAlphabet.contains("a"));

		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a code point non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(ints = { 0x0040, 0x003F, 0x002A, 0x0021 })
	void containsWithNonCodePoint(final int codePoint) {
		final boolean expected = false;

		final boolean isContained = assertDoesNotThrow(() -> writeInAlphabet.contains(codePoint));
		assertEquals(expected, isContained);
	}

	@DisplayName("contains with a negative code point throws an IllegalArgumentException")
	@Test
	void containsCodePointNegativeThrows() {
		final int codePoint = -1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> writeInAlphabet.contains(codePoint));

		assertEquals(String.format("The provided code point is out-of-range. [codePoint: %s]", codePoint),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("contains with a bigger code point throws an IllegalArgumentException")
	@Test
	void containsCodePointBiggerThrows() {
		final int codePoint = Character.MAX_CODE_POINT + 1;

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> writeInAlphabet.contains(codePoint));

		assertEquals(String.format("The provided code point is out-of-range. [codePoint: %s]", codePoint),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@DisplayName("contains with a code point part of the alphabet behaves as expected")
	@Test
	void containsCodePointHappyPath() {
		final boolean expected = true;

		final boolean isContained = assertDoesNotThrow(() -> writeInAlphabet.contains(0x0034));

		assertEquals(expected, isContained);
	}

	@DisplayName("indexOf with a character non-part of the alphabet behaves as expected")
	@ParameterizedTest
	@ValueSource(strings = { "@", "?", "*", "!" })
	void indexOfNonPartHappyPath(final String character) {
		final int expected = -1;

		final int index = assertDoesNotThrow(() -> writeInAlphabet.indexOf(character));

		assertEquals(expected, index);
	}

	@DisplayName("indexOf with a null character behaves as expected")
	@Test
	void indexOfNullHappyPath() {
		final int expected = -1;

		final int index = assertDoesNotThrow(() -> writeInAlphabet.indexOf(null));

		assertEquals(expected, index);
	}

	@DisplayName("indexOf with a character part of the alphabet behaves as expected")
	@Test
	void indexOfHappyPath() {
		final int expected = 2;

		final int index = assertDoesNotThrow(() -> writeInAlphabet.indexOf(Character.toString(0x0027)));

		assertEquals(expected, index);
	}

}