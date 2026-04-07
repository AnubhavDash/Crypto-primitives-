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
package ch.post.it.evoting.cryptoprimitives.internal.hashing;

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
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RecursiveHashBenchmark {

	@Benchmark
	public ImmutableByteArray recursiveHash(final RecursiveHashBenchmarkState state) {
		return state.hashService.recursiveHash(state.hashable);
	}

	@Benchmark
	public ZqElement recursiveHashToZq(final RecursiveHashBenchmarkState state) {
		return state.hashService.recursiveHashToZq(RecursiveHashBenchmarkState.UPPER_BOUND, state.hashable);
	}

	@Benchmark
	public ImmutableByteArray recursiveHashOfLength(final RecursiveHashBenchmarkState state) {
		return state.hashService.recursiveHashOfLength(3071, state.hashable);
	}

	@State(Scope.Benchmark)
	public static class RecursiveHashBenchmarkState {
		private static final RandomService randomService = new RandomService();
		private static final int LENGTH = 3072;
		private static final BigInteger UPPER_BOUND = BigInteger.TWO.pow(LENGTH);

		private final HashService hashService = HashService.getInstance();

		@Param({ "BYTE_ARRAY", "BIG_INTEGER", "STRING", "LIST", "NESTED" })
		private String mode;

		private Hashable hashable;

		@Setup
		public void setup() {
			final Mode modeEnum = Mode.valueOf(mode);

			hashable = switch (modeEnum) {
				case BYTE_ARRAY -> randomService.randomBytes(LENGTH);
				case BIG_INTEGER -> HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND));
				case STRING -> HashableString.from(randomService.genRandomString(LENGTH, Base64Alphabet.getInstance()));
				case LIST -> HashableList.of(randomService.randomBytes(LENGTH),
						HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND)),
						HashableString.from(randomService.genRandomString(LENGTH, Base64Alphabet.getInstance())));
				case NESTED -> HashableList.of(randomService.randomBytes(LENGTH),
						HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND)),
						HashableString.from(randomService.genRandomString(LENGTH, Base64Alphabet.getInstance())),
						HashableList.of(randomService.randomBytes(LENGTH),
								HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND)),
								HashableString.from(randomService.genRandomString(LENGTH, Base64Alphabet.getInstance())),
								HashableList.of(randomService.randomBytes(LENGTH),
										HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND)),
										HashableString.from(randomService.genRandomString(LENGTH, Base64Alphabet.getInstance())))));
			};
		}
	}

	public enum Mode {
		BYTE_ARRAY,
		BIG_INTEGER,
		STRING,
		LIST,
		NESTED
	}
}
