package ch.post.it.evoting.cryptoprimitives.internal.elgamal;

import java.util.ArrayList;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import ch.post.it.evoting.cryptoprimitives.internal.math.PrimesInternal;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

public class EncryptionParametersBenchmark {

	private static final ArrayList<Integer> SMALL_PRIMES = PrimesInternal.getSmallPrimes();
	public static final EncryptionParameters encryptionParameters = new EncryptionParameters();
	static final Random random = RandomFactory.createRandom();

	@Benchmark
	@Warmup(iterations = 0)
	@Fork(value = 10)
	@Measurement(iterations = 10)
	@BenchmarkMode(Mode.AverageTime)
	public GqGroup benchGetEncryptionParameters() {
		final String seed = random.genRandomBase64String(10);
		return encryptionParameters.getEncryptionParameters(seed, SMALL_PRIMES);
	}
}
