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
package ch.post.it.evoting.cryptoprimitives.elgamal;

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;

import java.math.BigInteger;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

public class ElGamalUtils {

	//Convert a matrix of values to ciphertexts
	public static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> valuesToCiphertext(final Stream<ImmutableList<Integer>> ciphertextValues,
			final GqGroup group) {
		return ciphertextValues
				.map(values -> values.stream()
						.map(BigInteger::valueOf)
						.map(value -> GqElement.GqElementFactory.fromValue(value, group))
						.collect(toGroupVector()))
				.map(values -> ElGamalMultiRecipientCiphertext.create(values.get(0), values.subVector(1, values.size())))
				.collect(toGroupVector());
	}
}
