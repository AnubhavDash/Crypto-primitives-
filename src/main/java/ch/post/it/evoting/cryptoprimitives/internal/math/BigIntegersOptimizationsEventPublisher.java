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
package ch.post.it.evoting.cryptoprimitives.internal.math;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.verificatum.vmgj.FpowmTab;

import ch.post.it.evoting.cryptoprimitives.math.BigIntegersOptimizationsEvent;
import ch.post.it.evoting.cryptoprimitives.math.BigIntegersOptimizationsCacheKey;

@SuppressWarnings("java:S6548") // Allow the usage of singleton pattern
public class BigIntegersOptimizationsEventPublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(BigIntegersOptimizationsEventPublisher.class);

	public static final BigIntegersOptimizationsEventPublisher INSTANCE = new BigIntegersOptimizationsEventPublisher();

	private final ConcurrentHashMap<Object, Consumer<BigIntegersOptimizationsEvent>> subscribers = new ConcurrentHashMap<>();

	private BigIntegersOptimizationsEventPublisher() {
		//to be used as singleton instance
	}

	@SuppressWarnings("unchecked")
	public static Cache<BigIntegersOptimizationsCacheKey, FpowmTab> attachCachePublisher(final Cache<BigIntegersOptimizationsCacheKey, FpowmTab> cache) {
		return (Cache<BigIntegersOptimizationsCacheKey, FpowmTab>) Proxy.newProxyInstance(
				Cache.class.getClassLoader(),
				new Class[] { Cache.class },
				(proxy, method, args) -> {
					final Object result = method.invoke(cache, args);
					if (method.getName().equals("getIfPresent")) {
						INSTANCE.publishCacheRequestEvent((BigIntegersOptimizationsCacheKey) args[0], result != null);
					} else if (method.getName().equals("cleanUp")) {
						LOGGER.debug("Cleaning up the cache. [cacheSize: {}]", cache.size());
					}
					return result;
				});
	}

	private void publishCacheRequestEvent(final BigIntegersOptimizationsCacheKey bigIntegersOptimizationsCacheKey, final boolean inCache) {
		for (final Consumer<BigIntegersOptimizationsEvent> subscriber : subscribers.values()) {
			Thread.startVirtualThread(() -> subscriber.accept(
					new BigIntegersOptimizationsEvent(BigIntegersOptimizationsEvent.EventType.CACHE_REQUEST, bigIntegersOptimizationsCacheKey, inCache)));
		}
	}

	public void publishGqGroupCreationEvent(final BigInteger g, final BigInteger p) {
		for (final Consumer<BigIntegersOptimizationsEvent> subscriber : subscribers.values()) {
			Thread.startVirtualThread(() -> subscriber.accept(
					new BigIntegersOptimizationsEvent(BigIntegersOptimizationsEvent.EventType.GQ_GROUP_CREATION, new BigIntegersOptimizationsCacheKey(g, p), false)));
		}
	}

	public void subscribeCacheEvent(final Object subscriber, final Consumer<BigIntegersOptimizationsEvent> callback) {
		checkNotNull(subscriber);
		checkNotNull(callback);
		checkState(!subscribers.containsKey(subscriber), "subscriber already exists");
		subscribers.put(subscriber, callback);
	}

	public void unsubscribeCacheEvent(final Object subscriber) {
		checkNotNull(subscriber);
		subscribers.remove(subscriber);
	}
}
