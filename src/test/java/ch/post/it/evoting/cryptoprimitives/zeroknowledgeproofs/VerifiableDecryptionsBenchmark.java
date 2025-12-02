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
package ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.AuxiliaryInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs.ZeroKnowledgeProofService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.utils.VerificationResult;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VerifiableDecryptionsBenchmark {

	@Benchmark
	public VerifiableDecryptions genVerifiableDecryptions_variableNumberOfCiphertext(final VerifiableDecryptionsState state) {
		return state.zeroKnowledgeProofService.genVerifiableDecryptions(state.ciphertexts, state.keyPair, state.auxiliaryInformation);
	}

	@Benchmark
	public VerificationResult verifyDecryptions_variableNumberOfCiphertext(final VerifiableDecryptionsState state) {
		return state.zeroKnowledgeProofService.verifyDecryptions(state.ciphertexts, state.keyPair.getPublicKey(), state.verifiableDecryptions,
				state.auxiliaryInformation);
	}

	@Benchmark
	public VerifiableDecryptions genVerifiableDecryptions_variableCiphertextSize(final VerifiableDecryptionsState2 state) {
		return state.zeroKnowledgeProofService.genVerifiableDecryptions(state.ciphertexts, state.keyPair, state.auxiliaryInformation);
	}

	@Benchmark
	public VerificationResult verifyDecryptions_variableCiphertextSize(final VerifiableDecryptionsState2 state) {
		return state.zeroKnowledgeProofService.verifyDecryptions(state.ciphertexts, state.keyPair.getPublicKey(), state.verifiableDecryptions,
				state.auxiliaryInformation);
	}

	@State(Scope.Benchmark)
	public static class VerifiableDecryptionsState {

		private static final RandomService randomService = new RandomService();

		private final ZeroKnowledgeProofService zeroKnowledgeProofService = new ZeroKnowledgeProofService();

		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
		private ElGamalMultiRecipientKeyPair keyPair;
		private AuxiliaryInformation auxiliaryInformation;
		private VerifiableDecryptions verifiableDecryptions;

		@Param({ "100", "484", "1024", "2500", "10000" })
		private int numCiphertexts;

		@Param({ "1" })
		private int ciphertextSize;

		@Setup(Level.Trial)
		public void setup() {
			ciphertexts = elGamalGenerator.genRandomCiphertextVector(numCiphertexts, ciphertextSize);
			keyPair = elGamalGenerator.genRandomKeyPair(ciphertextSize);
			auxiliaryInformation = AuxiliaryInformation.from(randomService.genUniqueDecimalStrings(32, 4));
			verifiableDecryptions = zeroKnowledgeProofService.genVerifiableDecryptions(ciphertexts, keyPair, auxiliaryInformation);
		}
	}

	@State(Scope.Benchmark)
	public static class VerifiableDecryptionsState2 {

		private static final RandomService randomService = new RandomService();

		private final ZeroKnowledgeProofService zeroKnowledgeProofService = new ZeroKnowledgeProofService();

		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
		private ElGamalMultiRecipientKeyPair keyPair;
		private AuxiliaryInformation auxiliaryInformation;
		private VerifiableDecryptions verifiableDecryptions;

		@Param({ "100" })
		private int numCiphertexts;

		@Param({ "2", "4", "8", "16", "32" })
		private int ciphertextSize;

		@Setup(Level.Trial)
		public void setup() {
			ciphertexts = elGamalGenerator.genRandomCiphertextVector(numCiphertexts, ciphertextSize);
			keyPair = elGamalGenerator.genRandomKeyPair(ciphertextSize);
			auxiliaryInformation = AuxiliaryInformation.from(randomService.genUniqueDecimalStrings(32, 4));
			verifiableDecryptions = zeroKnowledgeProofService.genVerifiableDecryptions(ciphertexts, keyPair, auxiliaryInformation);
		}
	}
}
