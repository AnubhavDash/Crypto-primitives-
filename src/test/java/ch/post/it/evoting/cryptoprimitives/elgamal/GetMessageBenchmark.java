/*
 * Copyright 2024 Swiss Post Ltd
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

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientMessages;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class GetMessageBenchmark {

	// In order to run this benchmark, the following environment variables must be set:
	// SECURITY_LEVEL=TESTING_ONLY

	@Benchmark
	public void getMessage(final MyState state, final Blackhole bh) {
		final ElGamalMultiRecipientMessage message = ElGamalMultiRecipientMessages.getMessage(state.ciphertext, state.keyPair.getPrivateKey());

		bh.consume(message);
	}

	@Benchmark
	public void getMessageWithExtractedReciprocal(final MyState state, final Blackhole bh) {
		final ElGamalMultiRecipientMessage message = state.getMessageWithExtractedReciprocal(state.ciphertext, state.keyPair.getPrivateKey());

		bh.consume(message);
	}

	@State(Scope.Benchmark)
	public static class MyState {

		private static final boolean ENABLE_PARALLEL_STREAMS = Boolean.parseBoolean(
				System.getProperty("enable.parallel.streams", Boolean.TRUE.toString()));
		private final int numElements = 30;
		private final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
		final ElGamalMultiRecipientCiphertext ciphertext = new ElGamalGenerator(gqGroup).genRandomCiphertext(numElements);
		private final RandomService randomService = new RandomService();
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, numElements, randomService);

		public ElGamalMultiRecipientMessage getMessageWithExtractedReciprocal(final ElGamalMultiRecipientCiphertext ciphertext,
				final ElGamalMultiRecipientPrivateKey secretKey) {
			checkNotNull(ciphertext);
			checkNotNull(secretKey);
			checkArgument(ciphertext.getGroup().hasSameOrderAs(secretKey.getGroup()), "Ciphertext and secret key must be of the same order");
			checkArgument(0 < ciphertext.size(), "A ciphertext must not be empty");
			checkArgument(ciphertext.size() <= secretKey.size(), "There cannot be more message elements than private key elements.");

			final ElGamalMultiRecipientCiphertext c = ciphertext;
			final ElGamalMultiRecipientPrivateKey sk = secretKey;

			final int l = c.size();
			final GqElement gamma = c.getGamma();

			IntStream indices = IntStream.range(0, l);
			if (MyState.ENABLE_PARALLEL_STREAMS) {
				indices = indices.parallel();
			}

			// Algorithm.
			final GqElement gamma_reciprocal = gamma.invert();
			final GroupVector<GqElement, GqGroup> messageElements = indices
					.mapToObj(i -> c.get(i).multiply(gamma_reciprocal.exponentiate(sk.get(i))))
					.collect(toGroupVector());

			return new ElGamalMultiRecipientMessage(messageElements);
		}
	}
}
