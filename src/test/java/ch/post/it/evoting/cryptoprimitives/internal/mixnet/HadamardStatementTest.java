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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.mixnet.HadamardStatement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;

class HadamardStatementTest extends TestGroupSetup {

	private GroupVector<GqElement, GqGroup> commitmentsA;
	private GqElement commitmentB;

	@BeforeEach
	void setup() {
		final int n = randomService.genRandomInteger(10) + 1;
		commitmentsA = gqGroupGenerator.genRandomGqElementVector(n);
		commitmentB = gqGroupGenerator.genMember();
	}

	@Test
	@DisplayName("Constructing a Hadamard statement with valid input does not throw")
	void constructStatement() {
		assertDoesNotThrow(() -> new HadamardStatement(commitmentsA, commitmentB));
	}

	@Test
	@DisplayName("Constructing a Hadamard statement with null arguments should throw a NullPointerException")
	void constructStatementWithNullArguments() {
		assertAll(
				() -> assertThrows(NullPointerException.class, () -> new HadamardStatement(null, commitmentB)),
				() -> assertThrows(NullPointerException.class, () -> new HadamardStatement(commitmentsA, null))
		);
	}

	@Test
	@DisplayName("Constructing a Hadamard statement with commitments A and commitment b of different groups should throw")
	void constructStatementWithCommitmentsFromDifferentGroups() {
		commitmentB = new GqGroupGenerator(otherGqGroup).genMember();
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> new HadamardStatement(commitmentsA, commitmentB));
		assertEquals("The commitments A and commitment b must have the same group.", exception.getMessage());
	}
}
