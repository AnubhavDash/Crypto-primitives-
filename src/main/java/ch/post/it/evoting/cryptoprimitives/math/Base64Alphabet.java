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
package ch.post.it.evoting.cryptoprimitives.math;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;

/**
 * The Base 64 Alphabet excluding padding "=" of "Table 1: The Base 64 Alphabet" from RFC 4648.
 */
public final class Base64Alphabet extends Alphabet {

	private static final int SIZE = 64;
	private static final ImmutableList<String> ALPHABET = ImmutableList.of(

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
			Character.toString(0x0061), // a (U+0061)
			Character.toString(0x0062), // b (U+0062)
			Character.toString(0x0063), // c (U+0063)
			Character.toString(0x0064), // d (U+0064)
			Character.toString(0x0065), // e (U+0065)
			Character.toString(0x0066), // f (U+0066)
			Character.toString(0x0067), // g (U+0067)
			Character.toString(0x0068), // h (U+0068)

			Character.toString(0x0069), // i (U+0069)
			Character.toString(0x006A), // j (U+006A)
			Character.toString(0x006B), // k (U+006B)
			Character.toString(0x006C), // l (U+006C)
			Character.toString(0x006D), // m (U+006D)
			Character.toString(0x006E), // n (U+006E)
			Character.toString(0x006F), // o (U+006F)
			Character.toString(0x0070), // p (U+0070)
			Character.toString(0x0071), // q (U+0071)
			Character.toString(0x0072), // r (U+0072)
			Character.toString(0x0073), // s (U+0073)
			Character.toString(0x0074), // t (U+0074)
			Character.toString(0x0075), // u (U+0075)
			Character.toString(0x0076), // v (U+0076)
			Character.toString(0x0077), // w (U+0077)
			Character.toString(0x0078), // x (U+0078)
			Character.toString(0x0079), // y (U+0079)

			Character.toString(0x007A), // z (U+007A)
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
			Character.toString(0x002B), // + (U+002B)
			Character.toString(0x002F)  // / (U+002F)
	);

	private static final Base64Alphabet INSTANCE = new Base64Alphabet(SIZE, ALPHABET);

	private Base64Alphabet(final int size, final ImmutableList<String> alphabet) {
		super(size, alphabet);
	}

	public static Base64Alphabet getInstance() {
		return INSTANCE;
	}
}