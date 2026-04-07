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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import java.math.BigInteger;
import java.util.Arrays;
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

import com.verificatum.vmgj.VMG;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MultiModExpComparisonBenchmark {

	@Benchmark
	public BigInteger modExponentiateMultiply(final MyState state) {
		return IntStream.range(0, state.basesList.size())
				.mapToObj(i -> state.bigIntegerOperations.modExponentiate(state.basesList.get(i), state.exponentsList.get(i), state.modulus))
				.reduce(BigInteger.ONE, (a, b) -> state.bigIntegerOperations.modMultiply(a, b, state.modulus));
	}

	@Benchmark
	public BigInteger multiModExp(final MyState state) {
		return state.bigIntegerOperations.multiModExp(state.basesList, state.exponentsList, state.modulus);
	}

	@Benchmark
	public BigInteger spowm_BigInteger(final MyState state) {
		return VMG.spowm(state.bases, state.exponents, state.modulus);
	}

	@State(Scope.Benchmark)
	public static class MyState {

		@Param({ "1", "2", "4", "8", "16", "32", "33", "34", "63", "64", "65", "100", "10000" })
		private int size;
		RandomService random = new RandomService();
		BigIntegerOperations bigIntegerOperations = new BigIntegerOperationsVMGJ();
		GqGroup group = GroupTestData.getLargeGqGroup();

		BigInteger modulus = group.getP();
		byte[] modulusBytes = modulus.toByteArray();

		BigInteger[] bases;
		BigInteger[] exponents;
		byte[][] basesBytes;
		byte[][] exponentsBytes;
		ImmutableList<BigInteger> basesList;
		ImmutableList<BigInteger> exponentsList;

		@Setup(Level.Trial)
		public void setup() {
			bases = random.genRandomVector(group.getQ(), size).stream().map(ZqElement::getValue).toArray(BigInteger[]::new);
			exponents = random.genRandomVector(group.getQ(), size).stream().map(ZqElement::getValue).toArray(BigInteger[]::new);

			basesBytes = Arrays.stream(bases).map(BigInteger::toByteArray).toArray(byte[][]::new);
			exponentsBytes = Arrays.stream(exponents).map(BigInteger::toByteArray).toArray(byte[][]::new);

			basesList = ImmutableList.of(bases);
			exponentsList = ImmutableList.of(exponents);

		}
	}
}
