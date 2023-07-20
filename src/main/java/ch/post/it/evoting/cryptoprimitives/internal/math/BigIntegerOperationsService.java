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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.verificatum.vmgj.VMG;

/**
 * <p>This class is thread-safe.</p>
 */
public class BigIntegerOperationsService {

	private static final Logger LOG = LoggerFactory.getLogger(BigIntegerOperationsService.class);
	private static final BigIntegerOperations bigIntegerOperations;

	static {
		if (VMG.checkLoaded()) {
			LOG.info("Verificatum Multiplicative Groups Library for Java (VMGJ)  is installed and ready to use");
			bigIntegerOperations = new BigIntegerOperationsVMGJ();
		} else {
			LOG.warn("Verificatum Multiplicative Groups Library for Java (VMGJ)  is not installed, some native code optimizations are not available, "
					+ "integer operations will now take longer. Verify that the libraries GMP, GMPMEE and VMGJ are installed and referenced in the java.library.path");
			bigIntegerOperations = new BigIntegerOperationsJava();
		}
	}

	private BigIntegerOperationsService() {
		throw new UnsupportedOperationException("BigIntegerOperationsService is not supposed to be instantiated");
	}

	public static BigInteger modMultiply(final BigInteger n1, final BigInteger n2, final BigInteger modulus) {
		return bigIntegerOperations.modMultiply(n1, n2, modulus);
	}

	public static BigInteger modExponentiate(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		return bigIntegerOperations.modExponentiate(base, exponent, modulus);
	}

	public static BigInteger multiModExp(final List<BigInteger> bases, final List<BigInteger> exponents, final BigInteger modulus) {
		return bigIntegerOperations.multiModExp(bases, exponents, modulus);
	}

	public static BigInteger modInvert(final BigInteger n, final BigInteger modulus) {
		return bigIntegerOperations.modInvert(n, modulus);
	}

	public static int getLegendre(final BigInteger a, final BigInteger p) {
		return bigIntegerOperations.getLegendre(a, p);
	}

	public static void generateCache(final BigInteger basis, final BigInteger modulus) {
		if (bigIntegerOperations.isFixedBaseExponentiationSupported()) {
			bigIntegerOperations.generateCache(basis, modulus);
		}
	}
}
