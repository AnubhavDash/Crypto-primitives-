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
package ch.post.it.evoting.cryptoprimitives.math;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestSizedElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.math.TestGroup;

class GroupVectorTest {

	private static final TestRandomService randomService = new TestRandomService();

	@Test
	void testOf() {
		final TestGroup group = new TestGroup();
		final TestGroupElement e1 = new TestGroupElement(group);
		final TestGroupElement e2 = new TestGroupElement(group);

		final GroupVector<TestGroupElement, TestGroup> groupVector = GroupVector.of(e1, e2);
		assertEquals(2, groupVector.size());

		final GroupVector<TestGroupElement, TestGroup> emptyGroupVector = GroupVector.empty();
		assertEquals(0, emptyGroupVector.size());
	}

	@Test
	void testOfWithInvalidParametersThrows() {
		assertThrows(NullPointerException.class, () -> GroupVector.of((TestGroupElement[]) null));

		// With null elem.
		final TestGroup group = new TestGroup();
		final TestGroupElement e1 = new TestGroupElement(group);
		assertThrows(NullPointerException.class, () -> GroupVector.of(e1, null));

		// Different group elems.
		final TestGroupElement e2 = new TestGroupElement(new TestGroup());
		final IllegalArgumentException diffGroupIllegalArgumentException2 = assertThrows(IllegalArgumentException.class,
				() -> GroupVector.of(e1, e2));
		assertEquals("All elements must belong to the same group.", diffGroupIllegalArgumentException2.getMessage());
	}

	@Test
	void testNullElementsThrows() {
		assertThrows(NullPointerException.class, () -> GroupVector.from((List) null));
		assertThrows(NullPointerException.class, () -> GroupVector.from((ImmutableList) null));
	}

	@Test
	void testEmptyElementsDoesNotThrow() {
		final GroupVector<TestGroupElement, TestGroup> vector = GroupVector.empty();
		assertEquals(0, vector.size());
	}

	@Test
	void testGroupOfEmptyVectorThrows() {
		final GroupVector<TestGroupElement, TestGroup> vector = GroupVector.empty();
		assertThrows(IllegalStateException.class, vector::getGroup);
	}

	@Test
	void testElementsOfDifferentGroupsThrows() {
		final TestGroup group1 = new TestGroup();
		final TestGroupElement first = new TestGroupElement(group1);
		final TestGroup group2 = new TestGroup();
		final TestGroupElement second = new TestGroupElement(group2);
		final ImmutableList<TestGroupElement> elements = ImmutableList.of(first, second);
		assertThrows(IllegalArgumentException.class, () -> GroupVector.from(elements));
	}

	@Test
	void testElementsOfDifferentSizeThrows() {
		final TestGroup group = new TestGroup();
		final TestSizedElement first = new TestSizedElement(group, 1);
		final TestSizedElement second = new TestSizedElement(group, 2);
		final ImmutableList<TestSizedElement> elements = ImmutableList.of(first, second);
		assertThrows(IllegalArgumentException.class, () -> GroupVector.from(elements));
	}

