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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableMap;
import ch.post.it.evoting.cryptoprimitives.math.Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base10Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base16Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base32Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.UsabilityBase32Alphabet;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class GenRandomStringBenchmark {

	@Benchmark
	public String genRandomString(final MyState state) {
		return state.randomService.genRandomString(state.length, state.alphabets.get(state.alphabet));
	}

	@State(Scope.Benchmark)
	public static class MyState {
		private static final String BASE_10_ALPHABET = "Base10Alphabet";
		private static final String BASE_16_ALPHABET = "Base16Alphabet";
		private static final String BASE_32_ALPHABET = "Base32Alphabet";
		private static final String USABILITY_BASE_32_ALPHABET = "UsabilityBase32Alphabet";
		private static final String BASE_64_ALPHABET = "Base64Alphabet";

		private final RandomService randomService = new RandomService();
		private final ImmutableMap<String, Alphabet> alphabets = ImmutableMap.of(
				ImmutableMap.entry(BASE_10_ALPHABET, Base10Alphabet.getInstance()),
				ImmutableMap.entry(BASE_16_ALPHABET, Base16Alphabet.getInstance()),
				ImmutableMap.entry(BASE_32_ALPHABET, Base32Alphabet.getInstance()),
				ImmutableMap.entry(USABILITY_BASE_32_ALPHABET, UsabilityBase32Alphabet.getInstance()),
				ImmutableMap.entry(BASE_64_ALPHABET, Base64Alphabet.getInstance()));

		@Param({"1", "100", "10000"})
		private int length;
		@Param({ BASE_10_ALPHABET, BASE_16_ALPHABET, BASE_32_ALPHABET, USABILITY_BASE_32_ALPHABET, BASE_64_ALPHABET })
		private String alphabet;
	}
}
