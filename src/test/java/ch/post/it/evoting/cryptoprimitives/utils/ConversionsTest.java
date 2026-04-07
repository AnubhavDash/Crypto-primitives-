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
package ch.post.it.evoting.cryptoprimitives.utils;

import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.byteArrayToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToFixedLengthByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.stringToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.stringToInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;

class ConversionsTest {

	private static final TestRandomService randomService = new TestRandomService();

	@Nested
	@DisplayName("integerToByteArray")
	class IntegerToByteArrayTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testConversionOfNullBigIntegerToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> integerToByteArray(null));
		}

		@Test
		@DisplayName("of zero returns empty byte array")
		void testConversionOfZeroBigIntegerIsEmptyByte() {
			final BigInteger zero = BigInteger.ZERO;
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray converted = integerToByteArray(zero);
			assertEquals(expected, converted);
		}

		static Stream<Arguments> integerToByteArrayGoldenVectorsProvider() {
			return Stream.of(
					Arguments.of(BigInteger.valueOf(128), ImmutableByteArray.of((byte) 0x80)),
					Arguments.of(BigInteger.valueOf(255), ImmutableByteArray.of((byte) 0xFF)),
					Arguments.of(BigInteger.valueOf(256), ImmutableByteArray.of((byte) 1, (byte) 0)),
					Arguments.of(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE),
							ImmutableByteArray.of((byte) 0b10000000, (byte) 0, (byte) 0, (byte) 0))
			);
		}

		@ParameterizedTest(name = "integerToByteArray({0}) = {1}")
		@MethodSource("integerToByteArrayGoldenVectorsProvider")
		@DisplayName("of specific value returns expected bytes")
		void testIntegerToByteArrayGoldenVectors(final BigInteger value, final ImmutableByteArray expected) {
			assertEquals(expected, integerToByteArray(value));
		}

		@Test
		@DisplayName("with negative input throws IllegalArgumentException")
		void testOfNegativeIntegerThrows() {
			final BigInteger value = BigInteger.valueOf(-1);
			assertThrows(IllegalArgumentException.class, () -> integerToByteArray(value));
		}

		@RepeatedTest(10)
		@DisplayName("produces canonical form without leading zeros")
		void testIntegerToByteArrayProducesCanonicalForm() {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size).add(BigInteger.ONE);
			final ImmutableByteArray converted = integerToByteArray(value);

			assertFalse(converted.isEmpty(), "Positive value must produce non-empty byte array");
			final byte firstByte = converted.get(0);
			assertNotEquals(0, firstByte, "First byte must not be 0x00 for positive values (canonical form)");
		}

		@RepeatedTest(10)
		@DisplayName("round-trip preserves value")
		void testRandomBigIntegerToByteArrayAndBackIsOriginalValue() {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final BigInteger cycledValue = byteArrayToInteger(integerToByteArray(value));
			assertEquals(value, cycledValue);
		}
	}

	@Nested
	@DisplayName("integerToFixedLengthByteArray")
	class IntegerToFixedLengthByteArrayTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testConversionOfNullBigIntegerToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> integerToFixedLengthByteArray(null, 1));
		}

		@Test
		@DisplayName("with negative length throws IllegalArgumentException")
		void testConversionWithNegativeLengthThrows() {
			assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(BigInteger.ZERO, -1));
		}

		@Test
		@DisplayName("of zero with length 0 returns empty")
		void testConversionOfZeroBigIntegerIsEmptyByte() {
			final BigInteger zero = BigInteger.ZERO;
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray converted = integerToFixedLengthByteArray(zero, 0);
			assertEquals(expected, converted);
		}

		@Test
		@DisplayName("of positive value with length 0 throws")
		void testConversionOfPositiveValueWithLengthZeroThrows() {
			assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(BigInteger.ONE, 0));
		}

		static Stream<Arguments> zeroWithVariousLengthsProvider() {
			return Stream.of(
					Arguments.of(1, ImmutableByteArray.of((byte) 0x00)),
					Arguments.of(2, ImmutableByteArray.of((byte) 0x00, (byte) 0x00)),
					Arguments.of(32, ImmutableByteArray.of(new byte[32]))
			);
		}

		@ParameterizedTest(name = "integerToFixedLengthByteArray(0, {0}) = {1}")
		@MethodSource("zeroWithVariousLengthsProvider")
		@DisplayName("of zero with various lengths returns zero-padded array")
		void testConversionOfZeroWithVariousLengths(final int length, final ImmutableByteArray expected) {
			assertEquals(expected, integerToFixedLengthByteArray(BigInteger.ZERO, length));
		}

		@Test
		@DisplayName("of 256 with length 1 throws")
		void testConversionOf256BigIntegerWithLengthOneThrows() {
			final BigInteger value = BigInteger.valueOf(256);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(value, 1));
			assertEquals("The desired length m must be greater than or equal to the byte length of x.", exception.getMessage());
		}

		@Test
		@DisplayName("of 256 with length 3 returns padded")
		void testConversionOf256BigIntegerWithLengthThreeIsThreeBytes() {
			final BigInteger value = BigInteger.valueOf(256);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0, (byte) 1, (byte) 0);
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, 3);
			assertEquals(expected, converted);
		}

		@Test
		@DisplayName("of Integer.MAX_VALUE+1 returns expected bytes")
		void testConversionOfIntegerMaxValuePlusOneIsCorrect() {
			final BigInteger value = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0b10000000, (byte) 0, (byte) 0, (byte) 0);
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, expected.length());
			assertEquals(expected, converted);
		}

		@Test
		@DisplayName("with negative input throws")
		void testOfNegativeIntegerThrows() {
			final BigInteger value = BigInteger.valueOf(-1);
			assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(value, 0));
		}

		@Test
		@DisplayName("high-bit stripping regression test for 2^255 with length 32")
		void testHighBitStrippingRegressionFor2Power255() {
			final BigInteger value = BigInteger.ONE.shiftLeft(255);
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, 32);

			assertEquals(32, converted.length(), "Result must be exactly 32 bytes");
			assertEquals((byte) 0x80, converted.get(0), "First byte must be 0x80");

			// Verify all remaining bytes are zero
			for (int i = 1; i < 32; i++) {
				assertEquals((byte) 0x00, converted.get(i), "Byte at index " + i + " must be 0x00");
			}
		}

		@ParameterizedTest(name = "padding offset = {0}")
		@ValueSource(ints = { 0, 1, 2 })
		@DisplayName("round-trip with padding offset preserves value")
		void testRoundTripWithPaddingOffset(final int paddingOffset) {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final int byteLength = (value.bitLength() + 7) / 8;
			final int targetLength = byteLength + paddingOffset;
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, targetLength);

			assertEquals(targetLength, converted.length(), "Result length must match requested length");
			assertEquals(value, byteArrayToInteger(converted), "Round-trip must preserve value");
		}
	}

	@Nested
	@DisplayName("byteArrayToInteger")
	class ByteArrayToIntegerTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testConversionOfNullToBigIntegerThrows() {
			assertThrows(NullPointerException.class, () -> byteArrayToInteger(null));
		}

		@Test
		@DisplayName("with empty byte array returns zero")
		void testConversionOfEmptyByteArrayToBigIntegerIsZero() {
			assertEquals(BigInteger.ZERO, byteArrayToInteger(ImmutableByteArray.EMPTY));
		}

		static Stream<Arguments> byteArrayToIntegerGoldenVectorsProvider() {
			return Stream.of(
					Arguments.of(ImmutableByteArray.of((byte) 0x80), BigInteger.valueOf(128)),
					Arguments.of(ImmutableByteArray.of((byte) 0x00), BigInteger.ZERO),
					Arguments.of(ImmutableByteArray.of((byte) 0x00, (byte) 0x00), BigInteger.ZERO),
					Arguments.of(ImmutableByteArray.of((byte) 0x00, (byte) 0x80), BigInteger.valueOf(128)),
					Arguments.of(ImmutableByteArray.of((byte) 1, (byte) 0), BigInteger.valueOf(256))
			);
		}

		@ParameterizedTest(name = "byteArrayToInteger({0}) = {1}")
		@MethodSource("byteArrayToIntegerGoldenVectorsProvider")
		@DisplayName("of specific bytes returns expected value")
		void testByteArrayToIntegerGoldenVectors(final ImmutableByteArray bytes, final BigInteger expected) {
			assertEquals(expected, byteArrayToInteger(bytes));
		}

		static Stream<Arguments> canonicalizationProvider() {
			return Stream.of(
					Arguments.of(ImmutableByteArray.of((byte) 0x00, (byte) 0x01), ImmutableByteArray.of((byte) 0x01)),
					Arguments.of(ImmutableByteArray.of((byte) 0x00, (byte) 0x00, (byte) 0x01), ImmutableByteArray.of((byte) 0x01))
			);
		}

		@ParameterizedTest(name = "re-encoding {0} produces canonical {1}")
		@MethodSource("canonicalizationProvider")
		@DisplayName("canonicalization: re-encoding non-canonical input removes leading zeros")
		void testCanonicalization(final ImmutableByteArray nonCanonical, final ImmutableByteArray expectedCanonical) {
			assertEquals(expectedCanonical, integerToByteArray(byteArrayToInteger(nonCanonical)));
		}

		@RepeatedTest(10)
		@DisplayName("round-trip preserves value")
		void testRandomBigIntegerToByteArrayAndBackIsOriginalValue() {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final BigInteger cycledValue = byteArrayToInteger(integerToByteArray(value));
			assertEquals(value, cycledValue);
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("stringToInteger")
	class StringToIntegerTest {

		Stream<Arguments> stringToIntegerWithValidInputIsOkProvider() {
			return Stream.of(
					Arguments.of("0", BigInteger.ZERO),
					Arguments.of("1", BigInteger.ONE),
					Arguments.of("1001", BigInteger.valueOf(1001L)),
					Arguments.of("0021", BigInteger.valueOf(21L))
			);
		}

		@ParameterizedTest(name = "s = \"{0}\", expected = {1}")
		@MethodSource("stringToIntegerWithValidInputIsOkProvider")
		@DisplayName("with valid input returns expected value")
		void stringToIntegerWithValidInputIsOk(final String s, final BigInteger expected) {
			final BigInteger converted = stringToInteger(s);
			assertEquals(expected, converted);
		}

		@Test
		@DisplayName("with all zeros returns zero")
		void testStringToIntegerWithAllZerosReturnsZero() {
			final String s = "0000";
			final BigInteger converted = stringToInteger(s);
			assertEquals(BigInteger.ZERO, converted);
		}

		@Test
		@DisplayName("with large 100+ digit input is handled correctly")
		void testStringToIntegerWithLargeInputIsHandledCorrectly() {
			// 100-digit decimal number: 10^99
			final String largeDecimalString = "1" + "0".repeat(99);
			final BigInteger expected = BigInteger.TEN.pow(99);
			final BigInteger converted = stringToInteger(largeDecimalString);
			assertEquals(expected, converted);
		}

		Stream<Arguments> stringToIntegerWithNonValidInputThrowsIllegalArgumentExceptionProvider() {
			return Stream.of(
					Arguments.of("", "The string to convert \"\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("A", "The string to convert \"A\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1A", "The string to convert \"1A\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("A1", "The string to convert \"A1\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("+1", "The string to convert \"+1\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1+", "The string to convert \"1+\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("-1", "The string to convert \"-1\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1-", "The string to convert \"1-\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1 1", "The string to convert \"1 1\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of(" 1", "The string to convert \" 1\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1 ", "The string to convert \"1 \" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1\t", "The string to convert \"1\t\" is not a valid non-negative decimal representation of a BigInteger."),
					Arguments.of("1\n", "The string to convert \"1\n\" is not a valid non-negative decimal representation of a BigInteger.")
			);
		}

		@ParameterizedTest(name = "s = \"{0}\", expectedExceptionMessage = \"{1}\"")
		@MethodSource("stringToIntegerWithNonValidInputThrowsIllegalArgumentExceptionProvider")
		@DisplayName("with invalid input throws IllegalArgumentException")
		void stringToIntegerWithNonValidInputThrowsIllegalArgumentException(final String s, final String expectedExceptionMessage) {
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> stringToInteger(s));

			assertEquals(expectedExceptionMessage, illegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("with null throws NullPointerException")
		void stringToIntegerWithNullInputThrowsNullPointerException() {
			assertThrows(NullPointerException.class, () -> stringToInteger(null));
		}
	}

	@Nested
	@DisplayName("integerToString")
	class IntegerToStringTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testIntegerToStringWithNullInputThrowsNullPointerException() {
			assertThrows(NullPointerException.class, () -> integerToString((BigInteger) null));

			assertThrows(NullPointerException.class, () -> integerToString((Integer) null));
		}

		@Test
		@DisplayName("with negative input throws IllegalArgumentException")
		void testIntegerToStringWithNegativeInputThrowsIllegalArgumentException() {
			final BigInteger x = BigInteger.valueOf(-1L);
			assertThrows(IllegalArgumentException.class, () -> integerToString(x));

			final Integer y = -1;
			assertThrows(IllegalArgumentException.class, () -> integerToString(y));
		}

		static Stream<Arguments> bigIntegerToStringGoldenVectorsProvider() {
			return Stream.of(
					Arguments.of(BigInteger.ZERO, "0"),
					Arguments.of(BigInteger.valueOf(42), "42")
			);
		}

		@ParameterizedTest(name = "integerToString({0}) = \"{1}\"")
		@MethodSource("bigIntegerToStringGoldenVectorsProvider")
		@DisplayName("(BigInteger) of specific value returns expected string")
		void testIntegerToStringOfBigIntegerReturnsExpectedString(final BigInteger value, final String expected) {
			assertEquals(expected, integerToString(value));
		}

		static Stream<Arguments> integerToStringGoldenVectorsProvider() {
			return Stream.of(
					Arguments.of(0, "0"),
					Arguments.of(42, "42")
			);
		}

		@ParameterizedTest(name = "integerToString({0}) = \"{1}\"")
		@MethodSource("integerToStringGoldenVectorsProvider")
		@DisplayName("(Integer) of specific value returns expected string")
		void testIntegerToStringOfIntegerReturnsExpectedString(final Integer value, final String expected) {
			assertEquals(expected, integerToString(value));
		}
	}

	@Nested
	@DisplayName("integerToString round-trip")
	class CyclicIntegerToStringTest {


		@Test
		@DisplayName("zero BigInteger preserves value")
		void testZeroBigIntegerToStringAndBackIsOriginalValue() {
			final BigInteger value = BigInteger.ZERO;
			final BigInteger cycledValue = stringToInteger(integerToString(value));
			assertEquals(value, cycledValue);
		}

		@RepeatedTest(10)
		@DisplayName("random BigInteger preserves value")
		void testRandomBigIntegerToStringAndBackIsOriginalValue() {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final BigInteger cycledValue = stringToInteger(integerToString(value));
			assertEquals(value, cycledValue);
		}

		@Test
		@DisplayName("zero Integer preserves value")
		void testZeroIntegerToStringAndBackIsOriginalValue() {
			final Integer value = 0;
			final Integer cycledValue = stringToInteger(integerToString(value)).intValue();
			assertEquals(value, cycledValue);
		}

		@RepeatedTest(10)
		@DisplayName("random Integer preserves value")
		void testRandomIntegerToStringAndBackIsOriginalValue() {
			final Integer value = randomService.genRandomInteger(32);
			final Integer cycledValue = stringToInteger(integerToString(value)).intValue();
			assertEquals(value, cycledValue);
		}

		@Test
		@DisplayName("Integer.MAX_VALUE preserves value")
		void testIntegerMaxValueToStringAndBackIsOriginalValue() {
			final Integer value = Integer.MAX_VALUE;
			final Integer cycledValue = stringToInteger(integerToString(value)).intValue();
			assertEquals(value, cycledValue);
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("stringToByteArray")
	class StringToByteArrayTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testConversionOfNullStringToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> stringToByteArray(null));
		}

		@Test
		@DisplayName("with empty string returns empty byte array")
		void testConversionOfEmptyStringToByteArrayGivesEmptyByteArray() {
			assertEquals(ImmutableByteArray.EMPTY, stringToByteArray(""));
		}

		Stream<String> invalidUTF8Strings() {
			return Stream.of(
					"\uD800",  // Unpaired high surrogate
					"\uDC00",  // Unpaired low surrogate
					"text\uD800",  // High surrogate at end
					"\uDC00text"   // Low surrogate at start
			);
		}

		@ParameterizedTest(name = "string = \"{0}\"")
		@MethodSource("invalidUTF8Strings")
		@DisplayName("with unpaired surrogates throws IllegalArgumentException")
		void testConversionOfUnpairedSurrogatesToByteArrayThrows(final String str) {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> stringToByteArray(str));
			assertEquals("The string does not correspond to a valid sequence of UTF-8 encoding.", exception.getMessage());
		}

		static Stream<Arguments> knownUTF8EncodingVectorsProvider() {
			return Stream.of(
					Arguments.of("😀", ImmutableByteArray.of((byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80)), // U+1F600, surrogate pair
					Arguments.of("€", ImmutableByteArray.of((byte) 0xE2, (byte) 0x82, (byte) 0xAC))                // U+20AC, 3-byte sequence
			);
		}

		@ParameterizedTest(name = "stringToByteArray(\"{0}\") = {1}")
		@MethodSource("knownUTF8EncodingVectorsProvider")
		@DisplayName("of known characters returns expected UTF-8 bytes")
		void testConversionOfKnownCharactersReturnsExpectedUTF8Bytes(final String input, final ImmutableByteArray expected) {
			assertEquals(expected, stringToByteArray(input));
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("byteArrayToString")
	class ByteArrayToStringTest {

		@Test
		@DisplayName("with null throws NullPointerException")
		void testConversionOfNullByteArrayToStringThrows() {
			assertThrows(NullPointerException.class, () -> byteArrayToString(null));
		}

		@Test
		@DisplayName("with empty byte array returns empty string")
		void testConversionOfZeroLengthByteArrayToStringGivesEmptyString() {
			assertEquals("", byteArrayToString(ImmutableByteArray.EMPTY));
		}

		Stream<ImmutableByteArray> invalidUTF8ByteArrays() {
			return Stream.of(
					ImmutableByteArray.of((byte) 0x80),                              // Isolated continuation byte
					ImmutableByteArray.of((byte) 0xC0, (byte) 0xAF),                // Overlong encoding
					ImmutableByteArray.of((byte) 0xE2, (byte) 0x82),                // Truncated 3-byte sequence
					ImmutableByteArray.of((byte) 0xF0, (byte) 0x9F, (byte) 0x98),  // Truncated 4-byte sequence
					ImmutableByteArray.of((byte) 0xFF)                               // Illegal leading byte
			);
		}

		@ParameterizedTest(name = "byteArray = \"{0}\"")
		@MethodSource("invalidUTF8ByteArrays")
		@DisplayName("with malformed UTF-8 throws IllegalArgumentException")
		void testConversionOfMalformedUTF8ByteArrayToStringThrows(final ImmutableByteArray byteArray) {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> byteArrayToString(byteArray));
			assertEquals("The byte array does not correspond to a valid sequence of UTF-8 encoding.", exception.getMessage());
		}

		static Stream<Arguments> knownUTF8DecodingVectorsProvider() {
			return Stream.of(
					Arguments.of(ImmutableByteArray.of((byte) 0xE2, (byte) 0x82, (byte) 0xAC), "€"),
					Arguments.of(ImmutableByteArray.of((byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80), "😀")
			);
		}

		@ParameterizedTest(name = "byteArrayToString({0}) = \"{1}\"")
		@MethodSource("knownUTF8DecodingVectorsProvider")
		@DisplayName("of known UTF-8 vectors returns expected string")
		void testConversionOfKnownUTF8VectorsReturnsExpectedString(final ImmutableByteArray input, final String expected) {
			assertEquals(expected, byteArrayToString(input));
		}

	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("stringToByteArray round-trip")
	class CyclicStringToByteArrayTest {

		@RepeatedTest(10)
		@DisplayName("random ASCII String preserves value")
		void testRandomASCIIStringToByteArrayAndBackIsOriginalValue() {
			final String value = randomService.genRandomString(randomService.genRandomInteger(10) + 1, Base64Alphabet.getInstance());
			final ImmutableByteArray bytes = stringToByteArray(value);
			final String cycledValue = byteArrayToString(bytes);
			assertEquals(value, cycledValue);
		}

		Stream<String> nonASCIIStrings() {
			return Stream.of(
					"é",        // Latin-1 Supplement (2-byte UTF-8)
					"€",        // Euro sign (3-byte UTF-8)
					"😀",       // Emoji (4-byte UTF-8, non-BMP)
					"中文",     // Chinese characters (3-byte UTF-8 each)
					"Hello",    // ASCII for comparison
					"Héllo€",   // Mixed ASCII and non-ASCII
					"😀🎉中文"   // Mixed multi-byte characters
			);
		}

		@ParameterizedTest(name = "string = \"{0}\"")
		@MethodSource("nonASCIIStrings")
		@DisplayName("non-ASCII String preserves value")
		void testNonASCIIStringToByteArrayAndBackIsOriginalValue(final String value) {
			final ImmutableByteArray bytes = stringToByteArray(value);
			final String cycledValue = byteArrayToString(bytes);
			assertEquals(value, cycledValue);
		}
	}

}
