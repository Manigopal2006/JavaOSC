package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;
import com.javaosc.benchmark.util.BrowserProbe;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class BrowserStartupBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Browser Startup Probe";
    }

    @Override
    public String category() {
        return "Browser";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) throws Exception {
        Optional<BrowserProbe.BrowserCommand> browser = BrowserProbe.detectBrowser();
        if (browser.isEmpty()) {
            return new BenchmarkTaskResult(0, "unavailable", "No supported browser executable found");
        }
        long started = System.nanoTime();
        Process process = new ProcessBuilder(browser.get().command()).redirectErrorStream(true).start();
        boolean exited = process.waitFor(5, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly();
        }
        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        double score = millis == 0 ? 0 : 1000.0 / millis;
        return new BenchmarkTaskResult(score, "startup-index", browser.get().name() + " responded in " + millis + " ms");
    }
}
