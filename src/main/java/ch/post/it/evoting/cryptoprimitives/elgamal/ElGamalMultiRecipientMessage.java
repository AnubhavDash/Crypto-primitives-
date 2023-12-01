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
package ch.post.it.evoting.cryptoprimitives.elgamal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.GroupVectorElement;

/**
 * Represents an ElGamal message containing multiple elements.
 *
 * <p>Instances of this class are immutable.
 */
@SuppressWarnings({ "java:S117" })
public final class ElGamalMultiRecipientMessage implements GroupVectorElement<GqGroup>, HashableList {

	private final GroupVector<GqElement, GqGroup> messageElements;

	/**
	 * Creates an {@link ElGamalMultiRecipientMessage} object.
	 *
	 * @param messageElements the group vector of Gq group message elements. Must be non-null, non-empty and not contain null elements.
	 */
	public ElGamalMultiRecipientMessage(final GroupVector<GqElement, GqGroup> messageElements) {
		this.messageElements = checkNotNull(messageElements);
		this.messageElements.forEach(Preconditions::checkNotNull);
		checkArgument(!this.messageElements.isEmpty(), "An ElGamal message must not be empty.");
	}

	@Override
	public GqGroup getGroup() {
		//A ElGamalMultiRecipientMessage is never empty
		return this.messageElements.getGroup();
	}

	/**
	 * Gets the elements composing this multi recipient message.
	 */
	public GroupVector<GqElement, GqGroup> getElements() {
		return messageElements;
	}

	@Override
	public int size() {
		return this.messageElements.size();
	}

	public GqElement get(final int i) {
		return this.messageElements.get(i);
	}

	public Stream<GqElement> stream() {
		return this.messageElements.stream();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ElGamalMultiRecipientMessage that = (ElGamalMultiRecipientMessage) o;
		return messageElements.equals(that.messageElements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageElements);
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		return this.messageElements.toHashableForm();
	}
}
