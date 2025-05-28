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

import static ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts.getCiphertext;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientMessages;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleStatement;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleWitness;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;

/**
 * This benchmark compares the performance of <i>getShuffleArgument</i> for two cases:
 * <ul>
 *     <li><i>(m, n) = getMatrixDimensions(N)</i></li>
 *     <li><i>(m, n) = (1, N)</i></li>
 * </ul>
 * The values for the total number of ciphertexts (N) and the ciphertext size (l) are parameterized and can be easily changed according to the needs.
 */
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MINUTES)
public class GetShuffleArgumentBenchmark {

	@Fork(value = 1, jvmArgs = {"-Xms5g", "-Xmx30g"})
	@Benchmark
	public ShuffleArgument getShuffleArgumentFor_m_And_n_Chosen_With_getMatrixDimensions(final BenchmarkState_m_And_n_Chosen_With_getMatrixDimensions state) {
		return state.getShuffleArgument();
	}

	@Fork(value = 1, jvmArgs = {"-Xms5g", "-Xmx30g"})
	@Benchmark
	public ShuffleArgument getShuffleArgumentFor_m_Equals_1(final BenchmarkState_m_Equals_1 state) {
		return state.getShuffleArgument();
	}

	@SuppressWarnings("java:S116")
	@State(Scope.Thread)
	public static class BenchmarkState_m_And_n_Chosen_With_getMatrixDimensions extends BenchmarkState {

		@Param({ "900", "901", "902", "903", "904", "905", "906", "907", "908", "909", "4900", "10000" })
		int N;
		@Param({ "1", "31" })
		int l;
		private int m;
		private int n;

		@Setup(Level.Trial)
		public void setup() {
			final int[] dimensions = MatrixUtils.getMatrixDimensions(N);
			m = dimensions[0];
			n = dimensions[1];
			super.setup(n, N, l);
		}

		public ShuffleArgument getShuffleArgument() {
			return this.getShuffleArgument(m, n);
		}
	}

	@SuppressWarnings("java:S116")
	@State(Scope.Thread)
	public static class BenchmarkState_m_Equals_1 extends BenchmarkState {

		@Param({ "900", "901", "902", "903", "904", "905", "906", "907", "908", "909", "4900", "10000" })
		int N;
		@Param({ "1", "31"})
		int l;

		@Setup(Level.Trial)
		public void setup() {
			super.setup(N, N, l);
		}

		public ShuffleArgument getShuffleArgument() {
			return this.getShuffleArgument(1, N);
		}
	}

	public static class BenchmarkState {

		private ShuffleArgumentService shuffleArgumentService;
		private ShuffleStatement shuffleStatement;
		private ShuffleWitness shuffleWitness;

		public void setup(final int n, final int N, final int l) {
			final TestRandomService randomService = new TestRandomService();
			final PermutationService permutationService = new PermutationService(randomService);
			final Permutation permutation = permutationService.genPermutation(N);
			final GqGroup largeGqGroup = GroupTestData.getLargeGqGroup();
			final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(largeGqGroup));
			final GroupVector<ZqElement, ZqGroup> randomness = zqGroupGenerator.genRandomZqElementVector(N);

			shuffleWitness = new ShuffleWitness(permutation, randomness);

			final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(largeGqGroup);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = elGamalGenerator.genRandomCiphertextVector(N, l);
			final ElGamalMultiRecipientPublicKey publicKey = elGamalGenerator.genRandomPublicKey(l);
			final ElGamalMultiRecipientMessage ones = ElGamalMultiRecipientMessages.ones(largeGqGroup, l);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts = IntStream.range(0, N)
					.mapToObj(i -> getCiphertext(ones, randomness.get(i), publicKey)
							.getCiphertextProduct(ciphertexts.get(permutation.get(i))))
					.collect(toGroupVector());

			final CommitmentKey commitmentKey = new TestCommitmentKeyGenerator(largeGqGroup).genCommitmentKey(n);
			final HashService hashService = TestHashService.create(largeGqGroup.getQ());
			shuffleStatement = new ShuffleStatement(ciphertexts, shuffledCiphertexts);
			this.shuffleArgumentService = new ShuffleArgumentService(publicKey, commitmentKey, randomService, hashService);
		}

		public ShuffleArgument getShuffleArgument(final int m, final int n) {
			return shuffleArgumentService.getShuffleArgument(shuffleStatement, shuffleWitness, m, n);
		}
	}
}
