package com.javaosc.benchmark.persistence;

import com.javaosc.benchmark.model.BenchmarkResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BenchmarkRepository {
    void saveAll(List<BenchmarkResult> results) throws IOException;

    List<BenchmarkResult> findAll() throws IOException;

    Map<String, Double> historicalAverages() throws IOException;
}
