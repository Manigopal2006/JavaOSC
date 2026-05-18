package com.javaosc.benchmark.model;

public record RegressionReport(
        String benchmarkName,
        double currentScore,
        double historicalAverage,
        double percentChange,
        String status
) {
}
