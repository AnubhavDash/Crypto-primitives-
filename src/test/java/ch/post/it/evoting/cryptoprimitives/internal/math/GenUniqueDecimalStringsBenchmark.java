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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GenUniqueDecimalStringsBenchmark {
	@Benchmark
	public void genUniqueDecimalStringsArrayList(final MyState state, final Blackhole bh) {
		final List<String> strings = state.genUniqueDecimalStringsArrayList(state.desiredCodeLength, state.numberOfUniqueCodes);
		bh.consume(strings);
	}

	@Benchmark
	public void genUniqueDecimalStringsHashSet(final MyState state, final Blackhole bh) {
		final List<String> strings = state.genUniqueDecimalStringsHashSet(state.desiredCodeLength, state.numberOfUniqueCodes);
		bh.consume(strings);
	}

	@Benchmark
	public void genUniqueDecimalStringsTreeSet(final MyState state, final Blackhole bh) {
		final List<String> strings = state.genUniqueDecimalStringsTreeSet(state.desiredCodeLength, state.numberOfUniqueCodes);
		bh.consume(strings);
	}

	@Benchmark
	public void genUniqueDecimalStringsConcurrentHashSetParallelized(final MyState state, final Blackhole bh) {
		final List<String> strings = state.genUniqueDecimalStringsConcurrentHashSet(state.desiredCodeLength, state.numberOfUniqueCodes, true);
		bh.consume(strings);
	}

	@Benchmark
	public void genUniqueDecimalStringsConcurrentHashSetNotParallelized(final MyState state, final Blackhole bh) {
		final List<String> strings = state.genUniqueDecimalStringsConcurrentHashSet(state.desiredCodeLength, state.numberOfUniqueCodes, false);
		bh.consume(strings);
	}

	@State(Scope.Benchmark)
	public static class MyState {
		private final TestRandomService randomService = new TestRandomService();

		private final int desiredCodeLength = 4;
		private final int numberOfUniqueCodes = 1000;

		public List<String> genUniqueDecimalStringsArrayList(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet A_10 = Base10Alphabet.getInstance();

			final List<String> codes = new ArrayList<>(n);
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, A_10);

				if (!codes.contains(c)) {
					codes.add(c);
				}
			}

			return codes;
		}

		public List<String> genUniqueDecimalStringsHashSet(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet A_10 = Base10Alphabet.getInstance();
			final Set<String> codes = HashSet.newHashSet(n);
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, A_10);

				codes.add(c);
			}

			return codes.stream().toList();
		}

		public List<String> genUniqueDecimalStringsTreeSet(final int desiredCodeLength, final int numberOfUniqueCodes) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet A_10 = Base10Alphabet.getInstance();

			final Set<String> codes = new TreeSet<>();
			while (codes.size() < n) {
				final String c = randomService.genRandomString(l, A_10);

				codes.add(c);
			}

			return codes.stream().toList();
		}

		public List<String> genUniqueDecimalStringsConcurrentHashSet(final int desiredCodeLength, final int numberOfUniqueCodes,
				final boolean parallel) {
			final int l = desiredCodeLength;
			final int n = numberOfUniqueCodes;
			checkArgument(l > 0, "The desired length of the unique codes must be strictly positive.");
			checkArgument(n > 0, "The number of unique codes must be strictly positive.");

			checkArgument(n <= Math.pow(10, l), "There cannot be more than 10^l codes.");

			final Alphabet A_10 = Base10Alphabet.getInstance();

			final Set<String> codes = ConcurrentHashMap.newKeySet();

			return (parallel ? IntStream.range(0, numberOfUniqueCodes).parallel() : IntStream.range(0, numberOfUniqueCodes))
					.mapToObj(i -> {
						String c;
						do {
							c = randomService.genRandomString(l, A_10);
						} while (!codes.add(c));
						return c;
					})
					.toList();
		}
	}
}

