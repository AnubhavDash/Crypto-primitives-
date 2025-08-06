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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.Base64Alphabet;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class EncryptionParametersBenchmark {

	private static final List<Integer> SMALL_PRIMES = PrimesInternal.getSmallPrimes();
	public static final EncryptionParameters encryptionParameters = new EncryptionParameters();
	static final TestRandomService randomService = new TestRandomService();

	@Benchmark
	@Warmup(iterations = 0)
	@Fork(value = 10)
	@Measurement(iterations = 10)
	@BenchmarkMode(Mode.AverageTime)
	public GqGroup benchGetEncryptionParameters() {
		final String seed = randomService.genRandomString(10, Base64Alphabet.getInstance());
		return encryptionParameters.getEncryptionParameters(seed, SMALL_PRIMES);
	}
}
