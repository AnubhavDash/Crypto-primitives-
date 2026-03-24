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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.verificatum.vmgj.VMG;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;
import ch.post.it.evoting.cryptoprimitives.math.BigIntegersOptimizations;
import ch.post.it.evoting.cryptoprimitives.math.BigIntegersOptimizationsCacheKey;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

/**
 * This class tests all classes that implement the {@link BigIntegerOperations} interface.
 * <p>
 * The classes to be tested are provided by {@link BigIntegerOperationsTest#bigIntegerServiceProvider()}.
 */
@ParameterizedClass
@MethodSource("bigIntegerServiceProvider")
class BigIntegerOperationsTest {

	protected static final TestRandomService randomService = new TestRandomService();

	private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);
	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = BigInteger.valueOf(2);
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FOUR = BigInteger.valueOf(4);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger SIX = BigInteger.valueOf(6);
	private static final BigInteger SEVEN = BigInteger.valueOf(7);
	private static final BigInteger EIGHT = BigInteger.valueOf(8L);
	private static final BigInteger NINE = BigInteger.valueOf(9);
	private static final BigInteger TEN = BigInteger.valueOf(10);
	private static final BigInteger ELEVEN = BigInteger.valueOf(11);
	private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

	@Parameter
	protected static BigIntegerOperations operations;

	static Stream<Arguments> bigIntegerServiceProvider() {
		if (VMG.checkLoaded()) {
			return Stream.of(
					Arguments.of(new BigIntegerOperationsJava()),
					Arguments.of(new BigIntegerOperationsVMGJ())
			);
		} else {
			return Stream.of(
					Arguments.of(new BigIntegerOperationsJava())
			);
		}

	}

	static boolean isFixedBaseExponentiationSupported() {
		return operations.isFixedBaseExponentiationSupported();
	}

	private static BigInteger p;
	private static BigInteger knownBase;
	private static BigInteger exponent;

	@BeforeAll
	static void prepare() {
		p = new BigInteger(1,
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
		final BigInteger q = new BigInteger(1,
				HexFormat.of().parseHex("5BF0A8B1457695355FB8AC404E7A79E3B1738B079C5A6D2B53C26C8228C867F79927" +
						"3B9C49367DF2FA5FC6C6C618EBB1ED0364055D88C2F5A7BE3DABABFACAC24867EA3EBE" +
						"0CDDA10AC6CAAA7BDA35E76AAE26BCFEAF926B309E18E1C1CD16EFC54D13B5E7DFD0E4" +
						"3BE2B1426D5BCE6A6159949E9074F2F5781563056649F6C3A21152976591C7F772D5B5" +
						"6EC1AFE8D03A9E8547BC729BE95CADDBCEC6E57632160F4F91DC14DAE13C05F9C39BEF" +
						"C5D98068099A50685EC322E5FD39D30B07FF1C9E2465DDE5030787FC763698DF5AE677" +
						"6BF9785D84400B8B1DE306FA2D07658DE6944D8365DFF510D68470C23F9FB9BC6AB676" +
						"CA3206B77869E9BDF3380470C368DF93ADCD920EF5B23A4D23EFEFDCB31961F5830DB2" +
						"395DFC26130A2724E1682619277886F289E9FA88A5C5AE9BA6C9E5C43CE3EA97FEB95D" +
						"0557393BED3DD0DA578A446C741B578A432F361BD5B43B7F3485AB88909C1579A0D7F4" +
						"A7BBDE783641DC7FAB3AF84BC83A56CD3C3DE2DCDEA5862C9BE9F6F261D3C9CB20CE6B"));

		exponent = randomService.genRandomIntegerOfLength(q.bitLength() + 256).mod(q);
		knownBase = BigInteger.TWO;
	}

	@Test
	@EnabledIf("isFixedBaseExponentiationSupported")
	void consistencyCheck() {
		final BigInteger resultBeforeCache = operations.modExponentiate(knownBase, exponent, p);

		if (operations.isFixedBaseExponentiationSupported()) {
			operations.generateCache(knownBase, p, BigIntegersOptimizations.BlockWidth.STANDARD);
		}

		final BigInteger resultAfterCache = operations.modExponentiate(knownBase, exponent, p);

		assertEquals(resultBeforeCache, resultAfterCache);
	}

	/**
	 * Tests the generateCache and deriveCacheKey methods.
	 * <p>
	 * The tests run for generateCache are:
	 * <ul>
	 *     <li>null input throws a {@link NullPointerException}</li>
	 *     <li>m < 2 throws an {@link IllegalArgumentException}</li>
	 * </ul>
	 * The tests run for deriveCacheKey are:
	 * <ul>
	 *     <li>null input throws a {@link NullPointerException}</li>
	 *     <li>m < 2 throws an {@link IllegalArgumentException}</li>
	 *     <li>deriveCacheKey(b - m, m) deriveCacheKey(b, m) = deriveCacheKey(b + m, m)</li>
	 *     <li>same base but different modulus returns different {@link BigIntegersOptimizationsCacheKey}</li>
	 * </ul>
	 */
	@Nested
	@EnabledIf("isBigIntegerOperationsVMGJ")
	class GenerateCacheTest {

		static boolean isBigIntegerOperationsVMGJ() {
			return operations instanceof BigIntegerOperationsVMGJ;
		}

		@Test
		void testGenerateCache_nullInputThrows() {
			assertThrows(NullPointerException.class, () -> operations.generateCache(null, p, BigIntegersOptimizations.BlockWidth.STANDARD));
			assertThrows(NullPointerException.class, () -> operations.generateCache(knownBase, null, BigIntegersOptimizations.BlockWidth.STANDARD));
		}

		@Test
		void testGenerateCache_invalidModulusThrows() {
			assertThrows(IllegalArgumentException.class, () -> operations.generateCache(knownBase, ONE, BigIntegersOptimizations.BlockWidth.STANDARD));
		}

		@Test
		void testDeriveCacheKey_nullInputThrows() {
			assertThrows(NullPointerException.class, () -> BigIntegerOperationsVMGJ.deriveCacheKey(null, p));
			assertThrows(NullPointerException.class, () -> BigIntegerOperationsVMGJ.deriveCacheKey(knownBase, null));
		}

		@Test
		void testDeriveCacheKey_invalidModulusThrows() {
			assertThrows(IllegalArgumentException.class, () -> BigIntegerOperationsVMGJ.deriveCacheKey(knownBase, ONE));
		}

		@Test
		void testDeriveCacheKey_equivalentBasesGiveSameKey() {
			final BigIntegersOptimizationsCacheKey expected = BigIntegerOperationsVMGJ.deriveCacheKey(knownBase, p);
			final BigIntegersOptimizationsCacheKey resultAddP = BigIntegerOperationsVMGJ.deriveCacheKey(knownBase.add(p), p);
			final BigIntegersOptimizationsCacheKey resultSubtractP = BigIntegerOperationsVMGJ.deriveCacheKey(knownBase.subtract(p), p);
			assertEquals(expected, resultAddP);
			assertEquals(expected, resultSubtractP);
		}

		@Test
		void testDeriveCacheKey_differentModulusGiveDifferentKey() {
			final BigIntegersOptimizationsCacheKey result1 = BigIntegerOperationsVMGJ.deriveCacheKey(knownBase, p);
			final BigIntegersOptimizationsCacheKey result2 = BigIntegerOperationsVMGJ.deriveCacheKey(knownBase, p.add(TWO));
			assertNotEquals(result1, result2);
		}
	}

	/**
	 * Tests the modMultiply method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>specific values give the expected output</li>
	 *     <li>null arguments throw a {@link NullPointerException}</li>
	 *     <li>modulus <= 1 throws an {@link IllegalArgumentException}</li>
	 *     <li>multiplication with the identity element: 1 * a = a * 1 = a</li>
	 *     <li>multiplication with zero: 0 * a = a * 0 = 0</li>
	 *     <li>commutativity: a * b = b * a</li>
	 *     <li>associativity: (a * b) * c = a * (b * c)</li>
	 *     <li>multiplication with negated elements: (-a) * b = a * (-b) = - (a * b) mod p and (-a) * (-b) = a * b mod p</li>
	 * </ul>
	 */
	@Nested
	class ModMultiplyTest {

		private BigInteger modulus;

		@BeforeEach
		void setUp() {
			modulus = randomService.genRandomInteger(MAX_LONG).multiply(TWO).add(THREE); // Make sure that modulus is odd
		}

		static Stream<Arguments> testModMultiplySpecificValues() {
			return Stream.of(
					Arguments.of(THREE, FOUR, FIVE, TWO),
					Arguments.of(FOUR, THREE, FIVE, TWO),
					Arguments.of(THREE, FOUR, SIX, ZERO),
					Arguments.of(THREE, FOUR, SEVEN, FIVE),
					Arguments.of(THREE.negate(), FOUR.negate(), SEVEN, FIVE),
					Arguments.of(THREE.negate(), FOUR, ELEVEN, TEN),
					Arguments.of(THREE, FOUR.negate(), ELEVEN, TEN),
					Arguments.of(ZERO, ONE, ELEVEN, ZERO),
					Arguments.of(TWO, ZERO, ELEVEN, ZERO),
					Arguments.of(ONE, SEVEN, ELEVEN, SEVEN),
					Arguments.of(FIVE, ONE, ELEVEN, FIVE)
			);
		}

		@ParameterizedTest
		@MethodSource("testModMultiplySpecificValues")
		void testModMultiply_validInputs(final BigInteger a, final BigInteger b, final BigInteger modulus, final BigInteger expected) {
			assertEquals(expected, operations.modMultiply(a, b, modulus));
		}

		@Test
		void testModMultiply_nullInputs() {
			final BigInteger n = randomService.genRandomInteger(modulus);
			assertThrows(NullPointerException.class, () -> operations.modMultiply(null, n, modulus));
			assertThrows(NullPointerException.class, () -> operations.modMultiply(n, null, modulus));
			assertThrows(NullPointerException.class, () -> operations.modMultiply(n, n, null));
		}

		@Test
		void testModMultiply_invalidModulus() {
			assertThrows(IllegalArgumentException.class, () -> operations.modMultiply(ONE, ONE, ONE));
		}

		@Test
		void testModMultiply_identityElement() {
			// Identity: a * 1 = 1 * a = a mod p
			final BigInteger a = randomService.genRandomInteger(modulus);
			assertEquals(a.mod(modulus), operations.modMultiply(a, ONE, modulus));
			assertEquals(a.mod(modulus), operations.modMultiply(ONE, a, modulus));
		}

		@Test
		void testModMultiply_zero() {
			// Zero: a * 0 = 0 * a = 0 mod p
			final BigInteger a = randomService.genRandomInteger(modulus);
			assertEquals(ZERO, operations.modMultiply(a, ZERO, modulus));
			assertEquals(ZERO, operations.modMultiply(ZERO, a, modulus));
		}

		@Test
		void testModMultiply_commutativity() {
			// a * b = b * a
			final BigInteger a = randomService.genRandomInteger(modulus);
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger result1 = operations.modMultiply(a, b, modulus);
			final BigInteger result2 = operations.modMultiply(b, a, modulus);
			assertEquals(result1, result2);
		}

		@Test
		void testModMultiply_associativity() {
			// (a * b) * c = a * (b * c)
			final BigInteger a = randomService.genRandomInteger(modulus);
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger c = randomService.genRandomInteger(modulus);

			final BigInteger ab = operations.modMultiply(a, b, modulus);
			final BigInteger abc1 = operations.modMultiply(ab, c, modulus);

			final BigInteger bc = operations.modMultiply(b, c, modulus);
			final BigInteger abc2 = operations.modMultiply(a, bc, modulus);

			assertEquals(abc1, abc2);
		}

		@Test
		void testModMultiply_withNegativeNumbers() {
			// (-a) * b = a * (-b) = - (a * b) mod p
			final BigInteger a = randomService.genRandomInteger(modulus);
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger result1 = operations.modMultiply(a, b, modulus).negate().mod(modulus);
			final BigInteger result2 = operations.modMultiply(a.negate(), b, modulus);
			final BigInteger result3 = operations.modMultiply(a, b.negate(), modulus);
			assertEquals(result1, result2);
			assertEquals(result2, result3);

			// (-a) * (-b) = a * b mod p
			final BigInteger expected = operations.modMultiply(a, b, modulus);
			assertEquals(expected, operations.modMultiply(a.negate(), b.negate(), modulus));
		}
	}

	/**
	 * Tests the modExponentiate method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>specific values give the expected output</li>
	 *     <li>negative exponent x < 0: <ul>
	 *         <li>gcd(b, m) = 1: modExponentiate(b, x, m) gives the expected result</li>
	 *         <li>gcd(b, m) > 1: modExponentiate(b, x, m) throws an {@link IllegalArgumentException}</li>
	 *     </ul></li>
	 *     <li>null inputs throw a {@link NullPointerException}</li>
	 *     <li>a modulus that is even or smaller than 2 throws an {@link IllegalArgumentException}</li>
	 *     <li>zero Exponent: b<sup>0</sup> = 1</li>
	 *     <li>zero base: 0<sup>x</sup> = 0 &forall; x &ne; 0</li>
	 *     <li>-1 base: <ul>
	 *         <li>(-1)<sup>2k</sup> = 1 &forall; k &isin; ℕ</li>
	 *         <li>(-1)<sup>2k + 1</sup> = -1 &forall; k &isin; ℕ</li>
	 *     </ul></li>
	 *     <li>b^(x + y) = b^x * b^y mod p</li>
	 *     <li>(b^x)^y = b^(x * y) mod p</li>
	 *     <li>b^(-k) = (b^k)^(-1) = 1 / b^k</li>
	 *     <li>Same results as {@link BigInteger#modPow(BigInteger, BigInteger)}</li>
	 * </ul>
	 */
	@Nested
	class ModExponentiateTest {

		private BigInteger modulus;

		@BeforeEach
		void setUp() {
			modulus = randomService.genRandomInteger(MAX_LONG).multiply(TWO).add(THREE); // Make sure that modulus is odd
		}

		@Test
		void testModExponentiate_positiveExponent() {
			assertEquals(ONE, operations.modExponentiate(TWO, FOUR, FIVE));
			assertEquals(TWO, operations.modExponentiate(TWO, FOUR, SEVEN));
			assertEquals(SEVEN, operations.modExponentiate(TWO, FOUR, NINE));
		}

		@Test
		void testModExponentiate_negativeExponent_relativelyPrime() {
			assertEquals(FIVE, operations.modExponentiate(THREE, MINUS_ONE, SEVEN));
		}

		@Test
		void testModExponentiate_negativeExponent_notRelativelyPrime() {
			assertThrows(IllegalArgumentException.class, () -> operations.modExponentiate(THREE, MINUS_ONE, SIX));
		}

		@Test
		void testModExponentiate_nullInputs() {
			assertThrows(NullPointerException.class, () -> operations.modExponentiate(null, ONE, THREE));
			assertThrows(NullPointerException.class, () -> operations.modExponentiate(ONE, null, THREE));
			assertThrows(NullPointerException.class, () -> operations.modExponentiate(ONE, ONE, null));
		}

		@Test
		void testModExponentiate_invalidModulus() {
			assertThrows(IllegalArgumentException.class, () -> operations.modExponentiate(TWO, THREE, ONE));
			assertThrows(IllegalArgumentException.class, () -> operations.modExponentiate(TWO, THREE, TWO));
			final BigInteger evenModulus = modulus.add(ONE);
			assertThrows(IllegalArgumentException.class, () -> operations.modExponentiate(TWO, THREE, evenModulus));
		}

		@Test
		void testModExponentiate_zeroExponent() {
			final BigInteger base = randomService.genRandomInteger(modulus.subtract(ONE)).add(ONE);
			assertEquals(ONE, operations.modExponentiate(base, ZERO, modulus));
		}

		@Test
		void testModExponentiate_zeroBase() {
			final BigInteger x = randomService.genRandomInteger(modulus.subtract(ONE)).add(ONE);
			assertEquals(ZERO, operations.modExponentiate(ZERO, x, modulus));
		}

		@Test
		void testModExponentiate_minusOneBase() {
			final BigInteger evenExponent = randomService.genRandomInteger(modulus.divide(TWO)).multiply(TWO);
			final BigInteger oddExponent = evenExponent.add(ONE);
			assertEquals(ONE.mod(modulus), operations.modExponentiate(MINUS_ONE, evenExponent, modulus));
			assertEquals(MINUS_ONE.mod(modulus), operations.modExponentiate(MINUS_ONE, oddExponent, modulus));
		}

		@Test
		void testModExponentiate_additionInExponent() {
			// b^(x + y) = b^x * b^y mod p
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger x = randomService.genRandomInteger(modulus);
			final BigInteger y = randomService.genRandomInteger(modulus);
			final BigInteger result1 = operations.modExponentiate(b, x.add(y), modulus);
			final BigInteger result2 = operations.modMultiply(operations.modExponentiate(b, x, modulus), operations.modExponentiate(b, y, modulus),
					modulus);
			assertEquals(result1, result2);
		}

		@Test
		void testModExponentiate_multiplicationInExponent() {
			// (b^x)^y = b^x * b^(x * y) mod p
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger x = randomService.genRandomInteger(modulus);
			final BigInteger y = randomService.genRandomInteger(modulus);
			final BigInteger result1 = operations.modExponentiate(operations.modExponentiate(b, x, modulus), y, modulus);
			final BigInteger result2 = operations.modExponentiate(b, x.multiply(y), modulus);
			assertEquals(result1, result2);
		}

		static Stream<Arguments> provideValidInverseAlignmentCases() {
			return Stream.of(
					Arguments.of(BigInteger.valueOf(35617), BigInteger.valueOf(66), BigInteger.valueOf(6331)),
					Arguments.of(BigInteger.valueOf(45980), BigInteger.valueOf(40), BigInteger.valueOf(7987)),
					Arguments.of(BigInteger.valueOf(2685), BigInteger.valueOf(29), BigInteger.valueOf(42749)),
					Arguments.of(BigInteger.valueOf(6305), BigInteger.valueOf(27), BigInteger.valueOf(20363)),
					Arguments.of(BigInteger.valueOf(37595), BigInteger.valueOf(94), BigInteger.valueOf(55003)),
					Arguments.of(BigInteger.valueOf(3105), BigInteger.valueOf(4), BigInteger.valueOf(23857)),
					Arguments.of(BigInteger.valueOf(10493), BigInteger.valueOf(71), BigInteger.valueOf(35535)),
					Arguments.of(BigInteger.valueOf(7735), BigInteger.valueOf(62), BigInteger.valueOf(22097)),
					Arguments.of(BigInteger.valueOf(56219), BigInteger.valueOf(28), BigInteger.valueOf(58407)),
					Arguments.of(BigInteger.valueOf(7033), BigInteger.valueOf(5), BigInteger.valueOf(48219))
			);
		}

		@ParameterizedTest
		@MethodSource("provideValidInverseAlignmentCases")
		void testModExponentiate_inverseAlignment_gcd1(final BigInteger b, final BigInteger k, final BigInteger m) {
			checkArgument(ONE.equals(b.gcd(m)), "Base and modulus must be coprime");

			final BigInteger positiveExp = operations.modExponentiate(b, k, m);
			final BigInteger expectedInverse = operations.modExponentiate(positiveExp, MINUS_ONE, m);
			final BigInteger negativeExp = operations.modExponentiate(b, k.negate(), m);
			final BigInteger inversePositiveExp = operations.modInvert(positiveExp, m);

			assertEquals(expectedInverse, negativeExp,
					String.format("Failed for b=%s, k=%s, m=%s", b, k, m));
			assertEquals(expectedInverse, inversePositiveExp,
					String.format("Failed for b=%s, k=%s, m=%s", b, k, m));
		}

		static Stream<Arguments> provideInvalidInverseAlignmentCases() {
			return Stream.of(
					Arguments.of(BigInteger.valueOf(35616), BigInteger.valueOf(66), BigInteger.valueOf(6333)),
					Arguments.of(BigInteger.valueOf(45981), BigInteger.valueOf(40), BigInteger.valueOf(6987)),
					Arguments.of(BigInteger.valueOf(2685), BigInteger.valueOf(29), BigInteger.valueOf(42745)),
					Arguments.of(BigInteger.valueOf(20363), BigInteger.valueOf(27), BigInteger.valueOf(6307)),
					Arguments.of(BigInteger.valueOf(37595), BigInteger.valueOf(94), BigInteger.valueOf(54969)),
					Arguments.of(BigInteger.valueOf(3105), BigInteger.valueOf(4), BigInteger.valueOf(23897)),
					Arguments.of(BigInteger.valueOf(10493), BigInteger.valueOf(71), BigInteger.valueOf(34477)),
					Arguments.of(BigInteger.valueOf(7735), BigInteger.valueOf(62), BigInteger.valueOf(22095)),
					Arguments.of(BigInteger.valueOf(56219), BigInteger.valueOf(28), BigInteger.valueOf(58429)),
					Arguments.of(BigInteger.valueOf(7032), BigInteger.valueOf(5), BigInteger.valueOf(48219))
			);
		}

		@ParameterizedTest
		@MethodSource("provideInvalidInverseAlignmentCases")
		void testModExponentiate_inverseAlignment_gcdNe1(final BigInteger b, final BigInteger k, final BigInteger m) {
			checkArgument(!ONE.equals(b.gcd(m)), "Base and modulus must not be coprime");
			final BigInteger minusK = k.negate();
			assertThrows(IllegalArgumentException.class, () -> operations.modExponentiate(b, minusK, m));
		}

		@RepeatedTest(100)
		void testModExponentiate_equalModPow_positiveExponent() {
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger x = randomService.genRandomInteger(modulus);
			final BigInteger expected = b.modPow(x, modulus);
			final BigInteger result = operations.modExponentiate(b, x, modulus);
			assertEquals(expected, result);
		}

		@RepeatedTest(100)
		void testModExponentiate_equalModPow_negativeExponent() {
			final BigInteger m = BigInteger.valueOf(4_294_967_279L);
			final BigInteger b = randomService.genRandomInteger(m);
			final BigInteger x = randomService.genRandomInteger(m).negate();
			final BigInteger expected = b.modPow(x, m);
			final BigInteger result = operations.modExponentiate(b, x, m);
			assertEquals(expected, result);
		}
	}

	/**
	 * Tests the multiModExp method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>specific inputs give the expected output</li>
	 *     <li>null inputs throw a {@link NullPointerException}</li>
	 *     <l>empty lists throw an {@link IllegalArgumentException}</l>
	 *     <li>bases and exponents of different lengths throw an {@link IllegalArgumentException}</li>
	 *     <li>a modulus that is even or smaller than 2 throws an {@link IllegalArgumentException}</li>
	 *     <li>if b = (b_0), x = (x_0) then multiModExp(b, x, m) = modExp(b_0, x_0, m)</li>
	 *     <li>negative exponent(s) throws an {@link IllegalArgumentException}</li>
	 *     <li>commutativity: multiModExp((b_0, b_1), (x_0, x_1), m) = multiModExp((b_1, b_0), (x_1, x_0), m)</li>
	 *     <li>Zero exponents: multiModExp((b_0, ..., b_k, ..., b_n), (x_0, ..., 0, ..., x_n), m) = multiModExp((b_0, ..., b_n), (x_0, ..., x_n), m)</li>
	 *     <li>One bases: multiModExp((b_0, ..., 1, ..., b_n), (x_0, ..., x_k, ..., x_n), m) = multiModExp((b_0, ..., b_n), (x_0, ..., x_n), m)</li>
	 * </ul>
	 */
	@Nested
	class MultiModExpTest {

		private BigInteger modulus;

		@BeforeEach
		void setUp() {
			modulus = randomService.genRandomInteger(MAX_LONG).multiply(TWO).add(THREE); // Make sure that modulus is odd
		}

		static Stream<Arguments> provideValidInputs() {
			return Stream.of(
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(TWO, ONE), FIVE, TWO),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(TWO, ONE), SEVEN, FIVE),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(TWO, ONE), NINE, THREE),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(TWO, ONE), ELEVEN, ONE),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(THREE, TWO), FIVE, TWO),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(THREE, TWO), SEVEN, TWO),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(THREE, TWO), NINE, ZERO),
					Arguments.of(ImmutableList.of(TWO, THREE), ImmutableList.of(THREE, TWO), ELEVEN, SIX),
					Arguments.of(ImmutableList.of(ONE, TWO, THREE), ImmutableList.of(ONE, TWO, THREE), ELEVEN, NINE)
			);
		}

		@ParameterizedTest
		@MethodSource("provideValidInputs")
		void testMultiModExp_validInputs(final ImmutableList<BigInteger> bases, final ImmutableList<BigInteger> exponents, final BigInteger modulus,
				final BigInteger expected) {
			assertEquals(expected, operations.multiModExp(bases, exponents, modulus));
		}

		@Test
		void testMultiModExp_nullLists() {
			final ImmutableList<BigInteger> listOfOne = ImmutableList.of(ONE);
			assertThrows(NullPointerException.class, () -> operations.multiModExp(null, listOfOne, THREE));
			assertThrows(NullPointerException.class, () -> operations.multiModExp(listOfOne, null, THREE));
		}

		@Test
		void testMultiModExp_emptyLists() {
			final ImmutableList<BigInteger> emptyList = ImmutableList.emptyList();
			assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(emptyList, emptyList, THREE));
		}

		@Test
		void testMultiModExp_mismatchedSizes() {
			final ImmutableList<BigInteger> bases = ImmutableList.of(ONE, TWO);
			final ImmutableList<BigInteger> exponents = ImmutableList.of(ONE);
			assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(bases, exponents, THREE));
		}

		@Test
		void testMultiModExp_invalidModulus() {
			final ImmutableList<BigInteger> bases = ImmutableList.of(ONE, TWO);
			final ImmutableList<BigInteger> exponents = ImmutableList.of(ONE, THREE);
			assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(bases, exponents, ONE));
			assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(bases, exponents, TWO));
			final BigInteger modulusPlusOne = modulus.add(ONE);
			assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(bases, exponents, modulusPlusOne));
		}

		@Test
		void testMultiModExp_BasesAndExponentsLengthOne() {
			final BigInteger b = randomService.genRandomInteger(modulus);
			final BigInteger e = randomService.genRandomInteger(modulus);
			final ImmutableList<BigInteger> bases = ImmutableList.of(b);
			final ImmutableList<BigInteger> exponents = ImmutableList.of(e);
			final BigInteger expected = operations.modExponentiate(b, e, modulus);
			assertEquals(expected, operations.multiModExp(bases, exponents, modulus));
		}

		@Test
		void testMultiModExp_negativeExponents() {
			final int size = 32;
			final ImmutableList<BigInteger> bases = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final ImmutableList<BigInteger> exponents = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			IntStream.range(0, size).forEach(i -> {
				final ImmutableList<BigInteger> exponentsWithNegative = IntStream.range(0, size)
						.mapToObj(j -> j != i ? exponents.get(j) : exponents.get(j).negate())
						.collect(ImmutableList.toImmutableList());
				assertThrows(IllegalArgumentException.class, () -> operations.multiModExp(bases, exponentsWithNegative, modulus));
			});
		}

		@Test
		void testMultiModExp_commutativity() {
			final BigInteger b1 = randomService.genRandomInteger(modulus);
			final BigInteger b2 = randomService.genRandomInteger(modulus);
			final BigInteger e1 = randomService.genRandomInteger(modulus);
			final BigInteger e2 = randomService.genRandomInteger(modulus);

			final ImmutableList<BigInteger> bases12 = ImmutableList.of(b1, b2);
			final ImmutableList<BigInteger> exponents12 = ImmutableList.of(e1, e2);
			final BigInteger result12 = operations.multiModExp(bases12, exponents12, modulus);

			final ImmutableList<BigInteger> bases21 = ImmutableList.of(b2, b1);
			final ImmutableList<BigInteger> exponents21 = ImmutableList.of(e2, e1);
			final BigInteger result21 = operations.multiModExp(bases21, exponents21, modulus);

			assertEquals(result12, result21);
		}

		@Test
		void testMultiModExp_zeroExponents() {
			final int size = randomService.genRandomInteger(31) + 1;
			final ImmutableList<BigInteger> bases = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final ImmutableList<BigInteger> exponents = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final BigInteger expected = operations.multiModExp(bases, exponents, modulus);

			for (int i = 0; i <= size; i++) {
				final ArrayList<BigInteger> testBases = new ArrayList<>(size + 1);
				testBases.addAll(bases.asList());
				testBases.add(i, randomService.genRandomInteger(modulus));

				final ArrayList<BigInteger> testExponents = new ArrayList<>(size + 1);
				testExponents.addAll(exponents.asList());
				testExponents.add(i, ZERO);

				assertEquals(expected, operations.multiModExp(ImmutableList.from(testBases), ImmutableList.from(testExponents), modulus));
			}
		}

		@Test
		void testMultiModExp_oneBase() {
			final int size = randomService.genRandomInteger(31) + 1;
			final ImmutableList<BigInteger> bases = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final ImmutableList<BigInteger> exponents = randomService.genRandomVector(modulus, size).stream()
					.map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final BigInteger expected = operations.multiModExp(bases, exponents, modulus);

			for (int i = 0; i <= size; i++) {
				final ArrayList<BigInteger> testBases = new ArrayList<>(size + 1);
				testBases.addAll(bases.asList());
				testBases.add(i, ONE);

				final ArrayList<BigInteger> testExponents = new ArrayList<>(size + 1);
				testExponents.addAll(exponents.asList());
				testExponents.add(i, randomService.genRandomInteger(modulus));

				assertEquals(expected, operations.multiModExp(ImmutableList.from(testBases), ImmutableList.from(testExponents), modulus));
			}
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 31, 32, 33, 63, 64, 65 })
		void testMultiModExp_differentListSizes(final int size) {
			final ImmutableList<BigInteger> bases = randomService.genRandomVector(modulus, size).stream().map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());
			final ImmutableList<BigInteger> exponents = randomService.genRandomVector(modulus, size).stream().map(ZqElement::getValue)
					.collect(ImmutableList.toImmutableList());

			final BigInteger expected = IntStream.range(0, size)
					.mapToObj(i -> operations.modExponentiate(bases.get(i), exponents.get(i), modulus))
					.reduce(BigInteger.ONE, (a, b) -> operations.modMultiply(a, b, modulus));
			assertEquals(expected, operations.multiModExp(bases, exponents, modulus));
		}
	}

	/**
	 * Tests the modInvert method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>specific inputs give the expected output</li>
	 *     <li>null inputs throw a {@link NullPointerException}</li>
	 *     <li>modulus m <= 1 throws an {@link IllegalArgumentException}</li>
	 *     <li>gcd(a, m) &ne; 1 throws an {@link IllegalArgumentException}</li>
	 *     <li>inverse of identity: 1^(-1) = 1</li>
	 *     <li>inverse of m-1: (m-1)^(-1) = m-1</li>
	 *     <li>multiplication with inverse: a^(-1) * a = 1</li>
	 *     <li>extended Euclidian algorithm: <i>ax</i> + <i>my</i> = gcd(<i>a</i>, <i>m</i>). For gcd(<i>a</i>, <i>m</i>) = 1, modInvert(a, m) = x</li>
	 * </ul>
	 */
	@Nested
	class ModInvertTest {

		private BigInteger modulus;

		@BeforeEach
		void setUp() {
			modulus = randomService.genRandomInteger(MAX_LONG).multiply(TWO).add(THREE); // Make sure that modulus is odd
		}

		static boolean isBigIntegerOperationsVMGJ() {
			return operations instanceof BigIntegerOperationsVMGJ;
		}

		static Stream<Arguments> provideInversions() {
			return Stream.of(
					Arguments.of(THREE, FIVE, TWO),
					Arguments.of(FIVE, SEVEN, THREE),
					Arguments.of(THREE, ELEVEN, FOUR),
					Arguments.of(BigInteger.valueOf(18), BigInteger.valueOf(197), ELEVEN),
					Arguments.of(SIX, BigInteger.valueOf(197), BigInteger.valueOf(33)),
					Arguments.of(FIVE, BigInteger.valueOf(197), BigInteger.valueOf(79))
			);
		}

		@ParameterizedTest
		@MethodSource("provideInversions")
		void testModInvert_validInput(final BigInteger a, final BigInteger m, final BigInteger expected) {
			assertEquals(expected, operations.modInvert(a, m));
		}

		@Test
		void testModInvert_nullInputs() {
			assertThrows(NullPointerException.class, () -> operations.modInvert(null, BigInteger.TWO));
			assertThrows(NullPointerException.class, () -> operations.modInvert(BigInteger.ONE, null));
		}

		@Test
		void testModInvert_invalidModulus() {
			assertThrows(IllegalArgumentException.class, () -> operations.modInvert(ONE, ONE));
		}

		static Stream<Arguments> provideInvalidCases() {
			return Stream.of(
					Arguments.of(TWO, FOUR),
					Arguments.of(THREE, NINE),
					Arguments.of(ZERO, ELEVEN),
					Arguments.of(BigInteger.valueOf(15), BigInteger.valueOf(25)),
					Arguments.of(BigInteger.valueOf(14), BigInteger.valueOf(49)),
					Arguments.of(BigInteger.valueOf(33), BigInteger.valueOf(121))
			);
		}

		@ParameterizedTest
		@MethodSource("provideInvalidCases")
		@DisabledIf("isBigIntegerOperationsVMGJ")
		void testModInvert_nonInvertibleElement(final BigInteger a, final BigInteger m) {
			assertThrows(IllegalArgumentException.class, () -> operations.modInvert(a, m));
		}

		@Test
		void testModInvert_identity() {
			assertEquals(ONE, operations.modInvert(ONE, modulus));
		}

		@Test
		void testModInvert_mMinusOne() {
			final BigInteger mMinusOne = modulus.subtract(ONE);
			assertEquals(mMinusOne, operations.modInvert(mMinusOne, modulus));
		}

		@Test
		void testModInvert_multiplyWithInverse() {
			BigInteger a;
			do {
				a = randomService.genRandomInteger(modulus.subtract(ONE)).add(ONE);
			} while (!a.gcd(modulus).equals(ONE));
			final BigInteger aInverted = operations.modInvert(a, modulus);
			assertEquals(ONE, operations.modMultiply(aInverted, a, modulus));
		}

		@RepeatedTest(100)
		void testModInvert_compareWithExtendedEuclideanAlgorithm() {
			BigInteger a;
			do {
				a = randomService.genRandomInteger(modulus.subtract(ONE)).add(ONE);
			} while (!a.gcd(modulus).equals(ONE));
			assertEquals(extendedGCD(a, modulus).x().mod(modulus), operations.modInvert(a, modulus));
		}

		record ExtendedGCD(BigInteger gcd, BigInteger x, BigInteger y) {}

		/**
		 * Calculates the greatest common divisor (gcd) of two integers <i>a</i> and <i>b</i> and the unique integers <i>x, y</i> such that <i>ax</i>
		 * + <i>by</i> = gcd(<i>a</i>, <i>b</i>).
		 * <p>
		 * If gcd(<i>a</i>, <i>b</i>) = 1, then <i>x</i> = <i>a</i><sup>-1</sup>
		 *
		 * @param a the first integer
		 * @param b the second integer
		 * @return (gcd(a, b), x, y) such that ax + by = gcd(a, b)
		 */
		public static ExtendedGCD extendedGCD(final BigInteger a, final BigInteger b) {
			if (b.equals(ZERO)) {
				return new ExtendedGCD(a, ONE, ZERO);
			}

			final ExtendedGCD next = extendedGCD(b, a.mod(b));
			final BigInteger x = next.y;
			final BigInteger y = next.x.subtract((a.divide(b)).multiply(next.y));

			return new ExtendedGCD(next.gcd, x, y);
		}

	}

	/**
	 * Tests the getLegendre method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>For quadratic residues, return 1</li>
	 *     <li>For non residues, return -1</li>
	 *     <li>For 0, return 0</li>
	 *     <li>m % 2 = 0 throws an {@link IllegalArgumentException}</li>
	 *     <li>null input throws a {@link NullPointerException}</li>
	 *     <li>Euler equivalence: getLegendre(a, m) = a<sup>(m - 1) / 2</sup> mod m</li>
	 * </ul>
	 */
	@Nested
	class GetLegendreTest {

		@Test
		void testGetLegendre_quadraticResidue() {
			assertEquals(1, operations.getLegendre(FOUR, SEVEN));
		}

		@Test
		void testGetLegendre_nonResidue() {
			assertEquals(-1, operations.getLegendre(THREE, SEVEN));
		}

		@Test
		void testGetLegendre_zero() {
			assertEquals(0, operations.getLegendre(ZERO, SEVEN));
		}

		@Test
		void testGetLegendre_invalidModulus() {
			assertThrows(IllegalArgumentException.class, () -> operations.getLegendre(THREE, TWO));
			assertThrows(IllegalArgumentException.class, () -> operations.getLegendre(THREE, TEN));
		}

		@Test
		void testGetLegendre_nullArguments() {
			assertThrows(NullPointerException.class, () -> operations.getLegendre(null, SEVEN));
			assertThrows(NullPointerException.class, () -> operations.getLegendre(THREE, null));
		}

		@RepeatedTest(100)
		void testGetLegendre_eulerEquivalence() {
			final BigInteger prime = BigInteger.valueOf(99989);
			final BigInteger a = randomService.genRandomInteger(prime);
			final BigInteger euler = euler(a, prime);
			final BigInteger legendre = BigInteger.valueOf(operations.getLegendre(a, prime)).add(prime).mod(prime);
			assertEquals(euler, legendre);
		}

		private BigInteger euler(final BigInteger a, final BigInteger p) {
			return a.modPow(p.subtract(ONE).divide(BigInteger.TWO), p);
		}

		@RepeatedTest(100)
		void testGetLegendre_quadraticResidues() {
			final BigInteger prime = BigInteger.valueOf(99989);
			final BigInteger a = randomService.genRandomInteger(prime).modPow(TWO, prime);
			assertEquals(1, operations.getLegendre(a, prime));
		}
	}

	/**
	 * Tests the millerRabin method.
	 * <p>
	 * The tests run are:
	 * <ul>
	 *     <li>invalid arguments throws an {@link IllegalArgumentException}</li>
	 *     <li>null input throws a {@link NullPointerException}</li>
	 *     <li>known primes result as expected</li>
	 *     <li>known composites result as expected</li>
	 * </ul>
	 */
	@Nested
	class MillerRabinTest {

		@Test
		void millerRabinNullArgument() {
			assertThrows(NullPointerException.class, () -> BigIntegerOperationsService.millerRabin(null, 1));
		}

		@Test
		void millerRabinInvalidArguments() {
			assertThrows(IllegalArgumentException.class, () -> BigIntegerOperationsService.millerRabin(BigInteger.ZERO, 1));
			assertThrows(IllegalArgumentException.class, () -> BigIntegerOperationsService.millerRabin(FIVE, 0));
			assertThrows(IllegalArgumentException.class, () -> BigIntegerOperationsService.millerRabin(ONE, 3));
			assertThrows(IllegalArgumentException.class, () -> BigIntegerOperationsService.millerRabin(EIGHT, 3));
		}

		static Stream<Arguments> provideKnownPrimes() {
			return Stream.of(
					Arguments.of(THREE, 1),
					Arguments.of(FIVE, 3),
					Arguments.of(SEVEN, 3),
					Arguments.of(ELEVEN, 3),
					Arguments.of(BigInteger.valueOf(13), 3),
					Arguments.of(BigInteger.valueOf(31), 3),
					Arguments.of(BigInteger.valueOf(97), 3)
			);
		}

		@ParameterizedTest
		@MethodSource("provideKnownPrimes")
		void millerRabinKnownPrimes(final BigInteger prime, final int rounds) {
			assertTrue(BigIntegerOperationsService.millerRabin(prime, rounds));
		}

		static Stream<Arguments> provideKnownComposites() {
			final int rounds = SecurityLevelInternal.TESTING_ONLY.getSecurityStrength();
			return Stream.of(
					Arguments.of(NINE, rounds),
					Arguments.of(BigInteger.valueOf(15), rounds),
					Arguments.of(BigInteger.valueOf(21), rounds),
					Arguments.of(BigInteger.valueOf(25), rounds),
					Arguments.of(BigInteger.valueOf(27), rounds),
					Arguments.of(BigInteger.valueOf(33), rounds),
					// Carmichael numbers
					Arguments.of(BigInteger.valueOf(561), rounds),
					Arguments.of(BigInteger.valueOf(1105), rounds),
					Arguments.of(BigInteger.valueOf(1729), rounds),
					Arguments.of(BigInteger.valueOf(2465), rounds),
					Arguments.of(BigInteger.valueOf(2821), rounds),
					Arguments.of(BigInteger.valueOf(6601), rounds),
					Arguments.of(BigInteger.valueOf(8911), rounds),
					Arguments.of(BigInteger.valueOf(10585), rounds),
					Arguments.of(BigInteger.valueOf(15841), rounds),
					Arguments.of(BigInteger.valueOf(29341), rounds),
					Arguments.of(BigInteger.valueOf(552721), rounds),
					// Other prime factors
					Arguments.of(BigInteger.valueOf(97).pow(16), rounds),
					Arguments.of(BigInteger.valueOf(31).pow(32), rounds)
			);
		}

		@ParameterizedTest
		@MethodSource("provideKnownComposites")
		void millerRabinKnownComposites(final BigInteger prime, final int rounds) {
			assertFalse(BigIntegerOperationsService.millerRabin(prime, rounds));
		}
	}

	/**
	 * Tests that all methods work correctly in multithreaded environments.
	 */
	@Nested
	class MultiThreadingTests {

		@RepeatedTest(100)
		void testMultiThreading_modMultiply() {
			final int size = 32;
			final List<BigInteger> modulus = randomService.genRandomVector(MAX_LONG, size).stream()
					.map(m -> m.getValue().multiply(TWO).add(THREE))
					.toList();
			final List<BigInteger> firstElements = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE))
					.toList();
			final List<BigInteger> secondElements = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE))
					.toList();

			final List<BigInteger> expected = IntStream.range(0, size)
					.sequential()
					.mapToObj(i -> operations.modMultiply(firstElements.get(i), secondElements.get(i), modulus.get(i)))
					.toList();

			final List<BigInteger> results = IntStream.range(0, size)
					.parallel()
					.mapToObj(i -> operations.modMultiply(firstElements.get(i), secondElements.get(i), modulus.get(i)))
					.toList();

			assertEquals(expected, results);
		}

		@RepeatedTest(100)
		void testMultiThreading_modExponentiate() {
			final int size = 32;
			final List<BigInteger> modulus = randomService.genRandomVector(MAX_LONG, size).stream()
					.map(m -> m.getValue().multiply(TWO).add(THREE))
					.toList();
			final List<BigInteger> bases = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE))
					.toList();
			final List<BigInteger> exponents = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE))
					.toList();

			final List<BigInteger> expected = IntStream.range(0, size)
					.sequential()
					.mapToObj(i -> operations.modExponentiate(bases.get(i), exponents.get(i), modulus.get(i)))
					.toList();

			final List<BigInteger> results = IntStream.range(0, size)
					.parallel()
					.mapToObj(i -> operations.modExponentiate(bases.get(i), exponents.get(i), modulus.get(i)))
					.toList();

			assertEquals(expected, results);
		}

		@RepeatedTest(100)
		void testMultiThreading_multiModExp() {
			final int size = 32;
			final int length = 100;
			final List<BigInteger> modulus = randomService.genRandomVector(MAX_LONG, size).stream()
					.map(m -> m.getValue().multiply(TWO).add(THREE))
					.toList();
			final List<ImmutableList<BigInteger>> bases = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomVector(modulus.get(i).subtract(ONE), length).stream()
							.map(ZqElement::getValue)
							.collect(ImmutableList.toImmutableList()))
					.toList();
			final List<ImmutableList<BigInteger>> exponents = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomVector(modulus.get(i).subtract(ONE), length).stream()
							.map(ZqElement::getValue)
							.collect(ImmutableList.toImmutableList()))
					.toList();

			final List<BigInteger> expected = IntStream.range(0, size)
					.sequential()
					.mapToObj(i -> operations.multiModExp(bases.get(i), exponents.get(i), modulus.get(i)))
					.toList();

			final List<BigInteger> results = IntStream.range(0, size)
					.parallel()
					.mapToObj(i -> operations.multiModExp(bases.get(i), exponents.get(i), modulus.get(i)))
					.toList();

			assertEquals(expected, results);
		}

		@RepeatedTest(100)
		void testMultiThreading_modInvert() {
			final int size = 32;
			final List<BigInteger> modulus = randomService.genRandomVector(MAX_LONG, size).stream()
					.map(m -> m.getValue().multiply(TWO).add(THREE))
					.toList();
			final List<BigInteger> elements = IntStream.range(0, size)
					.mapToObj(i -> {
						BigInteger a;
						do {
							a = randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE);
						} while (!a.gcd(modulus.get(i)).equals(ONE));
						return a;
					})
					.toList();

			final List<BigInteger> expected = IntStream.range(0, size)
					.sequential()
					.mapToObj(i -> operations.modInvert(elements.get(i), modulus.get(i)))
					.toList();

			final List<BigInteger> results = IntStream.range(0, size)
					.parallel()
					.mapToObj(i -> operations.modInvert(elements.get(i), modulus.get(i)))
					.toList();

			assertEquals(expected, results);
		}

		@RepeatedTest(100)
		void testMultiThreading_getLegendre() {
			final int size = 32;
			final List<BigInteger> modulus = randomService.genRandomVector(MAX_LONG, size).stream()
					.map(m -> m.getValue().multiply(TWO).add(THREE))
					.toList();
			final List<BigInteger> elements = IntStream.range(0, size)
					.mapToObj(i -> randomService.genRandomInteger(modulus.get(i).subtract(ONE)).add(ONE))
					.toList();

			final List<Integer> expected = IntStream.range(0, size)
					.sequential()
					.mapToObj(i -> operations.getLegendre(elements.get(i), modulus.get(i)))
					.toList();

			final List<Integer> results = IntStream.range(0, size)
					.parallel()
					.mapToObj(i -> operations.getLegendre(elements.get(i), modulus.get(i)))
					.toList();

			assertEquals(expected, results);
		}
	}

}
