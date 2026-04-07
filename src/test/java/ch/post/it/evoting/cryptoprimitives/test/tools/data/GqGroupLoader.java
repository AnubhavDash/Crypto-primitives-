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
package ch.post.it.evoting.cryptoprimitives.test.tools.data;

import java.math.BigInteger;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class GqGroupLoader {

	private final GqGroup group;

	GqGroupLoader(final String fileName) {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode jsonNode = mapper.readTree(GqGroupLoader.class.getResourceAsStream(fileName));

		final BigInteger p = new BigInteger(jsonNode.get("p").asString(), 10);
		final BigInteger q = new BigInteger(jsonNode.get("q").asString(), 10);
		final BigInteger g = new BigInteger(jsonNode.get("g").asString(), 10);

		this.group = new GqGroup(p, q, g);
	}

	GqGroup getGroup() {
		return this.group;
	}
}
