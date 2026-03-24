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

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.function.Consumer;

import ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegerOperationsService;
import ch.post.it.evoting.cryptoprimitives.internal.math.BigIntegersOptimizationsEventPublisher;

public class BigIntegersOptimizations {

	public static final String CACHE_METHOD_NAME_CLEAN_UP = "cleanUp";
	public static final String CACHE_METHOD_NAME_INVALIDATE = "invalidate";
	public static final String CACHE_METHOD_NAME_GET = "get";
	public static final String CACHE_METHOD_NAME_GET_IF_PRESENT = "getIfPresent";
	public static final String CACHE_METHOD_NAME_PUT = "put";

	private BigIntegersOptimizations() {
		// Intentionally left blank.
	}

	public static void prepareFixedBaseOptimizations(final BigInteger basis, final BigInteger modulus) {
		checkNotNull(basis);
		checkNotNull(modulus);
		prepareFixedBaseOptimizations(basis, modulus, BlockWidth.STANDARD);
	}

	public static void prepareFixedBaseOptimizations(final BigInteger basis, final BigInteger modulus,
			final BigIntegersOptimizations.BlockWidth blockWidth) {
		checkNotNull(basis);
		checkNotNull(modulus);
		checkNotNull(blockWidth);
		BigIntegerOperationsService.generateCache(basis, modulus, blockWidth);
	}

	public static void releaseFixedBaseOperations(final BigIntegersOptimizationsCacheKey key) {
		checkNotNull(key);
		BigIntegerOperationsService.releaseCache(key);
	}

	/**
	 * Subscribes to big integers optimizations events.
	 *
	 * @param subscriber the subscriber object.
	 * @param callback   the callback to be invoked when an event is published.
	 */
	public static void subscribeBigIntegersOptimizationsEvent(final Object subscriber, final Consumer<BigIntegersOptimizationsEvent> callback) {
		checkNotNull(subscriber);
		checkNotNull(callback);
		BigIntegersOptimizationsEventPublisher.INSTANCE.subscribeCacheEvent(subscriber, callback);
	}

	/**
	 * Unsubscribes from big integers optimizations events.
	 *
	 * @param subscriber the subscriber object.
	 */
	public static void unsubscribeBigIntegersOptimizationsEvent(final Object subscriber) {
		checkNotNull(subscriber);
		BigIntegersOptimizationsEventPublisher.INSTANCE.unsubscribeCacheEvent(subscriber);
	}

	/**
	 * Represents the block width used for big integer optimizations. Each block width defines a specific value and estimated memory usage. The choice
	 * of block width impacts the performance and memory consumption of fixed-base exponentiation optimizations.
	 */
	public enum BlockWidth {

		TINY(10, 393_216L),
		SMALL(12, 1_572_864L),
		STANDARD(16, 25_165_824L),
		BIG(20, 402_653_184L),
		BIGGER(22, 1_610_612_736L),
		HUGE(24, 6_442_450_944L);

		private final int value;
		private final long memoryUsage;

		BlockWidth(final int value, final long memoryUsage) {
			this.value = value;
			this.memoryUsage = memoryUsage;
		}

		public int getValue() {
			return value;
		}

		public long getMemoryUsage() {
			return memoryUsage;
		}
	}
}
