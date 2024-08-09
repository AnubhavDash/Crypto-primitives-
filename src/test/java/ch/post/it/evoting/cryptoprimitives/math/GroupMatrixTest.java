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

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static ch.post.it.evoting.cryptoprimitives.test.tools.generator.GroupVectorElementGenerator.generateElementList;
import static ch.post.it.evoting.cryptoprimitives.test.tools.generator.GroupVectorElementGenerator.generateElementMatrix;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestSizedElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.math.TestGroup;

class GroupMatrixTest {

	private static final int BOUND_MATRIX_SIZE = 10;
	private static final TestRandomService randomService = new TestRandomService();

	private static TestGroup group = new TestGroup();

	private int numRows;
	private int numColumns;

	@BeforeAll
	static void setup() {
		group = new TestGroup();
	}

	@BeforeEach
	void setUp() {
		numRows = randomService.genRandomInteger(10) + 1;
		numColumns = randomService.genRandomInteger(10) + 1;
	}

	@Test
	void createGroupMatrixWithNullValues() {
		assertThrows(NullPointerException.class, () -> GroupMatrix.fromRows(null));
	}

	@Test
	void createGroupMatrixWithEmptyRows() {
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> emptyRows = generateElementMatrix(0, numColumns,
				() -> new TestGroupElement(group));
		final IllegalArgumentException exceptionFirst = assertThrows(IllegalArgumentException.class, () -> GroupMatrix.fromRows(emptyRows));
		assertEquals("Empty matrices are not supported.", exceptionFirst.getMessage());
	}