	@Test
	void testLengthReturnsElementsLength() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements =
				Stream
						.generate(() -> new TestGroupElement(group))
						.limit(n)
						.collect(toImmutableList());
		assertEquals(n, GroupVector.from(elements).size());
	}

	@Test
	void testGetElementReturnsElement() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final int i = randomService.genRandomInteger(n);
		assertEquals(elements.get(i), GroupVector.from(elements).get(i));
	}

	@Test
	void testGetElementAboveRangeThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> actor = GroupVector.from(elements);
		assertThrows(IndexOutOfBoundsException.class, () -> actor.get(n));
	}

	@Test
	void testGetElementBelowRangeThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> actor = GroupVector.from(elements);
		assertThrows(IndexOutOfBoundsException.class, () -> actor.get(-1));
	}

	@Test
	void testGetGroupReturnsElementsGroup() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> actor = GroupVector.from(elements);
		assertEquals(group, actor.getGroup());
	}

	@Test
	void givenElementsWhenGetElementsThenExpectedElements() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(100) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> actor = GroupVector.from(elements);
		assertEquals(elements, ImmutableList.from(actor));
	}

	@Test
	void givenAPropertyHoldsForAnEmptyVector() {
		final GroupVector<TestGroupElement, ?> empty = GroupVector.empty();
		final Function<TestGroupElement, ?> randomFunction = ignored -> randomService.genRandomInteger(Integer.MAX_VALUE);
		assertTrue(empty.allEqual(randomFunction));
	}

	@Test
	void threeElementsWithTheSamePropertyAreAllEqualAndDifferentPropertiesNotEqual() {
		final TestGroup group = new TestGroup();
		final TestValuedElement first = new TestValuedElement(BigInteger.ONE, group);
		final TestValuedElement second = new TestValuedElement(BigInteger.TWO, group);
		final TestValuedElement third = new TestValuedElement(BigInteger.valueOf(3), group);
		final ImmutableList<TestValuedElement> elements = ImmutableList.of(first, second, third);
		final GroupVector<TestValuedElement, TestGroup> vector = GroupVector.from(elements);
		assertAll(() -> {
			assertFalse(vector.allEqual(TestValuedElement::getValue));
			assertTrue(vector.allEqual(TestValuedElement::getGroup));
		});
	}

	@Test
	void isEmptyReturnsTrueForEmptyVector() {
		final ImmutableList<TestGroupElement> elements = ImmutableList.emptyList();
		final GroupVector<TestGroupElement, TestGroup> vector = GroupVector.from(elements);
		assertTrue(vector.isEmpty());
	}

	@Test
	void isEmptyReturnsFalseForNonEmptyVector() {
		final TestGroup group = new TestGroup();
		final ImmutableList<TestGroupElement> elements = ImmutableList.of(new TestGroupElement(group));
		final GroupVector<TestGroupElement, TestGroup> vector = GroupVector.from(elements);
		assertFalse(vector.isEmpty());
	}

	@Test
	void appendWithInvalidParamsThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> groupVector = GroupVector.from(elements);

		assertThrows(NullPointerException.class, () -> groupVector.append(null));

		final TestGroupElement element = new TestGroupElement(new TestGroup());
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> groupVector.append(element));
		assertEquals("The element to append must be in the same group.", illegalArgumentException.getMessage());
	}

	@Test
	void appendWithDifferentSizeThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestSizedElement> elements = Stream.generate(() -> new TestSizedElement(group, 1)).limit(n).collect(toImmutableList());
		final GroupVector<TestSizedElement, TestGroup> groupVector = GroupVector.from(elements);

		final TestSizedElement element = new TestSizedElement(group, 2);
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> groupVector.append(element));
		assertEquals("The element to append must be the same size.", illegalArgumentException.getMessage());
	}

	@RepeatedTest(10)
	void appendCorrectlyAppends() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> groupVector = GroupVector.from(elements);

		final TestGroupElement element = new TestGroupElement(group);
		final GroupVector<TestGroupElement, TestGroup> augmentedVector = groupVector.append(element);

		assertEquals(groupVector.size() + 1, augmentedVector.size());
		assertEquals(element, augmentedVector.get(n));
	}

	@Test
	void prependWithInvalidParamsThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> groupVector = GroupVector.from(elements);

		assertThrows(NullPointerException.class, () -> groupVector.prepend(null));

		final TestGroupElement element = new TestGroupElement(new TestGroup());
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> groupVector.prepend(element));
		assertEquals("The element to prepend must be in the same group.", illegalArgumentException.getMessage());
	}

	@Test
	void prependWithDifferentSizeThrows() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestSizedElement> elements = Stream.generate(() -> new TestSizedElement(group, 1)).limit(n).collect(toImmutableList());
		final GroupVector<TestSizedElement, TestGroup> groupVector = GroupVector.from(elements);

		final TestSizedElement element = new TestSizedElement(group, 2);
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> groupVector.prepend(element));
		assertEquals("The element to prepend must be the same size.", illegalArgumentException.getMessage());
	}

	@RepeatedTest(10)
	void prependCorrectlyPrepends() {
		final TestGroup group = new TestGroup();
		final int n = randomService.genRandomInteger(10) + 1;
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> groupVector = GroupVector.from(elements);

		final TestGroupElement element = new TestGroupElement(group);
		final GroupVector<TestGroupElement, TestGroup> augmentedVector = groupVector.prepend(element);

		assertEquals(groupVector.size() + 1, augmentedVector.size());
		assertEquals(element, augmentedVector.get(0));
	}

	@Test
	void toSameGroupVectorCorrectlyCollects() {
		final int n = randomService.genRandomInteger(10) + 1;
		final TestGroup group = new TestGroup();
		final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit(n).collect(toImmutableList());
		final GroupVector<TestGroupElement, TestGroup> actual = elements.stream().collect(toGroupVector());
		final GroupVector<TestGroupElement, TestGroup> expected = GroupVector.from(elements);

		assertEquals(expected, actual);
	}

	private static class TestValuedElement extends GroupElement<TestGroup> {
		protected TestValuedElement(final BigInteger value, final TestGroup group) {
			super(value, group);
		}
	}

	@Nested
	@DisplayName("Transforming a vector of ciphertext into a matrix of ciphertexts...")
	class ToMatrixTest {

		private static final int BOUND_MATRIX_SIZE = 10;

		private int m;
		private int n;

		private TestGroup group;
		private GroupVector<TestGroupElement, TestGroup> groupVector;

		@BeforeEach
		void setup() {
			m = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
			n = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;

			group = new TestGroup();
			final ImmutableList<TestGroupElement> elements = Stream.generate(() -> new TestGroupElement(group)).limit((long) n * m)
					.collect(toImmutableList());
			groupVector = GroupVector.from(elements);
		}

		@Test
		@DisplayName("with negative number of rows or columns throws IllegalArgumentException")
		void toMatrixWithInvalidNumRowsOrColumns() {
			Exception exception = assertThrows(IllegalArgumentException.class, () -> groupVector.toMatrix(-m, n));
			assertEquals("The number of rows must be positive.", exception.getMessage());
			exception = assertThrows(IllegalArgumentException.class, () -> groupVector.toMatrix(m, -n));
			assertEquals("The number of columns must be positive.", exception.getMessage());
		}

		@Test
		@DisplayName("with incompatible decomposition into rows and columns throws an IllegalArgumentException")
		void toMatrixWithWrongN() {
			final Exception exception = assertThrows(IllegalArgumentException.class, () -> groupVector.toMatrix(m + 1, n));
			assertEquals("The vector of ciphertexts must be decomposable into m rows and n columns.", exception.getMessage());
		}

		@Test
		@DisplayName("with valid input yields expected result")
		void toMatrixTest() {
			final GroupMatrix<TestGroupElement, TestGroup> matrix = groupVector.toMatrix(m, n);

			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					assertEquals(groupVector.get(n * i + j), matrix.get(i, j));
				}
			}
		}
	}
}
