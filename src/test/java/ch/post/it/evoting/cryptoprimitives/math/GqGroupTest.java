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
package ch.post.it.evoting.cryptoprimitives.math;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;

class GqGroupTest {

	private static BigInteger p;
	private static BigInteger q;
	private static BigInteger g;
	private static GqGroup smallGroup;
	private static GqGroupGenerator smallGroupGenerator;

	@BeforeAll
	static void setUp() {

		p = BigInteger.valueOf(23);
		q = BigInteger.valueOf(11);
		g = BigInteger.TWO;
		smallGroup = new GqGroup(p, q, g);
		smallGroupGenerator = new GqGroupGenerator(smallGroup);
	}

	//Object instantiation validations

	@Test
	void testCreateGroupWithNonPrimePFails() {
		final BigInteger nonPrime = BigInteger.valueOf(22);
		assertThrows(IllegalArgumentException.class, () -> new GqGroup(nonPrime, q, g));
	}

	@Test
	void testCreateGroupWithNonPrimeQFails() {
		final BigInteger nonPrime = BigInteger.TEN;
		assertThrows(IllegalArgumentException.class, () -> new GqGroup(p, nonPrime, g));
	}

	@Test
	void testCreateGroupWithNonSafeQFails() {
		final BigInteger nonSafePrimeQ = BigInteger.valueOf(7);
		assertThrows(IllegalArgumentException.class, () -> new GqGroup(p, nonSafePrimeQ, g));
	}

	@Test
	void testCreateGroupWithNonMemberGeneratorFails() {
		final BigInteger nonMember = smallGroupGenerator.genNonMemberValue();
		assertThrows(IllegalArgumentException.class, () -> new GqGroup(p, q, nonMember));
	}

	//Methods

	@Test
	void testGroupMemberReturnsTrueForGroupMember() {
		final BigInteger member = smallGroupGenerator.genMemberValue();
		assertTrue(smallGroup.isGroupMember(member));
	}

	@Test
	void testGroupMemberReturnsFalseForNonGroupMember() {
		final BigInteger nonMember = smallGroupGenerator.genNonMemberValue();
		assertFalse(smallGroup.isGroupMember(nonMember));
	}

	@Test
	void test0IsNotAGroupMember() {
		assertFalse(smallGroup.isGroupMember(BigInteger.ZERO));
	}

	@Test
	void testPIsNotAGroupMember() {
		assertFalse(smallGroup.isGroupMember(smallGroup.getP()));
	}

	@Test
	void testNullIsNotAGroupMember() {
		assertFalse(smallGroup.isGroupMember(null));
	}

	@Test
	void testGetIdentityElementOnce() {
		final GqElement identity = GqElementFactory.fromValue(BigInteger.ONE, smallGroup);
		assertEquals(identity, smallGroup.getIdentity(), "The element returned is not the expected identity element");
	}

	@Test
	void testGetIdentityElementTwice() {
		final String errorMessage = "The %s element returned is not the expected identity element";
		final GqElement identityElement = GqElementFactory.fromValue(BigInteger.ONE, smallGroup);
		final GqElement firstIdentity = smallGroup.getIdentity();
		final GqElement secondIdentity = smallGroup.getIdentity();

		assertEquals(identityElement, firstIdentity, String.format(errorMessage, "first"));
		assertEquals(identityElement, secondIdentity, String.format(errorMessage, "second"));
	}

	@Test
	void testGetQ() {
		assertEquals(q, smallGroup.getQ(), "The Q element is not the expected one");
	}

	@Test
	void testGetG() {
		assertEquals(g, smallGroup.getGenerator().getValue(), "The generator element is not the expected one");
	}

	@Test
	void testEqualsDifferentObjectType() {
		final String notAGroup = "I am not a group";
		final String errorMessage = "Expected that objects would not be equals";
		assertNotEquals(new GqGroup(p, q, g), notAGroup, errorMessage);
	}

	@Test
	void testEqualsTrue() {
		final String errorMessage = "Expected that objects would be equals";
		assertEquals(new GqGroup(p, q, g), smallGroup, errorMessage);
	}
}