	@RepeatedTest(10)
	void sizesAreCorrectForRandomMatrix() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);
		assertEquals(numRows, matrix.numRows());
		assertEquals(numColumns, matrix.numColumns());
	}

	@Test
	void getThrowsForIndexOutOfBounds() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);
		assertThrows(IllegalArgumentException.class, () -> matrix.get(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> matrix.get(numRows, 0));
		assertThrows(IllegalArgumentException.class, () -> matrix.get(0, -1));
		assertThrows(IllegalArgumentException.class, () -> matrix.get(0, numColumns));
	}

	@RepeatedTest(10)
	void getReturnsExpectedElement() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupMatrix<TestValuedElement, TestGroup> matrix = generateIncrementingMatrix(numRows, numColumns, group);
		final int row = randomService.genRandomInteger(numRows);
		final int column = randomService.genRandomInteger(numColumns);
		assertEquals(numColumns * row + column, matrix.get(row, column).getValue().intValueExact());
	}

	@RepeatedTest(10)
	void getRowReturnsExpectedRow() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupMatrix<TestValuedElement, TestGroup> matrix = generateIncrementingMatrix(numRows, numColumns, group);
		final int row = randomService.genRandomInteger(numRows);
		final GroupVector<TestValuedElement, TestGroup> expected = generateIncrementingRow(row * numColumns, numColumns, group);
		assertEquals(expected, matrix.getRow(row));
	}

	@RepeatedTest(10)
	void getColumnReturnsExpectedColumn() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupMatrix<TestValuedElement, TestGroup> matrix = generateIncrementingMatrix(numRows, numColumns, group);
		final int column = randomService.genRandomInteger(numColumns);
		final GroupVector<TestValuedElement, TestGroup> expected = IntStream.range(0, numRows)
				.map(row -> row * numColumns + column)
				.mapToObj(value -> new TestValuedElement(BigInteger.valueOf(value), group))
				.collect(toGroupVector());
		assertEquals(expected, matrix.getColumn(column));
	}

	@RepeatedTest(10)
	void matrixFromColumnsIsMatrixFromRowsTransposed() {
		final int n = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int m = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> rows = generateElementMatrix(n, m, () -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> expected = GroupMatrix.fromRows(rows);

		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> columns =
				IntStream.range(0, m)
						.mapToObj(column ->
								rows.stream()
										.map(row -> row.get(column))
										.collect(toGroupVector())
						).collect(toGroupVector());
		final GroupMatrix<TestGroupElement, TestGroup> actual = GroupMatrix.fromColumns(columns);

		assertEquals(expected, actual);
	}

	@Test
	void transposeCorrectlyTransposesMatrix() {
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);
		final GroupMatrix<TestGroupElement, TestGroup> transposedMatrix = matrix.transpose();

		assertAll(
				() -> assertEquals(matrix.numColumns(), transposedMatrix.numRows()),
				() -> assertEquals(matrix.numRows(), transposedMatrix.numColumns()),
				() -> assertEquals(matrix.rowStream().collect(toGroupVector()), transposedMatrix.columnStream().collect(toGroupVector()))
		);
	}

	@Test
	void transposeTwiceGivesOriginalMatrix() {
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);
		assertEquals(matrix, matrix.transpose().transpose());
	}

	@Test
	void transposedMatrixContainsExpectedValues() {
		final TestValuedElement zero = new TestValuedElement(BigInteger.ZERO, group);
		final TestValuedElement one = new TestValuedElement(BigInteger.ONE, group);
		final TestValuedElement ten = new TestValuedElement(BigInteger.TEN, group);
		final GroupVector<GroupVector<TestValuedElement, TestGroup>, TestGroup> matrixElements = GroupVector.of(
				GroupVector.of(zero, one, ten),
				GroupVector.of(one, ten, zero)
		);

		final GroupMatrix<TestValuedElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);
		final GroupMatrix<TestValuedElement, TestGroup> transposedMatrix = matrix.transpose();

		assertAll(
				() -> assertEquals(zero, transposedMatrix.get(0, 0)),
				() -> assertEquals(one, transposedMatrix.get(0, 1)),
				() -> assertEquals(one, transposedMatrix.get(1, 0)),
				() -> assertEquals(ten, transposedMatrix.get(1, 1)),
				() -> assertEquals(ten, transposedMatrix.get(2, 0)),
				() -> assertEquals(zero, transposedMatrix.get(2, 1))
		);
	}

	@RepeatedTest(10)
	void streamGivesElementsInCorrectOrder() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final int totalElements = numRows * numColumns;
		assertEquals(totalElements, matrix.flatStream().count());

		final GroupVector<TestGroupElement, TestGroup> flatMatrix = matrix.flatStream().collect(toGroupVector());
		final int i = numRows - 1;
		final int j = numColumns - 1;
		// Index in new list is: i * numColumns + j
		assertEquals(matrix.get(0, 0), flatMatrix.get(0));
		assertEquals(matrix.get(0, j), flatMatrix.get(j));
		assertEquals(matrix.get(i, 0), flatMatrix.get(i * numColumns));
		assertEquals(matrix.get(i, j), flatMatrix.get(totalElements - 1));
	}

	@RepeatedTest(10)
	void rowStreamGivesRows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		assertEquals(numRows, matrix.rowStream().count());
		assertEquals(matrixElements.stream().collect(toGroupVector()), matrix.rowStream().collect(toGroupVector()));
	}

	@RepeatedTest(10)
	void columnStreamGivesColumns() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		assertEquals(numColumns, matrix.columnStream().count());

		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> columnMatrixElements = IntStream.range(0, matrix.numColumns())
				.mapToObj(i -> matrixElements.stream().map(row -> row.get(i)).collect(toGroupVector())).collect(toGroupVector());
		assertEquals(columnMatrixElements.stream().collect(toGroupVector()), matrix.columnStream().collect(toGroupVector()));
	}

	@Test
	void appendColumnWithInvalidParamsThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		assertThrows(NullPointerException.class, () -> matrix.appendColumn(null));

		final GroupVector<TestGroupElement, TestGroup> emptyVector = GroupVector.empty();
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> matrix.appendColumn(emptyVector));
		assertEquals(String.format("The new column size does not match size of matrix' columns. Size: %d, numRows: %d", 0, numRows),
				illegalArgumentException.getMessage());
	}

	@Test
	void appendColumnWithDifferentElementSizeThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestSizedElement, TestGroup>, TestGroup> matrixElements =
				generateElementMatrix(numRows, numColumns, () -> new TestSizedElement(group, 1));
		final GroupMatrix<TestSizedElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final GroupVector<TestSizedElement, TestGroup> vector = generateElementList(numRows, () -> new TestSizedElement(group, 2));
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> matrix.appendColumn(vector));
		assertEquals("The elements' size does not match this matrix's elements' size.",
				illegalArgumentException.getMessage());
	}

	@Test
	void appendColumnOfDifferentGroupThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final TestGroup differentTestGroup = new TestGroup();
		final GroupVector<TestGroupElement, TestGroup> newCol = generateElementList(numRows, () -> new TestGroupElement(differentTestGroup));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> matrix.appendColumn(newCol));
		assertEquals("The group of the new column must be equal to the matrix' group", exception.getMessage());
	}

	@RepeatedTest(10)
	void appendColumnCorrectlyAppends() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final GroupVector<TestGroupElement, TestGroup> newCol = generateElementList(numRows, () -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> augmentedMatrix = matrix.appendColumn(newCol);

		assertEquals(numColumns + 1, augmentedMatrix.numColumns());
		assertEquals(newCol, augmentedMatrix.getColumn(numColumns));
	}

	@Test
	void prependColumnWithInvalidParamsThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		assertThrows(NullPointerException.class, () -> matrix.prependColumn(null));

		final GroupVector<TestGroupElement, TestGroup> emptyVector = GroupVector.empty();
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> matrix.prependColumn(emptyVector));
		assertEquals(String.format("The new column size does not match size of matrix' columns. Size: %d, numRows: %d", 0, numRows),
				illegalArgumentException.getMessage());
	}

	@Test
	void prependColumnWithDifferentElementSizeThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestSizedElement, TestGroup>, TestGroup> matrixElements =
				generateElementMatrix(numRows, numColumns, () -> new TestSizedElement(group, 1));
		final GroupMatrix<TestSizedElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final GroupVector<TestSizedElement, TestGroup> vector = generateElementList(numRows, () -> new TestSizedElement(group, 2));
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> matrix.prependColumn(vector));
		assertEquals("The elements' size does not match this matrix's elements' size.",
				illegalArgumentException.getMessage());
	}

	@Test
	void prependColumnOfDifferentGroupThrows() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final TestGroup differentTestGroup = new TestGroup();
		final GroupVector<TestGroupElement, TestGroup> newCol = generateElementList(numRows, () -> new TestGroupElement(differentTestGroup));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> matrix.prependColumn(newCol));
		assertEquals("The group of the new column must be equal to the matrix' group", exception.getMessage());
	}

	@RepeatedTest(10)
	void prependColumnCorrectlyPrepends() {
		final int numRows = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final int numColumns = randomService.genRandomInteger(BOUND_MATRIX_SIZE) + 1;
		final TestGroup group = new TestGroup();
		final GroupVector<GroupVector<TestGroupElement, TestGroup>, TestGroup> matrixElements = generateElementMatrix(numRows, numColumns,
				() -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> matrix = GroupMatrix.fromRows(matrixElements);

		final GroupVector<TestGroupElement, TestGroup> newCol = generateElementList(numRows, () -> new TestGroupElement(group));
		final GroupMatrix<TestGroupElement, TestGroup> augmentedMatrix = matrix.prependColumn(newCol);

		assertEquals(numColumns + 1, augmentedMatrix.numColumns());
		assertEquals(newCol, augmentedMatrix.getColumn(0));
	}

	//***************************//
	// Utilities //
	//***************************//

	//Generate a matrix with incrementing count.
	private GroupMatrix<TestValuedElement, TestGroup> generateIncrementingMatrix(final int numRows, final int numColumns, final TestGroup group) {
		final GroupVector<GroupVector<TestValuedElement, TestGroup>, TestGroup> matrixElements =
				IntStream.range(0, numRows)
						.mapToObj(row -> generateIncrementingRow(numColumns * row, numColumns, group))
						.collect(toGroupVector());
		return GroupMatrix.fromRows(matrixElements);
	}

	//Generate a row with incrementing number starting at start.
	private GroupVector<TestValuedElement, TestGroup> generateIncrementingRow(final int start, final int numColumns, final TestGroup group) {
		return IntStream.range(0, numColumns)
				.map(column -> start + column)
				.mapToObj(BigInteger::valueOf)
				.map(value -> new TestValuedElement(value, group))
				.collect(toGroupVector());
	}

	private static class TestValuedElement extends GroupElement<TestGroup> {
		protected TestValuedElement(final BigInteger value, final TestGroup group) {
			super(value, group);
		}
	}
}
