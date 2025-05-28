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
 * An extended latin alphabet including common european special characters.
 */
public final class LatinAlphabet extends Alphabet {

	private static final int SIZE = 141;
	private static final ImmutableList<String> ALPHABET = ImmutableList.of(

			Character.toString(0x0023), // # (U+0023)
			Character.toString(0x0020), //   (U+0020)
			Character.toString(0x0027), // ' (U+0027)
			Character.toString(0x0028), // ( (U+0028)
			Character.toString(0x0029), // ) (U+0029)
			Character.toString(0x002C), // , (U+002C)
			Character.toString(0x002D), // - (U+002D)
			Character.toString(0x002E), // . (U+002E)
			Character.toString(0x002F), // / (U+002F)
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
			Character.toString(0x00A2), // ¢ (U+00A2)
			Character.toString(0x0160), // Š (U+0160)
			Character.toString(0x0161), // š (U+0161)
			Character.toString(0x017D), // Ž (U+017D)
			Character.toString(0x017E), // ž (U+017E)
			Character.toString(0x0152), // Œ (U+0152)
			Character.toString(0x0153), // œ (U+0153)
			Character.toString(0x0178), // Ÿ (U+0178)
			Character.toString(0x00C0), // À (U+00C0)

			Character.toString(0x00C1), // Á (U+00C1)
			Character.toString(0x00C2), // Â (U+00C2)
			Character.toString(0x00C3), // Ã (U+00C3)
			Character.toString(0x00C4), // Ä (U+00C4)
			Character.toString(0x00C5), // Å (U+00C5)
			Character.toString(0x00C6), // Æ (U+00C6)
			Character.toString(0x00C7), // Ç (U+00C7)
			Character.toString(0x00C8), // È (U+00C8)
			Character.toString(0x00C9), // É (U+00C9)
			Character.toString(0x00CA), // Ê (U+00CA)

			Character.toString(0x00CB), // Ë (U+00CB)
			Character.toString(0x00CC), // Ì (U+00CC)
			Character.toString(0x00CD), // Í (U+00CD)
			Character.toString(0x00CE), // Î (U+00CE)
			Character.toString(0x00CF), // Ï (U+00CF)
			Character.toString(0x00D0), // Ð (U+00D0)
			Character.toString(0x00D1), // Ñ (U+00D1)
			Character.toString(0x00D2), // Ò (U+00D2)
			Character.toString(0x00D3), // Ó (U+00D3)
			Character.toString(0x00D4), // Ô (U+00D4)

			Character.toString(0x00D5), // Õ (U+00D5)
			Character.toString(0x00D6), // Ö (U+00D6)
			Character.toString(0x00D8), // Ø (U+00D8)
			Character.toString(0x00D9), // Ù (U+00D9)
			Character.toString(0x00DA), // Ú (U+00DA)
			Character.toString(0x00DB), // Û (U+00DB)
			Character.toString(0x00DC), // Ü (U+00DC)
			Character.toString(0x00DD), // Ý (U+00DD)
			Character.toString(0x00DE), // Þ (U+00DE)
			Character.toString(0x00DF), // ß (U+00DF)

			Character.toString(0x00E0), // à (U+00E0)
			Character.toString(0x00E1), // á (U+00E1)
			Character.toString(0x00E2), // â (U+00E2)
			Character.toString(0x00E3), // ã (U+00E3)
			Character.toString(0x00E4), // ä (U+00E4)
			Character.toString(0x00E5), // å (U+00E5)
			Character.toString(0x00E6), // æ (U+00E6)
			Character.toString(0x00E7), // ç (U+00E7)
			Character.toString(0x00E8), // è (U+00E8)

			Character.toString(0x00E9), // é (U+00E9)
			Character.toString(0x00EA), // ê (U+00EA)
			Character.toString(0x00EB), // ë (U+00EB)
			Character.toString(0x00EC), // ì (U+00EC)
			Character.toString(0x00ED), // í (U+00ED)
			Character.toString(0x00EE), // î (U+00EE)
			Character.toString(0x00EF), // ï (U+00EF)
			Character.toString(0x00F0), // ð (U+00F0)
			Character.toString(0x00F1), // ñ (U+00F1)
			Character.toString(0x00F2), // ò (U+00F2)

			Character.toString(0x00F3), // ó (U+00F3)
			Character.toString(0x00F4), // ô (U+00F4)
			Character.toString(0x00F5), // õ (U+00F5)
			Character.toString(0x00F6), // ö (U+00F6)
			Character.toString(0x00F8), // ø (U+00F8)
			Character.toString(0x00F9), // ù (U+00F9)
			Character.toString(0x00FA), // ú (U+00FA)
			Character.toString(0x00FB), // û (U+00FB)
			Character.toString(0x00FC), // ü (U+00FC)
			Character.toString(0x00FD), // ý (U+00FD)
			Character.toString(0x00FE), // þ (U+00FE)

			Character.toString(0x00FF)  // ÿ (U+00FF)
	);

	private static final LatinAlphabet INSTANCE = new LatinAlphabet(SIZE, ALPHABET);

	private LatinAlphabet(final int size, final ImmutableList<String> alphabet) {
		super(size, alphabet);
	}

	public static LatinAlphabet getInstance() {
		return INSTANCE;
	}
}
