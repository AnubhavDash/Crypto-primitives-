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
package ch.post.it.evoting.cryptoprimitives.hashing;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * Interface to be implemented by classes whose hashable form is an {@link ImmutableList} of {@link Hashable} elements.
 */
public interface HashableList extends Hashable {

	@Override
	ImmutableList<? extends Hashable> toHashableForm();

	/**
	 * Creates a {@link HashableList} whose hashable form is the provided {@link ImmutableList} list.
	 *
	 * @param list the hashable form. Non null.
	 * @return a new {@link HashableList} whose hashable form is {@code list}
	 */
	static HashableList from(final ImmutableList<? extends Hashable> list) {
		checkNotNull(list);

		return () -> list;
	}

	/**
	 * Creates a {@link HashableList} whose hashable form is an {@link ImmutableList} containing the provided elements.
	 *
	 * @param elements the hashable elements to construct a {@link HashableList} from. Non-null and must not contain nulls.
	 * @param <E>      the type of the elements
	 * @return a {@link HashableList} with the provided elements
	 */
	@SafeVarargs
	static <E extends Hashable> HashableList of(final E... elements) {
		checkNotNull(elements);
		Arrays.stream(elements).forEach(Preconditions::checkNotNull);

		return from(ImmutableList.of(elements));
	}

	/**
	 * @return a {@link Collector} for accumulating the input elements into a {@link HashableList}
	 */
	static Collector<Hashable, ?, HashableList> toHashableList() {
		return Collectors.collectingAndThen(toImmutableList(), HashableList::from);
	}

}
