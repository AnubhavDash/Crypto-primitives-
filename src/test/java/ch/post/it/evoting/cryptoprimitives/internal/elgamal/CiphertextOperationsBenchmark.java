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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@BenchmarkMode(value = Mode.AverageTime)
@Fork(value = 1)
@Measurement(iterations = 5)
@Warmup(iterations = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CiphertextOperationsBenchmark {

	@Benchmark
	public ElGamalMultiRecipientCiphertext getCiphertextExponentiation(final CiphertextOperationsState state) {
		return state.ciphertext1.getCiphertextExponentiation(state.exponent);
	}

	@Benchmark
	public ElGamalMultiRecipientCiphertext getCiphertextProduct(final CiphertextOperationsState state) {
		return state.ciphertext1.getCiphertextProduct(state.ciphertext2);
	}

	@State(Scope.Benchmark)
	public static class CiphertextOperationsState {

		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
		public ElGamalMultiRecipientCiphertext ciphertext1;
		public ElGamalMultiRecipientCiphertext ciphertext2;
		public ZqElement exponent;

		@Param({ "1", "16", "31" })
		public int ciphertextSize;

		@Setup(Level.Trial)
		public void setup() {
			final RandomService randomService = new RandomService();

			final BigInteger q = gqGroup.getQ();
			final ZqGroup zqGroup = new ZqGroup(q);
			exponent = ZqElement.create(randomService.genRandomInteger(q), zqGroup);

			ciphertext1 = ElGamalMultiRecipientCiphertext.create(
					GqElement.GqElementFactory.fromSquareRoot(randomService.genRandomInteger(q), gqGroup),
					Stream.generate(() -> randomService.genRandomInteger(q))
							.map(v -> GqElement.GqElementFactory.fromSquareRoot(v, gqGroup))
							.limit(ciphertextSize)
							.collect(toGroupVector()));
			ciphertext2 = ElGamalMultiRecipientCiphertext.create(
					GqElement.GqElementFactory.fromSquareRoot(randomService.genRandomInteger(q), gqGroup),
					Stream.generate(() -> randomService.genRandomInteger(q))
							.map(v -> GqElement.GqElementFactory.fromSquareRoot(v, gqGroup))
							.limit(ciphertextSize)
							.collect(toGroupVector()));
		}
	}

}
