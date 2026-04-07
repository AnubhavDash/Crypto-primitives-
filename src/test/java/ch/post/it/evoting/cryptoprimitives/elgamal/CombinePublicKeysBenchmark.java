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
package ch.post.it.evoting.cryptoprimitives.elgamal;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientPublicKeys;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CombinePublicKeysBenchmark {

	@Benchmark
	public ElGamalMultiRecipientPublicKey combinePublicKeys(final CombinePublicKeysState state) {
		return ElGamalMultiRecipientPublicKeys.combinePublicKeys(state.publicKeysList);
	}

	@State(Scope.Benchmark)
	public static class CombinePublicKeysState {
		private static final GqGroup group = GroupTestData.getLargeGqGroup();
		private static final ElGamalGenerator generator = new ElGamalGenerator(group);

		private GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> publicKeysList;

		@Param({ "2", "5" })
		private int numberOfKeys;

		@Param({ "1", "16", "31" })
		private int keySize;

		@Setup
		public void setup() {
			publicKeysList = Stream.generate(() -> generator.genRandomPublicKey(keySize))
					.limit(numberOfKeys)
					.collect(GroupVector.toGroupVector());
		}
	}
}
