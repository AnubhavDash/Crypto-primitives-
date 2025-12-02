/*
 * Copyright 2025 Swiss Post Ltd
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
package ch.post.it.evoting.cryptoprimitives.utils;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class KDFBenchmark {

	@Benchmark
	public ImmutableByteArray KDF(final KDFBenchmarkState state) {
		return state.kdfService.KDF(state.pseudoRandomKey, state.contextInformation, state.keyLength);
	}

	@Benchmark
	public ZqElement KDFToZq(final KDFBenchmarkState state) {
		return state.kdfService.KDFToZq(state.pseudoRandomKey, state.contextInformation, state.uppeBound);
	}

	@State(Scope.Benchmark)
	public static class KDFBenchmarkState {
		private static final RandomService randomService = new RandomService();

		private final KDFService kdfService = KDFService.getInstance();

		private ImmutableByteArray pseudoRandomKey;
		private ImmutableList<String> contextInformation;

		@Param({ "128", "256", "512", "1024", "2048", "3072" })
		private int keyLength;
		private BigInteger uppeBound;

		@Setup
		public void setup() {
			pseudoRandomKey = randomService.randomBytes(keyLength);
			contextInformation = randomService.genUniqueDecimalStrings(32, 4);
			uppeBound = BigInteger.TWO.pow(keyLength);
		}
	}
}
