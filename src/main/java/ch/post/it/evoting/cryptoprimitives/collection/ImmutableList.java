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
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/**
 * An immutable list of non-null elements.
 *
 * <p>Instances of this class are immutable. </p>
 *
 * @param <E> the type of elements in the list.
 */
public class ImmutableList<E> {

	protected final List<E> elements;

	public ImmutableList(final List<E> elements) {
		this.elements = checkNotNull(elements).stream().map(Preconditions::checkNotNull).toList();
	}

	public List<E> elements() {
		return List.copyOf(elements);
	}

	public Stream<E> stream() {
		return elements().stream();
	}

	public int size() {
		return elements.size();
	}

	public E get(final int index) {
		return elements.get(index);
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public boolean contains(final E element) {
		return elements.contains(element);
	}

	public boolean containsAll(final List<E> elements) {
		return this.elements.containsAll(elements);
	}

}
