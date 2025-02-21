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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import static ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientCiphertexts.getCiphertext;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.elgamal.ElGamalMultiRecipientMessages;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.Permutation;
import ch.post.it.evoting.cryptoprimitives.mixnet.Shuffle;

/**
 * Re-encrypting shuffle service.
 *
 * <p>This class is thread safe.</p>
 */
@SuppressWarnings("java:S117")
public class ShuffleService {

	private final RandomService randomService;
	private final PermutationService permutationService;

	ShuffleService(final RandomService randomService, final PermutationService permutationService) {
		this.randomService = randomService;
		this.permutationService = permutationService;
	}

	/**
	 * Shuffles and re-encrypts a list of ciphertext with the given key.
	 *
	 * @param ciphertexts the ciphertexts to re-encrypt and shuffle. Must be non null.
	 * @param publicKey   the public key with which to re-encrypt the ciphertexts. Must be non null.
	 * @return a {@link Shuffle} with the result of the re-encrypting shuffle.
	 */
	Shuffle genShuffle(final List<ElGamalMultiRecipientCiphertext> ciphertexts, final ElGamalMultiRecipientPublicKey publicKey) {
		// Input.
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> C = GroupVector.from(checkNotNull(ciphertexts).stream()
				.map(Preconditions::checkNotNull)
				.toList());
		final ElGamalMultiRecipientPublicKey pk = checkNotNull(publicKey);
		final int N = C.size();
		final int l = C.getElementSize();
		final int k = publicKey.size();

		if (C.isEmpty()) {
			return Shuffle.EMPTY;
		}

		checkArgument(C.getGroup().equals(publicKey.getGroup()));
		final GqGroup group = C.getGroup();
		final ZqGroup exponentGroup = ZqGroup.sameOrderAs(group);
		final BigInteger q = exponentGroup.getQ();

		// Require.
		checkArgument(0 < l && l <= k, "The ciphertexts element size must be positive and at most the public key size. [l: %s, k: %s]", l, k);

		// Operation.
		final Permutation pi = this.permutationService.genPermutation(N);

		final ElGamalMultiRecipientMessage one = ElGamalMultiRecipientMessages.ones(group, l);
		final List<IntermediaryResult> intermediaryResults = IntStream.range(0, N).parallel()
				.mapToObj(i -> {
					final ZqElement r_i = ZqElement.create(randomService.genRandomInteger(q), exponentGroup);
					final ElGamalMultiRecipientCiphertext e = getCiphertext(one, r_i, pk);
					final ElGamalMultiRecipientCiphertext C_i_prime = e.getCiphertextProduct(C.get(pi.get(i)));
					return new IntermediaryResult(C_i_prime, r_i);
				})
				.toList();

		// Output.
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> C_prime = intermediaryResults.stream()
				.map(IntermediaryResult::C_i_prime)
				.collect(GroupVector.toGroupVector());
		final GroupVector<ZqElement, ZqGroup> r = intermediaryResults.stream()
				.map(IntermediaryResult::r_i)
				.collect(GroupVector.toGroupVector());
		return new Shuffle(C_prime, pi, r);
	}

	private record IntermediaryResult(ElGamalMultiRecipientCiphertext C_i_prime, ZqElement r_i) {}
}
