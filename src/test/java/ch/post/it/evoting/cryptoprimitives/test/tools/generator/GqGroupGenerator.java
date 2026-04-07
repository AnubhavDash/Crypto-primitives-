/*
 * Copyright 2026 Swiss Post Ltd
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
package ch.post.it.evoting.cryptoprimitives.test.tools.generator;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableSet.toImmutableSet;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.test.tools.generator.GroupVectorElementGenerator.generateElementList;
import static ch.post.it.evoting.cryptoprimitives.test.tools.generator.GroupVectorElementGenerator.generateElementMatrix;

import java.math.BigInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableSet;
import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.internal.math.TestRandomService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

/**
 * Brute force the generation of group members.
 */
public class GqGroupGenerator {

	private static final BigInteger MAX_GROUP_SIZE = BigInteger.valueOf(1000);

	private final GqGroup group;
	private final TestRandomService randomService;

	public GqGroupGenerator(final GqGroup group) {
		this.group = group;
		this.randomService = new TestRandomService();
	}

	/**
	 * Get all members of the group.
	 */
	public ImmutableSet<BigInteger> getMembers() {
		if (group.getP().compareTo(MAX_GROUP_SIZE) > 0) {
			throw new IllegalArgumentException("It would take too much time to generate all the group members for such a large group.");
		}

		return integersModP()
						.map(bi -> bi.modPow(BigInteger.TWO, group.getP()))
				.filter(e -> !e.equals(BigInteger.ZERO))
				.collect(toImmutableSet());
	}

	/**
	 * Get all prime members of the group different from the group generator.
	 */
	public ImmutableSet<Integer> getSmallPrimeMembers() {
		final ImmutableSet<BigInteger> members = getMembers();
		return members.stream()
				.map(BigInteger::intValueExact)
				.filter(e -> !e.equals(group.getGenerator().getValue().intValueExact()))
				.collect(toImmutableSet());
	}

	/**
	 * Get all non members of the group smaller than p.
	 */
	public ImmutableSet<BigInteger> getNonMembers() {
		if (group.getP().compareTo(MAX_GROUP_SIZE) > 0) {
			throw new IllegalArgumentException("It would take too much time to generate all the group members for such a large group.");
		}

		final ImmutableSet<BigInteger> members = getMembers();
		return integersModP()
				.filter(e -> !members.contains(e))
				.collect(toImmutableSet());
	}

	/**
	 * Generate a BigInteger value that belongs to the group.
	 */
	public BigInteger genMemberValue() {
		BigInteger member;
		do {
			final BigInteger randomInteger = randomBigInteger(group.getP().bitLength());
			member = randomInteger.modPow(BigInteger.TWO, group.getP());
		} while (member.signum() <= 0 || member.compareTo(group.getP()) >= 0);
		return member;
	}

	/**
	 * Generate a GqElement belonging to the group.
	 */
	public GqElement genMember() {
		return GqElementFactory.fromValue(genMemberValue(), group);
	}

	/**
	 * Generate a PrimeGqElement belonging to the group.
	 */
	public PrimeGqElement genSmallPrimeMember() {
		final BigInteger primeMember = Generators.genWhile(this::genMemberValue,
				member -> member.equals(group.getGenerator().getValue()) || !PrimesInternal.isSmallPrime(member.intValueExact()));
		return PrimeGqElement.PrimeGqElementFactory.fromValue(primeMember.intValueExact(), group);
	}

	/**
	 * Generate a BigInteger value that does not belong to the group.
	 */
	public BigInteger genNonMemberValue() {
		BigInteger nonMember;
		do {
			nonMember = randomBigInteger(group.getP().bitLength());
		} while (nonMember.signum() <= 0 || nonMember.compareTo(group.getP()) >= 0 || group.isGroupMember(nonMember));
		return nonMember;
	}

	/**
	 * Generate a non identity member of the group.
	 */
	public GqElement genNonIdentityMember() {
		return Generators.genWhile(this::genMember, member -> member.equals(group.getIdentity()));
	}

	/**
	 * Generate a non identity, non generator member of the group.
	 */
	public GqElement genNonIdentityNonGeneratorMember() {
		return Generators.genWhile(this::genMember, member -> member.equals(group.getIdentity()) || member.equals(group.getGenerator()));
	}

	/**
	 * Generate a random {@link GroupVector} of {@link GqElement} in this {@code group}.
	 *
	 * @param numElements the number of elements to generate.
	 * @return a vector of {@code numElements} random {@link GqElement}.
	 */
	public GroupVector<GqElement, GqGroup> genRandomGqElementVector(final int numElements) {
		return generateElementList(numElements, this::genMember);
	}

	public GroupMatrix<GqElement, GqGroup> genRandomGqElementMatrix(final int numRows, final int numColumns) {
		return GroupMatrix.fromRows(generateElementMatrix(numRows, numColumns, this::genMember));
	}

	private BigInteger randomBigInteger(final int bitLength) {
		return randomService.genRandomIntegerOfLength(bitLength);
	}

	private Stream<BigInteger> integersModP() {
		return IntStream.range(1, group.getP().intValue()).mapToObj(BigInteger::valueOf);
	}

	public GqElement otherElement(final GqElement element) {
		return Generators.genWhile(this::genMember, element::equals);
	}
}
