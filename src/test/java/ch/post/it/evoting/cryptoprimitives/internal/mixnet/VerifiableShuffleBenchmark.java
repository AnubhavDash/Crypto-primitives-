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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

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

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.utils.VerificationResult;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class VerifiableShuffleBenchmark {

	@Benchmark
	public VerifiableShuffle genVerifiableShuffle_variableNumberOfCiphertext(final VerifiableShuffleState state) {
		return state.mixnetService.genVerifiableShuffle(state.ciphertexts, state.publicKey);
	}

	@Benchmark
	public VerificationResult verifyShuffle_variableNumberOfCiphertext(final VerifiableShuffleState state) {
		return state.mixnetService.verifyShuffle(state.ciphertexts, state.shuffledCiphertexts, state.shuffleArgument, state.publicKey);
	}

	@Benchmark
	public VerifiableShuffle genVerifiableShuffle_variableCiphertextSize(final VerifiableShuffleState2 state) {
		return state.mixnetService.genVerifiableShuffle(state.ciphertexts, state.publicKey);
	}

	@Benchmark
	public VerificationResult verifyShuffle_variableCiphertextSize(final VerifiableShuffleState2 state) {
		return state.mixnetService.verifyShuffle(state.ciphertexts, state.shuffledCiphertexts, state.shuffleArgument, state.publicKey);
	}

	@State(Scope.Benchmark)
	public static class VerifiableShuffleState {

		private final GqGroup group = GroupTestData.getLargeGqGroup();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);

		private final MixnetService mixnetService = new MixnetService();

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
		private ElGamalMultiRecipientPublicKey publicKey;

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts;
		private ShuffleArgument shuffleArgument;

		/*
		    Number classifications:
    		100 = 10 × 10 (square)
    		101 = prime
    		102 = 6 × 17
    		484 = 22 × 22 (square)
    		487 = prime
    		488 = 8 × 61
    		1024 = 32 × 32 (square)
    		1031 = prime
    		1034 = 22 × 47
    		2500 = 50 × 50 (square)
    		2503 = prime
    		2507 = 23 × 109
    		10000 = 100 × 100 (square)
    		10007 = prime
    		10011 = 33 × 303
		 */
		@Param({ "100", "101", "102", "484", "487", "488", "1024", "1031", "1034", "2500", "2503", "2507", "10000", "10007", "10011" })
		private int numCiphertexts;

		@Param({ "1" })
		private int ciphertextSize;

		@Setup
		public void setup() {
			ciphertexts = elGamalGenerator.genRandomCiphertextVector(numCiphertexts, ciphertextSize);
			publicKey = elGamalGenerator.genRandomPublicKey(ciphertextSize);

			final VerifiableShuffle verifiableShuffle = mixnetService.genVerifiableShuffle(ciphertexts, publicKey);
			shuffledCiphertexts = verifiableShuffle.shuffledCiphertexts();
			shuffleArgument = verifiableShuffle.shuffleArgument();
		}
	}

	@State(Scope.Benchmark)
	public static class VerifiableShuffleState2 {

		private final GqGroup group = GroupTestData.getLargeGqGroup();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(group);

		private final MixnetService mixnetService = new MixnetService();

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
		private ElGamalMultiRecipientPublicKey publicKey;

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts;
		private ShuffleArgument shuffleArgument;

		@Param({ "100" })
		private int numCiphertexts;

		@Param({ "2", "4", "8", "16", "32" })
		private int ciphertextSize;

		@Setup
		public void setup() {
			ciphertexts = elGamalGenerator.genRandomCiphertextVector(numCiphertexts, ciphertextSize);
			publicKey = elGamalGenerator.genRandomPublicKey(ciphertextSize);

			final VerifiableShuffle verifiableShuffle = mixnetService.genVerifiableShuffle(ciphertexts, publicKey);
			shuffledCiphertexts = verifiableShuffle.shuffledCiphertexts();
			shuffleArgument = verifiableShuffle.shuffleArgument();
		}
	}
}
