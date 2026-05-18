package com.javaosc.benchmark.model;

import java.time.Instant;
import java.util.Locale;

public record BenchmarkResult(
        String runId,
        String benchmarkName,
        String category,
        double score,
        String unit,
        long durationMillis,
        long memoryDeltaBytes,
        int threadCount,
        double cpuLoad,
        boolean success,
        String message,
        Instant startedAt
) {
    public String scoreLabel() {
        return String.format(Locale.US, "%.2f %s", score, unit);
    }
}
