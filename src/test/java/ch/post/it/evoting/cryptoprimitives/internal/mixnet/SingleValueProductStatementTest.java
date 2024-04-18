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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.SingleValueProductStatement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;

@DisplayName("Instantiating a SingleValueProductStatement should...")
class SingleValueProductStatementTest extends TestGroupSetup {

	private static final int NUM_ELEMENTS = 5;

	private GqElement commitment;
	private ZqElement product;

	@BeforeEach
	void setup() {
		final TestCommitmentKeyGenerator ckGenerator = new TestCommitmentKeyGenerator(gqGroup);
		final CommitmentKey commitmentKey = ckGenerator.genCommitmentKey(NUM_ELEMENTS);

		final GroupVector<ZqElement, ZqGroup> elements = zqGroupGenerator.genRandomZqElementVector(NUM_ELEMENTS);
		final ZqElement randomness = ZqElement.create(randomService.genRandomInteger(zqGroup.getQ()), zqGroup);
		product = elements.stream().reduce(ZqElement.create(BigInteger.ONE, zqGroup), ZqElement::multiply);
		commitment = CommitmentService.getCommitment(elements, randomness, commitmentKey);
	}

	@Test
	@DisplayName("throw a NullPointerException when passed null arguments")
	void constructSingleValueProductStatementWithNullThrows() {
		assertThrows(NullPointerException.class, () -> new SingleValueProductStatement(null, product));
		assertThrows(NullPointerException.class, () -> new SingleValueProductStatement(commitment, null));
	}

	@Test
	@DisplayName("throw an IllegalArgumentException when the commitment and the product have different orders")
	void constructSingleValueProductStatementWithCommitmentAndProductDifferentQThrows() {
		final GqGroup differentGqGroup = GroupTestData.getDifferentGqGroup(commitment.getGroup());
		final GqGroupGenerator generator = new GqGroupGenerator(differentGqGroup);
		final GqElement differentCommitment = generator.genMember();
		assertThrows(IllegalArgumentException.class, () -> new SingleValueProductStatement(differentCommitment, product));
	}

	@Test
	void testEquals() {
		final SingleValueProductStatement singleValueProdStatement1 = new SingleValueProductStatement(commitment, product);
		final SingleValueProductStatement singleValueProdStatement2 = new SingleValueProductStatement(commitment, product);
		final ZqElement otherProduct = product.add(ZqElement.create(BigInteger.ONE, product.getGroup()));
		final SingleValueProductStatement singleValueProdStatement3 = new SingleValueProductStatement(commitment, otherProduct);

		assertEquals(singleValueProdStatement1, singleValueProdStatement1);
		assertEquals(singleValueProdStatement1, singleValueProdStatement2);
		assertNotEquals(singleValueProdStatement1, singleValueProdStatement3);
		assertNotEquals(null, singleValueProdStatement3);
	}
}
