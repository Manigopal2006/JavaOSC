package com.javaosc.benchmark.benchmark;

public interface BenchmarkTask {
    String name();

    String category();

    BenchmarkTaskResult execute(BenchmarkExecutionContext context) throws Exception;
}
