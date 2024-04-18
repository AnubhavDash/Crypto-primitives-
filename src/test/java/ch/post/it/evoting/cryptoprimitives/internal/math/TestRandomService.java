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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;

public class TestRandomService extends RandomService {

	public int genRandomInteger(final int lowerBound, final int upperBound) {
		checkArgument(lowerBound >= 0, "The lower bound must be non-negative.");
		checkArgument(lowerBound < upperBound, "The lower bound must be less than the upper bound.");

		return super.genRandomInteger(upperBound - lowerBound) + lowerBound;
	}

	public BigInteger genRandomIntegerOfLength(final int bitLength) {
		checkArgument(bitLength >= 0, "The bit length must be positive.");

		return super.genRandomInteger(BigInteger.TWO.pow(bitLength));
	}

}
