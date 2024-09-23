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
import java.util.stream.Collectors;

public class ImmutableMap<K, V> {

	private final Map<K, V> elements;

	private ImmutableMap(final Map<K, V> elements) {
		this.elements = Collections.unmodifiableMap(elements);
	}

	public static <K, V> ImmutableMap<K, V> from(final Map<K, V> elements) {
		return checkNotNull(elements).entrySet().stream()
				.map(e -> ImmutableMap.entry(e.getKey(), e.getValue()))
				.collect(toImmutableMap());
	}

	public static <K, V> ImmutableMap<K, V> from(final Map<K, V> elements, final Supplier<Map<K, V>> mapFactory) {
		return checkNotNull(elements).entrySet().stream()
				.map(e -> ImmutableMap.entry(e.getKey(), e.getValue()))
				.collect(toImmutableMap(mapFactory));
	}

	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1) {
		return ImmutableMap.of(ImmutableMap.entry(k1, v1));
	}

	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2) {
		return ImmutableMap.of(new Entry[] { ImmutableMap.entry(k1, v1), ImmutableMap.entry(k2, v2) });
	}

	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
		return ImmutableMap.of(ImmutableMap.entry(k1, v1), ImmutableMap.entry(k2, v2), ImmutableMap.entry(k3, v3));
	}

	public static <K, V> ImmutableMap<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {
		return ImmutableMap.of(
				new Entry[] { ImmutableMap.entry(k1, v1), ImmutableMap.entry(k2, v2), ImmutableMap.entry(k3, v3), ImmutableMap.entry(k4, v4) });
	}

	@SafeVarargs
	public static <K, V> ImmutableMap<K, V> of(final Entry<K, V>... elements) {
		return Arrays.stream(checkNotNull(elements)).collect(toImmutableMap());
	}

	public static <K, V> ImmutableMap<K, V> emptyMap() {
		return ImmutableMap.from(Collections.emptyMap());
	}

	public static <K, V> Collector<Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
		return toImmutableMap(Entry::key, Entry::value);
	}

	public static <K, V> Collector<Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap(final Supplier<Map<K, V>> mapFactory) {
		return toImmutableMap(Entry::key,
				Entry::value,
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				mapFactory);
	}

	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			final Function<? super T, ? extends K> keyMapper,
			final Function<? super T, ? extends V> valueMapper) {
		return toImmutableMap(
				keyMapper,
				valueMapper,
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				ConcurrentHashMap::new);
	}

	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			final Function<? super T, ? extends K> keyMapper,
			final Function<? super T, ? extends V> valueMapper,
			final BinaryOperator<V> mergeFunction,
			final Supplier<Map<K, V>> mapFactory) {
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

	public V get(final K key) {
		validate(key);

		return elements.get(key);
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(elements.values());
	}

	public boolean containsKey(final K key) {
		validate(key);

		return elements.containsKey(key);
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public Set<K> keySet() {
		return Collections.unmodifiableSet(elements.keySet()); // TODO: change to ImmutableSet
	}

	public Set<Entry<K, V>> entrySet() {
		return elements.entrySet().stream()
				.map(e -> new Entry<>(e.getKey(), e.getValue()))
				.collect(Collectors.toUnmodifiableSet()); // TODO: change to ImmutableSet
	}

	public int size() {
		return elements.size();
	}

	public void forEach(final BiConsumer<? super K, ? super V> action) {
		elements.forEach(action);
	}

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

	public static <K, V> Entry<K, V> entry(final K key, final V value) {
		validate(key);
		validate(value);

		return new Entry<>(key, value);
	}

	public record Entry<K, V>(K key, V value) {
		public Entry {
			validate(key);
			validate(value);
		}

		public static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey() {
			return (Comparator<Entry<K, V>> & Serializable) (c1, c2) -> c1.key().compareTo(c2.key());
		}
	}
}
