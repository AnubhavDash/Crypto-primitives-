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
package ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.collection.AuxiliaryInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs.ZeroKnowledgeProofService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PlaintextEqualityProofBenchmark {

	@Benchmark
	public PlaintextEqualityProof genPlaintextEqualityProof(final PlaintextEqualityProofState state) {
		return state.zeroKnowledgeProofService.genPlaintextEqualityProof(state.firstCiphertext, state.secondCiphertext, state.firstPublicKey,
				state.secondPublicKey, state.randomness, state.auxiliaryInformation);
	}

	@Benchmark
	public boolean verifySchnorrProof(final PlaintextEqualityProofState state) {
		return state.zeroKnowledgeProofService.verifyPlaintextEquality(state.firstCiphertext, state.secondCiphertext, state.firstPublicKey,
				state.secondPublicKey, state.proof, state.auxiliaryInformation);
	}

	@State(Scope.Benchmark)
	public static class PlaintextEqualityProofState {

		private static final int SIZE = 1;
		private static final RandomService randomService = new RandomService();

		private final ZeroKnowledgeProofService zeroKnowledgeProofService = new ZeroKnowledgeProofService();
		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);


		private ElGamalMultiRecipientCiphertext firstCiphertext;
		private ElGamalMultiRecipientCiphertext secondCiphertext;
		private GqElement firstPublicKey;
		private GqElement secondPublicKey;
		private GroupVector<ZqElement, ZqGroup> randomness;
		private PlaintextEqualityProof proof;
		private AuxiliaryInformation auxiliaryInformation;

		@Setup(Level.Trial)
		public void setup() {
			final ElGamalMultiRecipientMessage message = elGamalGenerator.genRandomMessage(SIZE);
			final ElGamalMultiRecipientPublicKey publicKey1 = elGamalGenerator.genRandomPublicKey(SIZE);
			final ElGamalMultiRecipientPublicKey publicKey2 = elGamalGenerator.genRandomPublicKey(SIZE);

			firstPublicKey = publicKey1.getKeyElements().getFirst();
			secondPublicKey = publicKey2.getKeyElements().getFirst();
			randomness = randomService.genRandomVector(gqGroup.getQ(), 2);

			firstCiphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(message, randomness.getFirst(), publicKey1);
			secondCiphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(message, randomness.getLast(), publicKey2);

			auxiliaryInformation = AuxiliaryInformation.from(randomService.genUniqueDecimalStrings(32, 4));
			proof = zeroKnowledgeProofService.genPlaintextEqualityProof(firstCiphertext, secondCiphertext, firstPublicKey, secondPublicKey, randomness, auxiliaryInformation);
		}
	}

}
