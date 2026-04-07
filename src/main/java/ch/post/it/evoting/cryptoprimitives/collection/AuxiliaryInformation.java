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
package ch.post.it.evoting.cryptoprimitives.collection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

/**
 * The hashable auxiliary information wrapping an {@link ImmutableList} of {@link HashableString}s.
 *
 * <p>Instances of this class are immutable.
 */
public final class AuxiliaryInformation extends ImmutableList<HashableString> implements HashableList {

	private AuxiliaryInformation(final ImmutableList<HashableString> hashableStrings) {
		super(hashableStrings);
	}

	/**
	 * Creates a new AuxiliaryInformation with the given {@code strings}.
	 *
	 * @param strings the strings to wrap.
	 * @return the auxiliary information wrapping the given strings.
	 * @throws NullPointerException if {@code strings} is null.
	 */
	public static AuxiliaryInformation of(final String... strings) {
		checkNotNull(strings);

		return new AuxiliaryInformation(Arrays.stream(strings).map(HashableString::from).collect(toImmutableList()));
	}

	/**
	 * Creates a new AuxiliaryInformation from the given immutable list of {@code strings}.
	 *
	 * @param strings the strings to wrap.
	 * @return the auxiliary information wrapping the given list of strings.
	 * @throws NullPointerException if {@code strings} is null.
	 */
	public static AuxiliaryInformation from(final ImmutableList<String> strings) {
		checkNotNull(strings);

		return new AuxiliaryInformation(strings.stream().map(HashableString::from).collect(toImmutableList()));
	}

	/**
	 * @return a new {@link AuxiliaryInformation} with the given element appended to the list.
	 * @throws NullPointerException if {@code string} is null.
	 */
	@Override
	public AuxiliaryInformation append(final HashableString string) {
		checkNotNull(string);

		return new AuxiliaryInformation(super.append(string));
	}

	/**
	 * Appends the given string to the list.
	 *
	 * @param string the string to append.
	 * @return a new {@link AuxiliaryInformation} with the given element appended to the list.
	 * @throws NullPointerException if {@code string} is null.
	 */
	public AuxiliaryInformation append(final String string) {
		return append(HashableString.from(string));
	}

	@Override
	public ImmutableList<HashableString> toHashableForm() {
		return this;
	}

}

