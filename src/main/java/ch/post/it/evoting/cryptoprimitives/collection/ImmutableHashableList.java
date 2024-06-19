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

import java.util.List;
import java.util.function.Function;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;

/**
 * An {@link ImmutableList} of hashable elements.
 *
 * <p>Instances of this class are immutable. </p>
 *
 * @param <E> the type of elements in the list, it must extend {@link Hashable}.
 */
public final class ImmutableHashableList<E extends Hashable> extends ImmutableList<E> implements HashableList {

	private ImmutableHashableList(final List<E> elements) {
		super(elements);
	}

	@Override
	public List<E> toHashableForm() {
		return elements;
	}

	/**
	 * Returns an {@link ImmutableList} of hashable elements.
	 */
	public static <E extends Hashable, T> ImmutableHashableList<E> from(final ImmutableList<T> list, final Function<T, E> mappingFunction) {
		checkNotNull(list);
		checkNotNull(mappingFunction);

		return new ImmutableHashableList<>(list.stream().map(mappingFunction).toList());
	}

}

