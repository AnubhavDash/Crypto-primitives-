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
package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamal;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

public class ElGamalService implements ElGamal {

	@Override
	public ElGamalMultiRecipientCiphertext getCiphertext(final ElGamalMultiRecipientMessage message, final ZqElement exponent,
			final ElGamalMultiRecipientPublicKey publicKey) {
		return ElGamalMultiRecipientCiphertexts.getCiphertext(message, exponent, publicKey);
	}

	@Override
	public ElGamalMultiRecipientCiphertext neutralElement(final int numPhi, final GqGroup group) {
		return ElGamalMultiRecipientCiphertexts.neutralElement(numPhi, group);
	}

	@Override
	public ElGamalMultiRecipientMessage getMessage(final ElGamalMultiRecipientCiphertext ciphertext,
			final ElGamalMultiRecipientPrivateKey secretKey) {
		return ElGamalMultiRecipientMessages.getMessage(ciphertext, secretKey);
	}

	@Override
	public ElGamalMultiRecipientMessage ones(final GqGroup group, final int size) {
		return ElGamalMultiRecipientMessages.ones(group, size);
	}

	@Override
	public GqGroup getEncryptionParameters(final String seed) {
		final ImmutableList<Integer> sp = PrimesInternal.getSmallPrimes();
		return new EncryptionParameters().getEncryptionParameters(seed, sp);
	}

	@Override
	public ElGamalMultiRecipientKeyPair genKeyPair(final GqGroup group, final int numElements, final Random random) {
		return ElGamalMultiRecipientKeyPair.genKeyPair(group, numElements, random);
	}

	@Override
	public ElGamalMultiRecipientKeyPair from(final ElGamalMultiRecipientPrivateKey privateKey, final GqElement generator) {
		return ElGamalMultiRecipientKeyPair.from(privateKey, generator);
	}

	@Override
	public ElGamalMultiRecipientPublicKey combinePublicKeys(final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> publicKeyList) {
		return ElGamalMultiRecipientPublicKeys.combinePublicKeys(publicKeyList);
	}
}
