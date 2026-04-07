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
package ch.post.it.evoting.cryptoprimitives.internal.utils;

import static ch.post.it.evoting.cryptoprimitives.collection.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.collection.ImmutableList;
import ch.post.it.evoting.cryptoprimitives.math.BaseEncodingFactory;

public class LibraryLoader {

	private static final String JAVA_LIBRARY_PATH_PROPERTY_NAME = "java.library.path";

	private LibraryLoader() {
		// Intentionally left blank.
	}

	public static void loadLibrary(final String libName, final String hashesResourceFileName) {
		checkNotNull(libName);
		checkNotNull(hashesResourceFileName);

		final String libraryFilename = System.mapLibraryName(libName);
		final ImmutableList<String> expectedHash = getAuthorizedHashes(hashesResourceFileName);
		checkState(!expectedHash.isEmpty(), "At least one authorized hash should be defined.");

		for (final String javaLibraryPath : checkNotNull(System.getProperty(JAVA_LIBRARY_PATH_PROPERTY_NAME)).split(File.pathSeparator)) {
			final Path libAbsolutePath = Paths.get(javaLibraryPath).resolve(libraryFilename);

			final boolean exists = Files.exists(libAbsolutePath);
			if (exists) {
				ensureFileHash(libAbsolutePath, expectedHash);
				System.load(libAbsolutePath.toString());
				return;
			}
		}
		throw new UnsatisfiedLinkError("library not found");
	}

	private static void ensureFileHash(final Path filePath, final ImmutableList<String> expectedHashes) {
		checkNotNull(filePath);
		checkNotNull(expectedHashes);

		try {
			final byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(filePath));
			final String calculatedHash = BaseEncodingFactory.createBase16().base16Encode(new ImmutableByteArray(digest));

			if (expectedHashes.stream().noneMatch(hash -> hash.toUpperCase(Locale.ENGLISH).equals(calculatedHash))) {
				throw new IllegalArgumentException(
						String.format("Hash does not match. [filePath: %s, expectedHashes: %s, calculatedHash: %s]", filePath,
								expectedHashes, calculatedHash));
			}
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("Message digest not found");
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static ImmutableList<String> getAuthorizedHashes(final String hashesResourceFilename) {
		final ArrayList<String> hashes = new ArrayList<>();
		try (final InputStream hashesInputStream = LibraryLoader.class.getResourceAsStream(hashesResourceFilename)) {
			checkNotNull(hashesInputStream, "Could not find resource. [hashesResourceFilename: {}]", hashesResourceFilename);
			try (final Scanner scanner = new Scanner(hashesInputStream)) {
				while (scanner.hasNext()) {
					hashes.add(scanner.next());
				}
			}
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Unable to read the resource file. [hashesResourceFilename: %s]", hashesResourceFilename),
					e);
		}

		return hashes.stream().collect(toImmutableList());
	}
}
