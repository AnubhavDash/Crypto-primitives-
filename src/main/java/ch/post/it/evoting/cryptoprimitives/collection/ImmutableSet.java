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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
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
 * An immutable set of non-null elements.
 *
 * <p> Instances of this class are immutable. However, the immutability of the set does not guarantee the immutability of the elements contained
 * within the set. To achieve complete immutability, the elements themselves must be immutable.
 *
 * @param <E> the type of elements in the set. This type should be immutable to ensure the overall immutability of the set.
 */
public class ImmutableSet<E> implements Iterable<E> {

	private final Set<E> internalSet;

	private ImmutableSet(final Set<E> internalSet) {
		this.internalSet = Collections.unmodifiableSet(internalSet);
	}

	/**
	 * @param elements the elements to include in the set. The elements must be non-null.
	 * @param <E>      the type of elements in the set.
	 * @return an {@link ImmutableSet} containing the given elements.
	 * @throws NullPointerException if the given elements are null or if any of the elements is null.
	 */
	public static <E> ImmutableSet<E> from(final Set<E> elements) {
		return checkNotNull(elements).stream()
				.map(ImmutableSet::validateElement)
				.collect(toImmutableSet());
	}

	/**
	 * @param elements the elements to include in the set. The elements must be non-null.
	 * @param <E>      the type of elements in the set.
	 * @return an {@link ImmutableSet} containing the given elements.
	 * @throws NullPointerException if the given elements are null or if any of the elements is null.
	 */
	@SafeVarargs
	public static <E> ImmutableSet<E> of(final E... elements) {
		return Arrays.stream(checkNotNull(elements))
				.map(ImmutableSet::validateElement)
				.collect(toImmutableSet());
	}

	/**
	 * @param <E> the type of elements in the set.
	 * @return an empty {@link ImmutableSet}.
	 */
	public static <E> ImmutableSet<E> emptySet() {
		return from(Collections.emptySet());
	}

	/**
	 * @param <E> the type of elements in the set.
	 * @return a {@link Collector} that collects elements into an {@link ImmutableSet}.
	 */
	public static <E> Collector<E, Set<E>, ImmutableSet<E>> toImmutableSet() {
		return new Collector<>() {
			@Override
			public Supplier<Set<E>> supplier() {
				return HashSet::new;
			}

			@Override
			public BiConsumer<Set<E>, E> accumulator() {
				return (set, element) -> set.add(validateElement(element));
			}

			@Override
			public BinaryOperator<Set<E>> combiner() {
				return (left, right) -> {
					if (left.size() < right.size()) {
						right.addAll(left);
						return right;
					} else {
						left.addAll(right);
						return left;
					}
				};
			}

			@Override
			public Function<Set<E>, ImmutableSet<E>> finisher() {
				return ImmutableSet::new;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));
			}
		};
	}

	/**
	 * @see Set#size()
	 */
	public int size() {
		return internalSet.size();
	}

	/**
	 * @throws NullPointerException if the given element is null.
	 * @see Set#contains(Object)
	 */
	public boolean contains(final E element) {
		validateElement(element);

		return internalSet.contains(element);
	}

	/**
	 * @throws NullPointerException if the given collection is null.
	 * @see Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final ImmutableSet<E> immutableSet) {
		checkNotNull(immutableSet);

		return internalSet.containsAll(immutableSet.asSet());
	}

	/**
	 * @see Set#isEmpty()
	 */
	public boolean isEmpty() {
		return internalSet.isEmpty();
	}

	/**
	 * @see Set#stream()
	 */
	public Stream<E> stream() {
		return internalSet.stream();
	}

	/**
	 * @return an unmodifiable set containing the elements.
	 * @see Collections#unmodifiableSet(Set)
	 */
	public Set<E> asSet() {
		return internalSet;
	}

	/**
	 * @throws NullPointerException if the given action is null.
	 * @see Set#forEach(Consumer)
	 */
	@Override
	public void forEach(final Consumer<? super E> action) {
		checkNotNull(action);

		internalSet.forEach(action);
	}

	/**
	 * @see Set#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return internalSet.iterator();
	}

	/**
	 * @see Set#spliterator()
	 */
	@Override
	public Spliterator<E> spliterator() {
		return internalSet.spliterator();
	}

	@Override
	public String toString() {
		return String.format("ImmutableSet{elements=%s}", internalSet);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ImmutableSet<?> that = (ImmutableSet<?>) o;

		return internalSet.equals(that.asSet());
	}

	@Override
	public int hashCode() {
		return internalSet.hashCode();
	}

	private static <E> E validateElement(final E element) {
		return checkNotNull(element);
	}
}
