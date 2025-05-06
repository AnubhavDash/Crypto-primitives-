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

import java.util.List;

/**
 * The Base 32 Alphabet excluding padding "=" of "Table 3: The Base 32 Alphabet" from RFC 4648.
 */
public final class Base32Alphabet extends Alphabet {

	private static final int SIZE = 32;
	private static final List<String> ALPHABET = List.of(

			Character.toString(0x0041), // A (U+0041)
			Character.toString(0x0042), // B (U+0042)
			Character.toString(0x0043), // C (U+0043)
			Character.toString(0x0044), // D (U+0044)
			Character.toString(0x0045), // E (U+0045)
			Character.toString(0x0046), // F (U+0046)
			Character.toString(0x0047), // G (U+0047)
			Character.toString(0x0048), // H (U+0048)
			Character.toString(0x0049), // I (U+0049)

			Character.toString(0x004A), // J (U+004A)
			Character.toString(0x004B), // K (U+004B)
			Character.toString(0x004C), // L (U+004C)
			Character.toString(0x004D), // M (U+004D)
			Character.toString(0x004E), // N (U+004E)
			Character.toString(0x004F), // O (U+004F)
			Character.toString(0x0050), // P (U+0050)
			Character.toString(0x0051), // Q (U+0051)
			Character.toString(0x0052), // R (U+0052)

			Character.toString(0x0053), // S (U+0053)
			Character.toString(0x0054), // T (U+0054)
			Character.toString(0x0055), // U (U+0055)
			Character.toString(0x0056), // V (U+0056)
			Character.toString(0x0057), // W (U+0057)
			Character.toString(0x0058), // X (U+0058)
			Character.toString(0x0059), // Y (U+0059)
			Character.toString(0x005A), // Z (U+005A)
			Character.toString(0x0032), // 2 (U+0032)

			Character.toString(0x0033), // 3 (U+0033)
			Character.toString(0x0034), // 4 (U+0034)
			Character.toString(0x0035), // 5 (U+0035)
			Character.toString(0x0036), // 6 (U+0036)
			Character.toString(0x0037)  // 7 (U+0037)
	);

	private static final Base32Alphabet INSTANCE = new Base32Alphabet(SIZE, ALPHABET);

	private Base32Alphabet(final int size, final List<String> alphabet) {
		super(size, alphabet);
	}

	public static Base32Alphabet getInstance() {
		return INSTANCE;
	}
}