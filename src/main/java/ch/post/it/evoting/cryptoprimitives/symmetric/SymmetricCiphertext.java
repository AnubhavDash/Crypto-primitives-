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
package ch.post.it.evoting.cryptoprimitives.symmetric;

import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

/**
 * A symmetric ciphertext composed of a ciphertext and nonce.
 *
 * <p>Instances of this class are immutable.</p>
 */
public record SymmetricCiphertext(ImmutableByteArray ciphertext, ImmutableByteArray nonce) {

	public SymmetricCiphertext {
		checkNotNull(ciphertext);
		checkNotNull(nonce);
	}
}
