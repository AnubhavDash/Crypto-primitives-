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

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableSet.toImmutableSet;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/**
 * An immutable map of non-null elements.
 *
 * <p> Instances of this class are immutable. However, the immutability of the map
 * does not guarantee the immutability of the elements contained within the map. To achieve complete immutability, the elements themselves must be
 * immutable. </p>
 *
 * @param <K> the type of keys in the map. This type should be immutable to ensure the overall immutability of the map.
 * @param <V> the type of values in the map. This type should be immutable to ensure the overall immutability of the map.
 */
public class ImmutableMap<K, V> {

	private final Map<K, V> elements;

	private ImmutableMap(final Map<K, V> elements) {
		this.elements = Collections.unmodifiableMap(elements);
	}

	/**
	 * @param elements the elements to be added to the map.
	 * @param <K>      the type of keys in the map.
	 * @param <V>      the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if the elements are null, any of the elements is null or any of the keys/values is null.
	 */
	public static <K, V> ImmutableMap<K, V> from(final Map<K, V> elements) {
		return from(elements, ConcurrentHashMap::new);
	}

	/**
	 * @param elements   the elements to be added to the map.
	 * @param mapFactory the factory to be used to create the map.
	 * @param <K>        the type of keys in the map.
	 * @param <V>        the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a map created by the provided factory.
	 * @throws NullPointerException if the elements are null, any of the elements is null, any of the keys/values is null or the map factory is null.
	 */
	public static <K, V> ImmutableMap<K, V> from(final Map<K, V> elements, final Supplier<Map<K, V>> mapFactory) {
		checkNotNull(mapFactory);
		return checkNotNull(elements).entrySet().stream()
				.map(Preconditions::checkNotNull)
				.map(e -> entry(e.getKey(), e.getValue()))
				.collect(toImmutableMap(mapFactory));
	}

	/**
	 * @param k   the key to be added to the map.
	 * @param v   the value to be added to the map.
	 * @param <K> the type of keys in the map.
	 * @param <V> the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if the key or value is null.
	 */
	public static <K, V> ImmutableMap<K, V> of(final K k, final V v) {
		validate(k);
		validate(v);

		return Stream.of(entry(k, v)).collect(toImmutableMap());
	}

