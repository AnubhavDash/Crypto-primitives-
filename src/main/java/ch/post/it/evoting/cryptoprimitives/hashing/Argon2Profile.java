/*
 * Copyright 2022 Post CH Ltd
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
package ch.post.it.evoting.cryptoprimitives.hashing;

/**
 * A collection of predefined {@link Argon2Context}s.
 * <ul>
 *     <li>STANDARD - Corresponds to the "uniformly safe option" from RFC-9106</li>
 *     <li>LESS_MEMORY - Corresponds to the "uniformly safe option if less memory available" from RFC-9106</li>
 *     <li>TEST - a profile with reduced memory usage for testing purposes only</li>
 * </ul>
 */
public enum Argon2Profile {
	STANDARD(21, 4, 1),
	LESS_MEMORY(16, 4, 3),
	TEST(14, 4, 1);

	private final Argon2Context context;

	Argon2Profile(final int m, final int p, final int i) {
		this.context = new Argon2Context(m, p, i);
	}

	public Argon2Context getContext() {
		return this.context;
	}
}
