package com.javaosc.benchmark.service;

import com.javaosc.benchmark.model.BenchmarkResult;
import com.javaosc.benchmark.model.RegressionReport;
import com.javaosc.benchmark.persistence.BenchmarkRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PerformanceAnalysisService {
    private final BenchmarkRepository repository;

    public PerformanceAnalysisService(BenchmarkRepository repository) {
        this.repository = repository;
    }

    public List<RegressionReport> analyze(List<BenchmarkResult> currentResults) {
        try {
            Map<String, Double> averages = repository.historicalAverages();
            List<RegressionReport> reports = new ArrayList<>();
            for (BenchmarkResult result : currentResults) {
                double historical = averages.getOrDefault(result.benchmarkName(), result.score());
                double change = historical == 0 ? 0 : ((result.score() - historical) / historical) * 100.0;
                reports.add(new RegressionReport(result.benchmarkName(), result.score(), historical, change, status(change)));
            }
            return reports;
        } catch (IOException ex) {
            return List.of();
        }
    }

    public List<BenchmarkResult> history() {
        try {
            return repository.findAll();
        } catch (IOException ex) {
            return List.of();
        }
    }

    private String status(double percentChange) {
        if (percentChange <= -10) {
            return "Regression";
        }
        if (percentChange >= 10) {
            return "Improved";
        }
        return "Stable";
    }
}
