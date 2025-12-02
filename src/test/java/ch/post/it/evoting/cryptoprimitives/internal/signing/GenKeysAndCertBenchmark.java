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
package ch.post.it.evoting.cryptoprimitives.internal.signing;

import java.time.LocalDate;
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

import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.RSASSA_PSS;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SignatureSupportingAlgorithm;
import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;
import ch.post.it.evoting.cryptoprimitives.signing.KeysAndCert;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GenKeysAndCertBenchmark {

	@Benchmark
	public KeysAndCert genKeysAndCert(final GenKeysAndCertState state) {
		return state.genKeysAndCertService.genKeysAndCert(state.validFrom, state.validUntil);
	}

	@State(Scope.Benchmark)
	public static class GenKeysAndCertState {

		private GenKeysAndCertService genKeysAndCertService;

		private LocalDate validFrom;
		private LocalDate validUntil;

		@Setup
		public void setup() {
			final AuthorityInformation authorityInformation = new AuthorityInformation.Builder()
					.setCommonName("Common Name")
					.setOrganisation("Swiss Post")
					.setLocality("Neuchâtel")
					.setState("NE")
					.setCountry("CH")
					.build();
			final SignatureSupportingAlgorithm algorithm = RSASSA_PSS.getInstance();
			genKeysAndCertService = new GenKeysAndCertService(authorityInformation, algorithm);

			validFrom = LocalDate.now();
			validUntil = validFrom.plusDays(1);
		}
	}
}
