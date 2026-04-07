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

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.internal.utils.ConversionsInternal;

public interface Conversions {

	/**
	 * Converts a byte array to its BigInteger equivalent.
	 *
	 * @param bytes B, the byte array to convert. Must be non-null.
	 * @return a BigInteger corresponding to the provided byte array representation.
	 * @throws NullPointerException     if the byte array is null
	 */
	static BigInteger byteArrayToInteger(final ImmutableByteArray bytes) {
		return ConversionsInternal.byteArrayToInteger(bytes);
	}

	/**
	 * Converts a {@link BigInteger} to a byte array representation.
	 *
	 * @param x the non-negative BigInteger to convert. Must be non-null.
	 * @return the byte array representation of this BigInteger.
	 * @throws NullPointerException     if x is null
	 * @throws IllegalArgumentException if x is negative.
	 */
	static ImmutableByteArray integerToByteArray(final BigInteger x) {
		return ConversionsInternal.integerToByteArray(x);
	}

	/**
	 * Converts a BigInteger to a byte array representation of desired length.
	 *
	 * @param x the non-negative BigInteger to convert. Must be non-null.
	 * @param n the desired length in bytes of the resulting byte array.
	 * @return the byte array representation of this BigInteger.
	 * @throws IllegalArgumentException if n is smaller than the byte length of x
	 */
	static ImmutableByteArray integerToFixedLengthByteArray(final BigInteger x, final int n) {
		return ConversionsInternal.integerToFixedLengthByteArray(x, n);
	}

	/**
	 * Converts a string to a byte array representation.
	 *
	 * @param s S, the string to convert. Must be non-null.
	 * @return the byte array representation of the string.
	 * @throws NullPointerException     if the string s is null
	 * @throws IllegalArgumentException if the string s is not a valid UTF-8.
	 */
	static ImmutableByteArray stringToByteArray(final String s) {
		return ConversionsInternal.stringToByteArray(s);
	}

	/**
	 * Converts a byte array to a {@link String} representation.
	 *
	 * @param b B, the byte array to convert.
	 * @return the string representation of the byte array.
	 * @throws NullPointerException     if the byte array is null.
	 * @throws IllegalArgumentException if the byte array does not correspond to a valid sequence of UTF-8 encoding.
	 */
	static String byteArrayToString(final ImmutableByteArray b) {
		return ConversionsInternal.byteArrayToString(b);
	}

	/**
	 * Converts a decimal {@link String} representation to a {@link BigInteger} representation.
	 *
	 * @param s S, the decimal {@link String} representation to convert. Not Null, not empty, no whitespace, and all characters must be decimal characters.
	 * @return x, the {@link BigInteger} representation of the string.
	 * @throws NullPointerException     if the string s is null
	 * @throws IllegalArgumentException if the string s is empty, contains whitespace, or is not a valid non-negative decimal representation of a BigInteger.
	 */
	static BigInteger stringToInteger(final String s) {
		return ConversionsInternal.stringToInteger(s);
	}

	/**
	 * Converts a {@link BigInteger} representation to a decimal {@link String} representation.
	 *
	 * @param x, the {@link BigInteger} representation to convert. Not Null, non-negative.
	 * @return S, the decimal {@link String} representation of the bigInteger.
	 * @throws NullPointerException     if the bigInteger is null
	 * @throws IllegalArgumentException if the bigInteger is negative
	 */
	static String integerToString(final BigInteger x) {
		return ConversionsInternal.integerToString(x);
	}

	/**
	 * Converts an {@link Integer} representation to a decimal {@link String} representation.
	 *
	 * @param x, the {@link Integer} representation to convert. Not Null, non-negative.
	 * @return S, the decimal {@link String} representation of the Integer.
	 * @throws NullPointerException     if x is null.
	 * @throws IllegalArgumentException if x is negative.
	 */
	static String integerToString(final Integer x) {
		return ConversionsInternal.integerToString(x);
	}
}
