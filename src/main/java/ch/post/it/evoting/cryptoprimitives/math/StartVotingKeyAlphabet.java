/*
 * (c) Copyright 2023 Swiss Post Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
 * The start voting key alphabet. The alphabet corresponds to the Base32 lowercase version excluding padding "=" of "Table 3: The Base 32 Alphabet"
 * from RFC 4648. Moreover, the letters "l" and "o" are replaced by "8" and "9".
 */
public final class StartVotingKeyAlphabet extends Alphabet {

	private static final int SIZE = 32;
	private static final List<String> ALPHABET = List.of(

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
			// Character.toString(0x006C), // l (U+006C) --> Excluded
			Character.toString(0x006D), // m (U+006D)
			Character.toString(0x006E), // n (U+006E)
			// Character.toString(0x006F), // o (U+006F) --> Excluded
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
			Character.toString(0x0032), // 2 (U+0032)

			Character.toString(0x0033), // 3 (U+0033)
			Character.toString(0x0034), // 4 (U+0034)
			Character.toString(0x0035), // 5 (U+0035)
			Character.toString(0x0036), // 6 (U+0036)
			Character.toString(0x0037), // 7 (U+0037)

			Character.toString(0x0038), // 8 (U+0038) --> Added
			Character.toString(0x0039)  // 9 (U+0039) --> Added
	);

	private static final StartVotingKeyAlphabet INSTANCE = new StartVotingKeyAlphabet(SIZE, ALPHABET);

	private StartVotingKeyAlphabet(final int size, final List<String> alphabet) {
		super(size, alphabet);
	}

	public static StartVotingKeyAlphabet getInstance() {
		return INSTANCE;
	}
}