package com.javaosc.benchmark.model;

import java.time.Instant;
import java.util.List;

public record BenchmarkRun(
        String runId,
        Instant startedAt,
        HardwareProfile hardwareProfile,
        List<BenchmarkResult> results
) {
    public double averageScore() {
        if (results.isEmpty()) {
            return 0;
        }
        return results.stream().mapToDouble(BenchmarkResult::score).average().orElse(0);
    }
}
