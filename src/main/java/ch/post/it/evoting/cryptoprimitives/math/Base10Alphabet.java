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
 * The Base 10 Alphabet.
 */
public final class Base10Alphabet extends Alphabet {

	private static final int SIZE = 10;
	private static final List<String> ALPHABET = List.of(

			Character.toString(0x0030), // 0 (U+0030)
			Character.toString(0x0031), // 1 (U+0031)
			Character.toString(0x0032), // 2 (U+0032)
			Character.toString(0x0033), // 3 (U+0033)
			Character.toString(0x0034), // 4 (U+0034)
			Character.toString(0x0035), // 5 (U+0035)
			Character.toString(0x0036), // 6 (U+0036)
			Character.toString(0x0037), // 7 (U+0037)
			Character.toString(0x0038), // 8 (U+0038)
			Character.toString(0x0039)  // 9 (U+0039)
	);

	private static final Base10Alphabet INSTANCE = new Base10Alphabet(SIZE, ALPHABET);

	private Base10Alphabet(final int size, final List<String> alphabet) {
		super(size, alphabet);
	}

	public static Base10Alphabet getInstance() {
		return INSTANCE;
	}
}