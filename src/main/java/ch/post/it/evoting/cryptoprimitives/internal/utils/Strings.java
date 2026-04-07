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
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.utils.Validations;

public final class Strings {

	private Strings() {
		// Intentionally left blank.
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Strings#truncate(String, int)}
	 */
	public static String truncate(final String string, final int length) {

		final String S = checkNotNull(string);
		final int u = S.length();
		final int l = length;

		checkArgument(l >= 0, "The input length must be non-negative. [l: %s]", l);

		// Operation. This implementation yields the same result as the specification's pseudocode,
		// and we have a corresponding unit test that asserts the equivalence of the two implementations.
		final int m = Math.min(u, l);
		return S.substring(0, m);
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Strings#getMergedString(ImmutableList, Alphabet)}
	 */
	@SuppressWarnings("java:S117")
	public static String getMergedString(final ImmutableList<String> strings, final Alphabet alphabet) {
		// Input.
		final ImmutableList<String> s = checkNotNull(strings);
		final Alphabet A = checkNotNull(alphabet);

		checkArgument(alphabet.size() >= 1, "The alphabet must contain at least one character.");
		checkArgument(!s.isEmpty(), "The list of strings must not be empty.");
		checkArgument(s.stream().noneMatch(String::isEmpty), "The strings must have at least one character.");
		checkArgument(Validations.allEqual(s.stream(), String::length), "All strings must have the same length.");
		checkArgument(s.stream().allMatch(S_i -> S_i.chars().allMatch(A::contains)), "All strings must be defined in the given alphabet.");

		final int k = s.size();
		final int n = s.getFirst().length();
		final int N = A.size();

		// Operation.
		return IntStream.range(0, n)
				.map(j -> IntStream.range(0, k)
							.map(i -> A.indexOf(String.valueOf(s.get(i).charAt(j)))) // = rank_A(c_i_j)
							.sum())
				.map(x -> x % N)
				.mapToObj(A::get) // = rank_A^-1(x)
				.collect(Collectors.joining());
	}
}
