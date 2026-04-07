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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

class ByteArraysTest {

	@Nested
	@DisplayName("cutToBitLength")
	class CutToBitLengthTest {

		@Test
		@DisplayName("null input throws NullPointerException")
		void testCutToBitLengthWithNullThrows() {
			assertThrows(NullPointerException.class, () -> ByteArrays.cutToBitLength(null, 1));
		}

		@Test
		@DisplayName("negative requested length throws IllegalArgumentException")
		void testCutToBitLengthRequestedLengthNegativeThrows() {
			final ImmutableByteArray immutableByteArray = ImmutableByteArray.of((byte) 0b10011);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> ByteArrays.cutToBitLength(immutableByteArray, -1));
			assertEquals("The requested length must be non-negative. [n: -1]", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("requested length 0 returns empty byte array")
		void testCutToBitLengthRequestedLengthZeroReturnsEmpty() {
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray result = ByteArrays.cutToBitLength(ImmutableByteArray.of((byte) 0b10011), 0);
			assertEquals(expected, result);
		}

		@Test
		@DisplayName("requested length greater than bit length throws IllegalArgumentException")
		void testCutToBitLengthRequestedLengthGreaterThanByteArrayBitLengthThrows() {
			final ImmutableByteArray immutableByteArray = ImmutableByteArray.of((byte) 0b1001101);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> ByteArrays.cutToBitLength(immutableByteArray, 9));
			assertEquals("The requested length must not be greater than the bit length of the byte array. [n: 9, bitLength: 8]",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("empty input with length 0 returns empty")
		void testCutToBitLengthWithEmptyByteArrayAndZeroLengthReturnsEmpty() {
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray result = ByteArrays.cutToBitLength(ImmutableByteArray.EMPTY, 0);
			assertEquals(expected, result);
		}

		@Test
		@DisplayName("empty input with positive length throws IllegalArgumentException")
		void testCutToBitLengthWithEmptyByteArrayAndPositiveLengthThrows() {
			final ImmutableByteArray immutableByteArray = ImmutableByteArray.EMPTY;
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> ByteArrays.cutToBitLength(immutableByteArray, 1));
			assertEquals("The requested length must not be greater than the bit length of the byte array. [n: 1, bitLength: 0]",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("exact byte boundary keeps least significant bytes")
		void testCutToBitLengthExactByteBoundaryKeepsLeastSignificantBytes() {
			final ImmutableByteArray input = ImmutableByteArray.of((byte) 0x12, (byte) 0x34, (byte) 0x56);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0x34, (byte) 0x56);
			final ImmutableByteArray result = ByteArrays.cutToBitLength(input, 16);
			assertEquals(expected, result);
		}

		@Test
		@DisplayName("non-byte-aligned length masks most significant selected byte")
		void testCutToBitLengthNonByteAlignedMasksMostSignificantSelectedByte() {
			final ImmutableByteArray input = ImmutableByteArray.of((byte) 0x12, (byte) 0x34);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0x02, (byte) 0x34);
			final ImmutableByteArray result = ByteArrays.cutToBitLength(input, 12);
			assertEquals(expected, result);
		}

		@Test
		@DisplayName("single-bit length keeps only the least significant bit")
		void testCutToBitLengthSingleBitKeepsLeastSignificantBit() {
			final ImmutableByteArray input = ImmutableByteArray.of((byte) 0xFF);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0x01);
			final ImmutableByteArray result = ByteArrays.cutToBitLength(input, 1);
			assertEquals(expected, result);
		}

		static Stream<Arguments> jsonFileCutToBitLengthArgumentProvider() {
			final ImmutableList<TestParameters> parametersList = TestParameters.fromResource("/cut-to-bit-length.json");

			return parametersList.stream().parallel().map(testParameters -> {
				final String description = testParameters.getDescription();

				final JsonData input = testParameters.getInput();
				final Integer bitLength = input.get("bit_length", Integer.class);
				final ImmutableByteArray value = input.get("value", ImmutableByteArray.class);

				final JsonData output = testParameters.getOutput();
				final ImmutableByteArray result = output.get("result", ImmutableByteArray.class);

				return Arguments.of(value, bitLength, result, description);
			});
		}

		@ParameterizedTest
		@MethodSource("jsonFileCutToBitLengthArgumentProvider")
		@DisplayName("cutToBitLength of specific input returns expected output")
		void testCutToBitLengthWithRealValues(final ImmutableByteArray byteArray, final int requestedLength, final ImmutableByteArray expectedResult,
				final String description) {
			final ImmutableByteArray actualResult = ByteArrays.cutToBitLength(byteArray, requestedLength);
			assertEquals(expectedResult, actualResult, String.format("assertion failed for: %s", description));
		}
	}

	@Nested
	@DisplayName("byteLength")
	class ByteLengthTest {

		@Test
		@DisplayName("byteLength with null argument throws NullPointerException")
		void testByteLengthWithNullThrows() {
			assertThrows(NullPointerException.class, () -> ByteArrays.byteLength(null));
		}

		@Test
		@DisplayName("byteLength with negative input throws IllegalArgumentException")
		void testByteLengthWithNegativeInputThrows() {
			final BigInteger negativeValue = BigInteger.valueOf(-1);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ByteArrays.byteLength(negativeValue));
			assertEquals("The input must be non-negative. [x: -1]", exception.getMessage());
		}

		static Stream<Arguments> byteLengthArgumentProvider() {
			return Stream.of(
					Arguments.of(BigInteger.ZERO, 0),
					Arguments.of(BigInteger.ONE, 1),
					Arguments.of(BigInteger.valueOf(255), 1),
					Arguments.of(BigInteger.valueOf(256), 2),
					Arguments.of(BigInteger.valueOf(Integer.MAX_VALUE), 4),
					Arguments.of(BigInteger.ONE.shiftLeft(255), 32),
					Arguments.of(BigInteger.ONE.shiftLeft(256), 33)
			);
		}

		@ParameterizedTest
		@MethodSource("byteLengthArgumentProvider")
		@DisplayName("byteLength with valid input returns expected output")
		void testByteLengthWithValidInputReturnsExpectedOutput(final BigInteger input, final int expectedOutput) {
			final int result = ByteArrays.byteLength(input);

			assertEquals(expectedOutput, result);
		}
	}
}
