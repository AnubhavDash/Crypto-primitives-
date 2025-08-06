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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import java.math.BigInteger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(value = Mode.Throughput)
@Fork(value = 1)
@Measurement(iterations = 3)
@Warmup(iterations = 0)
public class FixedBaseBenchmark {
	@Benchmark
	public void knownBaseBeforeCache(final MyState state, final Blackhole bh) {
		final BigIntegerOperations operations = state.bigIntegerOperationsWithoutTable;

		final BigInteger result = operations.modExponentiate(state.knownBase, state.exponent, state.p);
		bh.consume(result);
	}

	@Benchmark
	public void randomBaseBeforeCache(final MyState state, final Blackhole bh) {
		final BigIntegerOperations operations = state.bigIntegerOperationsWithoutTable;

		final BigInteger result = operations.modExponentiate(state.randomBase, state.exponent, state.p);
		bh.consume(result);
	}

	@Benchmark
	public void generateCache(final MyState state) {
		final BigIntegerOperations operations = new BigIntegerOperationsVMGJ();
		operations.generateCache(state.knownBase, state.p);
	}

	@Benchmark
	public void knownBaseAfterCache(final MyState state, final Blackhole bh) {
		final BigIntegerOperations operations = state.bigIntegerOperationsWithTable;

		final BigInteger result = operations.modExponentiate(state.knownBase, state.exponent, state.p);
		bh.consume(result);
	}

	@Benchmark
	public void randomBaseAfterCache(final MyState state, final Blackhole bh) {
		final BigIntegerOperations operations = state.bigIntegerOperationsWithTable;

		final BigInteger result = operations.modExponentiate(state.randomBase, state.exponent, state.p);
		bh.consume(result);
	}

	@State(Scope.Benchmark)
	public static class MyState {

		private static final TestRandomService randomService = new TestRandomService();

		private final BigInteger p;
		private final BigInteger knownBase;
		private final BigInteger exponent;
		private final BigInteger randomBase;
		private final BigIntegerOperations bigIntegerOperationsWithoutTable = new BigIntegerOperationsVMGJ();
		private final BigIntegerOperations bigIntegerOperationsWithTable = new BigIntegerOperationsVMGJ();

		public MyState() {
			p = new BigInteger("B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF324E" +
					"7738926CFBE5F4BF8D8D8C31D763DA06C80ABB1185EB4F7C7B5757F5958490CFD47D7C" +
					"19BB42158D9554F7B46BCED55C4D79FD5F24D6613C31C3839A2DDF8A9A276BCFBFA1C8" +
					"77C56284DAB79CD4C2B3293D20E9E5EAF02AC60ACC93ED874422A52ECB238FEEE5AB6A" +
					"DD835FD1A0753D0A8F78E537D2B95BB79D8DCAEC642C1E9F23B829B5C2780BF38737DF" +
					"8BB300D01334A0D0BD8645CBFA73A6160FFE393C48CBBBCA060F0FF8EC6D31BEB5CCEE" +
					"D7F2F0BB088017163BC60DF45A0ECB1BCD289B06CBBFEA21AD08E1847F3F7378D56CED" +
					"94640D6EF0D3D37BE67008E186D1BF275B9B241DEB64749A47DFDFB96632C3EB061B64" +
					"72BBF84C26144E49C2D04C324EF10DE513D3F5114B8B5D374D93CB8879C7D52FFD72BA" +
					"0AAE7277DA7BA1B4AF1488D8E836AF14865E6C37AB6876FE690B571121382AF341AFE9" +
					"4F77BCF06C83B8FF5675F0979074AD9A787BC5B9BD4B0C5937D3EDE4C3A79396419CD7", 16);
			final BigInteger q = new BigInteger("5BF0A8B1457695355FB8AC404E7A79E3B1738B079C5A6D2B53C26C8228C867F79927" +
					"3B9C49367DF2FA5FC6C6C618EBB1ED0364055D88C2F5A7BE3DABABFACAC24867EA3EBE" +
					"0CDDA10AC6CAAA7BDA35E76AAE26BCFEAF926B309E18E1C1CD16EFC54D13B5E7DFD0E4" +
					"3BE2B1426D5BCE6A6159949E9074F2F5781563056649F6C3A21152976591C7F772D5B5" +
					"6EC1AFE8D03A9E8547BC729BE95CADDBCEC6E57632160F4F91DC14DAE13C05F9C39BEF" +
					"C5D98068099A50685EC322E5FD39D30B07FF1C9E2465DDE5030787FC763698DF5AE677" +
					"6BF9785D84400B8B1DE306FA2D07658DE6944D8365DFF510D68470C23F9FB9BC6AB676" +
					"CA3206B77869E9BDF3380470C368DF93ADCD920EF5B23A4D23EFEFDCB31961F5830DB2" +
					"395DFC26130A2724E1682619277886F289E9FA88A5C5AE9BA6C9E5C43CE3EA97FEB95D" +
					"0557393BED3DD0DA578A446C741B578A432F361BD5B43B7F3485AB88909C1579A0D7F4" +
					"A7BBDE783641DC7FAB3AF84BC83A56CD3C3DE2DCDEA5862C9BE9F6F261D3C9CB20CE6B", 16);

			exponent = randomService.genRandomIntegerOfLength(q.bitLength() + 256).mod(q);
			knownBase = BigInteger.TWO;
			final BigInteger random = randomService.genRandomIntegerOfLength(q.bitLength());
			randomBase = random.multiply(random).mod(p);

			bigIntegerOperationsWithTable.generateCache(knownBase, p);
		}
	}
}
