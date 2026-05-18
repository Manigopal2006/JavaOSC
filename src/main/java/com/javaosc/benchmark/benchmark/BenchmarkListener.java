package com.javaosc.benchmark.benchmark;

import com.javaosc.benchmark.model.BenchmarkResult;
import com.javaosc.benchmark.model.BenchmarkSample;

public interface BenchmarkListener {
    void onLog(String message);

    void onSample(BenchmarkSample sample);

    void onResult(BenchmarkResult result);
}
