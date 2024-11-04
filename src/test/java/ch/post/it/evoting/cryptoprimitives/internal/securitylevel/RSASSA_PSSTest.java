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
package ch.post.it.evoting.cryptoprimitives.internal.securitylevel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.internal.signing.CertificateInfo;
import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;

@DisplayName("RSASSA_PSS")
class RSASSA_PSSTest {

	private static final RSASSA_PSS rsassa_pss = RSASSA_PSS.getInstance();
	private static final TestRandomService randomService = new TestRandomService();

	@Test
	@DisplayName("calling genKeyPair returns key pair")
	void genKeyPairDoesNotThrow() {
		final KeyPair keyPair = assertDoesNotThrow(rsassa_pss::genKeyPair);

		assertEquals("RSASSA-PSS", keyPair.getPublic().getAlgorithm());
		assertEquals("RSASSA-PSS", keyPair.getPrivate().getAlgorithm());
	}

	@Nested
	@DisplayName("calling getCertificate")
	class GetCertificateTest {

		private KeyPair keyPair;
		private CertificateInfo certificateInfo;

		@BeforeEach
		void setup() {
			keyPair = rsassa_pss.genKeyPair();

			final LocalDate from = LocalDate.of(2000, 1, 1);
			final LocalDate until = LocalDate.of(2035, 1, 1);

			final AuthorityInformation authorityInformation = AuthorityInformation.builder()
					.setCommonName("")
					.setCountry("")
					.setLocality("")
					.setState("")
					.setOrganisation("")
					.build();
			certificateInfo = new CertificateInfo(authorityInformation);
			certificateInfo.setValidFrom(from);
			certificateInfo.setValidUntil(until);
			certificateInfo.setUsage(new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature));
		}

		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void getCertificateWithNullArgumentsThrows() {
			assertThrows(NullPointerException.class, () -> rsassa_pss.getCertificate(null, certificateInfo));
			assertThrows(NullPointerException.class, () -> rsassa_pss.getCertificate(keyPair, null));
		}

		@Test
		@DisplayName("with valid arguments returns expected certificate")
		void getCertificateWithValidInputDoesNotThrow() {
			final X509Certificate certificate = assertDoesNotThrow(() -> rsassa_pss.getCertificate(keyPair, certificateInfo));

			final long certificateNotBeforeEpochDay = (certificate.getNotBefore().toInstant().getEpochSecond() + 86399) / 86400;
			final long certificateNotAfterEpochDay = (certificate.getNotAfter().toInstant().getEpochSecond() + 86399) / 86400;
			assertEquals(certificateInfo.getValidFrom().toEpochDay(), certificateNotBeforeEpochDay);
			assertEquals(certificateInfo.getValidUntil().toEpochDay(), certificateNotAfterEpochDay);
			assertEquals(keyPair.getPublic(), certificate.getPublicKey());
			assertEquals(0, certificate.getBasicConstraints());
		}
	}

	@Nested
	@DisplayName("calling sign")
	class SignTest {

		private PrivateKey privateKey;
		private ImmutableByteArray message;

		@BeforeEach
		void setup() {
			privateKey = rsassa_pss.genKeyPair().getPrivate();
			message = randomService.randomBytes(10);
		}

		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void signWithNullArgumentsThrows() {
			assertThrows(NullPointerException.class, () -> rsassa_pss.sign(null, message));
			assertThrows(NullPointerException.class, () -> rsassa_pss.sign(privateKey, null));
		}

		@Test
		@DisplayName("with valid arguments signs message")
		void signWithValidArgumentsDoesNotThrow() {
			final ImmutableByteArray signature = assertDoesNotThrow(() -> rsassa_pss.sign(privateKey, message));

			assertEquals(384, signature.length());
		}
	}

	@Nested
	@DisplayName("calling verify")
	class VerifyTest {

		private PublicKey publicKey;
		private ImmutableByteArray message;
		private ImmutableByteArray signature;

		@BeforeEach
		void setup() {
			publicKey = rsassa_pss.genKeyPair().getPublic();
			message = randomService.randomBytes(10);
			signature = randomService.randomBytes(384);
		}

		@Test
		@DisplayName("with null arguments throws a NullPointerException")
		void verifyWithNullArgumentsThrows() {
			assertThrows(NullPointerException.class, () -> rsassa_pss.verify(null, message, signature));
			assertThrows(NullPointerException.class, () -> rsassa_pss.verify(publicKey, null, signature));
			assertThrows(NullPointerException.class, () -> rsassa_pss.verify(publicKey, message, null));
		}

		@Test
		@DisplayName("with signature bytes of incorrect size throws an IllegalArgumentException")
		void verifyWithSignatureBytesIncorrectSizeThrows() {
			final ImmutableByteArray tooShortSignature = randomService.randomBytes(383);
			final IllegalArgumentException exceptionTooShortSignature = assertThrows(IllegalArgumentException.class,
					() -> rsassa_pss.verify(publicKey, message, tooShortSignature));
			assertEquals("The signature must have the expected size. [found: 383, expected: 384]", exceptionTooShortSignature.getMessage());

			final ImmutableByteArray tooLongSignature = randomService.randomBytes(385);
			final IllegalArgumentException exceptionTooLongSignature = assertThrows(IllegalArgumentException.class,
					() -> rsassa_pss.verify(publicKey, message, tooLongSignature));
			assertEquals("The signature must have the expected size. [found: 385, expected: 384]", exceptionTooLongSignature.getMessage());
		}

		@Test
		@DisplayName("with incorrect signature does not verify")
		void verifyWithIncorrectSignatureReturnsFalse() {
			assertFalse(rsassa_pss.verify(publicKey, message, signature));
		}

		@Test
		@DisplayName("with correct signature verifies")
		void verifyWithCorrectSignatureReturnsTrue() {
			final KeyPair keyPair = rsassa_pss.genKeyPair();
			final ImmutableByteArray message = randomService.randomBytes(10);
			final ImmutableByteArray signature = rsassa_pss.sign(keyPair.getPrivate(), message);

			assertTrue(rsassa_pss.verify(keyPair.getPublic(), message, signature));
		}
	}
}
