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
package ch.post.it.evoting.cryptoprimitives.benchmark;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MoreCollectors;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

public class BenchmarkRunner {

	private static final String BENCHMARK_RESULT_FILENAME = "benchmark-results-%s.json";

	private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);
	private static final String OPS_PER_NANO = "ops/ns";
	private static final String OPS_PER_MICRO = "ops/us";
	private static final String OPS_PER_MILLI = "ops/ms";
	private static final String OPS_PER_SECOND = "ops/s";
	private static final String OPS_PER_MINUTE = "ops/min";
	private static final String OPS_PER_HOUR = "ops/h";
	private static final String OPS_PER_DAY = "ops/d";

	private static final String NANOS_PER_OP = "ns/op";
	private static final String MICROS_PER_OP = "us/op";
	private static final String MILLIS_PER_OP = "ms/op";
	private static final String SECONDS_PER_OP = "s/op";
	private static final String MINUTES_PER_OP = "min/op";
	private static final String HOURS_PER_OP = "h/op";
	private static final String DAYS_PER_OP = "d/op";

	static void main() throws IOException, RunnerException {

		final List<String> benchmarkClasses = findBenchmarkClasses();

		for (final String benchmarkClass : benchmarkClasses) {
			runBenchmark(benchmarkClass, 3, 1, 10, false);
		}
	}

	public static List<String> findBenchmarkClasses() throws IOException {
		final List<String> benchmarkClasses = new ArrayList<>();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final Enumeration<URL> resources = classLoader.getResources("");

		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			try {
				final Path dir = Paths.get(resource.toURI());
				benchmarkClasses.addAll(scanDirectoryForBenchmarks(dir, ""));
			} catch (final URISyntaxException e) {
				// Log or rethrow as needed
				throw new IOException("Invalid URI for resource: " + resource, e);
			}

		}

		return benchmarkClasses;
	}

	private static List<String> scanDirectoryForBenchmarks(final Path dir, final String packageName) throws IOException {
		final List<String> result = new ArrayList<>();

		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (final Path entry : stream) {
				if (Files.isDirectory(entry)) {
					result.addAll(scanDirectoryForBenchmarks(entry, packageName + entry.getFileName().toString() + "."));
				} else {
					final String fileName = entry.getFileName().toString();
					if (fileName.endsWith("Benchmark.class") && !fileName.endsWith("ComparisonBenchmark.class")) {
						final String className = packageName + fileName.substring(0, fileName.length() - ".class".length());
						result.add(className);
					}
				}
			}
		}

		return result;
	}

	public static void runBenchmark(final String benchmarkClass, final int forks, final int threads, final int iterations, final boolean checkResult)
			throws RunnerException {
		final String[] benchmarkClassParts = benchmarkClass.split("\\.");
		final String benchmarkName = benchmarkClassParts[benchmarkClassParts.length - 1];

		final Options opt = new OptionsBuilder()
				.include(benchmarkName)
				.forks(forks)
				.threads(threads)
				.measurementIterations(iterations)
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MILLISECONDS)
				.addProfiler(JavaFlightRecorderProfiler.class, "dir=target")
				.shouldDoGC(true)
				.resultFormat(ResultFormatType.JSON)
				.result("target/%s".formatted(BENCHMARK_RESULT_FILENAME.formatted(benchmarkName)))
				.build();
		final Collection<RunResult> newRunResults = new Runner(opt).run();

		/*
		 * Read the reference benchmark results
		 */
		if (checkResult) {
			final InputStream resultsStream = BenchmarkRunner.class.getResourceAsStream(
					"/benchmark-results/%s".formatted(BENCHMARK_RESULT_FILENAME.formatted(benchmarkName)));
			if (resultsStream != null) {
				final ObjectMapper objectMapper = new ObjectMapper();
				final List<BenchmarkResult> benchmarkResults = objectMapper.readValue(resultsStream, new TypeReference<>() {});
				checkResult(newRunResults, benchmarkResults);
			} else {
				LOG.warn("No already existing results [fileName: {}].", benchmarkName);
			}
		}
	}

	private static void checkResult(final Collection<RunResult> newRunResults, final List<BenchmarkResult> benchmarkResults) {
		newRunResults.forEach(runResult -> {
			final Optional<BenchmarkResult> oldResult = benchmarkResults.stream()
					.filter(result -> result.benchmark().equals(runResult.getParams().getBenchmark())
							&& (result.params() == null || result.params().entrySet().stream()
							.map(entrySet -> runResult.getParams().getParam(entrySet.getKey()).equals(entrySet.getValue()))
							.reduce(Boolean.TRUE, Boolean::logicalAnd)))
					.collect(MoreCollectors.toOptional());

			oldResult.ifPresent(oldRunResult -> {

				if (!oldRunResult.mode().equals(runResult.getParams().getMode().shortLabel())) {
					LOG.error("Different mode. Aborting result check. [old mode: {}, new mode: {}].", oldRunResult.mode(),
							runResult.getParams().getMode());
					return;
				}

				if (oldRunResult.forks() != runResult.getParams().getForks()) {
					LOG.warn("Different number of forks between old and new run results. [old: {}, new: {}]", oldRunResult.forks(),
							runResult.getParams().getForks());
				}
				if (oldRunResult.threads() != runResult.getParams().getThreads()) {
					LOG.warn("Different number of threads between old and new run results. [old: {}, new: {}]", oldRunResult.threads(),
							runResult.getParams().getThreads());
				}
				if (oldRunResult.measurementIterations() != runResult.getParams().getMeasurement().getCount()) {
					LOG.warn("Different number of iterations between old and new run results. [old: {}, new: {}]", oldRunResult.measurementIterations(),
							runResult.getParams().getMeasurement().getCount());
				}

				final Result primaryResult = runResult.getAggregatedResult().getPrimaryResult();
				final String oldScoreUnit = oldRunResult.primaryMetric().scoreUnit();
				final String scoreUnit = primaryResult.getScoreUnit();

				final double confidenceFactor = 1.2;
				final double score = primaryResult.getScore() * getFactor(oldScoreUnit, scoreUnit);
				final double[] scoreConfidence = oldRunResult.primaryMetric().scoreConfidence();
				checkState(score <= scoreConfidence[1] * confidenceFactor,
						"The score is greater than the adjusted maximum of the old confidence interval. [max: %s, score: %s]", scoreConfidence[1] * confidenceFactor,
						score);

				if (score < scoreConfidence[0]) {
					LOG.info("The score is smaller than the minimum of the old confidence interval. [min: {}, score: {}]", scoreConfidence[0], score);
				}
			});
		});
	}

	private static double getFactor(final String oldUnit, final String newUnit) {
		if (oldUnit.equals(newUnit)) {
			return 1;
		} else if (oldUnit.startsWith("ops") && newUnit.startsWith("ops")) {
			return getFactorThroughput(oldUnit, newUnit);
		} else if (oldUnit.endsWith("ops") && newUnit.endsWith("ops")) {
			return getFactorAverage(oldUnit, newUnit);
		} else {
			return -1;
		}
	}

	private static double getFactorThroughput(final String oldUnit, final String newUnit) {
		return switch (oldUnit) {
			case OPS_PER_DAY -> switch (newUnit) {
				case OPS_PER_DAY -> 1;
				case OPS_PER_HOUR -> 24;
				case OPS_PER_MINUTE -> 24 * 60;
				case OPS_PER_SECOND -> 24 * 60 * 60;
				case OPS_PER_MILLI -> 24 * 60 * 60 * 1000;
				case OPS_PER_MICRO -> 24.0 * 60 * 60 * 1000 * 1000;
				case OPS_PER_NANO -> 24.0 * 60 * 60 * 1000 * 1000 * 1000;
				default -> -1;
			};
			case OPS_PER_HOUR -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / 24;
				case OPS_PER_HOUR -> 1;
				case OPS_PER_MINUTE -> 60;
				case OPS_PER_SECOND -> 60 * 60;
				case OPS_PER_MILLI -> 60 * 60 * 1000;
				case OPS_PER_MICRO -> 60.0 * 60 * 1000 * 1000;
				case OPS_PER_NANO -> 60.0 * 60 * 1000 * 1000 * 1000;
				default -> -1;
			};
			case OPS_PER_MINUTE -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / (24 * 60);
				case OPS_PER_HOUR -> 1.0 / 60;
				case OPS_PER_MINUTE -> 1;
				case OPS_PER_SECOND -> 60;
				case OPS_PER_MILLI -> 60 * 1000;
				case OPS_PER_MICRO -> 60 * 1000 * 1000;
				case OPS_PER_NANO -> 60.0 * 1000 * 1000 * 1000;
				default -> -1;
			};
			case OPS_PER_SECOND -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / (24 * 60 * 60);
				case OPS_PER_HOUR -> 1.0 / (60 * 60);
				case OPS_PER_MINUTE -> 1.0 / 60;
				case OPS_PER_SECOND -> 1;
				case OPS_PER_MILLI -> 1000;
				case OPS_PER_MICRO -> 1000 * 1000;
				case OPS_PER_NANO -> 1000 * 1000 * 1000;
				default -> -1;
			};
			case OPS_PER_MILLI -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / (24 * 60 * 60 * 1000);
				case OPS_PER_HOUR -> 1.0 / (60 * 60 * 1000);
				case OPS_PER_MINUTE -> 1.0 / (60 * 1000);
				case OPS_PER_SECOND -> 1.0 / 1000;
				case OPS_PER_MILLI -> 1;
				case OPS_PER_MICRO -> 1000;
				case OPS_PER_NANO -> 1000 * 1000;
				default -> -1;
			};
			case OPS_PER_MICRO -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / (24.0 * 60 * 60 * 1000 * 1000);
				case OPS_PER_HOUR -> 1.0 / (60.0 * 60 * 1000 * 1000);
				case OPS_PER_MINUTE -> 1.0 / (60 * 1000 * 1000);
				case OPS_PER_SECOND -> 1.0 / (1000 * 1000);
				case OPS_PER_MILLI -> 1.0 / 1000;
				case OPS_PER_MICRO -> 1;
				case OPS_PER_NANO -> 1000;
				default -> -1;
			};
			case OPS_PER_NANO -> switch (newUnit) {
				case OPS_PER_DAY -> 1.0 / (24.0 * 60 * 60 * 1000 * 1000 * 1000);
				case OPS_PER_HOUR -> 1.0 / (60.0 * 60 * 1000 * 1000 * 1000);
				case OPS_PER_MINUTE -> 1.0 / (60.0 * 1000 * 1000 * 1000);
				case OPS_PER_SECOND -> 1.0 / (1000 * 1000 * 1000);
				case OPS_PER_MILLI -> 1.0 / (1000 * 1000);
				case OPS_PER_MICRO -> 1.0 / 1000;
				case OPS_PER_NANO -> 1;
				default -> -1;
			};
			default -> -1;
		};
	}

	private static double getFactorAverage(final String oldUnit, final String newUnit) {
		return switch (oldUnit) {
			case DAYS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 1;
				case HOURS_PER_OP -> 1.0 / 24;
				case MINUTES_PER_OP -> 1.0 / (24 * 60);
				case SECONDS_PER_OP -> 1.0 / (24 * 60 * 60);
				case MILLIS_PER_OP -> 1.0 / (24 * 60 * 60 * 1000);
				case MICROS_PER_OP -> 1.0 / (24.0 * 60 * 60 * 1000 * 1000);
				case NANOS_PER_OP -> 1.0 / (24.0 * 60 * 60 * 1000 * 1000 * 1000);
				default -> -1;
			};
			case HOURS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24;
				case HOURS_PER_OP -> 1;
				case MINUTES_PER_OP -> 1.0 / 60;
				case SECONDS_PER_OP -> 1.0 / (60 * 60);
				case MILLIS_PER_OP -> 1.0 / (60 * 60 * 1000);
				case MICROS_PER_OP -> 1.0 / (60.0 * 60 * 1000 * 1000);
				case NANOS_PER_OP -> 1.0 / (60.0 * 60 * 1000 * 1000 * 1000);
				default -> -1;
			};
			case MINUTES_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24 * 60;
				case HOURS_PER_OP -> 60;
				case MINUTES_PER_OP -> 1;
				case SECONDS_PER_OP -> 1.0 / 60;
				case MILLIS_PER_OP -> 1.0 / (60 * 1000);
				case MICROS_PER_OP -> 1.0 / (60 * 1000 * 1000);
				case NANOS_PER_OP -> 1.0 / (60.0 * 1000 * 1000 * 1000);
				default -> -1;
			};
			case SECONDS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24 * 60 * 60;
				case HOURS_PER_OP -> 60 * 60;
				case MINUTES_PER_OP -> 60;
				case SECONDS_PER_OP -> 1;
				case MILLIS_PER_OP -> 1.0 / 1000;
				case MICROS_PER_OP -> 1.0 / 1000 * 1000;
				case NANOS_PER_OP -> 1.0 / 1000 * 1000 * 1000;
				default -> -1;
			};
			case MILLIS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24 * 60 * 60 * 1000;
				case HOURS_PER_OP -> 60 * 60 * 1000;
				case MINUTES_PER_OP -> 60 * 1000;
				case SECONDS_PER_OP -> 1000;
				case MILLIS_PER_OP -> 1;
				case MICROS_PER_OP -> 1.0 / 1000;
				case NANOS_PER_OP -> 1.0 / 1000 * 1000;
				default -> -1;
			};
			case MICROS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24.0 * 60 * 60 * 1000 * 1000;
				case HOURS_PER_OP -> 60.0 * 60 * 1000 * 1000;
				case MINUTES_PER_OP -> 60 * 1000 * 1000;
				case SECONDS_PER_OP -> 1000 * 1000;
				case MILLIS_PER_OP -> 1000;
				case MICROS_PER_OP -> 1;
				case NANOS_PER_OP -> 1.0 / 1000;
				default -> -1;
			};
			case NANOS_PER_OP -> switch (newUnit) {
				case DAYS_PER_OP -> 24.0 * 60 * 60 * 1000 * 1000 * 1000;
				case HOURS_PER_OP -> 60.0 * 60 * 1000 * 1000 * 1000;
				case MINUTES_PER_OP -> 60.0 * 1000 * 1000 * 1000;
				case SECONDS_PER_OP -> 1000 * 1000 * 1000;
				case MILLIS_PER_OP -> 1000 * 1000;
				case MICROS_PER_OP -> 1000;
				case NANOS_PER_OP -> 1;
				default -> -1;
			};
			default -> -1;
		};
	}
}
