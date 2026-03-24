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
package ch.post.it.evoting.cryptoprimitives.test.tools.serialization;

import java.io.InputStream;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.json.JsonMapper;

/**
 * General deserialization of json test files according to the schema defined in the specifications.
 */
public final class TestParameters {

	private String description;

	@JsonDeserialize(using = JsonDataDeserializer.class)
	private JsonData context;

	@JsonDeserialize(using = JsonDataDeserializer.class)
	private JsonData input;

	@JsonDeserialize(using = JsonDataDeserializer.class)
	private JsonData mocked;

	@JsonDeserialize(using = JsonDataDeserializer.class)
	private JsonData output;

	/**
	 * Parse a json file to a list of TestParameters. The resource has to be on classpath.
	 *
	 * @param resourceName The name of the json file.
	 * @return The list of TestParameters after deserialization of the json file.
	 */
	public static ImmutableList<TestParameters> fromResource(final String resourceName) {
		final InputStream inputStream = TestParameters.class.getResourceAsStream(resourceName);

		final ObjectMapper jsonMapper = JsonMapper.builder()
				.disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
				.build();

		return ImmutableList.of(jsonMapper.readValue(inputStream, TestParameters[].class));
	}

	public SecurityLevelInternal getSecurityLevel() {
		final String size = this.description.substring(0, 4);
		final int bitlength = Integer.parseInt(size);

		if (bitlength == 3072) {
			return SecurityLevelInternal.STANDARD;
		}
		throw new IllegalArgumentException("Unexpected bit length of p");
	}

	public JsonData getContext() {
		return context;
	}

	public JsonData getInput() {
		return input;
	}

	public JsonData getOutput() {
		return output;
	}

	public String getDescription() {
		return description;
	}

	public JsonData getMocked() {
		return mocked;
	}

	private static final class JsonDataDeserializer extends ValueDeserializer<JsonData> {
		private final ObjectMapper mapper;

		public JsonDataDeserializer() {
			this.mapper = JsonMapper.builder()
					.disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
					.build();
		}

		@Override
		public JsonData deserialize(final JsonParser jsonParser, final DeserializationContext ctxt) {
			final JsonNode root = mapper.readTree(jsonParser);
			return new JsonData(root);
		}
	}

}
