package com.javaosc.benchmark.benchmark;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;

public final class BenchmarkExecutionContext {
    private final ExecutorService executorService;
    private final Random random;
    private final Duration targetDuration;

    public BenchmarkExecutionContext(ExecutorService executorService, Duration targetDuration) {
        this.executorService = executorService;
        this.targetDuration = targetDuration;
        this.random = new Random(42);
    }

    public ExecutorService executorService() {
        return executorService;
    }

    public Random random() {
        return random;
    }

    public Duration targetDuration() {
        return targetDuration;
    }
}
