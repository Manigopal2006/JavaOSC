package com.javaosc.benchmark.benchmark;

import com.javaosc.benchmark.model.BenchmarkResult;
import com.javaosc.benchmark.model.BenchmarkRun;
import com.javaosc.benchmark.model.BenchmarkSample;
import com.javaosc.benchmark.model.HardwareProfile;
import com.javaosc.benchmark.persistence.BenchmarkRepository;
import com.javaosc.benchmark.service.HardwareProfileService;
import com.javaosc.benchmark.service.SystemMetricsMonitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BenchmarkRunner {
    private final List<BenchmarkTask> tasks;
    private final BenchmarkRepository repository;
    private final HardwareProfileService hardwareProfileService;
    private final SystemMetricsMonitor metricsMonitor;

    public BenchmarkRunner(
            List<BenchmarkTask> tasks,
            BenchmarkRepository repository,
            HardwareProfileService hardwareProfileService,
            SystemMetricsMonitor metricsMonitor
    ) {
        this.tasks = List.copyOf(tasks);
        this.repository = repository;
        this.hardwareProfileService = hardwareProfileService;
        this.metricsMonitor = metricsMonitor;
    }

    public BenchmarkRun runAll(BenchmarkListener listener) {
        String runId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        HardwareProfile profile = hardwareProfileService.capture();
        List<BenchmarkResult> results = new ArrayList<>();
        ExecutorService workerPool = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        ScheduledExecutorService sampler = Executors.newSingleThreadScheduledExecutor();
        final String[] currentBenchmark = { "Preparing" };

        sampler.scheduleAtFixedRate(() -> {
            BenchmarkSample sample = metricsMonitor.sample(currentBenchmark[0]);
            listener.onSample(sample);
        }, 0, 300, TimeUnit.MILLISECONDS);

        try {
            listener.onLog("Hardware profile: " + profile.displayName());
            BenchmarkExecutionContext context = new BenchmarkExecutionContext(workerPool, Duration.ofSeconds(2));
            for (BenchmarkTask task : tasks) {
                currentBenchmark[0] = task.name();
                listener.onLog("Running " + task.name() + "...");
                BenchmarkResult result = runSingle(runId, task, context);
                results.add(result);
                listener.onResult(result);
                listener.onLog(result.benchmarkName() + " finished: " + result.scoreLabel() + " - " + result.message());
            }
            repository.saveAll(results);
            listener.onLog("Saved " + results.size() + " benchmark records to the local database.");
        } catch (IOException ex) {
            listener.onLog("Could not save benchmark history: " + ex.getMessage());
        } finally {
            currentBenchmark[0] = "Idle";
            sampler.shutdownNow();
            workerPool.shutdownNow();
        }
        return new BenchmarkRun(runId, startedAt, profile, results);
    }

    private BenchmarkResult runSingle(String runId, BenchmarkTask task, BenchmarkExecutionContext context) {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        long beforeNanos = System.nanoTime();
        Instant startedAt = Instant.now();
        int beforeThreads = ManagementFactory.getThreadMXBean().getThreadCount();
        double cpuBefore = metricsMonitor.currentCpuLoad();

        try {
            BenchmarkTaskResult output = task.execute(context);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeNanos);
            long afterMemory = runtime.totalMemory() - runtime.freeMemory();
            int afterThreads = ManagementFactory.getThreadMXBean().getThreadCount();
            return new BenchmarkResult(runId, task.name(), task.category(), output.score(), output.unit(), elapsedMillis,
                    afterMemory - beforeMemory, Math.max(beforeThreads, afterThreads), metricsMonitor.currentCpuLoad(),
                    true, output.message(), startedAt);
        } catch (Exception ex) {
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeNanos);
            long afterMemory = runtime.totalMemory() - runtime.freeMemory();
            return new BenchmarkResult(runId, task.name(), task.category(), 0, "failed", elapsedMillis,
                    afterMemory - beforeMemory, beforeThreads, cpuBefore, false, ex.getMessage(), startedAt);
        }
    }
}
