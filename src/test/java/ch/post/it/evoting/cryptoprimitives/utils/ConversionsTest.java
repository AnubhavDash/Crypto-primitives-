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
package ch.post.it.evoting.cryptoprimitives.utils;

import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.byteArrayToInteger;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.byteArrayToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToFixedLengthByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.integerToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.stringToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.Conversions.stringToInteger;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;

class ConversionsTest {

	private static final TestRandomService randomService = new TestRandomService();

	@Nested
	@DisplayName("Test BigInteger to byte array conversion")
	class IntegerToByteArrayTest {
		@Test
		void testConversionOfNullBigIntegerToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> integerToByteArray(null));
		}

		@Test
		void testConversionOfZeroBigIntegerIsEmptyByte() {
			final BigInteger zero = BigInteger.ZERO;
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray converted = integerToByteArray(zero);
			assertEquals(expected, converted);
		}

		@Test
		void testConversionOf256BigIntegerIsTwoBytes() {
			final BigInteger value = BigInteger.valueOf(256);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 1, (byte) 0);
			final ImmutableByteArray converted = integerToByteArray(value);
			assertEquals(expected, converted);
		}

		@Test
		void testConversionOfIntegerMaxValuePlusOneIsCorrect() {
			final BigInteger value = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0b10000000, (byte) 0, (byte) 0, (byte) 0);
			final ImmutableByteArray converted = integerToByteArray(value);
			assertEquals(expected, converted);
		}

		@Test
		void testOfNegativeIntegerThrows() {
			final BigInteger value = BigInteger.valueOf(-1);
			assertThrows(IllegalArgumentException.class, () -> integerToByteArray(value));
		}
	}

	@Nested
	@DisplayName("Test BigInteger to byte array conversion")
	class IntegerToFixedLengthByteArrayTest {
		@Test
		void testConversionOfNullBigIntegerToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> integerToFixedLengthByteArray(null, 1));
		}

		@Test
		void testConversionOfZeroBigIntegerIsEmptyByte() {
			final BigInteger zero = BigInteger.ZERO;
			final ImmutableByteArray expected = ImmutableByteArray.EMPTY;
			final ImmutableByteArray converted = integerToFixedLengthByteArray(zero, 0);
			assertEquals(expected, converted);
		}

		@Test
		void testConversionOf256BigIntegerWithLengthOneThrows() {
			final BigInteger value = BigInteger.valueOf(256);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(value, 1));
			assertEquals("The desired length m must be greater than or equal to the byte length of x.", exception.getMessage());
		}

		@Test
		void testConversionOf256BigIntegerWithLengthThreeIsThreeBytes() {
			final BigInteger value = BigInteger.valueOf(256);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0, (byte) 1, (byte) 0);
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, 3);
			assertEquals(expected, converted);
		}

		@Test
		void testConversionOfIntegerMaxValuePlusOneIsCorrect() {
			final BigInteger value = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0b10000000, (byte) 0, (byte) 0, (byte) 0);
			final ImmutableByteArray converted = integerToFixedLengthByteArray(value, expected.length());
			assertEquals(expected, converted);
		}

		@Test
		void testOfNegativeIntegerThrows() {
			final BigInteger value = BigInteger.valueOf(-1);
			assertThrows(IllegalArgumentException.class, () -> integerToFixedLengthByteArray(value, 0));
		}
	}

	@Nested
	@DisplayName("Test byte array to BigInteger conversion")
	class ByteArrayToIntegerTest {
		@Test
		void testConversionOfNullToBigIntegerThrows() {
			assertThrows(NullPointerException.class, () -> byteArrayToInteger(null));
		}

		@Test
		void testConversionOfEmptyByteArrayToBigIntegerIsZero() {
			final BigInteger result = assertDoesNotThrow(() -> byteArrayToInteger(ImmutableByteArray.EMPTY));
			assertEquals(BigInteger.ZERO, result);
		}

		@Test
		void testConversionOfByteArrayWithLeading1ToBigIntegerIsPositive() {
			final ImmutableByteArray bytes = ImmutableByteArray.of((byte) 0x80);
			final BigInteger converted = byteArrayToInteger(bytes);
			assertTrue(converted.signum() > 0);
		}

		@Test
		void testConversionOf256ByteArrayRepresentationIs256() {
			final ImmutableByteArray bytes = ImmutableByteArray.of((byte) 1, (byte) 0);
			final BigInteger converted = byteArrayToInteger(bytes);
			assertEquals(0, converted.compareTo(BigInteger.valueOf(256)));
		}

		//Cyclic test BigInteger to byte array and back
		@RepeatedTest(10)
		void testRandomBigIntegerToByteArrayAndBackIsOriginalValue() {
			final int size = randomService.genRandomInteger(32) + 1;
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final BigInteger cycledValue = byteArrayToInteger(integerToByteArray(value));
			assertEquals(value, cycledValue);
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("Test String to BigInteger conversion")
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
		void stringToIntegerWithValidInputIsOk(final String s, final BigInteger expected) {
			final BigInteger converted = stringToInteger(s);
			assertEquals(expected, converted);
		}

		Stream<Arguments> stringToIntegerWithNonValidInputThrowsIllegalArgumentExceptionProvider() {
			return Stream.of(
					Arguments.of("", "The string to convert \"\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("A", "The string to convert \"A\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("1A", "The string to convert \"1A\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("A1", "The string to convert \"A1\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("+1", "The string to convert \"+1\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("1+", "The string to convert \"1+\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("-1", "The string to convert \"-1\" is not a valid decimal representation of a BigInteger."),
					Arguments.of("1-", "The string to convert \"1-\" is not a valid decimal representation of a BigInteger.")
			);
		}

		@ParameterizedTest(name = "s = \"{0}\", expectedExceptionMessage = \"{1}\"")
		@MethodSource("stringToIntegerWithNonValidInputThrowsIllegalArgumentExceptionProvider")
		void stringToIntegerWithNonValidInputThrowsIllegalArgumentException(final String s, final String expectedExceptionMessage) {
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> stringToInteger(s));

			assertEquals(expectedExceptionMessage, illegalArgumentException.getMessage());
		}

		@Test
		void stringToIntegerWithNullInputThrowsNullPointerException() {
			assertThrows(NullPointerException.class, () -> stringToInteger(null));
		}
	}

	@Nested
	@DisplayName("Test BigInteger/Integer to String conversion")
	class IntegerToStringTest {
		@Test
		void testIntegerToStringWithNullInputThrowsNullPointerException() {
			assertThrows(NullPointerException.class, () -> integerToString((BigInteger) null));

			assertThrows(NullPointerException.class, () -> integerToString((Integer) null));
		}

		@Test
		void testIntegerToStringWithNegativeInputThrowsIllegalArgumentException() {
			final BigInteger x = BigInteger.valueOf(-1L);
			assertThrows(IllegalArgumentException.class, () -> integerToString(x));

			final Integer y = -1;
			assertThrows(IllegalArgumentException.class, () -> integerToString(y));
		}
	}

	@Nested
	@DisplayName("Cyclic test BigInteger/Integer to String conversion and back")
	class CyclicIntegerToStringTest {
		@Test
		void testZeroBigIntegerToStringAndBackIsOriginalValue() {
			final BigInteger value = BigInteger.ZERO;
			final BigInteger cycledValue = stringToInteger(integerToString(value));
			assertEquals(value, cycledValue);
		}

		//Cyclic test BigInteger to String and back
		@RepeatedTest(10)
		void testRandomBigIntegerToStringAndBackIsOriginalValue() {
			final int size = randomService.genRandomInteger(32);
			final BigInteger value = randomService.genRandomIntegerOfLength(size);
			final BigInteger cycledValue = stringToInteger(integerToString(value));
			assertEquals(value, cycledValue);
		}

		@Test
		void testZeroIntegerToStringAndBackIsOriginalValue() {
			final Integer value = 0;
			final Integer cycledValue = stringToInteger(integerToString(value)).intValue();
			assertEquals(value, cycledValue);
		}

		@RepeatedTest(10)
		void testRandomIntegerToStringAndBackIsOriginalValue() {
			final Integer value = randomService.genRandomInteger(32);
			final Integer cycledValue = stringToInteger(integerToString(value)).intValue();
			assertEquals(value, cycledValue);
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("Test String to byte array conversion")
	class StringToByteArrayTest {
		@Test
		void testConversionOfNullStringToByteArrayThrows() {
			assertThrows(NullPointerException.class, () -> stringToByteArray(null));
		}

		@Test
		void testConversionOfEmptyStringToByteArrayGivesEmptyByteArray() {
			final ImmutableByteArray result = assertDoesNotThrow(() -> stringToByteArray(""));
			assertEquals(ImmutableByteArray.EMPTY, result);
		}

		Stream<String> invalidUTF8Strings() {
			return Stream.of(
					"\uD8E5",
					"All good until here \uDFFF",
					"\uDEEF and something else"
			);
		}

		@ParameterizedTest(name = "string = \"{0}\"")
		@MethodSource("invalidUTF8Strings")
		void testConversionOfNonUTF8StringToByteArrayThrows(final String str) {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> stringToByteArray(str));
			assertEquals("The string does not correspond to a valid sequence of UTF-8 encoding.", exception.getMessage());
		}

		@Test
		void testConversionWithSpecificStringReturnsExpectedValue() {
			final ImmutableByteArray expected = ImmutableByteArray.of((byte) -30, (byte) -126, (byte) -84);
			assertEquals(expected, stringToByteArray("€"));
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("Test byte array to String conversion")
	class ByteArrayToStringTest {
		@Test
		void testConversionOfNullByteArrayToStringThrows() {
			assertThrows(NullPointerException.class, () -> byteArrayToString(null));
		}

		@Test
		void testConversionOfZeroLengthByteArrayToStringGivesEmptyString() {
			final String result = assertDoesNotThrow(() -> byteArrayToString(ImmutableByteArray.EMPTY));
			assertEquals("", result);
		}

		Stream<ImmutableByteArray> invalidUTF8ByteArrays() {
			return Stream.of(
					ImmutableByteArray.of((byte) -37, (byte) -10),
					ImmutableByteArray.of((byte) -50, (byte) -29, (byte) 48),
					ImmutableByteArray.of((byte) 107, (byte) -93, (byte) 75, (byte) 41));
		}

		@ParameterizedTest(name = "byteArray = \"{0}\"")
		@MethodSource("invalidUTF8ByteArrays")
		void testConversionOfInvalidUTF8ByteArrayToStringThrows(final ImmutableByteArray byteArray) {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> byteArrayToString(byteArray));
			assertEquals("The byte array does not correspond to a valid sequence of UTF-8 encoding.", exception.getMessage());
		}

		@Test
		void testConversionWithSpecificByteArrayReturnsExpectedValue() {
			final String expected = "€";
			assertEquals(expected, byteArrayToString(ImmutableByteArray.of((byte) -30, (byte) -126, (byte) -84)));
		}

	}

	@Nested
	@DisplayName("Cyclic test String to byte array conversion and back")
	class CyclicStringToByteArrayTest {
		TestRandomService randomService = new TestRandomService();

		@RepeatedTest(10)
		void testRandomStringToByteArrayAndBackIsOriginalValue() {
			final String value = randomService.genRandomString(randomService.genRandomInteger(10) + 1, Base64Alphabet.getInstance());
			final ImmutableByteArray bytes = stringToByteArray(value);
			final String cycledValue = byteArrayToString(bytes);
			assertEquals(value, cycledValue);
		}
	}

}
