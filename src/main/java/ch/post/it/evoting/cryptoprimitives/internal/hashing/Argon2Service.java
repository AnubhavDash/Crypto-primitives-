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
package ch.post.it.evoting.cryptoprimitives.internal.hashing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.Security;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ch.post.it.evoting.cryptoprimitives.collection.ImmutableByteArray;
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2;
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Hash;
import ch.post.it.evoting.cryptoprimitives.hashing.Argon2Profile;
import ch.post.it.evoting.cryptoprimitives.internal.math.RandomService;

public class Argon2Service implements Argon2 {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final RandomService randomService;
    private final Argon2Profile config;

    public Argon2Service(final RandomService randomService, final Argon2Profile config) {
        this.randomService = randomService;
        this.config = config;
    }

    /**
     * See {@link Argon2#genArgon2id}
     */
    @Override
    public Argon2Hash genArgon2id(final ImmutableByteArray inputKeyingMaterial) {
        final ImmutableByteArray k = checkNotNull(inputKeyingMaterial);

        final ImmutableByteArray s = randomService.randomBytes(16);
        final ImmutableByteArray t = getArgon2id(k, s);

        return new Argon2Hash(t, s);
    }

    /**
     * See {@link Argon2#getArgon2id}
     */
    @Override
    public ImmutableByteArray getArgon2id(final ImmutableByteArray inputKeyingMaterial, final ImmutableByteArray salt) {
        final ImmutableByteArray k = checkNotNull(inputKeyingMaterial);
        final ImmutableByteArray s = checkNotNull(salt);
        final int m = config.get_m();
        final int p = config.get_p();
        final int i = config.get_i();

        final Argon2Configuration c = new Argon2Configuration(32, s, m, p, i);
        return argon2id(c, k);
    }

    private ImmutableByteArray argon2id(final Argon2Configuration c, final ImmutableByteArray k) {
        final Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(c.salt().elements())
                .withMemoryPowOfTwo(c.memory())
                .withParallelism(c.parallelism())
                .withIterations(c.iterations())
                .build();

        final Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        final byte[] t = new byte[c.tagLength()];
        generator.generateBytes(k.elements(), t);

        return ImmutableByteArray.from(t);
    }

    private record Argon2Configuration(int tagLength, ImmutableByteArray salt, int memory, int parallelism,
                                       int iterations) {
    }
}
