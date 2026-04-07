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

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;

@DisplayName("A Permutation")
class PermutationTest {

	private static final int MAX_PERMUTATION_TEST_SIZE = 100;

	private static final TestRandomService randomService = new TestRandomService();
	private static final PermutationService permutationService = new PermutationService(randomService);

	@Test
	@DisplayName("calling size returns correct size")
	void sizeReturnsCorrectSize() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE);
		final Permutation permutation = permutationService.genPermutation(size);

		assertEquals(size, permutation.size());
	}

	@Test
	@DisplayName("calling stream returns correct elements")
	void streamReturnsCorrectElements() {
		final int size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE);
		final Permutation permutation = permutationService.genPermutation(size);

		final ImmutableList<Integer> expectedMapping = IntStream.range(0, permutation.size())
				.map(permutation::get)
				.boxed()
				.collect(toImmutableList());

		assertEquals(expectedMapping, permutation.stream().collect(toImmutableList()));
	}

	@Test
	@DisplayName("is immutable")
	void immutableValueMapping() {
		final List<Integer> valueMapping = new ArrayList<>() {{
			add(10);
			add(11);
		}};

		final Permutation permutation = new Permutation(valueMapping.stream().collect(toImmutableList()));
		valueMapping.removeFirst();

		assertEquals(2, permutation.size());
	}

	@Nested
	@DisplayName("constructed with")
	class ConstructorTest {

		@Test
		@DisplayName("null parameter throws NullPointerException")
		void nullParameterThrows() {
			assertThrows(NullPointerException.class, () -> new Permutation(null));
		}

		@Test
		@DisplayName("empty list returns empty permutation")
		void emptyListReturnsEmptyPermutation() {
			final Permutation permutation = new Permutation(ImmutableList.emptyList());

			assertEquals(Permutation.EMPTY, permutation);
		}

		@Test
		@DisplayName("valid parameter does not throw")
		void validParameterDoesNotThrow() {
			final ImmutableList<Integer> validValueMapping = IntStream.range(0, MAX_PERMUTATION_TEST_SIZE)
					.boxed()
					.collect(toImmutableList());

			assertDoesNotThrow(() -> new Permutation(validValueMapping));
		}

	}

	@Nested
	@DisplayName("calling get with")
	class GetTest {

		private int size;
		private Permutation permutation;

		@BeforeEach
		void setUp() {
			size = randomService.genRandomInteger(MAX_PERMUTATION_TEST_SIZE);
			permutation = permutationService.genPermutation(size);
		}

		@Test
		@DisplayName("negative value throws IllegalArgumentException")
		void getThrowsForNegativeValue() {
			final int value = -randomService.genRandomInteger(Integer.MAX_VALUE);

			assertThrows(IllegalArgumentException.class, () -> permutation.get(value));
		}

		@Test
		@DisplayName("value above size throws IllegalArgumentException")
		void getThrowsForValueAboveSize() {
			final int value = randomService.genRandomInteger(Integer.MAX_VALUE - size) + size;

			assertThrows(IllegalArgumentException.class, () -> permutation.get(value));
		}

		@Test
		@DisplayName("value equal to size throws IllegalArgumentException")
		void getThrowsForValueOfSize() {
			assertThrows(IllegalArgumentException.class, () -> permutation.get(size));
		}

		@Test
		@DisplayName("valid value does not throw")
		void getValidValueDoesNotThrow() {
			assertDoesNotThrow(() -> permutation.get(size - 1));
		}

	}

}
