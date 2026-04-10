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

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.internal.securitylevel.SecurityLevelInternal;

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

		try {
			final ObjectMapper jsonMapper = new ObjectMapper();

			return ImmutableList.of(jsonMapper.readValue(inputStream, TestParameters[].class));
		} catch (final IOException e) {
			throw new RuntimeException("Read values failed for file " + resourceName + ". " + e.getMessage());
		}
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

	private static final class JsonDataDeserializer extends JsonDeserializer<JsonData> {
		private final ObjectMapper mapper;

		public JsonDataDeserializer() {
			this.mapper = new ObjectMapper();
		}

		@Override
		public JsonData deserialize(final JsonParser jsonParser, final DeserializationContext ctxt) throws IOException {
			final JsonNode root = mapper.readTree(jsonParser);
			return new JsonData(root);
		}
	}

}
