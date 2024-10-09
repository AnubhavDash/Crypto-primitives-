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

package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.JsonData;
import ch.post.it.evoting.cryptoprimitives.test.tools.serialization.TestParameters;

class KDFServiceTest {

	private static final int DEFAULT_HASH_LENGTH_BYTES = 32;
	private static final TestRandomService randomService = new TestRandomService();
	private static final List<String> emptyInfo = List.of();
	private KDFService kdfService;
	private byte[] PRK;
	private int requiredLength;
	private BigInteger requestedUpperBound;

	@BeforeEach
	void setup() {
		kdfService = KDFService.getInstance();
		PRK = randomService.randomBytes(DEFAULT_HASH_LENGTH_BYTES * 8);
		requiredLength = randomService.genRandomInteger(255 * DEFAULT_HASH_LENGTH_BYTES);
		requestedUpperBound = randomService.genRandomIntegerOfLength(DEFAULT_HASH_LENGTH_BYTES * 8 + 3);
	}

	@Test
	void testKDFNulls() {
		assertAll(
				() -> assertThrows(NullPointerException.class, () -> kdfService.KDF(null, emptyInfo, requiredLength)),
				() -> assertThrows(NullPointerException.class, () -> kdfService.KDF(PRK, null, requiredLength))
		);
	}

	@Test
	void testNoInfoDoesntThrow() {
		assertDoesNotThrow(() -> kdfService.KDF(PRK, List.of(), requiredLength));
	}

	@Test
	void testZeroRequiredByteLengthThrows() {
		assertThrows(IllegalArgumentException.class, () -> kdfService.KDF(PRK, emptyInfo, 0));
	}

	@Test
	void testPRKLengthSmallerThanHashLengthThrows() {
		final byte[] tooSmallPRK = new byte[DEFAULT_HASH_LENGTH_BYTES - 1];
		assertThrows(IllegalArgumentException.class, () -> kdfService.KDF(tooSmallPRK, emptyInfo, requiredLength));
	}

	@Test
	void testRequiredLengthBiggerThan255HashLengthThrows() {
		assertThrows(IllegalArgumentException.class, () -> kdfService.KDF(PRK, emptyInfo, 255 * DEFAULT_HASH_LENGTH_BYTES + 1));
	}

	static Stream<Arguments> KDFRealValuesProvider() {
		final List<TestParameters> parametersList = TestParameters.fromResource("/utils/hkdf-expand.json");

		return parametersList.stream().parallel().map(testParameters -> {
			// Context.
			final JsonData context = testParameters.getContext();
			final String hash = context.get("hash", String.class);
			final Supplier<Digest> hashSupplier = getDigestSupplier(hash);

			// Inputs.
			final JsonData input = testParameters.getInput();
			final byte[] PRK = input.get("prk", byte[].class);
			final List<String> infos = List.of(input.get("info", String[].class));
			final Integer requiredByteLength = input.get("length", Integer.class);

			// Output.
			final JsonData output = testParameters.getOutput();
			final byte[] OKM = output.get("okm", byte[].class);

			return Arguments.of(hashSupplier, PRK, infos, requiredByteLength, OKM, testParameters.getDescription());
		});
	}

	@ParameterizedTest(name = "{5}")
	@MethodSource("KDFRealValuesProvider")
	@DisplayName("KeyDerivation returns expected output")
	void testKDFWithRealValues(final Supplier<Digest> hashSupplier, final byte[] PRK, final List<String> infos, final int requiredByteLength,
			final byte[] OKM, final String description) {
		final KDFService kdfService = new KDFService(hashSupplier);
		final byte[] actualResult = kdfService.KDF(PRK, infos, requiredByteLength);
		assertArrayEquals(OKM, actualResult, String.format("assertion failed for: %s", description));
	}

	@Test
	void testKDFToZqNulls() {
		assertAll(
				() -> assertThrows(NullPointerException.class, () -> kdfService.KDFToZq(null, emptyInfo, requestedUpperBound)),
				() -> assertThrows(NullPointerException.class, () -> kdfService.KDFToZq(PRK, null, requestedUpperBound)),
				() -> assertThrows(NullPointerException.class, () -> kdfService.KDFToZq(PRK, emptyInfo, null))
		);
	}

	@Test
	void testKDFToZqNoInfoDoesntThrow() {
		assertDoesNotThrow(() -> kdfService.KDFToZq(PRK, List.of(), requestedUpperBound));
	}

	@Test
	void testtKDFToZqTooSmallRequiredUpperBoundThrows() {
		final BigInteger tooSmallRequestedUpperbound = randomService.genRandomIntegerOfLength((DEFAULT_HASH_LENGTH_BYTES - 1) * 8);
		assertThrows(IllegalArgumentException.class, () -> kdfService.KDFToZq(PRK, emptyInfo, tooSmallRequestedUpperbound));
	}

	@Test
	void testKDFToZqPRKLengthSmallerThanHashLengthThrows() {
		final byte[] tooSmallPRK = new byte[DEFAULT_HASH_LENGTH_BYTES - 1];
		assertThrows(IllegalArgumentException.class, () -> kdfService.KDFToZq(tooSmallPRK, emptyInfo, requestedUpperBound));
	}

	static Stream<Arguments> KDFToZqRealValuesProvider() {
		final List<TestParameters> parametersList = TestParameters.fromResource("/utils/hkdf-expand-to-zq.json");

		return parametersList.stream().map(testParameters -> {
			// Context.
			final JsonData context = testParameters.getContext();
			final String hash = context.get("hash", String.class);
			final Supplier<Digest> hashSupplier = getDigestSupplier(hash);

			// Inputs.
			final JsonData input = testParameters.getInput();
			final byte[] PRK = input.get("prk", byte[].class);
			final List<String> infos = List.of(input.get("info", String[].class));
			final BigInteger q = input.get("q", BigInteger.class);

			// Output.
			final JsonData output = testParameters.getOutput();
			final ZqElement u = ZqElement.create(output.get("u", BigInteger.class), new ZqGroup(q));

			return Arguments.of(hashSupplier, PRK, infos, q, u, testParameters.getDescription(), testParameters.getSecurityLevel());
		});
	}

	@ParameterizedTest(name = "{5}")
	@MethodSource("KDFToZqRealValuesProvider")
	@DisplayName("KDFToZq returns expected output")
	void testKDFToZqWithRealValues(final Supplier<Digest> hashSupplier, final byte[] PRK, final List<String> infos, final BigInteger q,
			final ZqElement u, final String description, final SecurityLevelInternal securityLevel) {
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(securityLevel);

			final KDFService kdfService = new KDFService(hashSupplier);
			final ZqElement actualResult = kdfService.KDFToZq(PRK, infos, q);
			assertEquals(u, actualResult, String.format("assertion failed for: %s", description));
		}
	}

	private static Supplier<Digest> getDigestSupplier(final String hash) {
		return switch (hash) {
			case "SHA-256" -> SHA256Digest::new;
			case "SHA-1" -> SHA1Digest::new;
			case "SHA3-256" -> () -> new SHA3Digest(256);
			default -> throw new UnsupportedOperationException("Unrecognised hash function in test file.");
		};
	}
}
