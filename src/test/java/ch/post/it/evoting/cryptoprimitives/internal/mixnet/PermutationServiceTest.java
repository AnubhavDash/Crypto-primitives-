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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;

@DisplayName("PermutationService calling genPermutation")
class PermutationServiceTest {

	private static final int MAX_PERMUTATION_TEST_SIZE = 100;

	private static final TestRandomService randomService = new TestRandomService();
	private static final PermutationService permutationService = new PermutationService(randomService);

	private int size;

	@BeforeEach
	void setUp() {
		size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE);
	}

	@Test
	@DisplayName("with negative value throws IllegalArgumentException")
	void negativeValueThrows() {
		assertThrows(IllegalArgumentException.class, () -> permutationService.genPermutation(-1));
	}

	@Test
	@DisplayName("with valid size does not throw")
	void validSizeDoesNotThrow() {
		assertDoesNotThrow(() -> permutationService.genPermutation(size));
	}

	@RepeatedTest(10)
	@DisplayName("contains all values in input range")
	void genPermutationContainsAllValuesInInputRange() {
		final int greaterThanZeroSize = size + 1;
		final Permutation permutation = permutationService.genPermutation(greaterThanZeroSize);
		final TreeSet<Integer> values = computePermutationValues(permutation);

		assertEquals(greaterThanZeroSize, values.size());
		assertEquals(0, values.first());
		assertEquals(greaterThanZeroSize - 1, values.last());
	}

	private TreeSet<Integer> computePermutationValues(final Permutation permutation) {
		return IntStream.range(0, permutation.size()).map(permutation::get).boxed().collect(Collectors.toCollection(TreeSet::new));
	}

}
