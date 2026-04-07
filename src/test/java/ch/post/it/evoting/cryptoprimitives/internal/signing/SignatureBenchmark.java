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
package ch.post.it.evoting.cryptoprimitives.internal.signing;

import static java.time.LocalDate.now;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.internal.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.RSASSA_PSS;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SignatureSupportingAlgorithm;
import ch.post.it.evoting.cryptoprimitives.math.Base32Alphabet;
import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;
import ch.post.it.evoting.cryptoprimitives.signing.KeysAndCert;

@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SignatureBenchmark {

	@Benchmark
	public ImmutableByteArray genSignature(final SignatureBenchmarkState state) throws SignatureException {
		return state.keystoreService.generateSignature(state.message, state.additionalContextData);
	}

	@Benchmark
	public boolean verifySignature(final SignatureBenchmarkState state) throws SignatureException {
		return state.keystoreService.verifySignature(() -> state.alias, state.message, state.additionalContextData, state.signature);
	}

	@State(Scope.Benchmark)
	public static class SignatureBenchmarkState {

		private static final BigInteger UPPER_BOUND = BigInteger.TWO.pow(3071);
		private static final RandomService randomService = new RandomService();

		private SignatureKeystoreService<Supplier<String>> keystoreService;

		private Hashable message;
		private Hashable additionalContextData;
		private ImmutableByteArray signature;
		private String alias;

		@Setup
		public void setup() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, SignatureException {
			final HashService hashService = HashService.getInstance();
			final SignatureSupportingAlgorithm rsa = RSASSA_PSS.getInstance();
			final AuthorityInformation authorityInformation = new AuthorityInformation.Builder()
					.setCommonName("Common Name")
					.setOrganisation("Swiss Post")
					.setLocality("Neuchâtel")
					.setState("NE")
					.setCountry("CH")
					.build();
			final GenKeysAndCertService genKeysAndCertService = new GenKeysAndCertService(authorityInformation, rsa);
			alias = "Alias";
			final char[] testPassword = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
			final KeyStore keyStore = generateNewKeyStore(genKeysAndCertService, alias, testPassword);
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			keyStore.store(outputStream, testPassword);
			final ByteArrayInputStream keytoreInputStream = new ByteArrayInputStream(outputStream.toByteArray());

			keystoreService = new SignatureKeystoreService<>(keytoreInputStream, "JKS", testPassword,
					keystore -> true, () -> alias, hashService);

			final HashableBigInteger m1 = HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND));
			final HashableString m2 = HashableString.from(randomService.genRandomString(32, Base32Alphabet.getInstance()));
			final HashableList m3 = HashableList.of(m1, m2);
			message = HashableList.of(m1, m2, m3);
			final HashableBigInteger c1 = HashableBigInteger.from(randomService.genRandomInteger(UPPER_BOUND));
			final HashableString c2 = HashableString.from(randomService.genRandomString(32, Base32Alphabet.getInstance()));
			final HashableList c3 = HashableList.of(c1, c2);
			additionalContextData = HashableList.of(c1, c2, c3);
			signature = keystoreService.generateSignature(message, additionalContextData);
		}

		private KeyStore generateNewKeyStore(final GenKeysAndCertService genKeysAndCertService, final String alias, final char[] password)
				throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
			final KeysAndCert keysAndCert = genKeysAndCertService.genKeysAndCert(now(), now().plusDays(1));

			final KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, password);
			final KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keysAndCert.privateKey(),
					new X509Certificate[] { keysAndCert.certificate() });
			keyStore.setEntry(alias, privateKeyEntry, new KeyStore.PasswordProtection(password));

			return keyStore;
		}
	}
}
