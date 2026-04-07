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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetOptimizationMode;

/**
 * Test utility to temporarily set the MIXNET_OPTIMIZATION_MODE system property.
 */
public class MixnetOptimizationModeContext implements AutoCloseable {

	private static final String OPTIMIZATION_MODE_PROPERTY = "MIXNET_OPTIMIZATION_MODE";
	private final String previousValue;

	private MixnetOptimizationModeContext(final MixnetOptimizationMode mode) {
		this.previousValue = System.getProperty(OPTIMIZATION_MODE_PROPERTY);
		System.setProperty(OPTIMIZATION_MODE_PROPERTY, mode.name());
	}

	/**
	 * Sets the MIXNET_OPTIMIZATION_MODE system property to the given mode. The previous value will be restored when the context is closed.
	 *
	 * @param mode the MixnetOptimizationMode to set
	 * @return a MixnetOptimizationModeContext that should be used with try-with-resources
	 */
	public static MixnetOptimizationModeContext set(final MixnetOptimizationMode mode) {
		return new MixnetOptimizationModeContext(mode);
	}

	@Override
	public void close() {
		// Restore previous value
		if (previousValue == null) {
			System.clearProperty(OPTIMIZATION_MODE_PROPERTY);
		} else {
			System.setProperty(OPTIMIZATION_MODE_PROPERTY, previousValue);
		}
	}
}
