/*
 * Copyright 2022 Post CH Ltd
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
package ch.post.it.evoting.cryptoprimitives.internal.hashing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Profile;
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Hash;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base64;
import ch.post.it.evoting.cryptoprimitives.math.BaseEncodingFactory;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

@DisplayName("Argon2id")
class Argon2ServiceTest {

	private static RandomService randomService;
	private static Base64 base64;

	@BeforeAll
	static void setup() {
		randomService = Mockito.mock(RandomService.class);
		base64 = BaseEncodingFactory.createBase64();
	}

	@Nested
	@DisplayName("genArgon2id with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenArgon2idTest {

		@Test
		@DisplayName("empty byte array")
		void genArgon2idWithEmptyInput() {
			// Given
			when(randomService.randomBytes(16))
					.thenReturn(HexFormat.of().parseHex("7332424c365a744a44376e784b7a576e"));
			final Argon2Profile config = Argon2Profile.TEST;

			// When
			final Argon2Service service = new Argon2Service(randomService, config);
			final Argon2Hash argon2Hash = service.genArgon2id(new byte[] {});

			// Then
			assertArrayEquals(HexFormat.of().parseHex("f11fb1bd1d0240f57064cb14e8281509447719c6090e7d37f37f4831af81b6e8"), argon2Hash.getTag());
		}

		private Stream<Arguments> genArgon2idJsonFileArgumentProvider() {
			final List<TestParameters> parametersList = TestParameters.fromResource("/hash/gen-argon2id.json");

			return parametersList.stream().parallel().map(testParameters -> {
				// Context.
				final JsonData context = testParameters.getContext();
				final Integer m = context.get("m", Integer.class);
				final Integer p = context.get("p", Integer.class);
				final Integer i = context.get("i", Integer.class);



				// Input.
				final JsonData input = testParameters.getInput();
				final String k = input.get("k", String.class);

				// Mocked.
				final JsonData mocked = testParameters.getMocked();
				final String mocked_s = mocked.get("s", String.class);

				// Output.
				final JsonData output = testParameters.getOutput();
				final String t = output.get("t", String.class);
				final String s = output.get("s", String.class);

				return Arguments.of(m, p, i, k, mocked_s, t, s, testParameters.getDescription());
			});
		}

		@ParameterizedTest()
		@MethodSource("genArgon2idJsonFileArgumentProvider")
		@DisplayName("known values yield the expected result")
		void genArgon2idWithRealValues(final Integer m, final Integer p, final Integer i, final String k, final String mocked_s, final String t,
				final String s, final String description) {
			// Given
			when(randomService.randomBytes(16)).thenReturn(base64.base64Decode(mocked_s));
			final Argon2Profile config = Arrays.stream(Argon2Profile.values())
					.filter(profile -> (profile.get_m() == m) && (profile.get_p() == p) && (profile.get_i() == i))
					.findFirst().orElseThrow();

			// When
			final Argon2Service service = new Argon2Service(randomService, config);
			final Argon2Hash argon2Hash = service.genArgon2id(base64.base64Decode(k));

			// Then
			assertArrayEquals(base64.base64Decode(t), argon2Hash.getTag(), String.format("tag assertion failed for: %s", description));
			assertArrayEquals(base64.base64Decode(s), argon2Hash.getSalt(), String.format("salt assertion failed for: %s", description));
		}

	}

	@Nested
	@DisplayName("getArgon2id with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GetArgon2idTest {

		@Test
		@DisplayName("empty byte array")
		void getArgon2idWithEmptyInput() {
			// Given
			final Argon2Profile config = Argon2Profile.TEST;

			// When
			final Argon2Service service = new Argon2Service(randomService, config);
			final byte[] t = service.getArgon2id(new byte[] {}, HexFormat.of().parseHex("7332424c365a744a44376e784b7a576e"));

			// Then
			assertArrayEquals(HexFormat.of().parseHex("f11fb1bd1d0240f57064cb14e8281509447719c6090e7d37f37f4831af81b6e8"), t);
		}

		private Stream<Arguments> getArgon2idJsonFileArgumentProvider() {
			final List<TestParameters> parametersList = TestParameters.fromResource("/hash/get-argon2id.json");

			return parametersList.stream().parallel().map(testParameters -> {
				// Context.
				final JsonData context = testParameters.getContext();
				final Integer m = context.get("m", Integer.class);
				final Integer p = context.get("p", Integer.class);
				final Integer i = context.get("i", Integer.class);

				// Input.
				final JsonData input = testParameters.getInput();
				final String k = input.get("k", String.class);
				final String s = input.get("s", String.class);

				// Output.
				final JsonData output = testParameters.getOutput();
				final String t = output.get("t", String.class);

				return Arguments.of(m, p, i, k, s, t, testParameters.getDescription());
			});
		}

		@ParameterizedTest()
		@MethodSource("getArgon2idJsonFileArgumentProvider")
		@DisplayName("known values yield the expected result")
		void getArgon2idWithRealValues(final Integer m, final Integer p, final Integer i, final String k, final String s, final String t,
				final String description) {
			// Given
			final Argon2Profile config = Arrays.stream(Argon2Profile.values())
					.filter(profile -> (profile.get_m() == m) && (profile.get_p() == p) && (profile.get_i() == i))
					.findFirst().orElseThrow();

			// When
			final Argon2Service service = new Argon2Service(randomService, config);
			final byte[] actual_t = service.getArgon2id(base64.base64Decode(k), base64.base64Decode(s));

			// Then
			assertArrayEquals(base64.base64Decode(t), actual_t, String.format("tag assertion failed for: %s", description));
		}

	}

}
