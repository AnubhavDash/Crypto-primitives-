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
package ch.post.it.evoting.cryptoprimitives.math;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * The Base 16 Alphabet of "Table 5: The Base 16 Alphabet" from RFC 4648.
 */
public final class Base16Alphabet extends Alphabet {

	private static final ImmutableList<String> ALPHABET = ImmutableList.of(

			Character.toString(0x0030), // 0 (U+0030)
			Character.toString(0x0031), // 1 (U+0031)
			Character.toString(0x0032), // 2 (U+0032)
			Character.toString(0x0033), // 3 (U+0033)

			Character.toString(0x0034), // 4 (U+0034)
			Character.toString(0x0035), // 5 (U+0035)
			Character.toString(0x0036), // 6 (U+0036)
			Character.toString(0x0037), // 7 (U+0037)

			Character.toString(0x0038), // 8 (U+0038)
			Character.toString(0x0039), // 9 (U+0039)
			Character.toString(0x0041), // A (U+0041)
			Character.toString(0x0042), // B (U+0042)

			Character.toString(0x0043), // C (U+0043)
			Character.toString(0x0044), // D (U+0044)
			Character.toString(0x0045), // E (U+0045)
			Character.toString(0x0046)  // F (U+0046)
	);

	private static final Base16Alphabet INSTANCE = new Base16Alphabet(ALPHABET);

	private Base16Alphabet(final ImmutableList<String> alphabet) {
		super(16, alphabet);
	}

	public static Base16Alphabet getInstance() {
		return INSTANCE;
	}
}
