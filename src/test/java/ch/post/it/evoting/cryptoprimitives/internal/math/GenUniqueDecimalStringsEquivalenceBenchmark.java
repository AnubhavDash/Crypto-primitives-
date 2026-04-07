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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GenUniqueDecimalStringsEquivalenceBenchmark {
	@Benchmark
	public ImmutableList<String> genUniqueDecimalStringsArrayList(final MyState state) {
		return state.genUniqueDecimalStringsArrayList(state.desiredCodeLength, state.numberOfUniqueCodes);
	}

	@Benchmark
	public ImmutableList<String> genUniqueDecimalStringsLinkedList(final MyState state) {
		return state.genUniqueDecimalStringsLinkedList(state.desiredCodeLength, state.numberOfUniqueCodes);
	}

	@Benchmark
	public ImmutableList<String> genUniqueDecimalStringsLinkedHashSet(final MyState state) {
		return state.genUniqueDecimalStringsLinkedHashSet(state.desiredCodeLength, state.numberOfUniqueCodes);
	}

	@State(Scope.Benchmark)
	public static class MyState {
		private final TestRandomService randomService = new TestRandomService();

		private final int desiredCodeLength = 4;
		private final int numberOfUniqueCodes = 1000;

		public ImmutableList<String> genUniqueDecimalStringsArrayList(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet base10Alphabet = Base10Alphabet.getInstance();

			final List<String> codes = new ArrayList<>(n);
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, base10Alphabet);

				if (!codes.contains(c)) {
					codes.add(c);
				}
			}

			return codes.stream().collect(toImmutableList());
		}

		public ImmutableList<String> genUniqueDecimalStringsLinkedList(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet base10Alphabet = Base10Alphabet.getInstance();

			final List<String> codes = new LinkedList<>();
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, base10Alphabet);

				if (!codes.contains(c)) {
					codes.add(c);
				}
			}

			return codes.stream().collect(toImmutableList());
		}

		public ImmutableList<String> genUniqueDecimalStringsLinkedHashSet(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet base10Alphabet = Base10Alphabet.getInstance();

			final Set<String> codes = new LinkedHashSet<>();
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, base10Alphabet);

				codes.add(c);
			}

			return codes.stream().collect(toImmutableList());
		}
	}
}

