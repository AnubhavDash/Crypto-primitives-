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

import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientMessages;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ElGamalBenchmark {

	@Benchmark
	public ElGamalMultiRecipientCiphertext getCiphertext(final ElGamalState state) {
		return ElGamalMultiRecipientCiphertexts.getCiphertext(state.message, state.exponent, state.publicKey);
	}

	@Benchmark
	public ElGamalMultiRecipientMessage getMessage(final ElGamalState state) {
		return ElGamalMultiRecipientMessages.getMessage(state.ciphertext, state.secretKey);
	}

	@Benchmark
	public ElGamalMultiRecipientCiphertext getPartialDecryption(final ElGamalState state) {
		return ElGamalMultiRecipientCiphertexts.getPartialDecryption(state.ciphertext, state.secretKey);
	}

	@State(Scope.Benchmark)
	public static class ElGamalState {

		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();

		@Param({ "1", "16", "31" })
		private int numElements;

		private ElGamalMultiRecipientMessage message;
		private ElGamalMultiRecipientCiphertext ciphertext;
		private ZqElement exponent;
		private ElGamalMultiRecipientPublicKey publicKey;
		private ElGamalMultiRecipientPrivateKey secretKey;

		@Setup
		public void setup() {
			final RandomService randomService = new RandomService();
			message = new ElGamalGenerator(gqGroup).genRandomMessage(numElements);
			exponent = ZqElement.create(randomService.genRandomInteger(gqGroup.getQ()), ZqGroup.sameOrderAs(gqGroup));
			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, numElements, randomService);
			publicKey = keyPair.getPublicKey();

			ciphertext = ElGamalMultiRecipientCiphertexts.getCiphertext(message, exponent, publicKey);
			secretKey = keyPair.getPrivateKey();
		}
	}
}
