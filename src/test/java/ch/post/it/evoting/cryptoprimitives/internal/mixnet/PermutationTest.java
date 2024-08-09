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

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;

class PermutationTest {

	private static final int MAX_PERMUTATION_TEST_SIZE = 1000;

	private static final TestRandomService randomService = new TestRandomService();
	private static final PermutationService permutationService = new PermutationService(randomService);

	@Test
	void permutationWithNullParameter() {
		assertThrows(NullPointerException.class, () -> new Permutation(null));
	}

	@Test
	void genPermutationThrowsForNonPositiveSize() {
		final int size = -randomService.genRandomInteger(Integer.MAX_VALUE);
		assertThrows(IllegalArgumentException.class, () -> permutationService.genPermutation(size));
	}

	@RepeatedTest(10)
	void genPermutationContainsAllValuesInInputRange() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final Permutation permutation = permutationService.genPermutation(size);
		final TreeSet<Integer> values = computePermutationValues(permutation);

		assertEquals(size, values.size());
		assertEquals(0, values.first());
		assertEquals(size - 1, values.last());
	}

	private TreeSet<Integer> computePermutationValues(final Permutation permutation) {
		return IntStream.range(0, permutation.size()).map(permutation::get).boxed().collect(Collectors.toCollection(TreeSet::new));
	}

	@Test
	void getThrowsForNegativeValue() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final int value = -randomService.genRandomInteger(Integer.MAX_VALUE);
		final Permutation permutation = permutationService.genPermutation(size);
		assertThrows(IllegalArgumentException.class, () -> permutation.get(value));
	}

	@Test
	void getThrowsForValueAboveSize() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final int value = randomService.genRandomInteger(Integer.MAX_VALUE - size) + size;
		final Permutation permutation = permutationService.genPermutation(size);
		assertThrows(IllegalArgumentException.class, () -> permutation.get(value));
	}

	@Test
	void getThrowsForValueOfSize() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final Permutation permutation = permutationService.genPermutation(size);
		assertThrows(IllegalArgumentException.class, () -> permutation.get(size));
	}

	@Test
	void getSizeReturnsSize() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final Permutation permutation = permutationService.genPermutation(size);
		assertEquals(size, permutation.size());
	}

	@Test
	void streamReturnsCorrectElements() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE) + 1;
		final Permutation permutation = permutationService.genPermutation(size);

		final ImmutableList<Integer> expectedMapping = IntStream.range(0, permutation.size())
				.map(permutation::get)
				.boxed()
				.collect(toImmutableList());

		assertEquals(expectedMapping, permutation.stream().collect(toImmutableList()));
	}

	@Test
	void immutableValueMapping() {
		final List<Integer> valueMapping = new ArrayList<>();
		valueMapping.add(10);
		valueMapping.add(11);

		final Permutation permutation = new Permutation(valueMapping.stream().collect(toImmutableList()));
		valueMapping.remove(0);

		assertEquals(2, permutation.size());
	}
}
