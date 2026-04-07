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

import static com.google.common.base.Preconditions.checkArgument;

import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetOptimizationMode;

public class MatrixUtils {

	private MatrixUtils() {
		// Intentionally left blank.
	}

	/**
	 * Computes the matrix dimensions {@code (m, n)} for a given vector size {@code N} according to the optimization mode configured via the system
	 * property {@code MIXNET_OPTIMIZATION_MODE}.
	 * <p>
	 * This method implements the algorithm GetMatrixDimensions as specified in the Crypto-Primitives Specification.
	 * <p>
	 * If the optimization mode is {@link MixnetOptimizationMode#MEMORY_OPTIMIZED}, the dimensions are chosen to be size-optimal, meaning {@code m} and
	 * {@code n} are as close as possible to the dimensions of a square matrix. This minimizes the size of the Bayer-Groth shuffle argument and the
	 * communication complexity.
	 * <p>
	 * If the optimization mode is {@link MixnetOptimizationMode#COMPUTATION_OPTIMIZED}, the dimensions are chosen to optimize computation performance,
	 * resulting in {@code m = 1} and {@code n = N}.
	 *
	 * @param vectorSize N, the vector size to decompose into matrix dimensions. Must be greater than or equal to 2.
	 * @return an array {@code [m, n]} with {@code m} the number of rows, {@code n} the number of columns and {@code m × n = N}, where {@code m ≤ n}.
	 */
	public static int[] getMatrixDimensions(final int vectorSize) {
		final int N = vectorSize;
		checkArgument(N >= 2, "The size to decompose must be greater than or equal to 2.");

		final MixnetOptimizationMode optimizationMode = MixnetOptimizationConfig.getMixnetOptimizationMode();

		int m = 1;
		int n = N;

		if (optimizationMode == MixnetOptimizationMode.MEMORY_OPTIMIZED) {
			for (int i = (int) Math.floor(Math.sqrt(N)); i > 1; i--) {
				if (N % i == 0) {
					m = i;
					n = N / i;
					break;
				}
			}

		}
		return new int[] { m, n };
	}
}
