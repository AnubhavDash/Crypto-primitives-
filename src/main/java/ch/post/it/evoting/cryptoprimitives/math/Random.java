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
package ch.post.it.evoting.cryptoprimitives.math;

import java.math.BigInteger;
import java.util.List;

/**
 * Interface exposing all methods that need to be accessed outside of crypto-primitives.
 */
public interface Random {

	/**
	 * Generates a random BigInteger between 0 (incl.) and {@code upperBound} (excl.).
	 *
	 * @param upperBound m, the upper bound. Must be non null and strictly positive.
	 * @return A random BigInteger <code>r s.t. 0 &le; r &lt; m</code>.
	 */
	BigInteger genRandomInteger(final BigInteger upperBound);

	/**
	 * Generates a random int between 0 (incl.) and {@code upperBound} (excl.).
	 *
	 * @param upperBound m, the upper bound. Must be strictly positive.
	 * @return A random int <code>r s.t. 0 &le; r &lt; m</code>.
	 */
	int genRandomInteger(final int upperBound);

	/**
	 * Generates a list of unique decimal strings.
	 * <p>
	 * Each string in the list is guaranteed to have a different value. Strings that were generated in different calls of this method, might have the
	 * same value.
	 * </p>
	 *
	 * @param desiredCodeLength   l, the desired length of each code. Must be strictly positive.
	 * @param numberOfUniqueCodes n, the number of unique codes. Must be strictly positive.
	 * @return a list of unique decimal strings.
	 */
	List<String> genUniqueDecimalStrings(final int desiredCodeLength, final int numberOfUniqueCodes);

	/**
	 * Generates a random string of length &#119897; of the given alphabet.
	 *
	 * @param length   &#119897; &#8712; &#8469;<sup>+</sup>, the desired length of string. Must be strictly positive.
	 * @param alphabet &#120120;=(&#119878;<sub>0</sub>,...,&#119878;<sub>&#119896;-1</sub>), the alphabet from which to choose the string. Must be
	 *                 non-null.
	 * @return &#119878;' &#8712; (&#120120;)<sup>&#119897;</sup>, a random string of length &#119897; of the given alphabet.
	 * @throws NullPointerException     if &#120120; is null.
	 * @throws IllegalArgumentException if &#119897; is not strictly positive.
	 */
	@SuppressWarnings("java:S117")
	public String genRandomString(final int length, final Alphabet alphabet);
}
