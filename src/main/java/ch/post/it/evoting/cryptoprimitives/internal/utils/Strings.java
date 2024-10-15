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

public final class Strings {

	private Strings() {
		// Intentionally left blank.
	}

	public static String truncate(final String string, final int length) {

		final String S = checkNotNull(string);
		final int u = S.length();
		final int l = length;

		checkArgument(l >= 0, "The input length must be positive. [l: %s]", l);

		// Operation. This implementation yields the same result as the specification's pseudocode,
		// and we have a corresponding unit test that asserts the equivalence of the two implementations.
		final int m = Math.min(u, l);
		return S.substring(0, m);
	}
}
