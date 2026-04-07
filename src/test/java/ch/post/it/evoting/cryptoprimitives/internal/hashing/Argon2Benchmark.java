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
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Hash;
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Profile;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class Argon2Benchmark {

	@Benchmark
	public Argon2Hash genArgon2id(final Argon2State state) {
		return state.argon2Service.genArgon2id(state.inputKeyingMaterial);
	}

	@Benchmark
	public ImmutableByteArray getArgon2id(final Argon2State state) {
		return state.argon2Service.getArgon2id(state.inputKeyingMaterial, state.salt);
	}

	@State(Scope.Benchmark)
	public static class Argon2State {
		private static final RandomService randomService = new RandomService();

		private Argon2Service argon2Service;

		@Param({ "STANDARD", "LESS_MEMORY", "TEST" })
		private Argon2Profile argon2Profile;

		@Param({ "16", "64" })
		private int inputSize;

		private ImmutableByteArray inputKeyingMaterial;
		private ImmutableByteArray salt;

		@Setup
		public void setup() {
			argon2Service = new Argon2Service(randomService, argon2Profile);
			inputKeyingMaterial = randomService.randomBytes(inputSize);
			salt = randomService.randomBytes(16);
		}
	}
}
