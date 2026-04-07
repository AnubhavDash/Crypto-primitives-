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
package ch.post.it.evoting.cryptoprimitives.math;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingList;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.MathematicalGroup;
import ch.post.it.evoting.cryptoprimitives.utils.Validations;

/**
 * Represents a vector of non-null {@link GroupElement}s belonging to the same {@link MathematicalGroup} and having the same size.
 * <p>
 * This is effectively a decorator for an unmodifiable List class.
 *
 * <p>Instances of this class are immutable. </p>
 *
 * @param <E> the type of elements this list contains.
 * @param <G> the group type the elements of the list belong to.
 */
public class GroupVector<E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> extends ForwardingList<E>
		implements HashableList, RandomAccess, GroupVectorElement<G> {

	private final ImmutableList<E> elements;
	private final G group;
	private final int elementSize;

	// Private constructor without input validation. Used only for operations that provide a guarantee that the elements belong to the same
	// group and have the same size.
	private GroupVector(final ImmutableList<E> elements) {
		this.elements = checkNotNull(elements);
		this.group = elements.isEmpty() ? null : elements.get(0).getGroup();
		this.elementSize = elements.isEmpty() ? 0 : elements.get(0).size();
	}

	/**
	 * Returns a GroupVector of {@code elements}.
	 *
	 * @see GroupVector#from(ImmutableList)
	 */
	public static <E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> GroupVector<E, G> from(final List<E> elements) {
		// Check null values and immutable copy
		final ImmutableList<E> elementsCopy = checkNotNull(elements).stream()
				.map(Preconditions::checkNotNull)
				.collect(toImmutableList());

		return from(elementsCopy);
	}

	/**
	 * Returns a GroupVector of {@code elements}.
	 *
	 * @param elements the list of elements contained by this vector, which must respect the following:
	 *                 <ul>
	 *                 <li>the list must be non-null</li>
	 *                 <li>the list must not contain any nulls</li>
	 *                 <li>all elements must be from the same {@link MathematicalGroup} </li>
	 *                 <li>all elements must be of the same size</li>
	 *                 </ul>
	 */
	public static <E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> GroupVector<E, G> from(
			final ImmutableList<E> elements) {
		// Check null values
		checkNotNull(elements);

		// Check same group
		checkArgument(Validations.allEqual(elements.stream(), GroupVectorElement::getGroup), "All elements must belong to the same group.");

		// Check same size
		checkArgument(Validations.allEqual(elements.stream(), GroupVectorElement::size), "All vector elements must be the same size.");

		return new GroupVector<>(elements);
	}

	/**
	 * Returns a GroupVector of {@code elements}. The elements must comply with the GroupVector constraints.
	 *
	 * @param elements The elements to be contained in this vector. May be empty.
	 * @param <E>      The type of the elements.
	 * @param <G>      The group of the elements.
	 * @return A GroupVector containing {@code elements}.
	 */
	@SafeVarargs
	public static <E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> GroupVector<E, G> of(final E... elements) {
		Arrays.stream(checkNotNull(elements)).forEach(Preconditions::checkNotNull);

		return GroupVector.from(ImmutableList.of(elements));
	}

	@Override
	protected List<E> delegate() {
		return this.elements.asList();
	}

	/**
	 * @return the group all elements belong to.
	 * @throws IllegalStateException if the vector is empty.
	 */
	public G getGroup() {
		if (this.isEmpty()) {
			throw new IllegalStateException("An empty GroupVector does not have a group.");
		} else {
			return this.group;
		}
	}

	/**
	 * @return the size of elements. 0 if the vector is empty.
	 */
	public int getElementSize() {
		return elementSize;
	}

	/**
	 * Appends a new element to this vector. Returns a new GroupVector.
	 *
	 * @param element The element to append. Must be non-null and from the same group.
	 * @return A new GroupVector with the appended {@code element}.
	 */
	public GroupVector<E, G> append(final E element) {
		checkNotNull(element);
		checkArgument(element.getGroup().equals(this.group), "The element to append must be in the same group.");
		checkArgument(element.size() == this.elementSize, "The element to append must be the same size.");

		return new GroupVector<>(Stream.concat(this.elements.stream(), Stream.of(element)).collect(toImmutableList()));
	}

	/**
	 * Prepends a new element to this vector. Returns a new GroupVector.
	 *
	 * @param element The element to prepend. Must be non-null and from the same group.
	 * @return A new GroupVector with the prepended {@code element}.
	 */
	public GroupVector<E, G> prepend(final E element) {
		checkNotNull(element);
		checkArgument(element.getGroup().equals(this.group), "The element to prepend must be in the same group.");
		checkArgument(element.size() == this.elementSize, "The element to prepend must be the same size.");

		return new GroupVector<>(Stream.concat(Stream.of(element), this.elements.stream()).collect(toImmutableList()));
	}

	/**
	 * Validate that a property holds for all elements.
	 *
	 * @param property to check all elements against.
	 * @return true if the vector is empty or all elements are equal under this property. False otherwise.
	 */
	public boolean allEqual(final Function<? super E, ?> property) {
		return Validations.allEqual(this.elements.stream(), property);
	}

	/**
	 * Transforms this vector into a matrix.
	 * <p>
	 * The elements of this vector <b><i>v</i></b> of size <i>N</i> = <i>mn</i> are rearranged into a matrix of size <i>m</i> &times; <i>n</i>, where
	 * element M<sub>i,j</sub> of the matrix corresponds to element v<sub>n*i + j</sub> of the vector.
	 *
	 * @param numRows    m, the number of rows of the matrix to be created
	 * @param numColumns n, the number of columns of the matrix to be created
	 * @return a {@link GroupMatrix} of size m &times; n
	 */
	public GroupMatrix<E, G> toMatrix(final int numRows, final int numColumns) {
		checkArgument(numRows > 0, "The number of rows must be positive.");
		checkArgument(numColumns > 0, "The number of columns must be positive.");

		final GroupVector<E, G> v = this;
		final int m = numRows;
		final int n = numColumns;

		// Ensure N = nm
		checkArgument(this.size() == (m * n), "The vector of ciphertexts must be decomposable into m rows and n columns.");

		// Create the matrix
		return IntStream.range(0, m)
				.mapToObj(i -> IntStream.range(0, n)
						.mapToObj(j -> v.get(n * i + j))
						.collect(toGroupVector()))
				.collect(Collectors.collectingAndThen(toGroupVector(), GroupMatrix::fromRows));
	}

	/**
	 * Returns a Collector that accumulates the input elements into a GroupVector.
	 *
	 * @param <E> the type of elements this list contains.
	 * @return a {@code Collector} for accumulating the input elements into a GroupVector.
	 */
	public static <E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> Collector<E, ?, GroupVector<E, G>> toGroupVector() {
		return Collectors.collectingAndThen(toImmutableList(), GroupVector::from);
	}

	/**
	 * @return an empty GroupVector.
	 */
	public static <E extends GroupVectorElement<G> & Hashable, G extends MathematicalGroup<G>> GroupVector<E, G> empty() {
		return new GroupVector<>(ImmutableList.emptyList());
	}

	/*
		Equivalent to java.util.List.subList
	 */
	public GroupVector<E, G> subVector(final int fromIndex, final int toIndex) {
		return GroupVector.from(this.elements.subList(fromIndex, toIndex));
	}

	@Override
	public String toString() {
		return "GroupVector{" + "elements=" + elements + '}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final GroupVector<?, ?> that = (GroupVector<?, ?>) o;
		return elements.equals(that.elements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elements);
	}

	@Override
	public Spliterator<E> spliterator() {
		return this.elements.spliterator();
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return this.elements.stream().collect(toImmutableList());
	}
}