	/**
	 * @param k1  the first key to be added to the map.
	 * @param v1  the first value to be added to the map.
	 * @param k2  the second key to be added to the map.
	 * @param v2  the second value to be added to the map.
	 * @param <K> the type of keys in the map.
	 * @param <V> the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if any of the keys or values is null.
	 */
	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2) {
		validate(k1);
		validate(v1);
		validate(k2);
		validate(v2);

		return Stream.of(entry(k1, v1), entry(k2, v2)).collect(toImmutableMap());
	}

	/**
	 * @param k1  the first key to be added to the map.
	 * @param v1  the first value to be added to the map.
	 * @param k2  the second key to be added to the map.
	 * @param v2  the second value to be added to the map.
	 * @param k3  the third key to be added to the map.
	 * @param v3  the third value to be added to the map.
	 * @param <K> the type of keys in the map.
	 * @param <V> the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if any of the keys or values is null.
	 */
	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
		validate(k1);
		validate(v1);
		validate(k2);
		validate(v2);
		validate(k3);
		validate(v3);

		return Stream.of(entry(k1, v1), entry(k2, v2), entry(k3, v3)).collect(toImmutableMap());
	}

	/**
	 * @param k1  the first key to be added to the map.
	 * @param v1  the first value to be added to the map.
	 * @param k2  the second key to be added to the map.
	 * @param v2  the second value to be added to the map.
	 * @param k3  the third key to be added to the map.
	 * @param v3  the third value to be added to the map.
	 * @param k4  the fourth key to be added to the map.
	 * @param v4  the fourth value to be added to the map.
	 * @param <K> the type of keys in the map.
	 * @param <V> the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if any of the keys or values is null.
	 */
	@SuppressWarnings("java:S107") // This is the goal of the method to provide 8 parameters.
	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {
		validate(k1);
		validate(v1);
		validate(k2);
		validate(v2);
		validate(k3);
		validate(v3);
		validate(k4);
		validate(v4);

		return Stream.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4)).collect(toImmutableMap());
	}

	/**
	 * @param elements the elements to be added to the map.
	 * @param <K>      the type of keys in the map.
	 * @param <V>      the type of values in the map.
	 * @return an {@link ImmutableMap} of elements backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if the elements are null, any of the elements is null or any of the keys/values is null.
	 */
	@SafeVarargs
	public static <K, V> ImmutableMap<K, V> of(final Entry<K, V>... elements) {
		return Arrays.stream(checkNotNull(elements))
				.map(Preconditions::checkNotNull)
				.collect(toImmutableMap());
	}

	/**
	 * @return an empty {@link ImmutableMap}.
	 */
	public static <K, V> ImmutableMap<K, V> emptyMap() {
		return from(Collections.emptyMap());
	}

	/**
	 * @param <K> the type of keys in the map.
	 * @param <V> the type of values in the map.
	 * @return a collector that accumulates the input elements into an {@link ImmutableMap} backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException if any of the accumulated elements is null or any of the keys/values is null.
	 */
	public static <K, V> Collector<Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
		return toImmutableMap(Entry::key, Entry::value);
	}

	/**
	 * @param mapFactory the factory to be used to create the map.
	 * @param <K>        the type of keys in the map.
	 * @param <V>        the type of values in the map.
	 * @return a collector that accumulates the input elements into an {@link ImmutableMap} backed by a map created by the provided factory.
	 * @throws NullPointerException  if any of the accumulated elements is null, any of the keys/values is null or the map factory is null.
	 * @throws IllegalStateException if the map contains duplicate keys.
	 */
	public static <K, V> Collector<Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap(final Supplier<Map<K, V>> mapFactory) {
		checkNotNull(mapFactory);

		return toImmutableMap(Entry::key,
				Entry::value,
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s.", u));
				},
				mapFactory);
	}

	/**
	 * @param keyMapper   the function to be used to map the keys.
	 * @param valueMapper the function to be used to map the values.
	 * @param <T>         the type of elements in the stream.
	 * @param <K>         the type of keys in the map.
	 * @param <V>         the type of values in the map.
	 * @return a collector that accumulates the input elements into an {@link ImmutableMap} backed by a {@link ConcurrentHashMap}.
	 * @throws NullPointerException  if any of the accumulated elements is null, any of the keys/values is null or any of the mappers is null.
	 * @throws IllegalStateException if the map contains duplicate keys.
	 */
	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			final Function<? super T, ? extends K> keyMapper,
			final Function<? super T, ? extends V> valueMapper) {
		checkNotNull(keyMapper);
		checkNotNull(valueMapper);

		return toImmutableMap(
				keyMapper,
				valueMapper,
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s.", u));
				},
				ConcurrentHashMap::new);
	}

	/**
	 * @param keyMapper     the function to be used to map the keys.
	 * @param valueMapper   the function to be used to map the values.
	 * @param mergeFunction the function to be used to merge the values in case of duplicate keys.
	 * @param mapFactory    the factory to be used to create the map.
	 * @param <T>           the type of elements in the stream.
	 * @param <K>           the type of keys in the map.
	 * @param <V>           the type of values in the map.
	 * @return a collector that accumulates the input elements into an {@link ImmutableMap} backed by a map created by the provided factory.
	 * @throws NullPointerException if any of the accumulated elements is null, any of the keys/values is null or any of the parameters is null.
	 */
	private static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			final Function<? super T, ? extends K> keyMapper,
			final Function<? super T, ? extends V> valueMapper,
			final BinaryOperator<V> mergeFunction,
			final Supplier<Map<K, V>> mapFactory) {
		checkNotNull(keyMapper);
		checkNotNull(valueMapper);
		checkNotNull(mergeFunction);
		checkNotNull(mapFactory);

		return new Collector<T, Map<K, V>, ImmutableMap<K, V>>() {
			@Override
			public Supplier<Map<K, V>> supplier() {
				checkNotNull(mapFactory);

				return mapFactory;
			}

			@Override
			public BiConsumer<Map<K, V>, T> accumulator() {
				return (map, entry) -> {
					validate(entry);
					final K key = validate(keyMapper.apply(entry));
					final V value = validate(valueMapper.apply(entry));

					if (map.containsKey(key)) {
						map.put(key, validate(mergeFunction.apply(map.get(key), value)));
					} else {
						map.put(key, value);
					}
				};
			}

			@Override
			public BinaryOperator<Map<K, V>> combiner() {
				return (left, right) -> {
					final Set<K> rightKeySet = right.keySet();

					for (final K rightKey : rightKeySet) {
						if (left.containsKey(rightKey)) {
							left.put(rightKey, validate(mergeFunction.apply(left.get(rightKey), right.get(rightKey))));
						} else {
							left.put(rightKey, right.get(rightKey));
						}
					}

					return left;
				};
			}

			@Override
			public Function<Map<K, V>, ImmutableMap<K, V>> finisher() {
				return ImmutableMap::new;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};

	}

	/**
	 * @param key the key to be retrieved.
	 * @return the value associated with the key.
	 * @throws NullPointerException if the key is null.
	 */
	public V get(final K key) {
		validate(key);

		return elements.get(key);
	}

	/**
	 * @return an unmodifiable collection of the values in the map.
	 */
	public Collection<V> values() {
		return Collections.unmodifiableCollection(elements.values());
	}

	/**
	 * @param key the key to be checked.
	 * @return true if the map contains the key, false otherwise.
	 * @throws NullPointerException if the key is null.
	 */
	public boolean containsKey(final K key) {
		validate(key);

		return elements.containsKey(key);
	}

	/**
	 * @return true if the map is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * @return an {@link ImmutableSet} of the keys in the map.
	 */
	public ImmutableSet<K> keySet() {
		return ImmutableSet.from(elements.keySet());
	}

	/**
	 * @return an {@link ImmutableSet} of the entries in the map.
	 */
	public ImmutableSet<Entry<K, V>> entrySet() {
		return elements.entrySet().stream()
				.map(e -> new Entry<>(e.getKey(), e.getValue()))
				.collect(toImmutableSet());
	}

	/**
	 * @return the number of elements in the map.
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * @param action the action to be performed for each element.
	 */
	public void forEach(final BiConsumer<? super K, ? super V> action) {
		elements.forEach(action);
	}

	/**
	 * @return the elements in the map as an unmodifiable map.
	 */
	public Map<K, V> elements() {
		return elements;
	}

	@Override
	public String toString() {
		return String.format("ImmutableMap{elements=%s}", elements);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ImmutableMap<?, ?> that = (ImmutableMap<?, ?>) o;

		return elements.equals(that.elements);
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	private static <E> E validate(final E element) {
		return checkNotNull(element);
	}

	/**
	 * @param key   the key to be added to the map.
	 * @param value the value to be added to the map.
	 * @param <K>   the type of the key.
	 * @param <V>   the type of the value.
	 * @return a map entry with the provided key and value.
	 * @throws NullPointerException if the key or value is null.
	 */
	public static <K, V> Entry<K, V> entry(final K key, final V value) {
		validate(key);
		validate(value);

		return new Entry<>(key, value);
	}

	/**
	 * A map entry record.
	 * <p> Instances of this record are immutable. However, the immutability of the record
	 * does not guarantee the immutability of the elements contained within the record. To achieve complete immutability, the elements themselves must
	 * be immutable. </p>
	 *
	 * @param key   the key of the entry. Must be non-null.
	 * @param value the value of the entry. Must be non-null.
	 * @param <K>   the type of the key.
	 * @param <V>   the type of the value.
	 */
	public record Entry<K, V>(K key, V value) {
		public Entry {
			validate(key);
			validate(value);
		}

		/**
		 * @param <K> the type of the key.
		 * @param <V> the type of the value.
		 * @return a comparator that compares entries by their keys.
		 */
		public static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey() {
			return (Comparator<Entry<K, V>> & Serializable) (c1, c2) -> c1.key().compareTo(c2.key());
		}
	}
}
