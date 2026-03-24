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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal;

@Warmup(iterations = 1)
@Fork(value = 1)
@Measurement(iterations = 5)
public class ConversionServiceBenchmark {

	private static final TestRandomService randomService = new TestRandomService();

	@Benchmark
	public ImmutableByteArray bigIntegerToByteArray(final MyState state) {
		return ConversionsInternal.integerToByteArray(state.randomBigInteger);
	}

	@Benchmark
	public ImmutableByteArray bigIntegerToFixedLengthByteArrayUsingJdk(final MyState state) {
		return ConversionsInternal.integerToFixedLengthByteArray(state.randomBigInteger, state.bitLength);
	}

	@Benchmark
	public ImmutableByteArray bigIntegerToFixedLengthByteArray(final MyState state) {
		return ConversionsEquivalenceTest.integerToFixedLengthByteArraySpec(state.randomBigInteger, state.bitLength);
	}

	@State(Scope.Thread)
	public static class MyState {

		private BigInteger randomBigInteger;

		@Param({ "3072" })
		int bitLength;

		@Setup(Level.Invocation)
		public void genRandomBigInteger() {
			randomBigInteger = randomService.genRandomIntegerOfLength(bitLength);
		}
	}
}
