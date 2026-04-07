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
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.zeroknowledgeproofs.ZeroKnowledgeProofService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SchnorrProofBenchmark {

	@Benchmark
	public SchnorrProof genSchnorrProof(final SchnorrProofState state) {
		return state.zeroKnowledgeProofService.genSchnorrProof(state.witness, state.statement, state.auxiliaryInformation);
	}

	@Benchmark
	public boolean verifySchnorrProof(final SchnorrProofState state) {
		return state.zeroKnowledgeProofService.verifySchnorrProof(state.proof, state.statement, state.auxiliaryInformation);
	}

	@State(Scope.Benchmark)
	public static class SchnorrProofState {

		private static final RandomService randomService = new RandomService();

		private final ZeroKnowledgeProofService zeroKnowledgeProofService = new ZeroKnowledgeProofService();
		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();

		private ZqElement witness;
		private GqElement statement;
		private SchnorrProof proof;
		private AuxiliaryInformation auxiliaryInformation;

		@Setup(Level.Trial)
		public void setup() {
			witness = ZqElement.create(randomService.genRandomInteger(gqGroup.getQ()), ZqGroup.sameOrderAs(gqGroup));
			statement = gqGroup.getGenerator().exponentiate(witness);
			auxiliaryInformation = AuxiliaryInformation.from(randomService.genUniqueDecimalStrings(32, 4));
			proof = zeroKnowledgeProofService.genSchnorrProof(witness, statement, auxiliaryInformation);
		}
	}

}
