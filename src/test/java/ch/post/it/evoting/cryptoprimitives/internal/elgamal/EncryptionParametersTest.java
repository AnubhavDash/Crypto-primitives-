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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

@DisplayName("An EncryptionParameters object")
class EncryptionParametersTest {

	private static final String SEED = "Election_name";
	private static final List<Integer> SMALL_PRIMES = PrimesInternal.getSmallPrimes();
	private static final int NAME_MAX_LENGTH = 10;

	private static EncryptionParameters encryptionParameters;

	private final Random random = new RandomService();
	private final SecureRandom secureRandom = new SecureRandom();

	@BeforeAll
	static void setUpAll() {
		encryptionParameters = new EncryptionParameters();
	}

	@Test
	@DisplayName("calling getEncryptionParameters with null seed throws NullPointerException")
	void getEncryptionParametersNullSeed() {
		assertThrows(NullPointerException.class, () -> encryptionParameters.getEncryptionParameters(null, SMALL_PRIMES));
	}

	@Test
	@DisplayName("calling getEncryptionParameters with null small primes list throws NullPointerException")
	void getEncryptionParametersNullSmallPrimes() {
		assertThrows(NullPointerException.class, () -> encryptionParameters.getEncryptionParameters(SEED, null));
	}

	@Test
	@DisplayName("calling getEncryptionParameters with small primes list containing non-prime throws IllegalArgumentException")
	void getEncryptionParametersWithNonPrimeInSmallPrimesThrows() {
		final List<Integer> listWithNonPrime = new ArrayList<>(List.of(7, 8, 9, 10, 11));
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> encryptionParameters.getEncryptionParameters(SEED, listWithNonPrime));
		assertEquals("The given number is not a prime. [Number: 8]", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("calling getEncryptionParameters with an empty small primes list does not throw")
	void getEncryptionParametersEmptySmallPrimesDoesNotThrow() {
		assertDoesNotThrow(() -> encryptionParameters.getEncryptionParameters(SEED, Collections.emptyList()));
	}

	@Test
	@DisplayName("calling getEncryptionParameters with fixed seed gives expected parameters")
	void getEncryptionParametersFixedSeed() {
		final GqGroup expectedParameters = new GqGroup(BigInteger.valueOf(208155596507627L), BigInteger.valueOf(104077798253813L),
				BigInteger.valueOf(3));

		assertEquals(expectedParameters, encryptionParameters.getEncryptionParameters(SEED, SMALL_PRIMES));
	}

	@Test
	@DisplayName("calling getEncryptionParameters twice with the same seed but different small primes gives the same result")
	void getEncryptionParametersTwice() {
		final int electionNameLength = secureRandom.nextInt(NAME_MAX_LENGTH) + 1;
		final String randomSeed = random.genRandomBase64String(electionNameLength);
		final GqGroup gqGroup1 = encryptionParameters.getEncryptionParameters(randomSeed, SMALL_PRIMES);
		final GqGroup gqGroup2 = encryptionParameters.getEncryptionParameters(randomSeed, Collections.emptyList());

		assertEquals(gqGroup1, gqGroup2);
	}

	@RepeatedTest(100)
	@DisplayName("calling getEncryptionParameters with random seed does not throw")
	void getEncryptionParametersRandomSeed() {
		final int electionNameLength = secureRandom.nextInt(NAME_MAX_LENGTH) + 1;
		final String randomSeed = random.genRandomBase64String(electionNameLength);

		assertDoesNotThrow(() -> encryptionParameters.getEncryptionParameters(randomSeed, SMALL_PRIMES));
	}

	static Stream<Arguments> getEncryptionParametersProvider() {
		final List<TestParameters> parametersList = TestParameters.fromResource("/elgamal/get-encryption-parameters.json");

		return parametersList.stream().parallel().map(testParameters -> {
			// Inputs.
			final JsonData input = testParameters.getInput();
			final String seed = input.get("seed", String.class);

			// Outputs.
			final JsonData output = testParameters.getOutput();
			final BigInteger p = output.get("p", BigInteger.class);
			final BigInteger q = output.get("q", BigInteger.class);
			final BigInteger g = output.get("g", BigInteger.class);

			try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
				mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(testParameters.getSecurityLevel());

				final GqGroup expectedParameters = new GqGroup(p, q, g);

				return Arguments.of(seed, expectedParameters, testParameters.getDescription(), testParameters.getSecurityLevel());
			}
		});
	}

	@ParameterizedTest(name = "{2} with seed = {0}")
	@MethodSource("getEncryptionParametersProvider")
	@DisplayName("calling getEncryptionParameters with fixed seed gives expected parameters")
	void getEncryptionParameters(final String seed, final GqGroup expectedParameters,
			final String description, final SecurityLevelInternal securityLevel) {

		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(securityLevel);

			final GqGroup encryptionParameters = new EncryptionParameters().getEncryptionParameters(seed, SMALL_PRIMES);

			assertEquals(expectedParameters, encryptionParameters, String.format("assertion failed for: %s", description));
		}
	}

}
