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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetOptimizationMode;

class MatrixUtilsTest {

	@Test
	@DisplayName("invalid N throws IllegalArgumentException")
	void getMatrixDimensionsInvalidN() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> MatrixUtils.getMatrixDimensions(0)),
				() -> assertThrows(IllegalArgumentException.class, () -> MatrixUtils.getMatrixDimensions(-1))
		);
	}

	@Test
	@DisplayName("COMPUTATION_OPTIMIZED returns m=1 and n=N")
	void getMatrixDimensionsNotMemoryOptimizedReturnsRowVector() {
		try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.COMPUTATION_OPTIMIZED)) {
			assertAll(
					() -> assertArrayEquals(new int[] { 1, 2 }, MatrixUtils.getMatrixDimensions(2)),
					() -> assertArrayEquals(new int[] { 1, 3 }, MatrixUtils.getMatrixDimensions(3)),
					() -> assertArrayEquals(new int[] { 1, 9 }, MatrixUtils.getMatrixDimensions(9)),
					() -> assertArrayEquals(new int[] { 1, 12 }, MatrixUtils.getMatrixDimensions(12)),
					() -> assertArrayEquals(new int[] { 1, 16 }, MatrixUtils.getMatrixDimensions(16)),
					() -> assertArrayEquals(new int[] { 1, 18 }, MatrixUtils.getMatrixDimensions(18)),
					() -> assertArrayEquals(new int[] { 1, 23 }, MatrixUtils.getMatrixDimensions(23)),
					() -> assertArrayEquals(new int[] { 1, 25 }, MatrixUtils.getMatrixDimensions(25)),
					() -> assertArrayEquals(new int[] { 1, 27 }, MatrixUtils.getMatrixDimensions(27))
			);
		}
	}

	@Test
	@DisplayName("MEMORY_OPTIMIZED gives expected dimensions")
	void getMatrixDimensionsMemoryOptimizedGivesExpectedDimensions() {
		try (final var _ = MixnetOptimizationModeContext.set(MixnetOptimizationMode.MEMORY_OPTIMIZED)) {
			assertAll(
					() -> assertArrayEquals(new int[] { 1, 2 }, MatrixUtils.getMatrixDimensions(2)),
					() -> assertArrayEquals(new int[] { 1, 3 }, MatrixUtils.getMatrixDimensions(3)),
					() -> assertArrayEquals(new int[] { 3, 3 }, MatrixUtils.getMatrixDimensions(9)),
					() -> assertArrayEquals(new int[] { 3, 4 }, MatrixUtils.getMatrixDimensions(12)),
					() -> assertArrayEquals(new int[] { 4, 4 }, MatrixUtils.getMatrixDimensions(16)),
					() -> assertArrayEquals(new int[] { 3, 6 }, MatrixUtils.getMatrixDimensions(18)),
					() -> assertArrayEquals(new int[] { 1, 23 }, MatrixUtils.getMatrixDimensions(23)),
					() -> assertArrayEquals(new int[] { 5, 5 }, MatrixUtils.getMatrixDimensions(25)),
					() -> assertArrayEquals(new int[] { 3, 9 }, MatrixUtils.getMatrixDimensions(27))
			);
		}
	}

	@ParameterizedTest(name = "N={0}, mode={1}")
	@MethodSource("matrixDimensionTestCases")
	@DisplayName("returned dimensions satisfy m*n=N and m<=n for both modes")
	void getMatrixDimensionsInvariantsHold(final int N, final MixnetOptimizationMode optimizationMode) {
		try (final var _ = MixnetOptimizationModeContext.set(optimizationMode)) {
			final int[] dims = MatrixUtils.getMatrixDimensions(N);
			final int m = dims[0];
			final int n = dims[1];

			assertAll(
					() -> assertTrue(m >= 1, "m must be >= 1"),
					() -> assertTrue(n >= 1, "n must be >= 1"),
					() -> assertEquals(N, m * n, "m*n must equal N"),
					() -> assertTrue(m <= n, "m must be <= n")
			);
		}
	}

	private static Stream<Arguments> matrixDimensionTestCases() {
		final int[] testNs = { 2, 3, 4, 6, 8, 9, 10, 12, 18, 23, 25, 27 };
		return Arrays.stream(testNs)
				.boxed()
				.flatMap(vectorSize -> Stream.of(MixnetOptimizationMode.values())
						.map(mode -> Arguments.of(vectorSize, mode)));
	}
}
