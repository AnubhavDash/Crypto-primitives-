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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

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

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@BenchmarkMode(value = Mode.AverageTime)
@Fork(value = 1)
@Measurement(iterations = 5)
@Warmup(iterations = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
public class EncryptionParametersBenchmark {

	private static final ImmutableList<Integer> SMALL_PRIMES = PrimesInternal.getSmallPrimes();
	public static final EncryptionParameters encryptionParameters = new EncryptionParameters();

	@Benchmark
	public GqGroup benchGetEncryptionParameters(final BenchmarkState state) {
		return encryptionParameters.getEncryptionParameters(state.seed, SMALL_PRIMES);
	}

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
		String seed;
	}
}
