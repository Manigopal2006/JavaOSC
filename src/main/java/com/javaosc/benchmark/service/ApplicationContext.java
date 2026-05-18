package com.javaosc.benchmark.service;

import com.javaosc.benchmark.benchmark.BenchmarkRunner;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.tasks.BrowserResponsivenessBenchmark;
import com.javaosc.benchmark.benchmark.tasks.BrowserStartupBenchmark;
import com.javaosc.benchmark.benchmark.tasks.CompressionBenchmark;
import com.javaosc.benchmark.benchmark.tasks.ConcurrencyBenchmark;
import com.javaosc.benchmark.benchmark.tasks.ParsingBenchmark;
import com.javaosc.benchmark.benchmark.tasks.TokenGenerationBenchmark;
import com.javaosc.benchmark.persistence.BenchmarkRepository;
import com.javaosc.benchmark.persistence.FileBenchmarkRepository;

import java.nio.file.Path;
import java.util.List;

public final class ApplicationContext {
    private final BenchmarkRunner benchmarkRunner;
    private final PerformanceAnalysisService analysisService;
    private final HardwareProfileService hardwareProfileService;

    private ApplicationContext(BenchmarkRunner benchmarkRunner, PerformanceAnalysisService analysisService, HardwareProfileService hardwareProfileService) {
        this.benchmarkRunner = benchmarkRunner;
        this.analysisService = analysisService;
        this.hardwareProfileService = hardwareProfileService;
    }

    public static ApplicationContext create(Path databasePath) {
        BenchmarkRepository repository = new FileBenchmarkRepository(databasePath);
        HardwareProfileService hardwareProfileService = new HardwareProfileService();
        SystemMetricsMonitor metricsMonitor = new SystemMetricsMonitor();
        List<BenchmarkTask> tasks = List.of(
                new ParsingBenchmark(),
                new CompressionBenchmark(),
                new TokenGenerationBenchmark(),
                new ConcurrencyBenchmark(),
                new BrowserStartupBenchmark(),
                new BrowserResponsivenessBenchmark());
        BenchmarkRunner runner = new BenchmarkRunner(tasks, repository, hardwareProfileService, metricsMonitor);
        PerformanceAnalysisService analysisService = new PerformanceAnalysisService(repository);
        return new ApplicationContext(runner, analysisService, hardwareProfileService);
    }

    public BenchmarkRunner benchmarkRunner() {
        return benchmarkRunner;
    }

    public PerformanceAnalysisService analysisService() {
        return analysisService;
    }

    public HardwareProfileService hardwareProfileService() {
        return hardwareProfileService;
    }
}
