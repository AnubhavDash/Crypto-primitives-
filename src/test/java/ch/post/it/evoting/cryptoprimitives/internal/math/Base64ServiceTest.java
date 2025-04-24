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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

class Base64ServiceTest {

	private static RandomService randomService;
	private static Base64Service base64Service;

	@BeforeAll
	static void setupAll() {
		randomService = new TestRandomService();
		base64Service = new Base64Service();
	}

	private static Stream<Arguments> getInputsAndOutputs() {
		return Stream.of(
				Arguments.of(ImmutableByteArray.EMPTY, ""),
				Arguments.of(ImmutableByteArray.of((byte) 65), "QQ=="),
				Arguments.of(ImmutableByteArray.of((byte) 96), "YA=="),
				Arguments.of(ImmutableByteArray.of((byte) 0), "AA=="),
				Arguments.of(ImmutableByteArray.of((byte) 127), "fw=="),
				Arguments.of(ImmutableByteArray.of((byte) -128), "gA=="),
				Arguments.of(ImmutableByteArray.of((byte) -1), "/w=="),
				Arguments.of(ImmutableByteArray.of((byte) 65, (byte) 0), "QQA="),
				Arguments.of(ImmutableByteArray.of((byte) 1, (byte) 1, (byte) 1), "AQEB"),
				Arguments.of(ImmutableByteArray.of((byte) 127, (byte) 0, (byte) -2, (byte) 3), "fwD+Aw==")
		);
	}

	@ParameterizedTest
	@MethodSource("getInputsAndOutputs")
	@DisplayName("base64Encode with valid input gives expected output")
	void base64EncodeWithValidInputGivesExpectedResult(final ImmutableByteArray input, final String expectedOutput) {
		final String result = base64Service.base64Encode(input);

		assertEquals(expectedOutput, result);
	}

	@ParameterizedTest
	@MethodSource("getInputsAndOutputs")
	@DisplayName("base64Decode with valid inputs gives expected output")
	void base64DecodeWithValidInputGivesExpectedResult(final ImmutableByteArray expectedOutput, final String input) {
		final ImmutableByteArray result = base64Service.base64Decode(input);

		assertEquals(expectedOutput, result);
	}

	static Stream<String> getInvalidStrings() {
		return Stream.of("A=", "?sss", "Inv=====", "x-y.");
	}

	@ParameterizedTest
	@MethodSource("getInvalidStrings")
	@DisplayName("base64Decode with invalid strings throws an IllegalArgumentException")
	void base64DecodeWithInvalidStringsThrows(final String invalidString) {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> base64Service.base64Decode(invalidString));
		final String expectedErrorMessage = "The given string is not a valid Base64 string.";
		assertEquals(expectedErrorMessage, exception.getMessage());
	}

	@RepeatedTest(10)
	@DisplayName("base64Encode then base64Decode returns initial value")
	void base64EncodeThenBase64DecodeReturnsInitialValue() {
		final ImmutableByteArray randomBytes = randomService.randomBytes(16);

		final String string = base64Service.base64Encode(randomBytes);
		final ImmutableByteArray result = base64Service.base64Decode(string);
		assertEquals(randomBytes, result);
	}
}