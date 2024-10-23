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
package ch.post.it.evoting.cryptoprimitives.mixnet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

/**
 * Represents the result of a re-encrypting shuffle operation. It contains the re-encrypted ciphertexts, the list of exponents used for re-encryption
 * and the permutation used for shuffling.
 * <p>
 * Instances of this class are immutable.
 */
public record Shuffle(GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts,
					  Permutation permutation, GroupVector<ZqElement, ZqGroup> reEncryptionExponents) {
	public static final Shuffle EMPTY = new Shuffle(GroupVector.empty(), Permutation.EMPTY, GroupVector.empty());

	public Shuffle {
		checkNotNull(ciphertexts);
		checkNotNull(permutation);
		checkNotNull(reEncryptionExponents);

		final int N = permutation.size();

		checkArgument(ciphertexts.size() == N, "Shuffle ciphertext vector's size must be equal to the permutation size. [size: %s, N: %s]", ciphertexts.size(), N);
		checkArgument(reEncryptionExponents.size() == N, "Re-encryption exponents vector's size must be equal to the permutation size. [size: %s, N: %s]", reEncryptionExponents.size(), N);

		if (!ciphertexts.isEmpty()) {
			checkArgument(ciphertexts.getGroup().hasSameOrderAs(reEncryptionExponents.getGroup()),
					"Ciphertexts and re-encryption exponents must have groups of same order.");
		}
	}

	public GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getCiphertexts() {
		return this.ciphertexts;
	}

	public Permutation getPermutation() {
		return permutation;
	}

	public GroupVector<ZqElement, ZqGroup> getReEncryptionExponents() {
		return reEncryptionExponents;
	}
}
