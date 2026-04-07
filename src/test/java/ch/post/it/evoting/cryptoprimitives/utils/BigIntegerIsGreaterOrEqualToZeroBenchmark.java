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
package ch.post.it.evoting.cryptoprimitives.utils;

import java.math.BigInteger;
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
import org.openjdk.jmh.infra.Blackhole;

import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BigIntegerIsGreaterOrEqualToZeroBenchmark {

	@Benchmark
	public void compareTo(final MyState state, final Blackhole bh) {
		final boolean result = state.randomInteger.compareTo(BigInteger.ZERO) >= 0;
		bh.consume(result);
	}

	@Benchmark
	public void signum(final MyState state, final Blackhole bh) {
		final boolean result = state.randomInteger.signum() >= 0;
		bh.consume(result);
	}

	@State(Scope.Benchmark)
	public static class MyState {
		private final TestRandomService randomService = new TestRandomService();
		private final BigInteger randomInteger = randomService.genRandomInteger(BigInteger.TWO.pow(SecurityLevelInternal.STANDARD.getPBitLength()));
	}
}
