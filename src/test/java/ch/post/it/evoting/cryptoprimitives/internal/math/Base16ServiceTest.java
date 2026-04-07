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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

class Base16ServiceTest {

	private static RandomService randomService;
	private static Base16Service base16Service;

	@BeforeAll
	static void setupAll() {
		randomService = new TestRandomService();
		base16Service = new Base16Service();
	}

	private static Stream<Arguments> getInputsAndOutputs() {
		return Stream.of(
				Arguments.of(ImmutableByteArray.EMPTY, ""),
				Arguments.of(ImmutableByteArray.of((byte) 65), "41"),
				Arguments.of(ImmutableByteArray.of((byte) 96), "60"),
				Arguments.of(ImmutableByteArray.of((byte) 0), "00"),
				Arguments.of(ImmutableByteArray.of((byte) 127), "7F"),
				Arguments.of(ImmutableByteArray.of((byte) -128), "80"),
				Arguments.of(ImmutableByteArray.of((byte) -1), "FF"),
				Arguments.of(ImmutableByteArray.of((byte) 65, (byte) 0), "4100"),
				Arguments.of(ImmutableByteArray.of((byte) 1, (byte) 1, (byte) 1), "010101"),
				Arguments.of(ImmutableByteArray.of((byte) 127, (byte) 0, (byte) -2, (byte) 3), "7F00FE03")
		);
	}

	@ParameterizedTest
	@MethodSource("getInputsAndOutputs")
	@DisplayName("base16Encode with valid input gives expected output")
	void base16EncodeWithValidInputGivesExpectedResult(final ImmutableByteArray input, final String expectedOutput) {
		final String result = base16Service.base16Encode(input);

		assertEquals(expectedOutput, result);
	}

	@ParameterizedTest
	@MethodSource("getInputsAndOutputs")
	@DisplayName("base16Decode with valid inputs gives expected output")
	void base16DecodeWithValidInputGivesExpectedResult(final ImmutableByteArray expectedOutput, final String input) {
		final ImmutableByteArray result = base16Service.base16Decode(input);

		assertEquals(expectedOutput, result);
	}

	@RepeatedTest(10)
	@DisplayName("base16Encode then base16Decode returns initial value")
	void base16EncodeThenBase16DecodeReturnsInitialValue() {
		final ImmutableByteArray randomBytes = randomService.randomBytes(16);

		final String string = base16Service.base16Encode(randomBytes);
		final ImmutableByteArray result = base16Service.base16Decode(string);
		assertEquals(randomBytes, result);
	}
}
