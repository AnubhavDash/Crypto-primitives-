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
package ch.post.it.evoting.cryptoprimitives.benchmark;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ScorePercentiles(@JsonAlias("0.0") double p0, @JsonAlias("50.0") double p50, @JsonAlias("90.0") double p90,
							   @JsonAlias("95.0") double p95, @JsonAlias("99.0") double p99, @JsonAlias("99.9") double p99_9,
							   @JsonAlias("99.99") double p99_99, @JsonAlias("99.999") double p99_999, @JsonAlias("99.9999") double p99_9999,
							   @JsonAlias("100.0") double p100) {
}
