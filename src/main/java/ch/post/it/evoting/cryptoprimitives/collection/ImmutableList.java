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
package ch.post.it.evoting.cryptoprimitives.collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * An immutable list of non-null elements.
 *
 * <p>Instances of this class are immutable. However, the immutability of the list
 * does not guarantee the immutability of the elements contained within the list. To achieve complete immutability, the elements themselves must be
 * immutable.
 *
 * @param <E> the type of elements in the list. This type should be immutable to ensure the overall immutability of the list.
 */
public class ImmutableList<E> implements Iterable<E> {

	private final List<E> internalList;

	private ImmutableList(final List<E> internalList) {
		this.internalList = Collections.unmodifiableList(internalList);
	}

	/**
	 * @param elements the elements to be added to the list.
	 * @param <E>      the type of elements in the list.
	 * @return an {@link ImmutableList} of elements.
	 * @throws NullPointerException if the elements are null or any of the elements is null.
	 */
	public static <E> ImmutableList<E> from(final List<E> elements) {
		return checkNotNull(elements).stream()
				.map(ImmutableList::validateElement)
				.collect(toImmutableList());
	}

	/**
	 * @param elements the elements to be added to the list.
	 * @param <E>      the type of elements in the list.
	 * @return an {@link ImmutableList} of elements.
	 * @throws NullPointerException if the elements are null or any of the elements is null.
	 */
	@SafeVarargs
	public static <E> ImmutableList<E> of(final E... elements) {
		return Arrays.stream(checkNotNull(elements))
				.map(ImmutableList::validateElement)
				.collect(toImmutableList());
	}

	/**
	 * @return an empty {@link ImmutableList}.
	 */
	public static <E> ImmutableList<E> emptyList() {
		return ImmutableList.from(Collections.emptyList());
	}

