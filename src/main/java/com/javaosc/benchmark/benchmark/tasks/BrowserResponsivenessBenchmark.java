package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;
import com.javaosc.benchmark.util.BrowserProbe;

import java.util.Optional;

public final class BrowserResponsivenessBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Browser Responsiveness";
    }

    @Override
    public String category() {
        return "Browser";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) {
        Optional<BrowserProbe.BrowserCommand> browser = BrowserProbe.detectBrowser();
        if (browser.isEmpty()) {
            return new BenchmarkTaskResult(0, "unavailable", "No supported browser executable found");
        }
        long operations = 0;
        long deadline = System.nanoTime() + context.targetDuration().toNanos();
        while (System.nanoTime() < deadline) {
            operations += simulateDomLayoutAndScriptPass();
        }
        double seconds = context.targetDuration().toMillis() / 1000.0;
        return new BenchmarkTaskResult(operations / seconds, "ops/sec", browser.get().name() + " local rendering workload simulation");
    }

    private long simulateDomLayoutAndScriptPass() {
        double accumulator = 0;
        for (int node = 0; node < 2_500; node++) {
            double width = 320 + (node % 23) * 12.5;
            double height = 18 + (node % 17) * 1.7;
            accumulator += Math.sin(width) * Math.cos(height) + Math.sqrt(width * height);
        }
        return (long) accumulator;
    }
}
