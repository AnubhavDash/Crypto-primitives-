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
package ch.post.it.evoting.cryptoprimitives.internal.symmetric;

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
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricCiphertext;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SymmetricAuthenticatedEncryptionBenchmark {

	@Benchmark
	public SymmetricCiphertext genCiphertextSymmetric(final SymmetricCiphertextState state) {
		return state.symmetricService.genCiphertextSymmetric(state.encryptionKey, state.plaintext, state.associatedData);
	}

	@Benchmark
	public ImmutableByteArray getPlaintextSymmetric(final SymmetricCiphertextState state) {
		return state.symmetricService.getPlaintextSymmetric(state.encryptionKey, state.ciphertext, state.nonce, state.associatedData);
	}

	@State(Scope.Benchmark)
	public static class SymmetricCiphertextState {
		private static final RandomService randomService = new RandomService();

		private final SymmetricService symmetricService = new SymmetricService();

		@Param({ "128", "1024" })
		private int plaintextSize;

		private ImmutableByteArray encryptionKey;
		private ImmutableByteArray plaintext;
		private ImmutableByteArray ciphertext;
		private ImmutableByteArray nonce;
		private ImmutableList<String> associatedData;

		@Setup
		public void setup() {
			associatedData = randomService.genUniqueDecimalStrings(32, 4);
			encryptionKey = randomService.randomBytes(32);
			plaintext = randomService.randomBytes(plaintextSize);
			final SymmetricCiphertext symmetricCiphertext = symmetricService.genCiphertextSymmetric(encryptionKey, plaintext, associatedData);
			ciphertext = symmetricCiphertext.ciphertext();
			nonce = symmetricCiphertext.nonce();
		}
	}
}
