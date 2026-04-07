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
package ch.post.it.evoting.cryptoprimitives.utils;

import static ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal.byteArrayToInteger;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.RepeatedTest;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal;

class ConversionsEquivalenceTest {

	private static final TestRandomService randomService = new TestRandomService();

	@RepeatedTest(1000)
	void testByteArrayToIntegerIsEquivalentToSpec() {
		final ImmutableByteArray byteArray = randomService.randomBytes(32);

		assertEquals(byteArrayToIntegerSpec(byteArray), byteArrayToInteger(byteArray));
	}

	@RepeatedTest(100)
	void randomBigIntegerConversionToFixedLengthIsEquivalentWithTwoMethods() {
		final int BIT_LENGTH = 3072;
		final BigInteger random = randomService.genRandomIntegerOfLength(BIT_LENGTH);
		final int desiredLength = BIT_LENGTH + randomService.genRandomInteger(BIT_LENGTH);
		final ImmutableByteArray expected = integerToFixedLengthByteArraySpec(random, desiredLength);
		final ImmutableByteArray result = ConversionsInternal.integerToFixedLengthByteArray(random, desiredLength);
		assertEquals(expected, result);
	}

	/**
	 * Implements the specification ByteArrayToInteger algorithm. It is used in tests to show that it is equivalent to the more performant method used
	 * which is implemented in {@link ConversionsInternal#byteArrayToInteger}.
	 *
	 * @param byteArray B, the byte array to convert.
	 * @return the BigInteger representation of this byte array.
	 **/
	@SuppressWarnings("java:S117")
	private BigInteger byteArrayToIntegerSpec(final ImmutableByteArray byteArray) {
		final byte[] B = checkNotNull(byteArray).elements();
		final int n = byteArray.length();

		BigInteger x = BigInteger.ZERO;
		for (int i = 0; i < n; i++) {
			x = BigInteger.valueOf(256).multiply(x).add(BigInteger.valueOf(Byte.toUnsignedInt(B[i])));
		}
		return x;
	}

	/**
	 * Implements the specification IntegerToFixedLengthByteArray algorithm. It is used in tests to show that it is equivalent to the more performant
	 * method used which is implemented in {@link ConversionsInternal#integerToFixedLengthByteArray}.
	 *
	 * @param integer       x, the non-negative BigInteger to convert.
	 * @param desiredLength m, the desired byte length of the output.
	 * @return the byte array representation of this BigInteger.
	 **/
	@SuppressWarnings("java:S117")
	static ImmutableByteArray integerToFixedLengthByteArraySpec(final BigInteger integer, final int desiredLength) {
		final BigInteger twoHundredFiftySix = BigInteger.valueOf(256);
		// Input.
		BigInteger x = integer;
		final int m = desiredLength;

		// Operation.
		final byte[] B = new byte[m];
		for (int i = 0; i < m; i++) {
			B[m - i - 1] = x.mod(twoHundredFiftySix).byteValue();
			x = x.divide(twoHundredFiftySix);
		}
		return new ImmutableByteArray(B);
	}
}
