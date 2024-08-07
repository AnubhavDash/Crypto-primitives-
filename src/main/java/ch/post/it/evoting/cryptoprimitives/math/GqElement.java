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
package ch.post.it.evoting.cryptoprimitives.math;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegerOperationsService;

/**
 * Defines a Gq group element, ie elements of the quadratic residue group of order q and mod p.
 *
 * <p>Instances of this class are immutable.
 */
@SuppressWarnings("java:S117")
public sealed class GqElement extends GroupElement<GqGroup> permits PrimeGqElement {

	// Package private constructor without input validation. Used only for operations that provide a mathematical guarantee that the element is within the
	// group (such as multiplying two elements of the same group).
	GqElement(final BigInteger value, final GqGroup group) {
		super(value, group);
	}

	/**
	 * Returns a {@code GqElement} whose value is {@code (this * element)}.
	 *
	 * @param other the element to be multiplied by this. It must be from the same group and non-null.
	 * @return (this * element).
	 */
	public GqElement multiply(final GqElement other) {
		checkNotNull(other);
		checkArgument(this.group.equals(other.group));

		final BigInteger resultValue = BigIntegerOperationsService.modMultiply(value, other.getValue(), group.getP());
		return new GqElement(resultValue, this.group);
	}

	/**
	 * Returns a {@code GqElement} whose value is (this<sup>exponent</sup>).
	 *
	 * @param exponent the exponent to which this {@code GqElement} is to be raised. It must be a member of a group of the same order and be
	 *                 non-null.
	 * @return this<sup>exponent</sup>.
	 */
	public GqElement exponentiate(final ZqElement exponent) {
		checkNotNull(exponent);
		checkArgument(isOfSameOrderGroup(exponent));

		final BigInteger valueExponentiated = BigIntegerOperationsService.modExponentiate(value, exponent.getValue(), this.group.getP());
		return new GqElement(valueExponentiated, this.group);
	}

	private boolean isOfSameOrderGroup(final ZqElement exponent) {
		return this.group.hasSameOrderAs(exponent.getGroup());
	}

	/**
	 * Inverts the calling instance.
	 * <p>
	 * The inverse of a {@link GqElement} <i>a</i> is the element <i>a<sup>-1</sup></i> such that <i>a</i> * <i>a<sup>-1</sup></i> = 1 mod <i>p</i>.
	 * </p>
	 */
	public GqElement invert() {
		final BigInteger invertedValue = BigIntegerOperationsService.modInvert(this.getValue(), this.group.getP());
		return new GqElement(invertedValue, this.group);
	}

	/**
	 * Divides the calling instance by the given {@link GqElement}.
	 * <p>
	 * The division is done by multiplying with the inverted divisor.
	 * </p>
	 *
	 * @param divisor the element by which to divide
	 * @return the result of the division
	 */
	public GqElement divide(final GqElement divisor) {
		checkNotNull(divisor);
		checkArgument(this.group.equals(divisor.group));

		return this.multiply(divisor.invert());
	}

	@Override
	public String toString() {
		return "GqElement [value=" + value + "," + group.toString() + "]";
	}

	public static class GqElementFactory {

		private GqElementFactory() {
			// Intentionally left blank.
		}

		/**
		 * Creates a {@code GqElement}. The specified value should be an element of the group.
		 *
		 * @param value the value of the element. Must not be null and must be an element of the group.
		 * @param group the {@link GqGroup} to which this element belongs. Must be non-null.
		 * @return a new GqElement with the specified value in the given group
		 */
		public static GqElement fromValue(final BigInteger value, final GqGroup group) {
			checkNotNull(value);
			checkNotNull(group);
			checkArgument(group.isGroupMember(value), "Cannot create a GqElement with value %s as it is not an element of group %s", value, group);

			return new GqElement(value, group);
		}

		/**
		 * Creates a GqElement from a BigInteger by squaring it modulo p.
		 *
		 * @param element the BigInteger to be squared. Must be non-null.
		 * @param group   the GqGroup in which to get the new GqElement. Must be non-null.
		 * @return the squared element modulo p.
		 * @throws NullPointerException     if any of the arguments is null
		 * @throws IllegalArgumentException if the element is 0 or smaller or bigger than the group's order
		 */
		public static GqElement fromSquareRoot(final BigInteger element, final GqGroup group) {
			checkNotNull(element);
			checkNotNull(group);

			checkArgument(element.signum() > 0, "The element must be strictly greater than 0");
			checkArgument(element.compareTo(group.getQ()) < 0, "The element must be smaller than the group's order");

			final BigInteger y = BigIntegerOperationsService.modExponentiate(element, BigInteger.TWO, group.getP());
			return new GqElement(y, group);
		}

		/**
		 * Creates a GqElement from two GroupVector by computing Π<sub>i</sub> base<sub>i</sub> <sup>exponent_i</sup> mod p.
		 *
		 * @param bases     the GroupVector to be multiplied. They must be from the same group and non-null.
		 * @param exponents the GroupVector to be raised. They must be a member of a group of the same order as the bases and be non-null.
		 * @return Π<sub>i</sub> base<sub>i</sub> <sup>exponent_i</sup> mod p
		 * @throws NullPointerException     if any of the arguments is null
		 * @throws IllegalArgumentException if the exponents do not have the same group order as the bases
		 */
		public static GqElement multiModExp(final GroupVector<GqElement, GqGroup> bases, final GroupVector<ZqElement, ZqGroup> exponents) {
			checkNotNull(bases);
			checkNotNull(exponents);
			// the GroupVector constructor ensures all bases belong to the same group.
			checkArgument(exponents.getGroup().hasSameOrderAs(bases.getGroup()));

			final ImmutableList<BigInteger> basesList = bases.stream().parallel()
					.map(GqElement::getValue)
					.collect(toImmutableList());
			final ImmutableList<BigInteger> exponentsList = exponents.stream().parallel()
					.map(ZqElement::getValue)
					.collect(toImmutableList());

			return new GqElement(BigIntegerOperationsService.multiModExp(basesList, exponentsList, bases.getGroup().getP()), bases.getGroup());
		}

	}
}
