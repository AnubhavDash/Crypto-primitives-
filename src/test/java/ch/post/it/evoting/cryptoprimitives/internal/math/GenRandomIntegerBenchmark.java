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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
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

import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GenRandomIntegerBenchmark {

	@Benchmark
	public BigInteger genRandomInteger(final MyState state) {
		return state.randomService.genRandomInteger(state.upperBound);
	}

	@Benchmark
	public BigInteger genRandomIntegerWithBigInteger(final MyState state) {
		return MyState.genRandomIntegerWithBigInteger(state.upperBound, state.secureRandom);
	}

	@State(Scope.Benchmark)
	public static class MyState {
		private final TestRandomService randomService = new TestRandomService();
		private final SecureRandom secureRandom = new SecureRandom();

		private final BigInteger upperBound = BigInteger.TWO.pow(SecurityLevelInternal.STANDARD.getPBitLength());

		public static BigInteger genRandomIntegerWithBigInteger(final BigInteger upperBound, final SecureRandom secureRandom) {
			// Input.
			checkNotNull(upperBound);
			checkArgument(upperBound.signum() > 0, "The upper bound must be a positive integer greater than 0.");
			final BigInteger m = upperBound;

			// Operation.
			final int bitLength = m.bitLength();
			BigInteger r;
			do {
				// This constructor internally masks the excess generated bits.
				r = new BigInteger(bitLength, secureRandom);
			} while (r.compareTo(m) >= 0);

			// Output.
			return r;
		}
	}
}
