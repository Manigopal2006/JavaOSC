package com.javaosc.benchmark.model;

import java.time.Instant;

public record BenchmarkSample(
        Instant capturedAt,
        double cpuLoad,
        long usedMemoryBytes,
        int liveThreads,
        String activeBenchmark
) {
}
