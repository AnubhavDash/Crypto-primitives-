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
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * <p>This class is thread safe.</p>
 */
public final class ConversionsInternal {

	private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+$");

	private ConversionsInternal() {
		// Intentionally left blank.
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#byteArrayToInteger}.
	 * <p>
	 * Uses the {@link BigInteger} implementation of the byte array to integer transformation, which is equivalent to the specification of
	 * ByteArrayToInteger. We prefer this implementation due to its conciseness.
	 * </p>
	 */
	public static BigInteger byteArrayToInteger(final ImmutableByteArray bytes) {
		checkNotNull(bytes);
		checkArgument(!bytes.isEmpty(), "The byte array to convert must be non-empty.");
		return new BigInteger(1, bytes.elements());
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#integerToByteArray}.
	 * <p>
	 * NOTE: our implementation slightly deviates from the specifications for performance reasons. Benchmarks show that our implementation is orders
	 * of magnitude faster than the pseudo-code implementation integerToByteArraySpec. Both implementations are equivalent, and we have a unit test
	 * ensuring it.
	 * </p>
	 */
	public static ImmutableByteArray integerToByteArray(final BigInteger x) {
		checkNotNull(x);
		checkArgument(x.signum() >= 0);

		// BigInteger#toByteArray gives back a 2s complement representation of the value. Given that we work only with positive BigIntegers, this
		// representation is equivalent to the binary representation, except for a potential extra leading zero byte. (The presence or not of the
		// leading zero depends on the number of bits needed to represent this value).
		final ImmutableByteArray twosComplement = new ImmutableByteArray(x.toByteArray());
		final ImmutableByteArray result;
		if (twosComplement.get(0) == 0 && twosComplement.length() > 1) {
			result = ImmutableByteArray.copyOfRange(twosComplement, 1, twosComplement.length());
		} else {
			result = twosComplement;
		}
		return result;
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#stringToByteArray}
	 */
	public static ImmutableByteArray stringToByteArray(final String s) {
		checkNotNull(s);

		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
				// Explicitly set the error actions to REPORT to be sure no ignore nor replace action is performed.
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		try {
			// Check that s is a valid UTF-8 string
			final ByteBuffer buffer = encoder.encode(CharBuffer.wrap(s));

			final byte[] result = new byte[buffer.remaining()];
			buffer.get(result);

			return new ImmutableByteArray(result);
		} catch (final CharacterCodingException e) {
			throw new IllegalArgumentException("The string does not correspond to a valid sequence of UTF-8 encoding.");
		}
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#byteArrayToString}
	 */
	public static String byteArrayToString(final ImmutableByteArray b) {
		checkNotNull(b);
		checkArgument(!b.isEmpty(), "The length of the byte array must be strictly positive.");

		final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
				// Explicitly set the error actions to REPORT to be sure no ignore nor replace action is performed.
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		// The try-catch clause implements the pseudo-code's if statement
		try {
			// Corresponds to UTF-8^-1(B)
			return decoder.decode(ByteBuffer.wrap(b.elements())).toString();
		} catch (final CharacterCodingException e) {
			throw new IllegalArgumentException("The byte array does not correspond to a valid sequence of UTF-8 encoding.");
		}
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#stringToInteger}
	 */
	public static BigInteger stringToInteger(final String s) {
		checkNotNull(s);

		// Checks that s is not empty and is a valid decimal representation.
		checkArgument(DECIMAL_PATTERN.matcher(s).matches(),
				String.format("The string to convert \"%s\" is not a valid decimal representation of a BigInteger.", s));

		// Corresponds to Decimal(S)
		return new BigInteger(s, 10);
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#integerToString(BigInteger)} )}
	 */
	public static String integerToString(final BigInteger x) {
		checkNotNull(x);
		checkArgument(x.signum() >= 0);

		// Corresponds to Decimal^-1(x)
		return x.toString(10);
	}

	/**
	 * See {@link ch.post.it.evoting.cryptoprimitives.utils.Conversions#integerToString(Integer)}
	 */
	public static String integerToString(final Integer x) {
		checkNotNull(x);
		checkArgument(x >= 0);

		// Corresponds to Decimal^-1(x)
		return Integer.toString(x, 10);
	}
}
