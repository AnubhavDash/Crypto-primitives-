/*
 * Copyright 2023 Post CH Ltd
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

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

@BenchmarkMode(value = Mode.Throughput)
@Fork(value = 1)
@Measurement(iterations = 3)
@Warmup(iterations = 0)
public class CiphertextExponentiationBenchmark {
	@Benchmark
	public void ciphertextMultimodExp(CiphertextMultimodExpState state, Blackhole bh) {
		bh.consume(ElGamalMultiRecipientCiphertexts.getCiphertextVectorExponentiation(state.ciphertexts, state.exponents));
	}

	@State(Scope.Benchmark)
	public static class CiphertextMultimodExpState {
		private final BigInteger p = new BigInteger(1,
				HexFormat.of().parseHex("B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF324E" +
						"7738926CFBE5F4BF8D8D8C31D763DA06C80ABB1185EB4F7C7B5757F5958490CFD47D7C" +
						"19BB42158D9554F7B46BCED55C4D79FD5F24D6613C31C3839A2DDF8A9A276BCFBFA1C8" +
						"77C56284DAB79CD4C2B3293D20E9E5EAF02AC60ACC93ED874422A52ECB238FEEE5AB6A" +
						"DD835FD1A0753D0A8F78E537D2B95BB79D8DCAEC642C1E9F23B829B5C2780BF38737DF" +
						"8BB300D01334A0D0BD8645CBFA73A6160FFE393C48CBBBCA060F0FF8EC6D31BEB5CCEE" +
						"D7F2F0BB088017163BC60DF45A0ECB1BCD289B06CBBFEA21AD08E1847F3F7378D56CED" +
						"94640D6EF0D3D37BE67008E186D1BF275B9B241DEB64749A47DFDFB96632C3EB061B64" +
						"72BBF84C26144E49C2D04C324EF10DE513D3F5114B8B5D374D93CB8879C7D52FFD72BA" +
						"0AAE7277DA7BA1B4AF1488D8E836AF14865E6C37AB6876FE690B571121382AF341AFE9" +
						"4F77BCF06C83B8FF5675F0979074AD9A787BC5B9BD4B0C5937D3EDE4C3A79396419CD7"));
		private final BigInteger q = p.shiftRight(1);
		private final BigInteger g = BigInteger.TWO;
		private final GqGroup group = new GqGroup(p, q, g);
		public GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
		public GroupVector<ZqElement, ZqGroup> exponents;

		@Param({ "5", "10" })
		public int ciphertextCount;

		@Param({ "1", "5" })
		public int ciphertextSize;

		@Setup(Level.Trial)
		public void setup() {
			Random random = RandomFactory.createRandom();

			final ZqGroup zqGroup = new ZqGroup(q);
			exponents = Stream.generate(() -> random.genRandomInteger(q))
					.map(v -> ZqElement.create(v, zqGroup))
					.limit(ciphertextCount)
					.collect(GroupVector.toGroupVector());

			ciphertexts = Stream.generate(() -> ElGamalMultiRecipientCiphertext.create(
							GqElement.GqElementFactory.fromSquareRoot(random.genRandomInteger(q), group),
							Stream.generate(() -> random.genRandomInteger(q))
									.map(v -> GqElement.GqElementFactory.fromSquareRoot(v, group))
									.limit(ciphertextSize)
									.toList()))
					.limit(ciphertextCount)
					.collect(GroupVector.toGroupVector());
		}
	}

}