	/**
	 * @param <E> the type of elements in the list.
	 * @return a collector that accumulates the input elements into an {@link ImmutableList}.
	 * @throws NullPointerException if any of the accumulated elements is null.
	 */
	public static <E> Collector<E, List<E>, ImmutableList<E>> toImmutableList() {
		return new Collector<>() {
			@Override
			public Supplier<List<E>> supplier() {
				return ArrayList::new;
			}

			@Override
			public BiConsumer<List<E>, E> accumulator() {
				return (list, element) -> list.add(validateElement(element));
			}

			@Override
			public BinaryOperator<List<E>> combiner() {
				return (left, right) -> {
					left.addAll(right);
					return left;
				};
			}

			@Override
			public Function<List<E>, ImmutableList<E>> finisher() {
				return ImmutableList::new;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};
	}

	/**
	 * @param elements the elements to be appended to the list. Must be non-null and must not contain null elements.
	 * @return a new {@link ImmutableList} with the appended elements.
	 */
	@SafeVarargs
	public final ImmutableList<E> append(final E... elements) {
		final List<E> validated = Arrays.stream(checkNotNull(elements))
				.map(ImmutableList::validateElement)
				.toList();

		final List<E> list = new ArrayList<>(this.internalList);
		list.addAll(validated);

		// Since the existing elements have already been validated we can safely instantiate the new ImmutableList directly through the constructor.
		return new ImmutableList<>(list);
	}

	/**
	 * @param other the other immutable list whose elements are to be appended to this list. Must be non-null.
	 * @return a new {@link ImmutableList} with the appended elements.
	 */
	public final ImmutableList<E> append(final ImmutableList<E> other) {
		checkNotNull(other);
		// other's elements do not require validation since it is already an ImmutableList.

		final List<E> list = new ArrayList<>(this.internalList);
		list.addAll(other.asList());

		// Since the existing elements have already been validated we can safely instantiate the new ImmutableList directly through the constructor.
		return new ImmutableList<>(list);
	}

	/**
	 * @return an unmodifiable list containing the elements.
	 * @see Collections#unmodifiableList(List)
	 */
	public List<E> asList() {
		return internalList;
	}

	/**
	 * @see List#stream()
	 */
	public Stream<E> stream() {
		return internalList.stream();
	}

	/**
	 * @see List#size()
	 */
	public int size() {
		return internalList.size();
	}

	/**
	 * @param index the index of the element to return. Must be non-negative and less than the size of this list.
	 * @return the element at the specified position in this list.
	 * @throws IllegalArgumentException if the index is out of range.
	 */
	public E get(final int index) {
		checkArgument(index >= 0 && index < size(), "Index is out of bounds. [index: %s, size: %s]", index, size());

		return internalList.get(index);
	}

	/**
	 * @return the first element of the list.
	 * @throws NoSuchElementException if the list is empty.
	 * @see List#getFirst()
	 */
	public E getFirst() {
		if (this.isEmpty()) {
			throw new NoSuchElementException("The list is empty.");
		} else {
			return internalList.getFirst();
		}
	}

	/**
	 * @return the last element of the list.
	 * @throws NoSuchElementException if the list is empty.
	 * @see List#getLast()
	 */
	public E getLast() {
		if (this.isEmpty()) {
			throw new NoSuchElementException("The list is empty.");
		} else {
			return internalList.getLast();
		}
	}

	/**
	 * @see List#isEmpty()
	 */
	public boolean isEmpty() {
		return internalList.isEmpty();
	}

	/**
	 * @throws NullPointerException if the element is null.
	 * @see List#contains(Object)
	 */
	public boolean contains(final E element) {
		validateElement(element);

		return internalList.contains(element);
	}

	/**
	 * @throws NullPointerException if the list is null.
	 * @see List#containsAll(Collection)
	 */
	public boolean containsAll(final ImmutableList<E> immutableList) {
		checkNotNull(immutableList);

		return internalList.containsAll(immutableList.asList());
	}

	/**
	 * @throws NullPointerException if the set is null.
	 * @see List#containsAll(Collection)
	 */
	public boolean containsAll(final ImmutableSet<E> immutableSet) {
		checkNotNull(immutableSet);

		return internalList.containsAll(immutableSet.asSet());
	}

	/**
	 * @param fromIndex the starting index (inclusive). Must be non-negative and less than the size of this list.
	 * @param toIndex   the ending index (exclusive). Must be greater than or equal to the starting index and less than or equal to the size of this
	 *                  list.
	 * @return a new {@link ImmutableList} containing the elements between the specified indexes.
	 * @throws IllegalArgumentException if the indexes are out of bounds.
	 */
	public ImmutableList<E> subList(final int fromIndex, final int toIndex) {
		checkArgument(0 <= fromIndex && fromIndex <= toIndex && toIndex <= size(),
				"Indexes are out of bounds. [fromIndex: %s, toIndex: %s, size: %s]", fromIndex, toIndex, size());

		return internalList.subList(fromIndex, toIndex).stream().collect(toImmutableList());
	}

	/**
	 * @see List#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return internalList.iterator();
	}

	/**
	 * @see List#spliterator()
	 */
	@Override
	public Spliterator<E> spliterator() {
		return internalList.spliterator();
	}

	/**
	 * @see List#forEach(Consumer)
	 */
	@Override
	public void forEach(final Consumer<? super E> action) {
		checkNotNull(action);

		internalList.forEach(action);
	}

	/**
	 * @throws NullPointerException if the element is null.
	 * @see List#indexOf(Object)
	 */
	public int indexOf(final E element) {
		validateElement(element);

		return internalList.indexOf(element);
	}

	/**
	 * @throws NullPointerException if the specified array is null.
	 * @see List#toArray(Object[])
	 */
	public <T> T[] toArray(final T[] a) {
		checkNotNull(a);

		return internalList.toArray(a);
	}

	/**
	 * @return an {@link ImmutableSet} containing the elements of the list.
	 */
	public ImmutableSet<E> toImmutableSet() {
		return internalList.stream().collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public String toString() {
		return String.format("ImmutableList{elements=%s}", internalList);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ImmutableList<?> that = (ImmutableList<?>) o;

		return internalList.equals(that.internalList);
	}

	@Override
	public int hashCode() {
		return internalList.hashCode();
	}

	private static <E> E validateElement(final E element) {
		return checkNotNull(element);
	}
}
