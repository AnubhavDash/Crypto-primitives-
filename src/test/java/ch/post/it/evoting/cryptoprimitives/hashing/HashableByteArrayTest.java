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
package ch.post.it.evoting.cryptoprimitives.hashing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;

@DisplayName("A HashableByteArray")
class HashableByteArrayTest {

	@Test
	@DisplayName("correctly makes a copy of the input byte array and toHashableForm array")
	void hashableByteArrayFromMakesCopy() {
		final ImmutableByteArray bytes = ImmutableByteArray.of((byte) 0b01, (byte) 0b10, (byte) 0b11);
		final ImmutableByteArray expected = ImmutableByteArray.of((byte) 0b01, (byte) 0b10, (byte) 0b11);

		final HashableByteArray hashableByteArray = HashableByteArray.from(bytes);

		assertEquals(expected, hashableByteArray.toHashableForm());

		// Modify original byte array.
		bytes.elements()[0] = 0b11;

		assertEquals(expected, hashableByteArray.toHashableForm());

		// Modify toHashableForm array.
		hashableByteArray.toHashableForm().elements()[0] = 0b11;

		assertEquals(expected, hashableByteArray.toHashableForm());
	}

}
