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
package com.verificatum.vmgj;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VMGBenchmark {

	@Benchmark
	public void powm_bytes(final MyState state, final Blackhole bh) {
		VMG.powm(state.aBytes, state.bBytes, state.modulusBytes);
	}

	@Benchmark
	public void powm_BigInteger(final MyState state, final Blackhole bh) {
		VMG.powm(state.a, state.b, state.modulus);
	}

	@State(Scope.Benchmark)
	public static class MyState {

		private static Random random = new RandomService();
		private static GqGroup group = GroupTestData.getLargeGqGroup();

		BigInteger a;
		byte[] aBytes;
		BigInteger b;
		byte[] bBytes;
		BigInteger modulus = group.getP();
		byte[] modulusBytes = modulus.toByteArray();

		@Setup
		public void setup() {
			a = random.genRandomInteger(group.getQ());
			aBytes = a.toByteArray();
			b = random.genRandomInteger(group.getQ());
			bBytes = b.toByteArray();
		}
	}
}
