package com.javaosc.benchmark.benchmark;

public record BenchmarkTaskResult(
        double score,
        String unit,
        String message
) {
}
